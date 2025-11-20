@echo off
REM Seqplot v6.0.0 Launcher for Windows
REM Automatically runs with the correct classpath

echo ========================================
echo Seqplot v6.0.0 - Build 20251120
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in your PATH!
    echo.
    echo Seqplot requires Java 11 or higher to run.
    echo.
    echo Please download and install Java from:
    echo   https://adoptium.net/temurin/releases/
    echo.
    pause
    exit /b 1
)

echo Starting Seqplot...
echo.

REM Run Seqplot with the correct classpath
java -cp "bin;lib\jcommon-1.0.23.jar;lib\jfreechart-1.0.19.jar" AAVSOtools.Seqplot

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Seqplot failed to start!
    pause
)
