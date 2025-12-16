from datetime import datetime
from typing import Callable, Dict, List

from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlalchemy import select
from sqlalchemy.orm import Session

from .auth import (
    create_access_token,
    get_current_user,
    get_db,
    get_password_hash,
    list_user_roles,
    verify_password,
)
from .database import Base, SessionLocal, engine
from .models import Borrowing, OperationLog, Role, User, UserRole
from .schemas import (
    BorrowingCreate,
    BorrowingRead,
    BorrowingReturn,
    LoginRequest,
    OperationLogRead,
    TokenResponse,
    UserInfo,
)

app = FastAPI(title="Library Borrowing Analysis System")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup_event() -> None:
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        seed_roles_and_users(db)
    finally:
        db.close()


def seed_roles_and_users(db: Session) -> None:
    role_names = ["ADMIN", "STAFF", "VIEWER"]
    roles: Dict[str, Role] = {}
    for name in role_names:
        role = db.scalar(select(Role).where(Role.name == name))
        if not role:
            role = Role(name=name)
            db.add(role)
            db.flush()
        roles[name] = role

    default_users = {
        "admin": "ADMIN",
        "staff": "STAFF",
        "viewer": "VIEWER",
    }

    for username, role_name in default_users.items():
        user = db.scalar(select(User).where(User.username == username))
        if not user:
            user = User(
                username=username,
                password_hash=get_password_hash("password"),
            )
            db.add(user)
            db.flush()
        existing_link = db.scalar(
            select(UserRole).where(
                UserRole.user_id == user.id, UserRole.role_id == roles[role_name].id
            )
        )
        if not existing_link:
            db.add(UserRole(user_id=user.id, role_id=roles[role_name].id))
    db.commit()


def serialize_user(user: User) -> UserInfo:
    roles = [role.role for role in user.roles]
    return UserInfo(
        id=user.id,
        username=user.username,
        roles=[{"id": r.id, "name": r.name} for r in roles],
    )


def require_roles(allowed_roles: List[str]) -> Callable:
    def dependency(current_user: User = Depends(get_current_user)) -> User:
        user_roles = set(list_user_roles(current_user))
        if not user_roles.intersection(allowed_roles):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Insufficient permissions",
            )
        return current_user

    return dependency


def record_operation(
    db: Session,
    user: User,
    borrowing_id: int,
    action: str,
    details: str | None = None,
) -> None:
    log = OperationLog(
        user_id=user.id if user else None,
        borrowing_id=borrowing_id,
        action=action,
        details=details,
    )
    db.add(log)
    db.commit()


@app.post("/login", response_model=TokenResponse, response_model_by_alias=True)
def login(payload: LoginRequest, db: Session = Depends(get_db)) -> TokenResponse:
    user = db.scalar(select(User).where(User.username == payload.username))
    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(token=token, user=serialize_user(user))


@app.get("/me", response_model=UserInfo)
def get_profile(current_user: User = Depends(get_current_user)) -> UserInfo:
    return serialize_user(current_user)


@app.get("/borrowings", response_model=List[BorrowingRead])
def list_borrowings(db: Session = Depends(get_db)) -> List[Borrowing]:
    return db.scalars(select(Borrowing).order_by(Borrowing.borrowed_at.desc())).all()


@app.post("/borrowings", response_model=BorrowingRead)
def create_borrowing(
    payload: BorrowingCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_roles(["ADMIN", "STAFF"])),
) -> Borrowing:
    borrowing = Borrowing(
        book_title=payload.book_title,
        borrower_name=payload.borrower_name,
        due_date=payload.due_date,
    )
    db.add(borrowing)
    db.commit()
    db.refresh(borrowing)
    record_operation(db, current_user, borrowing.id, "CREATE", details="New borrowing created")
    return borrowing


@app.post("/borrowings/{borrowing_id}/return", response_model=BorrowingReturn)
def return_borrowing(
    borrowing_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_roles(["ADMIN", "STAFF"])),
) -> BorrowingReturn:
    borrowing = db.get(Borrowing, borrowing_id)
    if not borrowing:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Borrowing not found")
    if borrowing.returned_at:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Borrowing already returned")

    borrowing.returned_at = datetime.utcnow()
    db.add(borrowing)
    db.commit()
    db.refresh(borrowing)
    record_operation(db, current_user, borrowing.id, "RETURN", details="Borrowing marked as returned")
    return BorrowingReturn(message="Book returned", borrowing=borrowing)


@app.get("/audit-logs", response_model=List[OperationLogRead])
def audit_logs(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_roles(["ADMIN", "STAFF"])),
) -> List[OperationLog]:
    logs = db.scalars(select(OperationLog).order_by(OperationLog.created_at.desc())).all()
    return logs


app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")
