$ErrorActionPreference='Stop'
$root=Split-Path(Split-Path $PSScriptRoot)
$qaDatabase='PRJ_Fix_QA_20261013_age_'+[Guid]::NewGuid().ToString('N').Substring(0,8)
$qaOut=Join-Path $PSScriptRoot 'age';New-Item -ItemType Directory -Path $qaOut -Force|Out-Null
$conn=[Data.SqlClient.SqlConnection]::new('Server=tcp:localhost,1433;Database=master;Integrated Security=True;TrustServerCertificate=True');$conn.Open();$cmd=$conn.CreateCommand();$created=$false
$conn.add_InfoMessage({param($sender,$eventArgs)Write-Output $eventArgs.Message})
try{
 $cmd.CommandText="CREATE DATABASE [$qaDatabase]";$cmd.ExecuteNonQuery()|Out-Null;$created=$true
 foreach($path in @('database/01_Module_NhanSu_TV1.sql','database/02_Module_ChamCong_TV2.sql','database/03_phucap_khautru_TV3.sql','database/04_Module_TinhLuong_TV4.sql','database/05_Security_Payroll_TV5.sql','database/test_module_nhansu_TV1.sql','database/tests/TV1_Age_Boundaries.sql')){
  $source=Get-Content "$root/$path" -Raw -Encoding UTF8;$sql=$source.Replace('QuanLyNhanSuTienLuong',$qaDatabase);$name=[IO.Path]::GetFileName($path)
  [IO.File]::WriteAllText("$qaOut/$name",$sql,[Text.UTF8Encoding]::new($false))
  Write-Output "Source SHA256=$((Get-FileHash -LiteralPath "$root/$path").Hash) $path"
  $messages=[Collections.Generic.List[string]]::new();$handler=[Data.SqlClient.SqlInfoMessageEventHandler]{param($sender,$eventArgs)$messages.Add($eventArgs.Message)};$conn.add_InfoMessage($handler)
  try{foreach($batch in [regex]::Split($sql,'(?im)^\s*GO\s*;?\s*(?:--[^\r\n]*)?$')){if([string]::IsNullOrWhiteSpace($batch)){continue};$cmd.CommandText=$batch;$cmd.CommandTimeout=120;$cmd.ExecuteNonQuery()|Out-Null}}finally{$conn.remove_InfoMessage($handler);$messages|Set-Content "$qaOut/$name.messages.log" -Encoding UTF8}
  Write-Output "PASS $name"
 }
 Write-Output "PASS fresh create-only schema age rule on $qaDatabase; original/previous QA constraints not migrated."
}finally{
 if($created){if($qaDatabase -notmatch '^PRJ_Fix_QA_20261013_age_[a-f0-9]{8}$'){throw 'Unexpected cleanup target'};$cmd.CommandText="USE master; ALTER DATABASE [$qaDatabase] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$qaDatabase]";$cmd.ExecuteNonQuery()|Out-Null;Write-Output "PASS owned age database removed: $qaDatabase"}
 $conn.Dispose()
}
