from datetime import datetime, timezone
from io import BytesIO, StringIO
from typing import List, Optional

import pandas as pd
from fastapi import Depends, FastAPI, File, HTTPException, Query, UploadFile
from fastapi.responses import FileResponse, HTMLResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from sqlmodel import Session, select

from .database import get_session, init_db
from .models import Book, BorrowRecord, Reader
from .schemas import BatchActionRequest, BatchActionResponse, BorrowRecordResponse, ImportResult

app = FastAPI(title="Library Borrowing Analysis System")
app.mount("/static", StaticFiles(directory="app/static"), name="static")

REQUIRED_READER_COLUMNS = ["name", "email"]
REQUIRED_BOOK_COLUMNS = ["title", "category"]
REQUIRED_BORROW_COLUMNS = ["reader_email", "book_title", "borrowed_at"]


@app.on_event("startup")
def on_startup() -> None:
    init_db(sample=True)


@app.get("/", response_class=HTMLResponse)
def index() -> HTMLResponse:
    with open("app/static/index.html", "r", encoding="utf-8") as file:
        content = file.read()
    return HTMLResponse(content)


def load_dataframe(file: UploadFile) -> pd.DataFrame:
    filename = file.filename or ""
    if filename.lower().endswith(".csv"):
        return pd.read_csv(file.file)
    if filename.lower().endswith(".xlsx"):
        return pd.read_excel(file.file)
    raise HTTPException(status_code=400, detail="Only CSV or Excel (.xlsx) files are supported.")


def validate_columns(df: pd.DataFrame, required: List[str]) -> None:
    missing = [col for col in required if col not in df.columns]
    if missing:
        raise HTTPException(status_code=400, detail=f"Missing required columns: {', '.join(missing)}")


def parse_datetime(value: object) -> datetime:
    if isinstance(value, datetime):
        return value
    if isinstance(value, str) and value.strip():
        try:
            return datetime.fromisoformat(value)
        except ValueError as exc:  # noqa: PERF203
            raise HTTPException(status_code=400, detail=f"Invalid datetime format: {value}") from exc
    raise HTTPException(status_code=400, detail="Datetime value is required")


@app.post("/import/readers", response_model=ImportResult)
def import_readers(file: UploadFile = File(...), session: Session = Depends(get_session)) -> ImportResult:
    df = load_dataframe(file)
    validate_columns(df, REQUIRED_READER_COLUMNS)
    failed_rows: List[dict] = []
    success_count = 0

    for idx, row in df.iterrows():
        row_number = idx + 2
        name = str(row.get("name", "")).strip()
        email = str(row.get("email", "")).strip()
        phone = str(row.get("phone", "")).strip() if "phone" in df.columns else None

        errors = []
        if not name:
            errors.append("Missing name")
        if not email:
            errors.append("Missing email")

        if errors:
            failed_rows.append({"row_number": row_number, "errors": errors})
            continue

        existing = session.exec(select(Reader).where(Reader.email == email)).first()
        if existing:
            existing.name = name
            existing.phone = phone or existing.phone
        else:
            session.add(Reader(name=name, email=email, phone=phone))
        success_count += 1

    session.commit()
    return ImportResult(success_count=success_count, failed_rows=failed_rows)


@app.post("/import/books", response_model=ImportResult)
def import_books(file: UploadFile = File(...), session: Session = Depends(get_session)) -> ImportResult:
    df = load_dataframe(file)
    validate_columns(df, REQUIRED_BOOK_COLUMNS)
    failed_rows: List[dict] = []
    success_count = 0

    for idx, row in df.iterrows():
        row_number = idx + 2
        title = str(row.get("title", "")).strip()
        category = str(row.get("category", "")).strip()
        author = str(row.get("author", "")).strip() if "author" in df.columns else None
        errors = []
        if not title:
            errors.append("Missing title")
        if not category:
            errors.append("Missing category")
        if errors:
            failed_rows.append({"row_number": row_number, "errors": errors})
            continue

        existing = session.exec(
            select(Book).where(Book.title == title).where(Book.category == category)
        ).first()
        if existing:
            existing.author = author or existing.author
        else:
            session.add(Book(title=title, category=category, author=author))
        success_count += 1

    session.commit()
    return ImportResult(success_count=success_count, failed_rows=failed_rows)


@app.post("/import/borrows", response_model=ImportResult)
def import_borrows(file: UploadFile = File(...), session: Session = Depends(get_session)) -> ImportResult:
    df = load_dataframe(file)
    validate_columns(df, REQUIRED_BORROW_COLUMNS)
    failed_rows: List[dict] = []
    success_count = 0

    for idx, row in df.iterrows():
        row_number = idx + 2
        reader_email = str(row.get("reader_email", "")).strip()
        book_title = str(row.get("book_title", "")).strip()
        category = str(row.get("category", "")).strip() if "category" in df.columns else None
        status = str(row.get("status", "borrowed")).strip() or "borrowed"
        due_at_raw = row.get("due_at")
        borrowed_at_raw = row.get("borrowed_at")
        errors = []

        if not reader_email:
            errors.append("Missing reader_email")
        if not book_title:
            errors.append("Missing book_title")
        if not borrowed_at_raw:
            errors.append("Missing borrowed_at")

        borrowed_at: Optional[datetime] = None
        if borrowed_at_raw:
            try:
                borrowed_at = parse_datetime(borrowed_at_raw)
            except HTTPException as exc:
                errors.append(str(exc.detail))

        due_at: Optional[datetime] = None
        if due_at_raw:
            try:
                due_at = parse_datetime(due_at_raw)
            except HTTPException as exc:
                errors.append(str(exc.detail))

        if errors:
            failed_rows.append({"row_number": row_number, "errors": errors})
            continue

        reader = session.exec(select(Reader).where(Reader.email == reader_email)).first()
        if not reader:
            reader = Reader(name=reader_email.split("@")[0], email=reader_email)
            session.add(reader)
            session.commit()
            session.refresh(reader)

        book_query = select(Book).where(Book.title == book_title)
        if category:
            book_query = book_query.where(Book.category == category)
        book = session.exec(book_query).first()
        if not book:
            book = Book(title=book_title, category=category or "General")
            session.add(book)
            session.commit()
            session.refresh(book)

        session.add(
            BorrowRecord(
                reader_id=reader.id,
                book_id=book.id,
                status=status,
                borrowed_at=borrowed_at or datetime.now(timezone.utc),
                due_at=due_at,
                category=book.category,
            )
        )
        success_count += 1

    session.commit()
    return ImportResult(success_count=success_count, failed_rows=failed_rows)


