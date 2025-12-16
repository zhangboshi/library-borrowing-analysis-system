# Library Borrowing Analysis System

A lightweight FastAPI service for importing readers, books, and borrowing records from CSV/Excel, exporting filtered borrowing data, and performing batch return/delete operations with a simple web UI.

## Getting started

1. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```
2. **Run the server**
   ```bash
   uvicorn app.main:app --reload
   ```
3. Open the UI at http://localhost:8000/ to upload files, export data, and run batch actions.

## Import format

Each import endpoint validates required columns and reports failed rows.

- Readers: `name`, `email` (optional `phone`).
- Books: `title`, `category` (optional `author`).
- Borrow records: `reader_email`, `book_title`, `borrowed_at` (ISO datetime), optional `category`, `due_at`, `status`.

## Key endpoints

- `POST /import/readers` — Upload CSV/XLSX of readers.
- `POST /import/books` — Upload CSV/XLSX of books.
- `POST /import/borrows` — Upload CSV/XLSX of borrow records.
- `GET /borrows` — List borrow records with optional filters (`start_date`, `end_date`, `category`, `status`).
- `GET /export/borrows` — Export filtered records to CSV or Excel (`file_format=csv|xlsx`).
- `POST /borrows/batch-return` — Batch return by IDs (sets status to `returned`).
- `POST /borrows/batch-delete` — Soft delete by IDs.

A `GET /health` endpoint is available for readiness checks.

## Frontend

A minimal HTML/JavaScript page is served from `/` that supports:
- Uploading CSV/Excel for readers, books, and borrow records.
- Filtering borrow records by date, category, and status.
- Exporting filtered results as CSV or Excel.
- Selecting multiple rows for batch return or soft delete.

The database is stored in `library.db` (ignored via `.gitignore`). Sample data is seeded on startup for quick testing.
