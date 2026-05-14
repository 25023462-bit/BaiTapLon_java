@echo off
REM Dat file nay cung cap voi thu muc zip (D:\BaiTapLon_java-main).
REM Tu day go Maven ma khong can cd thu cong.
set "PROJ=%~dp0BaiTapLon_java-main\BaiTapLon_java-main\BaiTapLon_java-main"
if not exist "%PROJ%\pom.xml" (
  echo [LOI] Khong tim thay pom.xml tai:
  echo        %PROJ%
  exit /b 1
)
cd /d "%PROJ%"
echo [Maven] Thu muc: %CD%
echo.
call mvn %*
exit /b %ERRORLEVEL%
