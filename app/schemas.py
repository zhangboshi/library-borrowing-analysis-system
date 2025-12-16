from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, Field


class RoleInfo(BaseModel):
    id: int
    name: str

    class Config:
        orm_mode = True


class UserInfo(BaseModel):
    id: int
    username: str
    roles: List[RoleInfo]

    class Config:
        orm_mode = True


class TokenResponse(BaseModel):
    access_token: str = Field(..., alias="token")
    token_type: str = "bearer"
    user: UserInfo

    class Config:
        allow_population_by_field_name = True


class LoginRequest(BaseModel):
    username: str
    password: str


class BorrowingCreate(BaseModel):
    book_title: str
    borrower_name: str
    due_date: Optional[datetime]


class BorrowingRead(BaseModel):
    id: int
    book_title: str
    borrower_name: str
    borrowed_at: datetime
    due_date: Optional[datetime]
    returned_at: Optional[datetime]

    class Config:
        orm_mode = True


class BorrowingReturn(BaseModel):
    message: str
    borrowing: BorrowingRead


class OperationLogRead(BaseModel):
    id: int
    user: Optional[UserInfo]
    borrowing_id: int
    action: str
    details: Optional[str]
    created_at: datetime

    class Config:
        orm_mode = True
