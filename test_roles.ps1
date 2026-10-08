& (Join-Path $PSScriptRoot 'run_verification.ps1') -TestClass SecurityRegressionTest
exit $LASTEXITCODE
