import os
import sqlite3
from pathlib import Path

SCHEMA = """
CREATE TABLE IF NOT EXISTS borrow_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name TEXT NOT NULL,
    book_title TEXT NOT NULL,
    borrow_date TEXT NOT NULL,
    due_date TEXT NOT NULL,
    return_date TEXT,
    renew_count INTEGER NOT NULL DEFAULT 0,
    fine_cents INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'borrowed'
);

CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    record_id INTEGER NOT NULL,
    message TEXT NOT NULL,
    created_at TEXT NOT NULL,
    delivery_method TEXT NOT NULL DEFAULT 'email',
    FOREIGN KEY(record_id) REFERENCES borrow_records(id)
);
"""


def get_db_path() -> Path:
    env_path = os.environ.get("DATABASE_PATH")
    if env_path:
        return Path(env_path)
    return Path(__file__).with_name("library.db")


def get_connection() -> sqlite3.Connection:
    conn = sqlite3.connect(get_db_path())
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn


def init_db(seed: bool = True) -> None:
    """Create schema and optionally seed with demo data."""
    db_path = get_db_path()
    db_path.parent.mkdir(parents=True, exist_ok=True)

    with get_connection() as conn:
        conn.executescript(SCHEMA)
        if seed:
            seed_records(conn)


def seed_records(conn: sqlite3.Connection) -> None:
    cur = conn.execute("SELECT COUNT(*) FROM borrow_records")
    if cur.fetchone()[0] > 0:
        return

    sample_rows = [
        {
            "user_name": "Alice",
            "book_title": "Data Science 101",
            "borrow_date": "2024-06-01",
            "due_date": "2024-06-15",
            "return_date": None,
            "renew_count": 0,
            "fine_cents": 0,
            "status": "borrowed",
        },
        {
            "user_name": "Bob",
            "book_title": "Python Cookbook",
            "borrow_date": "2024-05-20",
            "due_date": "2024-06-05",
            "return_date": None,
            "renew_count": 1,
            "fine_cents": 0,
            "status": "borrowed",
        },
    ]

    conn.executemany(
        """
        INSERT INTO borrow_records (
            user_name, book_title, borrow_date, due_date,
            return_date, renew_count, fine_cents, status
        ) VALUES (:user_name, :book_title, :borrow_date, :due_date,
            :return_date, :renew_count, :fine_cents, :status)
        """,
        sample_rows,
    )


__all__ = ["get_connection", "init_db", "seed_records", "get_db_path"]
