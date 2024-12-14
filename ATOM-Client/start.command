#!/bin/bash

# Change to the script's directory
cd "$(dirname "$0")"

# Check if Node.js is installed
if ! command -v node >/dev/null 2>&1; then
    echo "Node.js is not installed. Please install Node.js from https://nodejs.org/"
    read -n 1 -s -r -p "Press any key to exit..."
    exit 1
fi

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Check if the server is already running on port 3000
if ! lsof -i:3000 >/dev/null 2>&1; then
    # Start the server in the background
    echo "Starting server..."
    npm start &
    
    # Wait for the server to start
    for i in {1..10}; do
        if curl -s http://localhost:3000 >/dev/null; then
            break
        fi
        sleep 1
    done
fi

# Open the default browser
echo "Opening application in browser..."
open "http://localhost:3000"
