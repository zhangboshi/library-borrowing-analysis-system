# Library Borrowing Analysis System

This project provides a simple library borrowing analytics API with a minimal React dashboard.

## Backend (Spring Boot + H2)
- Endpoints for authentication, borrowing, returning, and summary statistics.
- Token-based authentication via `X-Auth-Token`.
- Trace IDs injected via `X-Trace-Id` header with rolling file logging.
- Profile-specific configuration using environment variables for database credentials and log locations.

### Run
```bash
cd backend
mvn spring-boot:run
```

### Test
```bash
cd backend
mvn test
```

## Frontend (Vite + React)
- Lightweight dashboard that surfaces borrowing summary metrics.
- Unit tests written with Vitest and Testing Library.

### Run
```bash
cd frontend
npm install
npm run dev
```

### Test
```bash
cd frontend
npm test
```

## CI
GitHub Actions workflow builds and tests both backend and frontend, including linting for the frontend.
