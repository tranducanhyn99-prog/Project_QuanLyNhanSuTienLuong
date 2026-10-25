@echo off
chcp 65001 >nul
title Tro Ly Goi Y Code SQL trong SSMS (Alt + Q / F8)
cd /d "%~dp0"

python "%~dp0tools\ssms_assistant\ssms_daemon.py"
pause
