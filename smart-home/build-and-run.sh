#!/bin/bash

# Path to versions.txt in the same directory as docker-compose file
VERSIONS_FILE="./versions.txt"

# Check if versions.txt exists
if [ ! -f "$VERSIONS_FILE" ]; then
    # If the file doesn't exist, create it with version 0
    echo "0" > "$VERSIONS_FILE"
    echo "Created versions.txt with initial version 0"
fi

# Get the last version from versions.txt
LAST_VERSION=$(tail -n 1 "$VERSIONS_FILE" 2>/dev/null)

# If tail command failed or file is empty, set default value
if [ -z "$LAST_VERSION" ] || ! [[ "$LAST_VERSION" =~ ^[0-9]+$ ]]; then
    LAST_VERSION=0
    echo "Using default version 0"
fi

# Calculate the new version (last version + 1)
NEW_VERSION=$((LAST_VERSION + 1))

# Export the version as an environment variable
export VERSION="$NEW_VERSION"

echo "Building version $VERSION"

# Run docker-compose build with the new version
docker-compose down
docker-compose up --build -d

# If build was successful, append the new version to versions.txt
if [ $? -eq 0 ]; then
    echo "$NEW_VERSION" >> "$VERSIONS_FILE"
    echo "Successfully built version $VERSION"
    
    # Run docker-compose up with the new version
    echo "Starting containers with version $VERSION"
    docker-compose up -d
    
    echo "Docker containers are now running with version $VERSION"
else
    echo "Build failed, version not incremented"
fi