@echo off
:: This script must be run as Administrator
echo.
echo  ============================================
echo    MySQL Service Setup (Run as Admin)
echo  ============================================
echo.

:: Remove any existing service first
echo Removing old MySQL service (if any)...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --remove MySQL80 2>nul
echo.

:: Install with the correct datadir
echo Installing MySQL80 service...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --install MySQL80 --datadir="C:\ProgramData\MySQL\MySQL Server 8.0\Data"
echo.

:: Start the service
echo Starting MySQL80 service...
net start MySQL80
echo.

:: Verify
echo Verifying...
sc query MySQL80
echo.
echo  ============================================
echo  Done! MySQL will now auto-start with Windows.
echo  ============================================
pause
