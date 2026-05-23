@echo off
title SplitSmart - Starting...
color 0A

echo.
echo  ============================================
echo    SplitSmart - Full Stack App Launcher
echo  ============================================
echo.

:: -----------------------------------------------
:: Step 1: Verify MySQL service is running
:: -----------------------------------------------
echo [1/2] Checking MySQL81 service...

sc query MySQL81 | find "RUNNING" >NUL 2>&1
if not errorlevel 1 (
    echo  MySQL81 service is running!
) else (
    echo  ERROR: MySQL81 service is not running!
    echo  It should auto-start with Windows.
    echo  Open Services [services.msc] and start MySQL81 manually.
    echo.
    pause
    exit /b 1
)

echo.

:: -----------------------------------------------
:: Step 2: Start Spring Boot
:: -----------------------------------------------
echo [2/2] Starting Spring Boot Backend on port 8080...
echo.

:: Check if port 8080 is already in use (specifically looking for LISTENING state)
netstat -ano | find ":8080 " | find "LISTENING" >NUL 2>&1
if not errorlevel 1 (
    echo  WARNING: Port 8080 is already in use!
    echo  Please close the application using port 8080 and try again.
    echo.
    pause
    exit /b 1
)

echo  Wait for "Started SplitSmartApplication" message.
echo.
echo  Frontend: %~dp0index.html
echo  Backend:  http://localhost:8080
echo  Login:    test@splitsmart.com / password123
echo.
echo  ============================================
echo.

:: Open frontend in default browser
start "" "%~dp0index.html"

cd /d "%~dp0splitsmart-backend"

call mvn spring-boot:run

if errorlevel 1 (
    echo.
    echo  ============================================
    echo  ERROR: Spring Boot failed to start!
    echo  Common issues:
    echo    - MySQL not running (check services.msc for MySQL81)
    echo    - Port 8080 already in use
    echo    - Missing dependencies (run: mvn clean install)
    echo  ============================================
    echo.
)

pause