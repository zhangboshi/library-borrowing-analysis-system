import os
from functools import lru_cache
from typing import Dict, Optional

import mysql.connector
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from redis import Redis


class Settings:
    def __init__(self) -> None:
        self.env = os.getenv("APP_ENV", "development")
        self.db_host = os.getenv("DB_HOST", "mysql")
        self.db_port = int(os.getenv("DB_PORT", "3306"))
        self.db_user = os.getenv("DB_USER", "app_user")
        self.db_password = os.getenv("DB_PASSWORD", "app_password")
        self.db_name = os.getenv("DB_NAME", "library_db")
        self.redis_host = os.getenv("REDIS_HOST", "redis")
        self.redis_port = int(os.getenv("REDIS_PORT", "6379"))
        allowed_origins = os.getenv("ALLOWED_ORIGINS", "*")
        self.allowed_origins = [origin.strip() for origin in allowed_origins.split(",") if origin.strip()]
        self.healthcheck_deep = os.getenv("HEALTHCHECK_DEEP", "false").lower() == "true"


@lru_cache
def get_settings() -> Settings:
    return Settings()


def create_app() -> FastAPI:
    settings = get_settings()
    app = FastAPI(title="Library Borrowing Analysis API", version="0.1.0")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.allowed_origins or ["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @app.get("/health")
    def health() -> Dict[str, str]:
        checks: Dict[str, str] = {"status": "ok", "env": settings.env}

        if settings.healthcheck_deep:
            checks.update(run_dependency_checks(settings))

        return checks

    @app.get("/api/borrowings/summary")
    def borrowings_summary() -> Dict[str, Optional[int]]:
        """A placeholder summary endpoint that can be expanded to query MySQL/Redis."""
        return {
            "totalBorrowings": None,
            "activeUsers": None,
            "message": "Connect to MySQL/Redis and replace this stub with real analytics.",
        }

    return app


def run_dependency_checks(settings: Settings) -> Dict[str, str]:
    checks: Dict[str, str] = {}

    try:
        mysql.connector.connect(
            host=settings.db_host,
            port=settings.db_port,
            user=settings.db_user,
            password=settings.db_password,
            database=settings.db_name,
            connection_timeout=2,
        ).close()
        checks["mysql"] = "up"
    except Exception as exc:  # pragma: no cover - exercised via healthcheck
        checks["mysql"] = f"down: {exc}"  # type: ignore[str-format]

    try:
        client = Redis(host=settings.redis_host, port=settings.redis_port, socket_connect_timeout=2)
        client.ping()
        checks["redis"] = "up"
    except Exception as exc:  # pragma: no cover - exercised via healthcheck
        checks["redis"] = f"down: {exc}"  # type: ignore[str-format]

    return checks


app = create_app()
