#!/bin/bash
# Start Postgres (and optionally the backend) via Docker Compose.
# Environment variables are loaded from .env file
#
#   ./run.sh            # start Postgres only, then run the app locally with Maven
#   ./run.sh --all      # build + start Postgres AND the backend container

set -e

# Load environment variables from .env file
if [ -f .env ]; then
  export $(cat .env | grep -v '^#' | xargs)
  echo "Loaded environment variables from .env"
else
  echo "Warning: .env file not found. Please create .env with your configuration."
fi

PORT=${SERVER_PORT:-8090}

# Kill any process already using the target port
kill_port() {
  local pid
  pid=$(ss -tlnp "sport = :$1" | grep -oP 'pid=\K[0-9]+' | head -1)
  if [ -n "$pid" ]; then
    echo "Killing existing process on port $1 (PID $pid)..."
    kill -9 "$pid"
  fi
}

# On exit, bring down all containers
cleanup() {
  echo "Shutting down containers..."
  docker compose down
}
trap cleanup EXIT INT TERM

kill_port "$PORT"

if [ "$1" == "--all" ]; then
  ./build.sh
  docker compose up -d
  echo "Stack started. API: http://localhost:${PORT}  Swagger: http://localhost:${PORT}/swagger-ui.html"
  echo "Press Ctrl+C to stop."
  docker compose logs -f
else
  docker compose up -d postgres
  echo "Postgres is up on ${DB_HOST:-localhost}:${DB_PORT:-5432} (db=${DB_NAME:-agileflow_db}, user=${DB_USER:-postgres})."
  echo "Now run the app locally with:"
  echo "  ./mvnw spring-boot:run -Dspring-boot.run.arguments=\"--server.port=${PORT}\""
fi
