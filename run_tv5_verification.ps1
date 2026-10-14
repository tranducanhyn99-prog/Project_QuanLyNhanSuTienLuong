param([string]$ServerInstance='tcp:localhost,1433',[switch]$KeepDatabase)
# Owns its QA database; leaves original databases/logins/cache untouched.
$ErrorActionPreference='Stop'
$runId=(Get-Date -Format 'yyyyMMdd_HHmmss')+'_'+[Guid]::NewGuid().ToString('N').Substring(0,8)
$database='PRJ_Fix_QA_'+$runId
$output=Join-Path $PSScriptRoot ('build\tv5-verification\'+$runId)
New-Item -ItemType Directory -Path $output -Force | Out-Null
$encoding=New-Object System.Text.UTF8Encoding($false)
$script:messageLog=$null
$created=$false
$connection=$null
$master=$null
$exitCode=0
function New-QAConnection([string]$Catalog) {
    $builder=New-Object System.Data.SqlClient.SqlConnectionStringBuilder
    $builder['Data Source']=$ServerInstance
    $builder['Initial Catalog']=$Catalog
    $builder['Integrated Security']=$true
    $builder['Encrypt']=$true
    $builder['TrustServerCertificate']=$true
    $builder['Application Name']='TV5 isolated verification'
    $conn=New-Object System.Data.SqlClient.SqlConnection($builder.ConnectionString)
    $conn.add_InfoMessage({param($sender,$eventArgs)
        if ($script:messageLog) { [IO.File]::AppendAllText($script:messageLog,$eventArgs.Message+[Environment]::NewLine,$encoding) }
    })
    $conn.Open()
    return $conn
}
function Invoke-Text($Conn,[string]$Sql) {
    $command=$Conn.CreateCommand()
    try { $command.CommandTimeout=120; $command.CommandText=$Sql; $command.ExecuteNonQuery() | Out-Null }
    finally { $command.Dispose() }
}
function Cell-Text($Value) {
    if ($Value -is [DBNull]) { return 'NULL' }
    if ($Value -is [IFormattable]) { $s=$Value.ToString($null,[Globalization.CultureInfo]::InvariantCulture) }
    else { $s=[string]$Value }
    return $s.Replace('\','\\').Replace("`t",'\t').Replace("`r",'\r').Replace("`n",'\n')
}
function Invoke-File([string]$RelativePath,[string]$Label) {
    $sourcePath=Join-Path $PSScriptRoot $RelativePath
    $source=Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8
    $source=$source.Replace('QuanLyNhanSuTienLuong',$database)
    [IO.File]::WriteAllText((Join-Path $output ($Label+'.sql')),$source,$encoding)
    $script:messageLog=Join-Path $output ($Label+'.messages.log')
    [IO.File]::WriteAllText($script:messageLog,'',$encoding)
    $resultNumber=0
    $planNumber=0
    foreach($batch in [regex]::Split($source,'(?im)^\s*GO\s*;?\s*(?:--[^\r\n]*)?$')) {
        if ([string]::IsNullOrWhiteSpace($batch)) { continue }
        $command=$connection.CreateCommand()
        try {
            $command.CommandText=$batch
            $command.CommandTimeout=300
            $reader=$command.ExecuteReader()
            try {
                do {
                    if ($reader.FieldCount -le 0) { continue }
                    if ($reader.FieldCount -eq 1 -and $reader.GetName(0) -match 'Showplan') {
                        while($reader.Read()) {
                            $xml=[string]$reader.GetValue(0)
                            if ($xml -notmatch '<ShowPlanXML') { throw 'Invalid XML plan' }
                            $planNumber++
                            [IO.File]::WriteAllText((Join-Path $output ($Label+'_ActualPlan_'+$planNumber+'.sqlplan')),$xml,$encoding)
                        }
                    } else {
                        $resultNumber++
                        $path=Join-Path $output ($Label+'_Results_'+$resultNumber+'.tsv')
                        $writer=New-Object IO.StreamWriter($path,$false,$encoding)
                        try {
                            $headers=@(for($i=0;$i -lt $reader.FieldCount;$i++) { $reader.GetName($i) })
                            $writer.WriteLine($headers -join "`t")
                            $rows=0
                            while($reader.Read()) {
                                $cells=@(for($i=0;$i -lt $reader.FieldCount;$i++) { Cell-Text ($reader.GetValue($i)) })
                                $writer.WriteLine($cells -join "`t")
                                $rows++
                            }
                            [IO.File]::AppendAllText($script:messageLog,"RESULT $resultNumber rows=$rows file=$([IO.Path]::GetFileName($path))"+[Environment]::NewLine,$encoding)
                        } finally { $writer.Dispose() }
                    }
                } while($reader.NextResult())
            } finally { $reader.Dispose() }
        } finally { $command.Dispose() }
    }
    if ($Label -eq 'TV5_Benchmark' -and $planNumber -ne 2) { throw "Expected two actual plans; got $planNumber" }
    Write-Host "PASS $Label ($resultNumber complete result sets, $planNumber actual plans)"
}
try {
    $master=New-QAConnection 'master'
    Invoke-Text $master "CREATE DATABASE [$database]"
    $created=$true
    $connection=New-QAConnection $database
    $sources=@('database/01_Module_NhanSu_TV1.sql','database/02_Module_ChamCong_TV2.sql',
        'database/03_phucap_khautru_TV3.sql','database/04_Module_TinhLuong_TV4.sql',
        'database/05_Security_Payroll_TV5.sql','database/tests/TV5_Money_Boundaries.sql',
        'database/test_benchmark_index_TV5.sql','run_tv5_verification.ps1')
    $gitHead='unavailable (standalone package)'
    if ((Test-Path (Join-Path $PSScriptRoot '.git')) -and (Get-Command git -ErrorAction SilentlyContinue)) {
        $gitHead=& git -C $PSScriptRoot rev-parse HEAD
    }
    $provenance=@("RunId=$runId","Server=$ServerInstance","Database=$database","GitHEAD=$gitHead",
        'Source hashes identify this working tree, including uncommitted TV5 changes:')
    foreach($path in $sources) {
        $hash=(Get-FileHash -LiteralPath (Join-Path $PSScriptRoot $path) -Algorithm SHA256).Hash
        $provenance+= "$hash $path"
    }
    $provenance | Set-Content -LiteralPath (Join-Path $output 'source-provenance.txt') -Encoding UTF8
    foreach($path in $sources[0..4]) { Invoke-File $path ([IO.Path]::GetFileNameWithoutExtension($path)) }
    Invoke-File 'database/05_Security_Payroll_TV5.sql' 'TV5_Module_Rerun'
    Invoke-File 'database/tests/TV5_Money_Boundaries.sql' 'TV5_Money'
    Invoke-File 'database/test_benchmark_index_TV5.sql' 'TV5_Benchmark'
    $before=@(Get-Content -LiteralPath (Join-Path $output 'TV5_Benchmark_Results_2.tsv') -Encoding UTF8 | Sort-Object)
    $after=@(Get-Content -LiteralPath (Join-Path $output 'TV5_Benchmark_Results_3.tsv') -Encoding UTF8 | Sort-Object)
    if (($before -join "`n") -cne ($after -join "`n")) { throw 'Exported full before/after results differ' }
    $messages=Get-Content -LiteralPath (Join-Path $output 'TV5_Benchmark.messages.log') -Raw -Encoding UTF8
    $reads=@([regex]::Matches($messages,"Table '#BenchmarkNhanVien[^\r\n]*?logical reads (\d+)") | ForEach-Object {$_.Groups[1].Value})
    if ($reads.Count -ne 2) { throw "Expected two IO measurements; got $($reads.Count)" }
    $operators=@('Clustered Index Scan','Index Seek')
    for($i=1;$i -le 2;$i++) {
        [xml]$plan=Get-Content -LiteralPath (Join-Path $output ("TV5_Benchmark_ActualPlan_$i.sqlplan")) -Raw -Encoding UTF8
        $nodes=@($plan.SelectNodes('//*[local-name()="RelOp"]'))
        if ($nodes.Count -ne 1 -or $nodes[0].PhysicalOp -ne $operators[$i-1]) {
            throw "Unexpected operator in actual plan $i"
        }
        $runtime=$nodes[0].SelectSingleNode('.//*[local-name()="RunTimeCountersPerThread"]')
        if (!$runtime -or [int]$runtime.ActualRows -ne $before.Count-1) { throw "Actual row count mismatch in plan $i" }
    }
    $summary=@("PASS TV5-01 money boundaries and payroll overflow atomic rollback.",
        "PASS TV5-02 full bidirectional SQL EXCEPT and byte-equal sorted TSV exports.",
        "Fixture: 10000 temporary rows; predicate MaPB=2 AND MaCV=1; $($before.Count-1) rows per measured query.",
        "Logical reads: forced clustered scan=$($reads[0]); forced covering seek=$($reads[1]).",
        'Actual plans: Clustered Index Scan / Index Seek, same nonzero row count, no Key Lookup.',
        'Warm cache, MAXDOP 1, hints explicit. IO/TIME and actual XML from this same execution.',
        'Temp model of TV5 index, not proof of production workload speed; historical 142->15 remains historical.',
        'See full source/fixture SQL, complete TSV exports, messages, actual plans, source SHA-256 and cleanup log.')
    $summary | Set-Content -LiteralPath (Join-Path $output 'summary.txt') -Encoding UTF8
    Write-Host ($summary -join [Environment]::NewLine)
} catch {
    $exitCode=1
    $_.Exception.ToString() | Set-Content -LiteralPath (Join-Path $output 'failure.log') -Encoding UTF8
    Write-Host "FAIL $($_.Exception.Message)"
} finally {
    if ($connection) { $connection.Dispose() }
    $script:messageLog=$null
    if ($created -and !$KeepDatabase) {
        try {
            if ($database -notmatch '^PRJ_Fix_QA_[0-9]{8}_[0-9]{6}_[a-f0-9]{8}$') { throw 'Unsafe cleanup target' }
            Invoke-Text $master "ALTER DATABASE [$database] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$database]"
            $check=$master.CreateCommand()
            try {
                $check.CommandText="SELECT COUNT(*) FROM sys.databases WHERE name='$database'"
                if ($check.ExecuteScalar() -ne 0) { throw 'Database remains after cleanup' }
            } finally { $check.Dispose() }
            "PASS owned database removed: $database" | Set-Content -LiteralPath (Join-Path $output 'cleanup.log') -Encoding UTF8
            Write-Host "PASS owned database cleanup: $database"
        } catch { $exitCode=1; $_.Exception.ToString() | Set-Content -LiteralPath (Join-Path $output 'cleanup-failure.log') -Encoding UTF8 }
    } elseif ($created) {
        "Kept owned QA by explicit -KeepDatabase: $database; caller must clean up after follow-up QA." |
            Set-Content -LiteralPath (Join-Path $output 'database-kept.txt') -Encoding UTF8
        Write-Host "Kept owned QA by explicit -KeepDatabase: $database"
    }
    if($master) { $master.Dispose() }
    Write-Host "Evidence: $output"
}
exit $exitCode
