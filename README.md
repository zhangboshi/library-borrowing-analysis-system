# Library Borrowing Analysis System

A lightweight FastAPI + vanilla JS demo that tracks library borrowing records with role-based access control and audit logging.

## Features
- Roles (ADMIN/STAFF/VIEWER) with user-role relationships, returned on login.
- RBAC-protected endpoints: creating borrowings and marking returns require STAFF or ADMIN.
- Operation logs capturing who performed each borrowing action and when.
- Front-end dashboard with login, borrowing list, audit log list, and role-aware buttons.

## Getting started
1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
2. Run the API + static UI (serves `frontend/index.html`):
   ```bash
   uvicorn app.main:app --reload
   ```
3. Open `http://127.0.0.1:8000/` in your browser.

### Seeded accounts
All default passwords are `password`:
- `admin` (ADMIN)
- `staff` (STAFF)
- `viewer` (VIEWER)

### Notes
- The SQLite database file (`library.db`) is created on first run.
- Audit logs appear for borrowing creation and return actions and are visible to STAFF/ADMIN.
