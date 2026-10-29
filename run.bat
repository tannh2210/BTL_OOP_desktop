@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run-windows.ps1"
if errorlevel 1 (
  echo.
  echo An Enter de dong cua so nay.
  pause >nul
)
