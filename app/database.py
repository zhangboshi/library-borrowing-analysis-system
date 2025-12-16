from datetime import datetime, timezone
from typing import Iterable

from sqlmodel import Session, SQLModel, create_engine, select

from .models import Book, BorrowRecord, Reader

DATABASE_URL = "sqlite:///./library.db"
engine = create_engine(DATABASE_URL, echo=False)


def init_db(sample: bool = True) -> None:
    SQLModel.metadata.create_all(engine)
    if sample:
        seed_sample_data()


def get_session() -> Iterable[Session]:
    with Session(engine) as session:
        yield session


def seed_sample_data() -> None:
    with Session(engine) as session:
        reader_count = session.exec(select(Reader)).first()
        book_count = session.exec(select(Book)).first()
        if reader_count and book_count:
            return

        reader = Reader(name="Alice Johnson", email="alice@example.com", phone="123-456-7890")
        reader2 = Reader(name="Bob Smith", email="bob@example.com", phone="555-0102")
        book = Book(title="Python 101", category="Programming", author="Jane Doe")
        book2 = Book(title="Data Science Handbook", category="Data", author="John Roe")

        session.add_all([reader, reader2, book, book2])
        session.commit()

        borrow1 = BorrowRecord(
            reader_id=reader.id,
            book_id=book.id,
            status="borrowed",
            borrowed_at=datetime.now(timezone.utc),
            category=book.category,
        )
        borrow2 = BorrowRecord(
            reader_id=reader2.id,
            book_id=book2.id,
            status="returned",
            borrowed_at=datetime.now(timezone.utc),
            returned_at=datetime.now(timezone.utc),
            category=book2.category,
        )
        session.add_all([borrow1, borrow2])
        session.commit()
