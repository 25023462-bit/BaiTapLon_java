@echo off
setlocal

set "PROJECT_DIR=%~dp0"
if exist "%PROJECT_DIR%pom.xml" goto project_ok

set "PROJECT_DIR=%~dp0BaiTapLon_java-main\"
if exist "%PROJECT_DIR%pom.xml" goto project_ok

echo [LOI] Khong tim thay pom.xml.
echo Hay dat file nay trong thu muc project hoac thu muc cha cua project.
echo.
pause
exit /b 1

:project_ok
cd /d "%PROJECT_DIR%"

echo [1/3] Build project...
call Chay-Maven.bat clean compile
if errorlevel 1 goto build_failed

echo.
echo [2/3] Mo server...
start "BidPlaza Server" /D "%PROJECT_DIR%" cmd /k "java -cp target\classes com.bidplaza.network.AuctionServer"

echo Dang cho server khoi dong...
timeout /t 3 /nobreak >nul

echo.
echo [3/3] Mo client JavaFX...
start "BidPlaza Client" /D "%PROJECT_DIR%" cmd /k "Chay-Maven.bat javafx:run"

echo.
echo Da mo server va client.
echo Co the dong cua so nay.
pause
exit /b 0

:build_failed
echo.
echo [LOI] Build that bai. Hay xem loi Maven o tren.
pause
exit /b 1
