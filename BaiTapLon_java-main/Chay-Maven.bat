@echo off
setlocal
REM Chay Maven tu dung thu muc project.
REM Maven 4 tren Windows co the loi neu duong dan co dau tieng Viet,
REM nen script map thu muc cha sang mot o dia tam co duong dan ASCII.

set "PROJ=%~dp0"
if exist "%PROJ%pom.xml" goto project_found

set "TRY_PROJ=%~dp0BaiTapLon_java-main\"
if exist "%TRY_PROJ%pom.xml" set "PROJ=%TRY_PROJ%" && goto project_found

set "TRY_PROJ=%~dp0BaiTapLon_java-main\BaiTapLon_java-main\"
if exist "%TRY_PROJ%pom.xml" set "PROJ=%TRY_PROJ%" && goto project_found

echo [LOI] Khong tim thay pom.xml tai:
echo        %PROJ%
exit /b 1

:project_found
for %%I in ("%PROJ%..") do set "PROJECT_PARENT=%%~fI"
for %%I in ("%PROJ%.") do set "PROJECT_NAME=%%~nxI"

set "SUBST_DRIVE="
for %%D in (Z Y X W V U T S R Q P O N M L K J I H G F E) do call :pick_drive %%D

if not defined SUBST_DRIVE goto no_subst
subst %SUBST_DRIVE% "%PROJECT_PARENT%" >nul
set "RUN_DIR=%SUBST_DRIVE%\%PROJECT_NAME%"
goto run_maven

:no_subst
set "RUN_DIR=%PROJ%"

:run_maven
cd /d "%RUN_DIR%"
echo [Maven] Thu muc: %CD%
echo.
call mvn %*
set "EXIT_CODE=%ERRORLEVEL%"

if defined SUBST_DRIVE subst %SUBST_DRIVE% /D >nul

exit /b %EXIT_CODE%

:pick_drive
if defined SUBST_DRIVE exit /b
if not exist %1:\nul set "SUBST_DRIVE=%1:"
exit /b
