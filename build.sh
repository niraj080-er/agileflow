#!/bin/bash

set -e

IMAGE_NAME="agileflow-backend"

VERSION=$(
  mvn help:evaluate \
    -Dexpression=project.version \
    -q \
    -DforceStdout
)

echo "Removing existing Docker images..."

docker rmi "${IMAGE_NAME}:${VERSION}" 2>/dev/null || true
docker rmi "${IMAGE_NAME}:latest" 2>/dev/null || true

echo "Building version ${VERSION}"

docker build \
  -t "${IMAGE_NAME}:${VERSION}" \
  -t "${IMAGE_NAME}:latest" \
  .

echo "Build completed successfully."