@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist config\db.properties (
  copy config\db.properties.example config\db.properties >nul
  echo Hay sua config\db.properties va tao database servletjpa truoc.
  pause
  exit /b 1
)
if not exist config\mail.properties (
  copy config\mail.properties.example config\mail.properties >nul
  echo Hay sua config\mail.properties voi tai khoan SMTP that truoc.
  pause
  exit /b 1
)
call mvnw.cmd -U test-compile exec:java "-Dexec.mainClass=vn.iotstar.dev.DevServer" "-Dexec.classpathScope=test" "-Dapp.db=mysql" "-Dapp.demo=false" "-Dmail.mode=smtp"
pause
