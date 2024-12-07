#!/bin/bash

# Ensure script fails on any error
set -e

# Create dist directory
rm -rf dist
mkdir dist

# Install dependencies
npm install

# Copy static files
cp -r index.html offline.html manifest.json sw.js ble.js icons dist/

# Generate optimized service worker
npx workbox-cli generateSW workbox-config.js

# Create version file
echo "$(date '+%Y.%m.%d')" > dist/version.txt

# Create ZIP archive
VERSION=$(cat dist/version.txt)
zip -r "environmental-monitor-${VERSION}.zip" dist README.md package.json

echo "Package created: environmental-monitor-${VERSION}.zip"
