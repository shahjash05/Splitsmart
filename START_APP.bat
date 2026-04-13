@echo off
title SplitSmart - Starting...
color 0A

echo.
echo  ============================================
echo    SplitSmart - Full Stack App Launcher
echo  ============================================
echo.

:: Skip port killing (to avoid crash)
echo [0/2] Skipping port check (safe mode)...
echo  Make sure port 8080 is free!
echo.

:: Step 1: Start MySQL
echo [1/2] Starting MySQL Server...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I "mysqld.exe" >NUL

if "%ERRORLEVEL%"=="0" (
    echo  MySQL is already running!
) else (
    start "" "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --datadir="C:\ProgramData\MySQL\MySQL Server 8.0\Data" --port=3306
    echo  MySQL starting... waiting 6 seconds...
    timeout /t 6 >nul
    echo  MySQL started!
)

echo.
echo [2/2] Starting Spring Boot Backend on port 8080...
echo  Wait for "Started SplitSmartApplication" then open index.html
echo.
echo  Frontend: %~dp0index.html
echo  Backend:  http://localhost:8080
echo  Login:    test@splitsmart.com / password123
echo.
echo  ============================================
echo.

cd /d "%~dp0splitsmart-backend"

mvn spring-boot:run

pause