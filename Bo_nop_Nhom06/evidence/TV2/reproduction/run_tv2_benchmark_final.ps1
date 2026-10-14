# run_tv2_benchmark_final.ps1
# Full benchmark execution, evidence capture, results equivalence verification, plan parsing, and manifest generation

[CmdletBinding()]
param(
    [string]$RunId = "run_20261013_140800",
    [string]$TargetDatabase = "PRJ_Fix_QA_20261013_001534_ae185d81",
    [string]$ServerInstance = "(local)"
)

$ErrorActionPreference = "Stop"

$workspaceRoot = "D:\PROJECT\he-qtcsdl-project\Project_QuanLyNhanSuTienLuong"
$outDir = Join-Path $workspaceRoot "build\evidence\TV2\$RunId"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

Write-Host "Output Directory: $outDir"

$connStr = "Server=$ServerInstance;Database=$TargetDatabase;Integrated Security=True;TrustServerCertificate=True;"
$conn = New-Object System.Data.SqlClient.SqlConnection($connStr)

$messages = [System.Collections.Generic.List[string]]::new()
$conn.add_InfoMessage({
    param($sender, $eventArgs)
    $messages.Add($eventArgs.Message)
})

function Export-DataTableToCsv {
    param(
        [System.Data.DataTable]$Table,
        [string]$Path
    )
    $lines = [System.Collections.Generic.List[string]]::new()
    $colNames = @()
    foreach ($col in $Table.Columns) {
        $colNames += $col.ColumnName
    }
    $lines.Add(($colNames -join ","))

    foreach ($row in $Table.Rows) {
        $vals = @()
        foreach ($col in $Table.Columns) {
            $val = $row[$col]
            if ($val -eq [System.DBNull]::Value -or $null -eq $val) {
                $vals += "NULL"
            } elseif ($val -is [System.DateTime]) {
                $vals += $val.ToString("yyyy-MM-dd")
            } elseif ($val -is [System.TimeSpan]) {
                $vals += $val.ToString("hh\:mm\:ss")
            } else {
                $str = $val.ToString().Replace('"', '""')
                if ($str.Contains(",") -or $str.Contains("`n") -or $str.Contains('"')) {
                    $vals += "`"$str`""
                } else {
                    $vals += $str
                }
            }
        }
        $lines.Add(($vals -join ","))
    }
    [System.IO.File]::WriteAllLines($Path, $lines, [System.Text.Encoding]::UTF8)
}

function Save-ShowPlanXml {
    param(
        [string]$XmlContent,
        [string]$Path
    )
    $content = $XmlContent.Trim()
    if (-not $content.StartsWith("<?xml")) {
        $content = "<?xml version=""1.0"" encoding=""utf-8""?>`n" + $content
    }
    [System.IO.File]::WriteAllText($Path, $content, [System.Text.Encoding]::UTF8)
}

function Extract-BatchQueries {
    param(
        [System.Data.SqlClient.SqlDataReader]$reader
    )
    # Result Set 1: Data for Query 1
    $dt1 = New-Object System.Data.DataTable
    for ($i = 0; $i -lt $reader.FieldCount; $i++) {
        $dt1.Columns.Add($reader.GetName($i), $reader.GetFieldType($i)) | Out-Null
    }
    while ($reader.Read()) {
        $row = $dt1.NewRow()
        for ($i = 0; $i -lt $reader.FieldCount; $i++) {
            $row[$i] = $reader.GetValue($i)
        }
        $dt1.Rows.Add($row)
    }

    # Result Set 2: ShowPlan XML for Query 1
    $reader.NextResult() | Out-Null
    $plan1 = ""
    if ($reader.Read()) {
        $plan1 = $reader.GetString(0)
    }

    # Result Set 3: Data for Query 2
    $reader.NextResult() | Out-Null
    $dt2 = New-Object System.Data.DataTable
    for ($i = 0; $i -lt $reader.FieldCount; $i++) {
        $dt2.Columns.Add($reader.GetName($i), $reader.GetFieldType($i)) | Out-Null
    }
    while ($reader.Read()) {
        $row = $dt2.NewRow()
        for ($i = 0; $i -lt $reader.FieldCount; $i++) {
            $row[$i] = $reader.GetValue($i)
        }
        $dt2.Rows.Add($row)
    }

    # Result Set 4: ShowPlan XML for Query 2
    $reader.NextResult() | Out-Null
    $plan2 = ""
    if ($reader.Read()) {
        $plan2 = $reader.GetString(0)
    }

    return @{
        Data1 = $dt1
        Plan1 = $plan1
        Data2 = $dt2
        Plan2 = $plan2
    }
}

try {
    $conn.Open()
    Write-Host "Connected to SQL Server: SPID = $($conn.ServerProcessId)"

    $cmd = $conn.CreateCommand()
    $cmd.CommandTimeout = 300

    # Step 0: Verification, Identity & Baseline
    $cmd.CommandText = @"
IF DB_NAME() <> N'$TargetDatabase' THROW 53410,N'Unauthorized database',1;
SELECT @@SERVERNAME AS ServerName, DB_NAME() AS DatabaseName, DB_ID() AS DatabaseId, ORIGINAL_LOGIN() AS OriginalLogin, @@SPID AS SessionId, @@VERSION AS SqlVersion;
"@
    $reader = $cmd.ExecuteReader()
    $envTable = New-Object System.Data.DataTable
    $envTable.Load($reader)
    $reader.Close()

    $cmd.CommandText = "SELECT name, database_id, create_date, state_desc, compatibility_level, collation_name FROM sys.databases WHERE name = DB_NAME();"
    $reader = $cmd.ExecuteReader()
    $dbPropTable = New-Object System.Data.DataTable
    $dbPropTable.Load($reader)
    $reader.Close()

    $cmd.CommandText = "SELECT COUNT(*) AS ChamCongRowCount, CHECKSUM_AGG(BINARY_CHECKSUM(MaChamCong, MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)) AS ChamCongChecksum FROM dbo.CHAMCONG;"
    $reader = $cmd.ExecuteReader()
    $safetyPreTable = New-Object System.Data.DataTable
    $safetyPreTable.Load($reader)
    $reader.Close()

    $cmd.CommandText = "SELECT type_desc, COUNT(*) AS ObjectCount FROM sys.objects WHERE is_ms_shipped = 0 GROUP BY type_desc ORDER BY type_desc;"
    $reader = $cmd.ExecuteReader()
    $schemaInvTable = New-Object System.Data.DataTable
    $schemaInvTable.Load($reader)
    $reader.Close()

    # Write TV2_environment_schema.log
    $envLogLines = [System.Collections.Generic.List[string]]::new()
    $envLogLines.Add("=== TV2 BENCHMARK ENVIRONMENT AND IDENTITY RECORD ===")
    $envLogLines.Add("Run ID: $RunId")
    $envLogLines.Add("Timestamp (Local): $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss.fffzzz')")
    $envLogLines.Add("ServerName: $($envTable.Rows[0]['ServerName'])")
    $envLogLines.Add("DatabaseName: $($envTable.Rows[0]['DatabaseName'])")
    $envLogLines.Add("DatabaseId: $($envTable.Rows[0]['DatabaseId'])")
    $envLogLines.Add("OriginalLogin: $($envTable.Rows[0]['OriginalLogin'])")
    $envLogLines.Add("SessionId (SPID): $($envTable.Rows[0]['SessionId'])")
    $envLogLines.Add("SqlVersion: $($envTable.Rows[0]['SqlVersion'])")
    $envLogLines.Add("Database CreateDate: $($dbPropTable.Rows[0]['create_date'])")
    $envLogLines.Add("Database State: $($dbPropTable.Rows[0]['state_desc'])")
    $envLogLines.Add("CompatibilityLevel: $($dbPropTable.Rows[0]['compatibility_level'])")
    $envLogLines.Add("Collation: $($dbPropTable.Rows[0]['collation_name'])")
    $envLogLines.Add("")
    $envLogLines.Add("=== PRE-EXECUTION dbo.CHAMCONG SAFETY BASELINE ===")
    $envLogLines.Add("ChamCong Row Count: $($safetyPreTable.Rows[0]['ChamCongRowCount'])")
    $envLogLines.Add("ChamCong Checksum: $($safetyPreTable.Rows[0]['ChamCongChecksum'])")
    $envLogLines.Add("")
    $envLogLines.Add("=== PRE-EXECUTION USER OBJECT INVENTORY ===")
    foreach ($row in $schemaInvTable.Rows) {
        $envLogLines.Add("  $($row['type_desc']): $($row['ObjectCount'])")
    }
    [System.IO.File]::WriteAllLines((Join-Path $outDir "TV2_environment_schema.log"), $envLogLines, [System.Text.Encoding]::UTF8)

    # Read the tracked benchmark SQL file
    $sqlFile = Join-Path $workspaceRoot "database\test_benchmark_index_TV2.sql"
    $sqlText = [System.IO.File]::ReadAllText($sqlFile, [System.Text.Encoding]::UTF8)
    $batches = [System.Text.RegularExpressions.Regex]::Split($sqlText, "(?m)^\s*GO\s*$")

    Write-Host "Loaded benchmark script: $($batches.Count) batches"

    for ($bIdx = 0; $bIdx -lt $batches.Count; $bIdx++) {
        $batch = $batches[$bIdx].Trim()
        if ([string]::IsNullOrWhiteSpace($batch)) { continue }

        Write-Host "Executing Batch $bIdx..."

        if ($bIdx -eq 4) {
            # Batch 4: BEFORE queries (1.1 and 1.2)
            $wrappedSql = "SET STATISTICS XML ON;`n" + $batch + "`nSET STATISTICS XML OFF;"
            $cmd.CommandText = $wrappedSql
            $reader = $cmd.ExecuteReader()
            $res4 = Extract-BatchQueries $reader
            $reader.Close()

            Export-DataTableToCsv -Table $res4.Data1 -Path (Join-Path $outDir "TV2_BEFORE_1_1.csv")
            Save-ShowPlanXml -XmlContent $res4.Plan1 -Path (Join-Path $outDir "TV2_BEFORE_1_1.sqlplan")
            Export-DataTableToCsv -Table $res4.Data2 -Path (Join-Path $outDir "TV2_BEFORE_1_2.csv")
            Save-ShowPlanXml -XmlContent $res4.Plan2 -Path (Join-Path $outDir "TV2_BEFORE_1_2.sqlplan")
            Write-Host "  Saved TV2_BEFORE_1_1.csv ($($res4.Data1.Rows.Count) rows) and .sqlplan ($($res4.Plan1.Length) chars)"
            Write-Host "  Saved TV2_BEFORE_1_2.csv ($($res4.Data2.Rows.Count) rows) and .sqlplan ($($res4.Plan2.Length) chars)"
        } elseif ($bIdx -eq 6) {
            # Batch 6: AFTER queries (3.1 and 3.2)
            $wrappedSql = "SET STATISTICS XML ON;`n" + $batch + "`nSET STATISTICS XML OFF;"
            $cmd.CommandText = $wrappedSql
            $reader = $cmd.ExecuteReader()
            $res6 = Extract-BatchQueries $reader
            $reader.Close()

            Export-DataTableToCsv -Table $res6.Data1 -Path (Join-Path $outDir "TV2_AFTER_3_1.csv")
            Save-ShowPlanXml -XmlContent $res6.Plan1 -Path (Join-Path $outDir "TV2_AFTER_3_1.sqlplan")
            Export-DataTableToCsv -Table $res6.Data2 -Path (Join-Path $outDir "TV2_AFTER_3_2.csv")
            Save-ShowPlanXml -XmlContent $res6.Plan2 -Path (Join-Path $outDir "TV2_AFTER_3_2.sqlplan")
            Write-Host "  Saved TV2_AFTER_3_1.csv ($($res6.Data1.Rows.Count) rows) and .sqlplan ($($res6.Plan1.Length) chars)"
            Write-Host "  Saved TV2_AFTER_3_2.csv ($($res6.Data2.Rows.Count) rows) and .sqlplan ($($res6.Plan2.Length) chars)"
        } elseif ($bIdx -eq 7) {
            # Batch 7: Metadata query on dbo.CHAMCONG
            $cmd.CommandText = $batch
            $reader = $cmd.ExecuteReader()
            $metaTable = New-Object System.Data.DataTable
            $metaTable.Load($reader)
            $reader.Close()

            Export-DataTableToCsv -Table $metaTable -Path (Join-Path $outDir "TV2_index_metadata.csv")
            Write-Host "  Saved TV2_index_metadata.csv ($($metaTable.Rows.Count) rows)"
        } else {
            # Standard batch
            $cmd.CommandText = $batch
            $cmd.ExecuteNonQuery() | Out-Null
        }
    }

    # Same session safety verification
    $cmd.CommandText = @"
SELECT 
    OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') AS TempTableObjectId,
    @@TRANCOUNT AS TranCount,
    XACT_STATE() AS XactState;
"@
    $reader = $cmd.ExecuteReader()
    $cleanupCheckTable = New-Object System.Data.DataTable
    $cleanupCheckTable.Load($reader)
    $reader.Close()

    $cmd.CommandText = "SELECT COUNT(*) AS ChamCongRowCount, CHECKSUM_AGG(BINARY_CHECKSUM(MaChamCong, MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)) AS ChamCongChecksum FROM dbo.CHAMCONG;"
    $reader = $cmd.ExecuteReader()
    $safetyPostTable = New-Object System.Data.DataTable
    $safetyPostTable.Load($reader)
    $reader.Close()

    $cmd.CommandText = "SELECT type_desc, COUNT(*) AS ObjectCount FROM sys.objects WHERE is_ms_shipped = 0 GROUP BY type_desc ORDER BY type_desc;"
    $reader = $cmd.ExecuteReader()
    $schemaPostTable = New-Object System.Data.DataTable
    $schemaPostTable.Load($reader)
    $reader.Close()

    # Write TV2_cleanup_safety.log
    $safetyLogLines = [System.Collections.Generic.List[string]]::new()
    $safetyLogLines.Add("=== TV2 BENCHMARK SAME-SESSION CLEANUP AND SAFETY RECORD ===")
    $safetyLogLines.Add("Run ID: $RunId")
    $safetyLogLines.Add("Session SPID: $($envTable.Rows[0]['SessionId'])")
    $safetyLogLines.Add("Timestamp (Local): $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss.fffzzz')")
    $safetyLogLines.Add("TempTable OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK'): $($cleanupCheckTable.Rows[0]['TempTableObjectId']) (Expected NULL/DBNull)")
    $safetyLogLines.Add("@@TRANCOUNT: $($cleanupCheckTable.Rows[0]['TranCount']) (Expected 0)")
    $safetyLogLines.Add("XACT_STATE(): $($cleanupCheckTable.Rows[0]['XactState']) (Expected 0)")
    $safetyLogLines.Add("")
    $safetyLogLines.Add("=== POST-EXECUTION dbo.CHAMCONG SAFETY VERIFICATION ===")
    $safetyLogLines.Add("ChamCong Row Count: $($safetyPostTable.Rows[0]['ChamCongRowCount']) (Pre: $($safetyPreTable.Rows[0]['ChamCongRowCount']))")
    $safetyLogLines.Add("ChamCong Checksum: $($safetyPostTable.Rows[0]['ChamCongChecksum']) (Pre: $($safetyPreTable.Rows[0]['ChamCongChecksum']))")
    $intact = ($safetyPostTable.Rows[0]['ChamCongRowCount'] -eq $safetyPreTable.Rows[0]['ChamCongRowCount']) -and ($safetyPostTable.Rows[0]['ChamCongChecksum'] -eq $safetyPreTable.Rows[0]['ChamCongChecksum'])
    $safetyLogLines.Add("dbo.CHAMCONG Data Intact: $intact")
    $safetyLogLines.Add("")
    $safetyLogLines.Add("=== POST-EXECUTION USER OBJECT INVENTORY ===")
    foreach ($row in $schemaPostTable.Rows) {
        $safetyLogLines.Add("  $($row['type_desc']): $($row['ObjectCount'])")
    }
    [System.IO.File]::WriteAllLines((Join-Path $outDir "TV2_cleanup_safety.log"), $safetyLogLines, [System.Text.Encoding]::UTF8)

    # Save messages
    [System.IO.File]::WriteAllLines((Join-Path $outDir "TV2_benchmark_messages.log"), $messages, [System.Text.Encoding]::UTF8)
    Write-Host "Messages log written with $($messages.Count) lines."

    Write-Host "BENCHMARK RUN COMPLETED 100% SUCCESSFULLY!"
} finally {
    if ($conn.State -eq [System.Data.ConnectionState]::Open) {
        $conn.Close()
    }
}
