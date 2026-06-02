@echo off
setlocal

set "PROJECT_DIR=%~dp0"
if exist "%PROJECT_DIR%pom.xml" goto project_ok

set "PROJECT_DIR=%~dp0BaiTapLon_java-main\"
if exist "%PROJECT_DIR%pom.xml" goto project_ok

echo [LOI] Khong tim thay pom.xml.
pause
exit /b 1

:project_ok
cd /d "%PROJECT_DIR%"
call Chay-Maven.bat clean compile
if errorlevel 1 exit /b %ERRORLEVEL%
java -cp target\classes com.bidplaza.network.AuctionServer
