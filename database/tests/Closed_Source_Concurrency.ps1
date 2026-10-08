param([string]$ServerInstance = 'localhost', [Parameter(Mandatory=$true)][string]$Database)
$ErrorActionPreference = 'Stop'
if ($Database -notmatch '^PRJ_Fix_QA_[A-Za-z0-9_]+$') { throw 'Use a dedicated PRJ_Fix_QA database.' }
$builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
$builder['Data Source']=$ServerInstance; $builder['Initial Catalog']=$Database
$builder['Integrated Security']=$true; $builder['Encrypt']=$true; $builder['TrustServerCertificate']=$true
function Open-Connection {
    $connection=New-Object System.Data.SqlClient.SqlConnection $builder.ConnectionString
    $connection.Open(); return $connection
}
function Execute([System.Data.SqlClient.SqlConnection]$Connection,[string]$Sql) {
    $command=$Connection.CreateCommand(); $command.CommandTimeout=10; $command.CommandText=$Sql
    try { $command.ExecuteNonQuery() | Out-Null } finally { $command.Dispose() }
}
$a=$null; $b=$null; $nv=0; $bl=0; $pb=0; $cv=0
try {
    $a=Open-Connection; $b=Open-Connection
    $setup=$a.CreateCommand(); $setup.CommandTimeout=10
    $setup.CommandText=@'
SET NOCOUNT ON;
SET QUOTED_IDENTIFIER ON;
IF EXISTS(SELECT 1 FROM dbo.PHONGBAN WHERE TenPB=N'QA_CloseRace') OR EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE Thang=7 AND Nam=2020)
    THROW 53500,N'Fixture name/period already exists; refusing overwrite.',1;
DECLARE @PB INT,@CV INT,@NV INT,@BL INT,@CC INT;
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT dbo.PHONGBAN(TenPB) VALUES(N'QA_CloseRace'); SET @PB=SCOPE_IDENTITY();
    INSERT dbo.CHUCVU(TenCV) VALUES(N'QA_CloseRace'); SET @CV=SCOPE_IDENTITY();
    INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
    VALUES(N'QA Close Race','1990-01-01',N'Nam','699900009000','0699909000','qa_close_race@example.invalid','2019-01-01',26000000,@PB,@CV);
    SET @NV=SCOPE_IDENTITY();
    EXEC dbo.sp_GhiNhanChamCong @NV,'2020-07-02','08:00','17:00',N'CO_MAT',NULL,@CC OUTPUT;
    INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien) VALUES(@NV,7,2020,N'QA',100);
    INSERT dbo.KHAUTRUNHANVIEN(MaNV,Thang,Nam,TenKhauTru,SoTien) VALUES(@NV,7,2020,N'QA',50);
    EXEC dbo.sp_TinhBangLuongThang 7,2020,26,@BL OUTPUT;
    COMMIT;
    SELECT @PB AS PB,@CV AS CV,@NV AS NV,@BL AS BL;
