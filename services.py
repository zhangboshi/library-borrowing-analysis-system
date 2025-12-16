import datetime as dt
import textwrap
from typing import Dict, List, Optional

from db import get_connection, init_db

# Configuration constants
MAX_RENEWALS = 2
RENEWAL_DAYS = 14
FINE_PER_DAY_CENTS = 100


def _today(override: Optional[dt.date] = None) -> dt.date:
    return override or dt.date.today()


def calculate_overdue_days(due_date: dt.date, reference_date: Optional[dt.date] = None) -> int:
    ref = _today(reference_date)
    delta = (ref - due_date).days
    return delta if delta > 0 else 0


def calculate_fine_cents(overdue_days: int) -> int:
    return overdue_days * FINE_PER_DAY_CENTS


def serialize_record(row, reference_date: Optional[dt.date] = None) -> Dict:
    due_date = dt.date.fromisoformat(row["due_date"])
    overdue_days = calculate_overdue_days(due_date, reference_date) if row["return_date"] is None else 0
    return {
        "id": row["id"],
        "user_name": row["user_name"],
        "book_title": row["book_title"],
        "borrow_date": row["borrow_date"],
        "due_date": row["due_date"],
        "return_date": row["return_date"],
        "renew_count": row["renew_count"],
        "fine_cents": row["fine_cents"],
        "status": row["status"],
        "overdue_days": overdue_days,
    }


def list_records(reference_date: Optional[dt.date] = None) -> Dict[str, object]:
    init_db()
    ref = _today(reference_date)
    with get_connection() as conn:
        rows = conn.execute(
            "SELECT * FROM borrow_records ORDER BY id"
        ).fetchall()
        data = [serialize_record(row, ref) for row in rows]
    return {
        "data": data,
        "meta": {
            "max_renewals": MAX_RENEWALS,
            "renewal_days": RENEWAL_DAYS,
            "fine_per_day_cents": FINE_PER_DAY_CENTS,
        },
    }


def update_overdue_and_fines(reference_date: Optional[dt.date] = None) -> List[int]:
    init_db()
    ref = _today(reference_date)
    updated_ids: List[int] = []
    with get_connection() as conn:
        rows = conn.execute(
            "SELECT * FROM borrow_records WHERE return_date IS NULL"
        ).fetchall()
        for row in rows:
            due_date = dt.date.fromisoformat(row["due_date"])
            overdue_days = calculate_overdue_days(due_date, ref)
            fine_cents = calculate_fine_cents(overdue_days)
            status = "overdue" if overdue_days > 0 else "borrowed"
            conn.execute(
                "UPDATE borrow_records SET fine_cents=?, status=? WHERE id=?",
                (fine_cents, status, row["id"]),
            )
            updated_ids.append(row["id"])
    return updated_ids


def create_overdue_notifications(reference_date: Optional[dt.date] = None) -> List[int]:
    init_db()
    ref = _today(reference_date)
    created_ids: List[int] = []
    with get_connection() as conn:
        rows = conn.execute(
            "SELECT * FROM borrow_records WHERE status='overdue'"
        ).fetchall()
        for row in rows:
            message = textwrap.dedent(
                f"""
                Reminder: {row['user_name']}, your borrowed book '{row['book_title']}' is overdue.
                Due date: {row['due_date']}. Current fine: {row['fine_cents']/100:.2f}.
                """
            ).strip()
            cur = conn.execute(
                """
                INSERT INTO notifications (record_id, message, created_at)
                VALUES (?, ?, ?)
                """,
                (row["id"], message, ref.isoformat()),
            )
            created_ids.append(cur.lastrowid)
    return created_ids


def renew_record(record_id: int, reference_date: Optional[dt.date] = None) -> Dict:
    init_db()
    ref = _today(reference_date)
    with get_connection() as conn:
        row = conn.execute(
            "SELECT * FROM borrow_records WHERE id=?", (record_id,)
        ).fetchone()
        if row is None:
            raise ValueError("Record not found")
        if row["return_date"] is not None:
            raise ValueError("Record is already returned")

        if row["renew_count"] >= MAX_RENEWALS:
            raise ValueError("Maximum renewals reached")

        due_date = dt.date.fromisoformat(row["due_date"])
        overdue_days = calculate_overdue_days(due_date, ref)
        fine_cents = calculate_fine_cents(overdue_days)

        start_date = ref if ref > due_date else due_date
        new_due = start_date + dt.timedelta(days=RENEWAL_DAYS)

        conn.execute(
            """
            UPDATE borrow_records
            SET renew_count = renew_count + 1,
                due_date = ?,
                fine_cents = ?,
                status = 'borrowed'
            WHERE id = ?
            """,
            (new_due.isoformat(), fine_cents, record_id),
        )

        updated = conn.execute(
            "SELECT * FROM borrow_records WHERE id=?", (record_id,)
        ).fetchone()
    return serialize_record(updated, ref)


def run_daily_tasks(reference_date: Optional[dt.date] = None) -> Dict[str, List[int]]:
    updated_ids = update_overdue_and_fines(reference_date)
    notification_ids = create_overdue_notifications(reference_date)
    return {"updated_records": updated_ids, "notifications": notification_ids}


__all__ = [
    "list_records",
    "renew_record",
    "run_daily_tasks",
    "update_overdue_and_fines",
    "create_overdue_notifications",
    "serialize_record",
    "calculate_overdue_days",
    "calculate_fine_cents",
    "MAX_RENEWALS",
    "RENEWAL_DAYS",
    "FINE_PER_DAY_CENTS",
]
