# Deployment guide

This repository ships a batteries-included Docker/Compose setup for a FastAPI backend, a Vite + React frontend, MySQL, Redis, and an Nginx reverse proxy. Use this document to configure environment variables, start the stack, and locate health checks.

## Prerequisites
- Docker Engine 24+ and Docker Compose v2
- Access to a Node.js-compatible package registry (for building the frontend)
- Access to a Python package index (for backend dependencies)

## Environment variables
Copy `.env.example` to `.env` and adjust secrets and ports.

| Variable | Purpose | Default |
| --- | --- | --- |
| `APP_ENV` | Runtime environment label | `development` |
| `ALLOWED_ORIGINS` | CORS allowlist for the API | `http://localhost:8080` |
| `HEALTHCHECK_DEEP` | If `true`, the health endpoint pings MySQL/Redis | `true` |
| `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME` | MySQL connection settings used by the backend | `mysql`, `3306`, `app_user`, `app_password`, `library_db` |
| `REDIS_HOST`, `REDIS_PORT` | Redis connection settings used by the backend | `redis`, `6379` |
| `MYSQL_ROOT_PASSWORD` | MySQL root password (required by the container) | `supersecretroot` |
| `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` | App database, user, and password created at start | `library_db`, `app_user`, `app_password` |
| `BACKEND_PORT`, `FRONTEND_PORT` | Host ports exposed for backend/frontend | `8000`, `8080` |
| `MYSQL_PORT`, `REDIS_PORT` | Host ports exposed for MySQL/Redis | `3306`, `6379` |
| `VITE_API_BASE_URL` | Base URL compiled into the frontend for API calls | `http://localhost:8000` |

## Services and startup order
The stack is defined in `docker-compose.yml`:
1. **MySQL** (`mysql`): Provides relational storage. A persistent `mysql_data` volume is created automatically.
2. **Redis** (`redis`): Optional cache layer for API responses.
3. **Backend** (`backend`): FastAPI app listening on port `8000`. Depends on healthy MySQL/Redis.
4. **Frontend** (`frontend`): Nginx serving the built React app on port `8080` and reverse-proxying `/api` to the backend. Depends on a healthy backend.

> MySQL/Redis can be omitted by commenting out their services in `docker-compose.yml` and setting `HEALTHCHECK_DEEP=false` to avoid dependency pings.

## Health check endpoints
- Frontend (Nginx): `GET /health` (serves a static `ok` body)
- Backend: `GET /health` (returns JSON with runtime environment and optional dependency statuses)
- MySQL: internal Compose health check uses `mysqladmin ping`
- Redis: internal Compose health check uses `redis-cli ping`

## Build and run with Compose
1. Copy env file: `cp .env.example .env` and fill secrets.
2. Build and start everything: `./scripts/run-compose.sh`
   - Uses `.env` by default; override with `ENV_FILE=custom.env ./scripts/run-compose.sh`.
3. Stop and clean up containers: `./scripts/stop-compose.sh`

Services will be available at:
- Frontend: http://localhost:8080
- Backend API (direct): http://localhost:8000
- MySQL: localhost:3306
- Redis: localhost:6379

## Frontend bundling & reverse proxy
- Build tool: Vite + React (`npm run build` inside `frontend/`).
- Dockerfile: `frontend/Dockerfile` (Node build stage → Nginx runtime).
- Reverse proxy: `deploy/nginx.conf` proxies `/api/` to `backend:8000` inside the Compose network and serves static assets with gzip enabled.

For custom domains or TLS, mount a new Nginx config into `/etc/nginx/conf.d/default.conf` in the frontend container.

## Running services without Compose
- Backend only: `docker build -f backend/Dockerfile -t library-backend . && docker run -p 8000:8000 --env-file .env library-backend`
- Frontend only (static preview): `cd frontend && npm install && npm run dev -- --host --port 5173`

## Operational notes
- If your environment enforces an HTTP proxy, clear or override `npm_config_proxy`/`npm_config_https_proxy` when installing Node dependencies.
- Set `ALLOWED_ORIGINS` to the public frontend URL for production deployments.
- For Kubernetes or VM deployments, reuse the provided Dockerfiles and `deploy/nginx.conf` as templates and replicate the health checks in your probes.
