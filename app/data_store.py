from __future__ import annotations

import json
from collections import Counter, defaultdict
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional

ISO_FORMAT = "%Y-%m-%dT%H:%M:%S%z"


def _parse_datetime(value: Optional[str]) -> Optional[datetime]:
    if value is None:
        return None
    # Accept trailing Z or timezone-aware strings
    if value.endswith("Z"):
        value = value.replace("Z", "+00:00")
    return datetime.fromisoformat(value)


@dataclass
class Borrowing:
    id: str
    user_id: str
    book_id: str
    title: str
    genre: str
    branch: str
    borrowed_at: datetime
    returned_at: Optional[datetime]
    duration_days: Optional[int]
    late_fee: float
    rating: Optional[int]

    @classmethod
    def from_raw(cls, raw: Dict[str, Any]) -> "Borrowing":
        return cls(
            id=raw["id"],
            user_id=raw["user_id"],
            book_id=raw["book_id"],
            title=raw["title"],
            genre=raw["genre"],
            branch=raw["branch"],
            borrowed_at=_parse_datetime(raw["borrowed_at"]),
            returned_at=_parse_datetime(raw.get("returned_at")),
            duration_days=raw.get("duration_days"),
            late_fee=float(raw.get("late_fee", 0.0)),
            rating=raw.get("rating"),
        )

    def to_dict(self) -> Dict[str, Any]:
        payload = asdict(self)
        payload["borrowed_at"] = self.borrowed_at.strftime(ISO_FORMAT)
        payload["returned_at"] = (
            self.returned_at.strftime(ISO_FORMAT) if self.returned_at else None
        )
        return payload


class DataStore:
    """Lightweight in-memory store built on top of a JSON dataset."""

    def __init__(self, data_path: Path):
        self.data_path = data_path
        self.records: List[Borrowing] = self._load()

    def _load(self) -> List[Borrowing]:
        if not self.data_path.exists():
            raise FileNotFoundError(f"Missing seed data at {self.data_path}")
        raw_items = json.loads(self.data_path.read_text())
        return [Borrowing.from_raw(item) for item in raw_items]

    def _filter_records(
        self,
        *,
        user_id: Optional[str] = None,
        branch: Optional[str] = None,
        genre: Optional[str] = None,
        active_only: bool = False,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None,
    ) -> Iterable[Borrowing]:
        for record in self.records:
            if user_id and record.user_id != user_id:
                continue
            if branch and record.branch.lower() != branch.lower():
                continue
            if genre and record.genre.lower() != genre.lower():
                continue
            if active_only and record.returned_at is not None:
                continue
            if start_date and record.borrowed_at < start_date:
                continue
            if end_date and record.borrowed_at > end_date:
                continue
            yield record

    def overview(self) -> Dict[str, Any]:
        total = len(self.records)
        active = sum(1 for r in self.records if r.returned_at is None)
        unique_users = len({r.user_id for r in self.records})
        completed = [r for r in self.records if r.duration_days is not None]
        avg_duration = round(
            sum(r.duration_days for r in completed) / len(completed), 2
        ) if completed else 0

        genre_counts = Counter(r.genre for r in self.records)
        branch_counts = Counter(r.branch for r in self.records)
        top_titles = Counter(r.title for r in self.records)

        return {
            "totals": {
                "borrowings": total,
                "active_borrowings": active,
                "unique_users": unique_users,
                "average_duration_days": avg_duration,
            },
            "top_genres": genre_counts.most_common(5),
            "branch_load": dict(branch_counts),
            "popular_titles": top_titles.most_common(5),
        }

    def query_borrowings(
        self,
        *,
        user_id: Optional[str] = None,
        branch: Optional[str] = None,
        genre: Optional[str] = None,
        active_only: bool = False,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None,
        limit: int = 50,
    ) -> List[Dict[str, Any]]:
        items = list(
            self._filter_records(
                user_id=user_id,
                branch=branch,
                genre=genre,
                active_only=active_only,
                start_date=start_date,
                end_date=end_date,
            )
        )
        return [record.to_dict() for record in items[:limit]]

    def add_borrowing(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        new_id = f"b{len(self.records) + 1}"
        record = Borrowing(
            id=new_id,
            user_id=payload["user_id"],
            book_id=payload["book_id"],
            title=payload["title"],
            genre=payload["genre"],
            branch=payload.get("branch", "Central"),
            borrowed_at=now,
            returned_at=None,
            duration_days=None,
            late_fee=float(payload.get("late_fee", 0.0)),
            rating=None,
        )
        self.records.append(record)
        return record.to_dict()

    def user_history(self, user_id: str) -> Dict[str, Any]:
        history = list(self._filter_records(user_id=user_id))
        if not history:
            return {"user_id": user_id, "borrowings": [], "stats": {}}

        completed = [r for r in history if r.duration_days is not None]
        stats = {
            "total": len(history),
            "active": sum(1 for r in history if r.returned_at is None),
            "average_duration_days": round(
                sum(r.duration_days for r in completed) / len(completed), 2
            ) if completed else 0,
            "total_late_fees": round(sum(r.late_fee for r in history), 2),
        }
        return {
            "user_id": user_id,
            "borrowings": [r.to_dict() for r in history],
            "stats": stats,
        }

    def popular_books(self, limit: int = 5) -> List[Dict[str, Any]]:
        counts = Counter()
        branch_breakdown: Dict[str, Counter] = defaultdict(Counter)
        for record in self.records:
            counts[(record.book_id, record.title)] += 1
            branch_breakdown[(record.book_id, record.title)][record.branch] += 1

        popular = []
        for (book_id, title), total_count in counts.most_common(limit):
            popular.append(
                {
                    "book_id": book_id,
                    "title": title,
                    "total_borrowed": total_count,
                    "by_branch": dict(branch_breakdown[(book_id, title)]),
                }
            )
        return popular

    def branch_activity(self, branch: str) -> Dict[str, Any]:
        records = list(self._filter_records(branch=branch))
        return {
            "branch": branch,
            "active": sum(1 for r in records if r.returned_at is None),
            "late_fees": round(sum(r.late_fee for r in records), 2),
            "genres": dict(Counter(r.genre for r in records)),
            "borrowings": [r.to_dict() for r in records],
        }
