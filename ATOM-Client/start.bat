@echo off
cd /d "%~dp0"

:: Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Node.js is not installed. Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

:: Check if dependencies are installed
if not exist "node_modules\" (
    echo Installing dependencies...
    call npm install
)

:: Check if the server is already running on port 3000
netstat -ano | find "LISTENING" | find ":3000" >nul
if %ERRORLEVEL% NEQ 0 (
    :: Start the server in a hidden window
    echo Starting server...
    start /min cmd /c "npm start"
    
    :: Wait for the server to start
    timeout /t 3 /nobreak >nul
)

:: Open the default browser
echo Opening application in browser...
start http://localhost:3000
