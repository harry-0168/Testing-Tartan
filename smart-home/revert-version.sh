#!/bin/bash

# Path to versions.txt in the same directory as docker-compose file
VERSIONS_FILE="./versions.txt"

# Check if versions.txt exists
if [ ! -f "$VERSIONS_FILE" ]; then
    echo "Error: versions.txt file not found."
    exit 1
fi

# Check if the file has at least two versions
LINE_COUNT=$(wc -l < "$VERSIONS_FILE")
if [ "$LINE_COUNT" -lt 2 ]; then
    echo "Error: No previous version to revert to. Only one version exists."
    exit 1
fi

# Get the current version (last line)
CURRENT_VERSION=$(tail -n 1 "$VERSIONS_FILE")

# Get the previous version (second to last line)
PREVIOUS_VERSION=$(tail -n 2 "$VERSIONS_FILE" | head -n 1)

# Remove the last line from versions.txt (current version)
sed -i '$d' "$VERSIONS_FILE"

# Export the previous version as an environment variable
export VERSION="$PREVIOUS_VERSION"

echo "Reverting from version $CURRENT_VERSION to version $PREVIOUS_VERSION"

# Stop current containers
echo "Stopping current containers..."
docker-compose down

# Start containers with the previous version
echo "Starting containers with version $PREVIOUS_VERSION"
docker-compose up -d

echo "Docker containers are now running with version $PREVIOUS_VERSION"