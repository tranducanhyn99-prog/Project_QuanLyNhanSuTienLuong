param(
    [string]$ServerInstance,
    [string]$Database,
    [string]$User,
    [string]$Password,
    [switch]$SkipBenchmark
)

$ErrorActionPreference = "Stop"

$repoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
$configPath = Join-Path $repoRoot "src\resources\config.properties"
$serverSpecifiedOnCommandLine = $PSBoundParameters.ContainsKey("ServerInstance")
$serverLoadedFromConfig = $false
$hasCompleteCliOverrides = $serverSpecifiedOnCommandLine -and
    ![string]::IsNullOrWhiteSpace($ServerInstance) -and
    ![string]::IsNullOrWhiteSpace($Database) -and
    ![string]::IsNullOrWhiteSpace($User) -and
    ![string]::IsNullOrWhiteSpace($Password)

$config = @{}
if (!$hasCompleteCliOverrides -and (Test-Path -LiteralPath $configPath)) {
    Get-Content -LiteralPath $configPath -Encoding UTF8 | ForEach-Object {
        if ($_ -match '^\s*([^#!][^=]*)=(.*)$') {
            $config[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
}

$jdbcUrl = $config["db.url"]
$jdbcMatch = $null
if (![string]::IsNullOrWhiteSpace($jdbcUrl)) {
    $jdbcMatch = [regex]::Match(
        $jdbcUrl,
        '^jdbc:sqlserver://(?<host>\[[^\]]+\]|[^;:]+)(?::(?<port>\d+))?;(?<properties>.*)$',
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
    )
}

$jdbcProperties = @{}
if ($null -ne $jdbcMatch -and $jdbcMatch.Success) {
    $jdbcMatch.Groups["properties"].Value -split ';' | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            $jdbcProperties[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
}

if ($serverSpecifiedOnCommandLine -and [string]::IsNullOrWhiteSpace($ServerInstance)) {
    throw "-ServerInstance da duoc truyen nhung khong co gia tri hop le."
}
if (!$serverSpecifiedOnCommandLine) {
    if ($null -eq $jdbcMatch -or !$jdbcMatch.Success) {
        throw "Khong co -ServerInstance va khong the doc server tu db.url trong config.properties."
    }
    $hostName = $jdbcMatch.Groups["host"].Value
    $port = $jdbcMatch.Groups["port"].Value
    $ServerInstance = if ($port) { "$hostName,$port" } else { $hostName }
    $serverLoadedFromConfig = $true
}
if ([string]::IsNullOrWhiteSpace($Database)) {
    $Database = $jdbcProperties["databaseName"]
}
if ([string]::IsNullOrWhiteSpace($Database)) {
    $Database = "QuanLyNhanSuTienLuong"
}
if ([string]::IsNullOrWhiteSpace($User)) {
    $User = $config["db.user"]
}
if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = $config["db.password"]
}

if ([string]::IsNullOrWhiteSpace($User) -or [string]::IsNullOrWhiteSpace($Password) -or
    $Password -eq "YOUR_PASSWORD_HERE") {
    throw "Thieu tai khoan SQL Server hop le. Hay truyen -User/-Password hoac cau hinh db.user/db.password."
}

$resultDir = Join-Path $repoRoot "build\test-results"
if (!(Test-Path -LiteralPath $resultDir)) {
    New-Item -ItemType Directory -Path $resultDir | Out-Null
}
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logPath = Join-Path $resultDir "TV4_Payroll_Test_$timestamp.log"
$logLines = [System.Collections.Generic.List[string]]::new()

function Write-TestLog {
    param([string]$Message)
    $line = "[{0}] {1}" -f (Get-Date -Format "HH:mm:ss.fff"), $Message
    $logLines.Add($line)
    Write-Host $line
}

function New-TestConnection {
    param([string]$DataSource)

    $builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
    $builder["Data Source"] = $DataSource
    $builder["Initial Catalog"] = $Database
    $builder["User ID"] = $User
    $builder["Password"] = $Password
    $builder["Encrypt"] = $false
    $builder["TrustServerCertificate"] = $true
    $builder["Application Name"] = "TV4 Payroll Tests"

    $connection = New-Object System.Data.SqlClient.SqlConnection $builder.ConnectionString
    $connection.add_InfoMessage({
        param($sender, $eventArgs)
        foreach ($message in $eventArgs.Message -split "`r?`n") {
            if (![string]::IsNullOrWhiteSpace($message)) {
                Write-TestLog "SQL: $message"
            }
        }
    })
    return $connection
}

function Test-IsLocalSqlHost {
    param([string]$HostName)

    if ([string]::IsNullOrWhiteSpace($HostName)) {
        return $false
    }

    $normalizedHost = $HostName.Trim().TrimStart('[').TrimEnd(']').ToLowerInvariant()
    return $normalizedHost -in @("localhost", "127.0.0.1", "::1", ".", "(local)")
}

function Open-TestConnection {
    $candidates = [System.Collections.Generic.List[string]]::new()
    $candidates.Add($ServerInstance)

    if ($serverLoadedFromConfig -and $null -ne $jdbcMatch -and $jdbcMatch.Success) {
        $configuredHost = $jdbcMatch.Groups["host"].Value
        if ((Test-IsLocalSqlHost $configuredHost) -and !$candidates.Contains(".")) {
            # SqlClient dung Shared Memory voi '.', huu ich khi SQL Server local chua bat TCP/IP.
            $candidates.Add(".")
        }
    }

    $lastError = $null
    foreach ($candidate in $candidates) {
        $connection = New-TestConnection $candidate
        try {
            Write-TestLog "Dang ket noi SQL Server: $candidate / $Database"
            $connection.Open()
            Write-TestLog "Ket noi thanh cong bang $candidate."
            return $connection
        } catch {
            $lastError = $_.Exception
            $connection.Dispose()
            Write-TestLog "Khong ket noi duoc bang $candidate; thu phuong an tiep theo neu co."
        }
    }

    throw "Khong the ket noi SQL Server: $($lastError.Message)"
}

function ConvertTo-TestLogValue {
    param(
        [object]$Value,
        [int]$MaxLength = 500
    )

    if ($null -eq $Value -or $Value -is [System.DBNull]) {
        return "NULL"
    }
    if ($Value -is [byte[]]) {
        return "<binary:$($Value.Length)-bytes>"
    }

    if ($Value -is [System.IFormattable]) {
        $textValue = $Value.ToString($null, [System.Globalization.CultureInfo]::InvariantCulture)
    } else {
        $textValue = [string]$Value
    }
    $textValue = $textValue -replace '[\r\n\t]+', ' '
    if ($textValue.Length -gt $MaxLength) {
        return $textValue.Substring(0, $MaxLength) + "...<truncated>"
    }
    return $textValue
}

function Invoke-SqlFile {
    param(
        [System.Data.SqlClient.SqlConnection]$Connection,
        [string]$Path
    )

    if (!(Test-Path -LiteralPath $Path)) {
        throw "Khong tim thay SQL test: $Path"
    }

    Write-TestLog "Bat dau: $([System.IO.Path]::GetFileName($Path))"
    $sql = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
    $sqlWithoutUse = [regex]::Replace(
        $sql,
        '(?im)^\s*USE\s+(?:\[[^\]\r\n]+\]|[^\s;]+)\s*;\s*(?:--[^\r\n]*)?$',
        '-- Database context is selected by run_payroll_tests.ps1.'
    )
    if ($sqlWithoutUse -ne $sql) {
        Write-TestLog "Bo qua lenh USE trong file; giu database dich: $Database"
        $sql = $sqlWithoutUse
    }
    $batches = [regex]::Split(
        $sql,
        '(?im)^\s*GO\s*;?\s*(?:--.*)?$'
    )

    $batchNumber = 0
    foreach ($batch in $batches) {
        if ([string]::IsNullOrWhiteSpace($batch)) {
            continue
        }

        $batchNumber++
        $command = $Connection.CreateCommand()
        try {
            $command.CommandTimeout = 300
            $command.CommandText = $batch
            $reader = $command.ExecuteReader()
            try {
                $resultSetNumber = 0
                $hasResult = $true
                while ($hasResult) {
                    if ($reader.FieldCount -gt 0) {
                        $resultSetNumber++
                        $columnNames = @(for ($columnIndex = 0; $columnIndex -lt $reader.FieldCount; $columnIndex++) {
                            $reader.GetName($columnIndex)
                        })
                        Write-TestLog "RESULT batch=$batchNumber set=$resultSetNumber columns=$($columnNames -join ', ')"

                        $rowCount = 0
                        $maxLoggedRows = 25
                        while ($reader.Read()) {
                            $rowCount++
                            if ($rowCount -le $maxLoggedRows) {
                                $cells = for ($columnIndex = 0; $columnIndex -lt $reader.FieldCount; $columnIndex++) {
                                    $cellValue = ConvertTo-TestLogValue -Value ($reader.GetValue($columnIndex))
                                    "$($columnNames[$columnIndex])=$cellValue"
                                }
                                Write-TestLog "RESULT row=$rowCount $($cells -join '; ')"
                            }
                        }
                        if ($rowCount -gt $maxLoggedRows) {
                            Write-TestLog "RESULT omitted=$($rowCount - $maxLoggedRows) additional row(s)"
                        }
                        Write-TestLog "RESULT rows=$rowCount"
                    }
                    $hasResult = $reader.NextResult()
                }
            } finally {
                $reader.Dispose()
            }
        } catch {
            throw "Loi tai $([System.IO.Path]::GetFileName($Path)), batch $batchNumber`: $($_.Exception.Message)"
        } finally {
            $command.Dispose()
        }
    }
    Write-TestLog "Hoan tat: $([System.IO.Path]::GetFileName($Path))"
}

$connection = $null
$exitCode = 0
try {
    Write-TestLog "TV4 - Bat dau bo kiem thu payroll."
    $connection = Open-TestConnection

    Invoke-SqlFile $connection (Join-Path $repoRoot "database\tests\TV4_Payroll_E2E.sql")
    if (!$SkipBenchmark) {
        Invoke-SqlFile $connection (Join-Path $repoRoot "database\tests\TV4_Payroll_Benchmark.sql")
        Write-TestLog "PASS - E2E va benchmark payroll da chay thanh cong."
    } else {
        Write-TestLog "SKIP - Benchmark khong duoc chay theo tham so -SkipBenchmark."
        Write-TestLog "PASS - E2E payroll da chay thanh cong; benchmark da duoc bo qua."
    }
    Write-TestLog "Luu y: concurrency khong nam trong runner nay; chay hai script SessionA/SessionB rieng."
} catch {
    $exitCode = 1
    Write-TestLog "FAIL - $($_.Exception.Message)"
} finally {
    if ($null -ne $connection) {
        $connection.Dispose()
    }
    $logLines | Set-Content -LiteralPath $logPath -Encoding UTF8
    Write-Host "Log: $logPath"
}

exit $exitCode
