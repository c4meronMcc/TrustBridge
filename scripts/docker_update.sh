#!/bin/bash

# HOW TO RUN
# Type: 'update-db' in terminal

# Navigate to the project root directory (one level up from this script)
# This ensures the command works no matter where you run the script from.
cd "$(dirname "$0")/.."

echo "=========================================="
echo "     TRUSTBRIDGE SAFE UPDATE (LINUX)"
echo "=========================================="
echo ""

echo "[1/3] Stopping containers..."
docker compose down

echo ""
echo "[2/3] Pulling updates..."
docker compose pull

echo ""
echo "[3/3] Starting up..."
docker compose up -d

echo ""
echo "✅ DONE! Database is running."