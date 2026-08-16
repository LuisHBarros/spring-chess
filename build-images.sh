#!/usr/bin/env bash
# Build Spring Chess microservice images for Docker Swarm deployment.
# Requires Maven and Docker.

set -e

SERVICES=(auth social chat game)

for svc in "${SERVICES[@]}"; do
  echo "Building $svc ..."
  (
    cd "$svc"
    mvn -B "-Dmaven.test.skip=true" package
    docker build -t "chess-$svc:latest" .
  )
done

echo "Images built. Deploy with: docker stack deploy -c docker-compose.swarm.yml spring-chess"
