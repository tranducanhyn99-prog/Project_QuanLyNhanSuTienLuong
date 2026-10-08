param(
    [ValidatePattern('^PRJ_Fix_QA_[A-Za-z0-9_]+$')][string]$Database = 'PRJ_Fix_QA_20261007_01',
    [switch]$VerifyOnly
)
$ErrorActionPreference = 'Stop'
$repoRoot = $PSScriptRoot
# Public classroom fixtures, shared with docs; never use the DBA's password here.
$manifest = ConvertFrom-Json -InputObject (Get-Content (Join-Path $repoRoot 'database\demo_accounts.json') -Raw -Encoding UTF8)
$accounts = @(foreach ($entry in $manifest) {
    @{User=$entry.User; Role=$entry.Role; Password=$entry.Password; Email=$entry.Email; Employee=[DBNull]::Value}
})
if ($accounts.Count -ne 5) { throw 'Expected five demo accounts.' }
if (!(Test-Path (Join-Path $repoRoot 'build\verification\classes\com\test\TestRealAuth.class'))) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot 'run_verification.ps1')
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
if (-not $VerifyOnly) {
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
foreach ($account in $accounts) {
    $salt = New-Object byte[] 16; $rng.GetBytes($salt)
    $derive = [Security.Cryptography.Rfc2898DeriveBytes]::new($account.Password,$salt,600000,[Security.Cryptography.HashAlgorithmName]::SHA256)
    try { $account.Hash = 'pbkdf2-sha256$600000$' + [Convert]::ToBase64String($salt) + '$' + [Convert]::ToBase64String($derive.GetBytes(32)) }
    finally { $derive.Dispose() }
}
$rng.Dispose()
$connection = New-Object System.Data.SqlClient.SqlConnection "Server=localhost;Database=$database;Integrated Security=True;TrustServerCertificate=True"
$transaction = $null
$committed = $false
try {
    $connection.Open()
    $transaction = $connection.BeginTransaction()
    $preflight = $connection.CreateCommand(); $preflight.Transaction=$transaction
    $preflight.CommandText = @'
SET ANSI_NULLS ON; SET QUOTED_IDENTIFIER ON; SET ANSI_PADDING ON; SET ANSI_WARNINGS ON;
SET ARITHABORT ON; SET CONCAT_NULL_YIELDS_NULL ON; SET NUMERIC_ROUNDABORT OFF;
IF DB_NAME() NOT LIKE N'PRJ[_]Fix[_]QA[_]%' THROW 53700,N'Unexpected database.',1;
IF IS_SRVROLEMEMBER('sysadmin')<>1 THROW 53701,N'Windows DBA required.',1;
IF CONVERT(INT,SERVERPROPERTY('IsIntegratedSecurityOnly'))<>0 THROW 53702,N'SQL authentication must be enabled.',1;
IF EXISTS(SELECT 1 FROM sys.server_principals WHERE name IN('gui_admin','gui_hr','gui_payroll','gui_a','gui_b'))
 OR EXISTS(SELECT 1 FROM sys.database_principals WHERE name IN('gui_admin','gui_hr','gui_payroll','gui_a','gui_b'))
 OR EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE TenDangNhap IN('gui_admin','gui_hr','gui_payroll','gui_a','gui_b'))
 THROW 53703,N'An intended account already exists; refusing overwrite.',1;
IF EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE Email IN('gui-a@example.invalid','gui-b@example.invalid')
 OR CCCD IN('900000000101','900000000102') OR SoDienThoai IN('0900000101','0900000102'))
 THROW 53704,N'An intended employee fixture already exists; refusing overwrite.',1;
DECLARE @p INT=(SELECT MIN(MaPB) FROM dbo.PHONGBAN WHERE TrangThai=N'HOAT_DONG'),
 @c INT=(SELECT MIN(MaCV) FROM dbo.CHUCVU);
IF @p IS NULL OR @c IS NULL THROW 53705,N'Employee dictionaries missing.',1;
INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,DiaChi,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV,TrangThai)
VALUES(N'TEST Giao dien A','1995-01-15',N'Nam','900000000101',N'GUI QA','0900000101','gui-a@example.invalid','2026-08-01',26000000,@p,@c,N'DANG_LAM_VIEC'),
(N'TEST Giao dien B','1995-01-15',N'Nam','900000000102',N'GUI QA','0900000102','gui-b@example.invalid','2026-08-01',26000000,@p,@c,N'DANG_LAM_VIEC');
'@
    try { $preflight.ExecuteNonQuery() | Out-Null } finally { $preflight.Dispose() }
    foreach ($account in $accounts) {
        if ($account.Email) {
            $lookup=$connection.CreateCommand(); $lookup.Transaction=$transaction
            $lookup.CommandText='SELECT MaNV FROM dbo.NHANVIEN WHERE Email=@Email'
            $lookup.Parameters.Add('@Email',[Data.SqlDbType]::VarChar,100).Value=$account.Email
            try { $account.Employee=[int]$lookup.ExecuteScalar() } finally { $lookup.Dispose() }
        }
        $command=$connection.CreateCommand(); $command.Transaction=$transaction
        $command.CommandType=[Data.CommandType]::StoredProcedure; $command.CommandText='dbo.sp_DBAProvisionIdentity'
        $command.Parameters.Add('@Login',[Data.SqlDbType]::NVarChar,-1).Value=$account.User
        $command.Parameters.Add('@Password',[Data.SqlDbType]::NVarChar,-1).Value=$account.Password
        $command.Parameters.Add('@Hash',[Data.SqlDbType]::VarChar,255).Value=$account.Hash
        $command.Parameters.Add('@VaiTro',[Data.SqlDbType]::VarChar,30).Value=$account.Role
        $command.Parameters.Add('@MaNV',[Data.SqlDbType]::Int).Value=$account.Employee
        try { $command.ExecuteNonQuery() | Out-Null } finally { $command.Dispose() }
    }
    $transaction.Commit(); $committed=$true
    Write-Output 'PASS provisioned 5 SQL logins/users/application profiles, 4 roles, 2 employee records.'
} catch {
    if (-not $committed -and $transaction) {
        try { $transaction.Rollback() } catch { }
    }
    throw
} finally {
    if ($transaction) { $transaction.Dispose() }
    $connection.Dispose()
}
}
$previousUrl=$env:DB_URL; $previousUser=$env:TEST_SQL_USER; $previousPassword=$env:TEST_SQL_PASSWORD
try {
    $env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=$database;encrypt=true;trustServerCertificate=true"
    $classpath="$(Join-Path $repoRoot 'build\verification\classes');$(Join-Path $repoRoot 'src\resources');$(Join-Path $repoRoot 'lib\mssql-jdbc-12.6.1.jre11.jar')"
    foreach ($account in $accounts) {
        $env:TEST_SQL_USER=$account.User; $env:TEST_SQL_PASSWORD=$account.Password
        $output=& java -cp $classpath com.test.TestRealAuth 2>&1
        if ($LASTEXITCODE -ne 0) { $output | ForEach-Object { Write-Output $_.ToString() }; throw "Live login verification failed: $($account.User)" }
        Write-Output "PASS real application login and wrong-password rejection: $($account.User) ($($account.Role))"
    }
} finally {
    $env:DB_URL=$previousUrl; $env:TEST_SQL_USER=$previousUser; $env:TEST_SQL_PASSWORD=$previousPassword
    foreach ($account in $accounts) { $account.Password=$null; $account.Hash=$null }
}
