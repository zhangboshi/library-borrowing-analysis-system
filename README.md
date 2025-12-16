# library-borrowing-analysis-system

Infrastructure scaffolding for a FastAPI backend, a Vite + React frontend, and supporting MySQL/Redis services. Dockerfiles are provided for both the API and the web client along with an Nginx reverse proxy example and helper scripts.

## Quick start
1. Copy the sample environment: `cp .env.example .env` and edit secrets/ports.
2. Launch everything: `./scripts/run-compose.sh` (uses `docker-compose.yml`).
3. Visit the frontend at http://localhost:8080 (API lives at http://localhost:8000).
4. Stop the stack when finished: `./scripts/stop-compose.sh`.

## Key files
- `backend/Dockerfile`: FastAPI backend container.
- `frontend/Dockerfile`: Builds the React app and serves it via Nginx (reverse proxies `/api`).
- `deploy/nginx.conf`: Nginx config example for static assets + API proxying.
- `docker-compose.yml`: Orchestrates backend, frontend, MySQL, and Redis.
- `.env.example`: Environment variable template for all services.
- `docs/deployment.md`: Full deployment, health check, and environment reference.

See `docs/deployment.md` for detailed instructions, health checks, and operational notes.
