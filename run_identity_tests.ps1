# Explicit isolated fixture run. Requires a Windows SQL Server sysadmin on localhost.
# Passwords are random per run, passed only in-process, and cleared during cleanup.
$ErrorActionPreference = 'Stop'
$dbName = 'PRJ_Fix_QA_20261007_Identity'
$sqlcmd = 'C:/Program Files/Microsoft SQL Server/Client SDK/ODBC/180/Tools/Binn/SQLCMD.EXE'
$output = Join-Path $PSScriptRoot 'build/identity-agent'
New-Item -ItemType Directory -Force -Path $output | Out-Null
if (-not (Test-Path -LiteralPath $sqlcmd) -or -not (Get-Command javac -ErrorAction SilentlyContinue) -or -not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host 'SKIPPED: SQLCMD and JDK 11+ are required.'; exit 2
}
Push-Location $PSScriptRoot
$server = New-Object System.Data.SqlClient.SqlConnection 'Server=localhost;Database=master;Integrated Security=True;TrustServerCertificate=True'
function Query([System.Data.SqlClient.SqlConnection]$connection, [string]$sql) {
    $command = $connection.CreateCommand(); $command.CommandText = $sql
    try { $command.ExecuteNonQuery() | Out-Null } finally { $command.Dispose() }
}
function Scalar([System.Data.SqlClient.SqlConnection]$connection, [string]$sql) {
    $command = $connection.CreateCommand(); $command.CommandText = $sql
    try { $command.ExecuteScalar() } finally { $command.Dispose() }
}
$suffixes = @('admin','hr','payroll','a','b','legacy')
$createdLogins = @()
$created = $false
try {
    $server.Open()
    'Provision regressions for isolated identity QA run' | Set-Content -LiteralPath (Join-Path $output 'provision.log')
    if ([int](Scalar $server "SELECT COUNT(*) FROM sys.databases WHERE name='$dbName'") -ne 0) { throw 'QA database already exists; refusing overwrite' }
    foreach ($suffix in $suffixes) {
        if ([int](Scalar $server "SELECT COUNT(*) FROM sys.server_principals WHERE name='qaIdentity_20261007_$suffix'") -ne 0) { throw 'QA login already exists; refusing overwrite' }
    }
    Query $server "CREATE DATABASE [$dbName]"; $created = $true
    foreach ($file in @('01_Module_NhanSu_TV1.sql','02_Module_ChamCong_TV2.sql','03_phucap_khautru_TV3.sql','04_Module_TinhLuong_TV4.sql','05_Security_Payroll_TV5.sql')) {
        $copy = Join-Path $output $file
        (Get-Content -LiteralPath (Join-Path 'database' $file) -Raw).Replace('QuanLyNhanSuTienLuong',$dbName) | Set-Content -LiteralPath $copy -Encoding utf8
        & $sqlcmd -S localhost -E -C -I -b -f 65001 -i $copy -o (Join-Path $output ($file+'.log'))
        if ($LASTEXITCODE -ne 0) { Get-Content (Join-Path $output ($file+'.log')); throw "Schema failed: $file" }
    }
    $connection = New-Object System.Data.SqlClient.SqlConnection "Server=localhost;Database=$dbName;Integrated Security=True;TrustServerCertificate=True"
    $connection.Open()
    foreach ($invalidPassword in @([DBNull]::Value,('Qx9!' + ('x' * 125)))) {
        $invalid = $connection.CreateCommand(); $invalid.CommandType=[Data.CommandType]::StoredProcedure; $invalid.CommandText='dbo.sp_DBAProvisionIdentity'
        $invalid.Parameters.Add('@Login',[Data.SqlDbType]::NVarChar,128).Value='qaIdentity_20261007_invalid'
        $invalid.Parameters.Add('@Password',[Data.SqlDbType]::NVarChar,-1).Value=$invalidPassword
        $invalid.Parameters.Add('@Hash',[Data.SqlDbType]::VarChar,255).Value='pbkdf2-sha256$fixture'
        $invalid.Parameters.Add('@VaiTro',[Data.SqlDbType]::VarChar,30).Value='HR_Manager'
        try {
            $rejected = $false
            try { $invalid.ExecuteNonQuery() | Out-Null } catch {
                if ($_.Exception.InnerException.Number -ne 51031) { throw }
                $rejected = $true
            }
            if (-not $rejected) { throw 'Invalid provisioning unexpectedly accepted' }
            if ([int](Scalar $connection "SELECT COUNT(*) FROM dbo.TAIKHOAN WHERE SqlLogin='qaIdentity_20261007_invalid'") -ne 0 -or [int](Scalar $server "SELECT COUNT(*) FROM sys.server_principals WHERE name='qaIdentity_20261007_invalid'") -ne 0) { throw 'Failed provisioning left side effects' }
            'PASS invalid provisioning rejected without account/login side effects' | Tee-Object -FilePath (Join-Path $output 'provision.log') -Append
        } finally { $invalid.Dispose() }
    }
    Query $connection @'
INSERT dbo.PHONGBAN(TenPB) VALUES(N'Identity QA'); INSERT dbo.CHUCVU(TenCV) VALUES(N'Identity QA');
DECLARE @p INT=(SELECT MaPB FROM dbo.PHONGBAN WHERE TenPB=N'Identity QA'),@c INT=(SELECT MaCV FROM dbo.CHUCVU WHERE TenCV=N'Identity QA');
INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
VALUES(N'Identity A','1990-01-01',N'Nam','900000000001','0900000001','identity-a@example.invalid','2020-01-01',26000000,@p,@c),
(N'Identity B','1990-01-01',N'Nam','900000000002','0900000002','identity-b@example.invalid','2020-01-01',26000000,@p,@c);
INSERT dbo.BANGLUONG(Thang,Nam,NgayCongChuan) VALUES(9,2026,26);
INSERT dbo.CHITIETBANGLUONG(MaBangLuong,MaNV,LuongCoBan,NgayCongThucTe,TienCong,TongPhuCap,TongKhauTru,ThucNhan)
SELECT (SELECT MaBangLuong FROM dbo.BANGLUONG WHERE Thang=9 AND Nam=2026),MaNV,26000000,26,26000000,0,0,26000000 FROM dbo.NHANVIEN;
'@
    $roles = @('DB_Admin','HR_Manager','Payroll_Officer','Employee','Employee','HR_Manager')
    for ($i=0; $i -lt $suffixes.Count; $i++) {
        $suffix = $suffixes[$i]
        $password = 'Qa9!' + [guid]::NewGuid().ToString()
        [Environment]::SetEnvironmentVariable("QA_IDENTITY_PASSWORD_$suffix",$password,'Process')
        $salt = New-Object byte[] 16
        $rng = [Security.Cryptography.RandomNumberGenerator]::Create(); $rng.GetBytes($salt); $rng.Dispose()
        $derive = [Security.Cryptography.Rfc2898DeriveBytes]::new($password,$salt,600000,[Security.Cryptography.HashAlgorithmName]::SHA256)
        $hash = 'pbkdf2-sha256$600000$' + [Convert]::ToBase64String($salt) + '$' + [Convert]::ToBase64String($derive.GetBytes(32)); $derive.Dispose()
        $command = $connection.CreateCommand(); $command.CommandType = [Data.CommandType]::StoredProcedure; $command.CommandText='dbo.sp_DBAProvisionIdentity'
        $command.Parameters.Add('@Login',[Data.SqlDbType]::NVarChar,128).Value="qaIdentity_20261007_$suffix"
        $command.Parameters.Add('@Password',[Data.SqlDbType]::NVarChar,128).Value=$password
        $command.Parameters.Add('@Hash',[Data.SqlDbType]::VarChar,255).Value=$hash
        $command.Parameters.Add('@VaiTro',[Data.SqlDbType]::VarChar,30).Value=$roles[$i]
        $employee = [DBNull]::Value
        if ($suffix -eq 'a') { $employee=Scalar $connection "SELECT MaNV FROM dbo.NHANVIEN WHERE CCCD='900000000001'" }
        if ($suffix -eq 'b') { $employee=Scalar $connection "SELECT MaNV FROM dbo.NHANVIEN WHERE CCCD='900000000002'" }
        $command.Parameters.Add('@MaNV',[Data.SqlDbType]::Int).Value=$employee
        try { $command.ExecuteNonQuery() | Out-Null; $createdLogins += $suffix } finally { $command.Dispose() }
        if ($suffix -eq 'legacy') {
            $sha = [Security.Cryptography.SHA256]::Create()
            $legacyHash = [BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($password))).Replace('-','').ToLowerInvariant(); $sha.Dispose()
            Query $connection "UPDATE dbo.TAIKHOAN SET MatKhau='$legacyHash' WHERE SqlLogin='qaIdentity_20261007_legacy'"
        }
    }
    Query $server 'GRANT ALTER ANY LOGIN TO [qaIdentity_20261007_admin]'
    $connection.Close(); $connection.Dispose()
    $env:QA_IDENTITY_URL="jdbc:sqlserver://localhost:1433;databaseName=$dbName;encrypt=true;trustServerCertificate=true;"
    $classes = Join-Path $output 'classes'; New-Item -ItemType Directory -Force $classes | Out-Null
    $sources = Get-ChildItem src/main/java -Recurse -Filter '*.java' | ForEach-Object FullName
    & javac --release 11 -encoding UTF-8 -cp lib/mssql-jdbc-12.6.1.jre11.jar -d $classes @sources src/test/java/com/test/SqlIdentityIntegrationTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Compile failed' }
    'JDBC URL authentication override regressions' | Set-Content -LiteralPath (Join-Path $output 'config.log')
    foreach ($property in @(' user=forbidden','userName=forbidden','password=forbidden','integratedSecurity=true','authentication=ActiveDirectoryIntegrated',' accessToken=forbidden')) {
        & java "-Ddb.url=$($env:QA_IDENTITY_URL)$property" -cp "$classes;lib/mssql-jdbc-12.6.1.jre11.jar" com.test.SqlIdentityIntegrationTest --reject-config | Tee-Object -FilePath (Join-Path $output 'config.log') -Append
        if ($LASTEXITCODE -ne 0) { throw 'JDBC authentication override test failed' }
    }
    & java -cp "$classes;lib/mssql-jdbc-12.6.1.jre11.jar" com.test.SqlIdentityIntegrationTest | Tee-Object (Join-Path $output 'identity.log')
    if ($LASTEXITCODE -ne 0) { throw 'Identity test failed' }
} finally {
    if ($created) {
        Query $server "ALTER DATABASE [$dbName] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$dbName]"
        foreach ($suffix in $createdLogins) { Query $server "IF SUSER_ID('qaIdentity_20261007_$suffix') IS NOT NULL DROP LOGIN [qaIdentity_20261007_$suffix]" }
        if ([int](Scalar $server "SELECT COUNT(*) FROM sys.databases WHERE name='$dbName'") -ne 0) { throw 'QA database cleanup failed' }
        foreach ($suffix in $createdLogins) {
            if ([int](Scalar $server "SELECT COUNT(*) FROM sys.server_principals WHERE name='qaIdentity_20261007_$suffix'") -ne 0) { throw 'QA login cleanup failed' }
        }
        'PASS cleanup: isolated QA database and created SQL logins no longer exist' | Tee-Object -FilePath (Join-Path $output 'cleanup.log')
    }
    foreach ($suffix in $suffixes) { [Environment]::SetEnvironmentVariable("QA_IDENTITY_PASSWORD_$suffix",$null,'Process') }
    $env:QA_IDENTITY_URL=$null
    $server.Close(); $server.Dispose()
    Pop-Location
}