@app.get("/borrows", response_model=List[BorrowRecordResponse])
def list_borrows(
    session: Session = Depends(get_session),
    start_date: Optional[datetime] = Query(None),
    end_date: Optional[datetime] = Query(None),
    category: Optional[str] = Query(None),
    status: Optional[str] = Query(None),
) -> List[BorrowRecordResponse]:
    query = select(BorrowRecord, Reader, Book).join(Reader).join(Book).where(BorrowRecord.deleted_at.is_(None))
    if start_date:
        query = query.where(BorrowRecord.borrowed_at >= start_date)
    if end_date:
        query = query.where(BorrowRecord.borrowed_at <= end_date)
    if category:
        query = query.where(BorrowRecord.category == category)
    if status:
        query = query.where(BorrowRecord.status == status)

    results = session.exec(query).all()
    response: List[BorrowRecordResponse] = []
    for borrow, reader, book in results:
        response.append(
            BorrowRecordResponse(
                id=borrow.id,
                reader_name=reader.name,
                reader_email=reader.email,
                book_title=book.title,
                category=borrow.category,
                status=borrow.status,
                borrowed_at=borrow.borrowed_at,
                due_at=borrow.due_at,
                returned_at=borrow.returned_at,
            )
        )
    return response


@app.get("/export/borrows")
def export_borrows(
    session: Session = Depends(get_session),
    start_date: Optional[datetime] = Query(None),
    end_date: Optional[datetime] = Query(None),
    category: Optional[str] = Query(None),
    status: Optional[str] = Query(None),
    file_format: str = Query("csv", pattern="^(csv|xlsx)$"),
):
    records = list_borrows(session=session, start_date=start_date, end_date=end_date, category=category, status=status)
    df = pd.DataFrame([record.model_dump() for record in records])

    if df.empty:
        df = pd.DataFrame(columns=[
            "id",
            "reader_name",
            "reader_email",
            "book_title",
            "category",
            "status",
            "borrowed_at",
            "due_at",
            "returned_at",
        ])

    if file_format == "csv":
        buffer = StringIO()
        df.to_csv(buffer, index=False)
        buffer.seek(0)
        headers = {"Content-Disposition": "attachment; filename=borrow-records.csv"}
        return StreamingResponse(buffer, media_type="text/csv", headers=headers)

    output = BytesIO()
    with pd.ExcelWriter(output, engine="openpyxl") as writer:
        df.to_excel(writer, index=False, sheet_name="BorrowRecords")
    output.seek(0)
    headers = {"Content-Disposition": "attachment; filename=borrow-records.xlsx"}
    return StreamingResponse(output, media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", headers=headers)


@app.post("/borrows/batch-return", response_model=BatchActionResponse)
def batch_return(action: BatchActionRequest, session: Session = Depends(get_session)) -> BatchActionResponse:
    if not action.ids:
        raise HTTPException(status_code=400, detail="ids list cannot be empty")

    updated = 0
    skipped = 0
    for borrow_id in action.ids:
        borrow = session.get(BorrowRecord, borrow_id)
        if not borrow or borrow.deleted_at is not None:
            skipped += 1
            continue
        borrow.status = "returned"
        borrow.returned_at = datetime.now(timezone.utc)
        updated += 1

    session.commit()
    return BatchActionResponse(updated=updated, skipped=skipped)


@app.post("/borrows/batch-delete", response_model=BatchActionResponse)
def batch_delete(action: BatchActionRequest, session: Session = Depends(get_session)) -> BatchActionResponse:
    if not action.ids:
        raise HTTPException(status_code=400, detail="ids list cannot be empty")

    updated = 0
    skipped = 0
    for borrow_id in action.ids:
        borrow = session.get(BorrowRecord, borrow_id)
        if not borrow or borrow.deleted_at is not None:
            skipped += 1
            continue
        borrow.deleted_at = datetime.now(timezone.utc)
        updated += 1

    session.commit()
    return BatchActionResponse(updated=updated, skipped=skipped)


@app.get("/readers")
def list_readers(session: Session = Depends(get_session)) -> List[Reader]:
    return session.exec(select(Reader)).all()


@app.get("/books")
def list_books(session: Session = Depends(get_session)) -> List[Book]:
    return session.exec(select(Book)).all()


@app.get("/health")
def healthcheck() -> dict:
    return {"status": "ok"}
