param([ValidatePattern('^PRJ_Fix_QA_[A-Za-z0-9_]+$')][string]$Database = 'PRJ_Fix_QA_20261007_01')
$ErrorActionPreference = 'Stop'
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=$Database;encrypt=true;trustServerCertificate=true"
& (Join-Path $PSScriptRoot 'start_app.ps1')
