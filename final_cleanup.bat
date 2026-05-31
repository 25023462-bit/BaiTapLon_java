@echo off
cd /d "C:\Users\lenovo\Downloads\BaiTapLon_java-main (9)"
REM set or update remote
git remote remove origin 2>nul
git remote add origin https://github.com/25023462-bit/BaiTapLon_java.git

REM remove cached nested folders
git rm -r --cached "BaiTapLon_java-main/" 2>nul
git rm -r --cached "data/" 2>nul
git rm -r --cached ".idea/" 2>nul

REM stage, build, commit and push
git add .
mvn -DskipTests clean compile
git commit -m "chore: remove nested folder and data from repo" || echo Nothing to commit
git branch -M main
git push -u origin main
pause
