#!/bin/bash

set -e

IMAGE_NAME="agileflow-backend"

VERSION=$(./mvnw help:evaluate \
  -Dexpression=project.version \
  -q \
  -DforceStdout)

echo "Building version ${VERSION}"

./mvnw clean package -DskipTests

docker build \
  -t "${IMAGE_NAME}:${VERSION}" \
  -t "${IMAGE_NAME}:latest" \
  .

echo "Build completed successfully."
