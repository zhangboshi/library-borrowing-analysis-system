import datetime as dt
import os
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from db import get_connection, init_db
from services import (
    MAX_RENEWALS,
    RENEWAL_DAYS,
    calculate_fine_cents,
    calculate_overdue_days,
    create_overdue_notifications,
    list_records,
    renew_record,
    update_overdue_and_fines,
)


class TestBorrowingService(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.NamedTemporaryFile(delete=False)
        os.environ["DATABASE_PATH"] = self.tmp.name
        init_db()

    def tearDown(self):
        try:
            os.remove(self.tmp.name)
        except FileNotFoundError:
            pass

    def test_overdue_fines_are_calculated(self):
        reference_date = dt.date(2024, 6, 20)
        updated_ids = update_overdue_and_fines(reference_date)
        self.assertGreaterEqual(len(updated_ids), 2)

        with get_connection() as conn:
            row = conn.execute("SELECT * FROM borrow_records WHERE id=2").fetchone()
        overdue_days = calculate_overdue_days(dt.date.fromisoformat(row["due_date"]), reference_date)
        self.assertEqual(row["fine_cents"], calculate_fine_cents(overdue_days))
        self.assertEqual(row["status"], "overdue")

    def test_renew_respects_limit_and_extends_due(self):
        reference_date = dt.date(2024, 6, 10)
        before = list_records(reference_date)["data"][0]
        updated = renew_record(before["id"], reference_date)
        expected_due = dt.date.fromisoformat(before["due_date"]) + dt.timedelta(days=RENEWAL_DAYS)
        self.assertEqual(updated["renew_count"], before["renew_count"] + 1)
        self.assertEqual(updated["due_date"], expected_due.isoformat())

    def test_renew_blocks_after_max(self):
        with get_connection() as conn:
            conn.execute(
                "UPDATE borrow_records SET renew_count=? WHERE id=1", (MAX_RENEWALS,)
            )
        with self.assertRaises(ValueError):
            renew_record(1, dt.date(2024, 6, 20))

    def test_notifications_created_for_overdue_records(self):
        reference_date = dt.date(2024, 6, 25)
        update_overdue_and_fines(reference_date)
        notifications = create_overdue_notifications(reference_date)
        self.assertGreaterEqual(len(notifications), 1)


if __name__ == "__main__":
    unittest.main()