END TRY
BEGIN CATCH IF @@TRANCOUNT>0 ROLLBACK; THROW; END CATCH;
'@
    $reader=$setup.ExecuteReader()
    try {
        if (!$reader.Read()) { throw 'Fixture IDs were not returned.' }
        $pb=[int]$reader['PB']; $cv=[int]$reader['CV']; $nv=[int]$reader['NV']; $bl=[int]$reader['BL']
    } finally { $reader.Dispose(); $setup.Dispose() }
    $cases=@(
        @('attendance',"UPDATE dbo.CHAMCONG SET GhiChu=N'RACE' WHERE MaNV=$nv AND NgayChamCong='2020-07-02';", "EXISTS(SELECT 1 FROM dbo.CHAMCONG WHERE MaNV=$nv AND GhiChu IS NULL)"),
        @('allowance',"UPDATE dbo.PHUCAPNHANVIEN SET SoTien=101 WHERE MaNV=$nv AND Thang=7 AND Nam=2020;", "EXISTS(SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV=$nv AND SoTien=100)"),
        @('deduction',"UPDATE dbo.KHAUTRUNHANVIEN SET SoTien=51 WHERE MaNV=$nv AND Thang=7 AND Nam=2020;", "EXISTS(SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE MaNV=$nv AND SoTien=50)"),
        @('payroll detail',"UPDATE dbo.CHITIETBANGLUONG SET TienCong=TienCong+1 WHERE MaNV=$nv AND MaBangLuong=$bl;", "EXISTS(SELECT 1 FROM dbo.CHITIETBANGLUONG WHERE MaNV=$nv AND MaBangLuong=$bl AND TienCong=1000000)")
    )
    foreach ($case in $cases) {
        $transaction=$a.BeginTransaction()
        $close=$a.CreateCommand(); $close.Transaction=$transaction; $close.CommandText="EXEC dbo.sp_ChotBangLuong $bl;"
        $mutation=$b.CreateCommand(); $mutation.CommandTimeout=10; $mutation.CommandText=$case[1]
        try {
            $close.ExecuteNonQuery() | Out-Null
            $watch=[Diagnostics.Stopwatch]::StartNew(); $pending=$mutation.ExecuteNonQueryAsync()
            Start-Sleep -Milliseconds 1000
            if ($pending.IsCompleted) { throw "Mutation did not wait for closing transaction: $($case[0])" }
            $transaction.Commit()
            $rejected=$false
            try { $pending.GetAwaiter().GetResult() | Out-Null }
            catch {
                $sqlError=$_.Exception
                while ($sqlError -and $sqlError -isnot [System.Data.SqlClient.SqlException]) { $sqlError=$sqlError.InnerException }
                if (!$sqlError -or !(@($sqlError.Errors | Where-Object { $_.Number -eq 50000 -and $_.Message -like ('*ch' + [char]0x1ED1 + 't*') }).Count)) { throw }
                $rejected=$true
            }
            $watch.Stop()
            if (!$rejected -or $watch.ElapsedMilliseconds -lt 900) { throw 'Closing race was not rejected after the wait.' }
            Execute $a "IF NOT $($case[2]) OR NOT EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=$bl AND TrangThai='DA_CHOT') THROW 53501,N'Closed/source state changed.',1;"
            Execute $b "IF @@TRANCOUNT<>0 THROW 53502,N'Mutation leaked a transaction.',1;"
            Write-Host "PASS close versus $($case[0]): waited $($watch.ElapsedMilliseconds) ms, rejected, source unchanged."
        } finally {
            if ($transaction.Connection) { $transaction.Rollback() }
            $transaction.Dispose(); $close.Dispose(); $mutation.Dispose()
        }
        Execute $a "EXEC dbo.sp_HuyChotBangLuong $bl;"
    }
} finally {
    if ($b) { $b.Dispose() }
    if ($a) {
        try {
            if ($bl -gt 0) {
                Execute $a "IF EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=$bl AND TrangThai='DA_CHOT') EXEC dbo.sp_HuyChotBangLuong $bl; DELETE dbo.CHITIETBANGLUONG WHERE MaBangLuong=$bl; DELETE dbo.BANGLUONG WHERE MaBangLuong=$bl;"
            }
            if ($nv -gt 0) { Execute $a "DELETE dbo.PHUCAPNHANVIEN WHERE MaNV=$nv; DELETE dbo.KHAUTRUNHANVIEN WHERE MaNV=$nv; DELETE dbo.CHAMCONG WHERE MaNV=$nv; DELETE dbo.NHANVIEN WHERE MaNV=$nv;" }
            if ($pb -gt 0) { Execute $a "DELETE dbo.PHONGBAN WHERE MaPB=$pb; DELETE dbo.CHUCVU WHERE MaCV=$cv; IF EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=$nv) OR EXISTS(SELECT 1 FROM dbo.LICHSULUONG WHERE MaNV=$nv) THROW 53503,N'Race fixture cleanup failed.',1;"; Write-Host 'PASS closing-race fixture cleanup.' }
        } finally { $a.Dispose() }
    }
}
