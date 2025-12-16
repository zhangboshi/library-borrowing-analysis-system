from datetime import datetime
from typing import Optional

from sqlmodel import Field, SQLModel


class Reader(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    email: str = Field(index=True, unique=True)
    phone: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)


class Book(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    title: str
    category: str
    author: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)


class BorrowRecord(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    reader_id: int = Field(foreign_key="reader.id")
    book_id: int = Field(foreign_key="book.id")
    status: str = Field(default="borrowed")
    borrowed_at: datetime
    due_at: Optional[datetime] = None
    returned_at: Optional[datetime] = None
    deleted_at: Optional[datetime] = Field(default=None, index=True)
    category: Optional[str] = Field(default=None, index=True)
