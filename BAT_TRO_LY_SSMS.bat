@echo off
rem Use BAT_TRO_LY_SSMS.vbs directly to launch without even a brief console window.
"%SystemRoot%\System32\wscript.exe" //nologo "%~dp0BAT_TRO_LY_SSMS.vbs" %*
exit /b %errorlevel%
