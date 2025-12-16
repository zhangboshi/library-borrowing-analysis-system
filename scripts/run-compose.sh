#!/usr/bin/env bash
set -euo pipefail

ENV_FILE=${ENV_FILE:-.env}

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE. Copy .env.example and set your secrets first." >&2
  exit 1
fi

echo "Using env file: $ENV_FILE"
docker compose --env-file "$ENV_FILE" up --build
