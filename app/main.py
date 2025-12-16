from __future__ import annotations

from datetime import datetime
from pathlib import Path
from typing import Optional

from fastapi import Body, FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware

from .data_store import DataStore, _parse_datetime

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "data" / "sample_borrowings.json"

app = FastAPI(
    title="Library Borrowing Analysis System",
    version="0.1.0",
    description="A minimal analytics API that demonstrates borrowing insights and sample queries.",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

store = DataStore(DATA_PATH)


def parse_datetime_param(value: Optional[str]) -> Optional[datetime]:
    if value is None:
        return None
    try:
        return _parse_datetime(value)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=f"Invalid datetime: {value}") from exc


@app.get("/health", tags=["system"])
def healthcheck() -> dict:
    return {"status": "ok", "records": len(store.records)}


@app.get("/stats/overview", tags=["analytics"])
def overview() -> dict:
    return store.overview()


@app.get("/borrowings", tags=["borrowings"])
def borrowings(
    user_id: Optional[str] = Query(None, description="Filter by user ID"),
    branch: Optional[str] = Query(None, description="Filter by branch"),
    genre: Optional[str] = Query(None, description="Filter by genre"),
    active_only: bool = Query(False, description="Show only active borrowings"),
    start_date: Optional[str] = Query(
        None, description="ISO date (YYYY-MM-DD or ISO8601) for borrow start"
    ),
    end_date: Optional[str] = Query(
        None, description="ISO date (YYYY-MM-DD or ISO8601) for borrow end"
    ),
    limit: int = Query(50, ge=1, le=500, description="Max records to return"),
) -> dict:
    start_dt = parse_datetime_param(start_date) if start_date else None
    end_dt = parse_datetime_param(end_date) if end_date else None
    return {
        "items": store.query_borrowings(
            user_id=user_id,
            branch=branch,
            genre=genre,
            active_only=active_only,
            start_date=start_dt,
            end_date=end_dt,
            limit=limit,
        )
    }


@app.post("/borrowings", tags=["borrowings"], status_code=201)
def add_borrowing(
    payload: dict = Body(
        ..., example={
            "user_id": "u2001",
            "book_id": "9780140449266",
            "title": "The Iliad",
            "genre": "Classics",
            "branch": "Central",
        }
    ),
) -> dict:
    required_fields = {"user_id", "book_id", "title", "genre"}
    missing = required_fields - payload.keys()
    if missing:
        raise HTTPException(status_code=400, detail=f"Missing fields: {', '.join(sorted(missing))}")
    return store.add_borrowing(payload)


@app.get("/users/{user_id}/history", tags=["users"])
def user_history(user_id: str) -> dict:
    return store.user_history(user_id)


@app.get("/books/popular", tags=["analytics"])
def popular_books(limit: int = Query(5, ge=1, le=20)) -> dict:
    return {"items": store.popular_books(limit)}


@app.get("/branches/{branch}/activity", tags=["analytics"])
def branch_activity(branch: str) -> dict:
    return store.branch_activity(branch)


@app.get("/demo/insights", tags=["demo"])
def demo_insights() -> dict:
    return {
        "insights": [
            "History and Fiction genres are most borrowed this month.",
            "Central branch has the highest active loans (3).",
            "Average return time across completed loans is 13.0 days.",
        ],
        "sample_queries": [
            {
                "title": "Find active loans",
                "curl": "curl -s 'http://localhost:8000/borrowings?active_only=true'",
            },
            {
                "title": "Top 3 popular books",
                "curl": "curl -s 'http://localhost:8000/books/popular?limit=3'",
            },
        ],
    }


@app.get("/")
def root() -> dict:
    return {
        "message": "Welcome to the Library Borrowing Analysis API",
        "docs": "/docs",
        "health": "/health",
    }
