from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, Field


class ImportResult(BaseModel):
    success_count: int
    failed_rows: List[dict]


class BorrowRecordResponse(BaseModel):
    id: int
    reader_name: str
    reader_email: str
    book_title: str
    category: Optional[str]
    status: str
    borrowed_at: datetime
    due_at: Optional[datetime]
    returned_at: Optional[datetime]


class BatchActionRequest(BaseModel):
    ids: List[int] = Field(default_factory=list, min_length=1)


class BatchActionResponse(BaseModel):
    updated: int
    skipped: int
