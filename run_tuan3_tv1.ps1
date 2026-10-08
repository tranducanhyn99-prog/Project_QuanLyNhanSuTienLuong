param([switch]$SkipScreenshots)
# Read-only live Java smoke; SQL behavior/benchmark run separately.
& (Join-Path $PSScriptRoot 'run_verification.ps1') -TestClass NhanSuModuleTest
exit $LASTEXITCODE
