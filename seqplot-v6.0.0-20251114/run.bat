@echo off
REM Seqplot v6.0.0 Launch Script for Windows

cd /d "%~dp0"

REM Try to run JAR first
if exist "Seqplot-6.0.0.jar" (
    java -jar Seqplot-6.0.0.jar
) else (
    REM Fallback to class files
    java -cp "bin;lib\jcommon-1.0.23.jar;lib\jfreechart-1.0.19.jar" AAVSOtools.Seqplot
)
pause
