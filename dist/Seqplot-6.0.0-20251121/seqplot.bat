@echo off
REM Seqplot 6.0.0 - Windows Launcher
REM Release: November 21, 2025

REM Get the directory where this batch file is located
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or later to run Seqplot
    echo.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Display Java version
echo Checking Java version...
java -version

REM Run Seqplot
echo.
echo Starting Seqplot 6.0.0 (2025-11-21)...
java -jar Seqplot-6.0.0-20251121.jar %*

if %errorlevel% neq 0 (
    echo.
    echo Seqplot exited with error code %errorlevel%
    pause
)
