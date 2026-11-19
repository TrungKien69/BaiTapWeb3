@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo DEMO CUC BO: OTP duoc luu trong hop thu demo, KHONG gui email that.
call mvnw.cmd -U test-compile exec:java "-Dexec.mainClass=vn.iotstar.dev.DevServer" "-Dexec.classpathScope=test" "-Dapp.db=h2" "-Ddb.url=jdbc:h2:file:./data/servletjpa;MODE=MySQL" "-Ddb.user=sa" "-Ddb.password=" "-Dapp.demo=true" "-Dmail.mode=file"
pause
