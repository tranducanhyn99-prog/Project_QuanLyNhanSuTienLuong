param([switch]$SkipScreenshots)
# Mock screenshots are never run as test evidence. SQL concurrency runs separately.
& (Join-Path $PSScriptRoot 'run_verification.ps1') -LiveSql
exit $LASTEXITCODE
