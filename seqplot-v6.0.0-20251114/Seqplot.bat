@echo off
REM Seqplot Launcher for Windows
REM Checks for Java and provides helpful error messages

echo ========================================
echo Seqplot v6.0.0 Launcher
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
    echo Choose "JRE" for runtime only, or "JDK" if you're a developer.
    echo Make sure to select "Add to PATH" during installation.
    echo.
    pause
    exit /b 1
)

REM Check Java version
echo Checking Java version...
java -version 2>&1 | findstr /R "version" > temp_version.txt
for /f "tokens=3" %%g in (temp_version.txt) do (
    set JAVA_VERSION=%%g
)
del temp_version.txt

echo Found Java: %JAVA_VERSION%
echo.

REM Look for JAR file
if exist "Seqplot-6.0.0.jar" (
    echo Starting Seqplot...
    echo.
    java -jar Seqplot-6.0.0.jar
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Failed to start Seqplot!
        echo.
        pause
        exit /b 1
    )
) else if exist "bin\AAVSOtools\Seqplot.class" (
    echo Starting Seqplot from class files...
    echo.
    java -cp "bin;lib\jcommon-1.0.23.jar;lib\jfreechart-1.0.19.jar" AAVSOtools.Seqplot
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Failed to start Seqplot!
        echo.
        pause
        exit /b 1
    )
) else (
    echo ERROR: Could not find Seqplot files!
    echo.
    echo Please make sure you extracted all files from the ZIP archive.
    echo.
    pause
    exit /b 1
)

REM Only pause if there was an error (user won't see this normally)
