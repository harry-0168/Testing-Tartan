#!/bin/bash

# Get version from argument or use date-based version if not provided
VERSION=${1:-$(date +%Y%m%d%H%M%S)}

# Build the images with version tag
VERSION=$VERSION docker-compose build

# Create "latest" tags as well
docker tag house-cmu:$VERSION house-cmu:latest
docker tag house-mse:$VERSION house-mse:latest
docker tag platform:$VERSION platform:latest
docker tag mysql-app:$VERSION mysql-app:latest

echo "Images built with version: $VERSION"