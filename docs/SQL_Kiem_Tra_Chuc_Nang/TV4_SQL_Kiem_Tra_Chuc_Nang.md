# Code vấn đáp phân hệ tính lương PeopleOS

Nguyễn Quang Vinh — MSSV 24110385

Các đoạn dưới phục vụ thao tác SQL Server và giải thích phần tính lương trong source đính kèm. Chạy từng khối riêng trong SSMS trên database demo đã cài đủ module. Kết quả ghi dưới đây là kỳ vọng theo source, chưa chạy SQL Server để xác nhận trong phiên soạn này.

> [!IMPORTANT]
> **Database sử dụng chính thức:** `QuanLyNhanSuTienLuong` (đảm bảo chọn đúng DB này trên SSMS để mọi thao tác hiển thị và đồng bộ trực tiếp lên giao diện ứng dụng Java).

## 1. Chọn database và kiểm tra kỳ trước khi demo

```sql
USE QuanLyNhanSuTienLuong;
GO
SELECT MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayChot
FROM dbo.BANGLUONG
ORDER BY Nam DESC, Thang DESC;

SELECT COUNT(*) AS SoDongChamCong
FROM dbo.CHAMCONG
WHERE NgayChamCong >= '20261001'
  AND NgayChamCong < '20261101';
```

Kỳ demo tính lại phải là `CHUA_CHOT`. Nếu cả tháng không có dữ liệu chấm công, procedure từ chối tính. Các ví dụ dùng tháng 10/2026; đổi kỳ đồng bộ theo dữ liệu máy demo.

## 2. Gọi stored procedure tính lương và lấy OUTPUT

**Có thể hỏi:** Em gọi procedure và lấy mã bảng lương như thế nào?

```sql
DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang
    @Thang = 10,
    @Nam = 2026,
    @NgayCongChuan = 26,
    @MaBangLuong = @MaBangLuong OUTPUT;

SELECT @MaBangLuong AS MaBangLuong;
SELECT * FROM dbo.BANGLUONG
WHERE MaBangLuong = @MaBangLuong;
SELECT nv.HoTen, ct.*
FROM dbo.CHITIETBANGLUONG ct
JOIN dbo.NHANVIEN nv ON nv.MaNV = ct.MaNV
WHERE ct.MaBangLuong = @MaBangLuong;
```

**Giải thích:** Ba tham số đầu là đầu vào. `OUTPUT` trả mã kỳ đã xử lý. Lệnh này thực sự tính hoặc cập nhật kỳ nháp, không chỉ đọc dữ liệu.

## 3. Gọi function tính tiền công

```sql
SELECT dbo.fn_TinhTienCong(26000000, 26, 1) AS MotNgayCong;
SELECT dbo.fn_TinhTienCong(26000000, 26, 0) AS KhongCoCong;
SELECT dbo.fn_TinhTienCong(26000000, 0, 1) AS CongChuanBang0;
SELECT dbo.fn_TinhTienCong(NULL, 26, 1) AS LuongNull;
```

**Kỳ vọng:** 1.000.000,00; 0,00; 0,00; 0,00.

**Giải thích:** Tiền công = lương cơ bản / ngày công chuẩn × ngày công thực tế; làm tròn hai chữ số. Function trả 0 khi đầu vào không hợp lệ theo các điều kiện trong hàm. Riêng procedure từ chối ngày công chuẩn bằng 0.

## 4. Kiểm tra thực nhận và trường hợp âm

```sql
SELECT dbo.fn_TinhThucNhan(1000000, 730000, 1000000)
       AS ThucNhan730Nghin;
SELECT dbo.fn_TinhThucNhan(0, 0, 1000000)
       AS KhauTruVuotThuNhap;
```

**Kỳ vọng:** 730.000,00 và -1.000.000,00.

**Giải thích:** Source hiện tính thẳng tiền công + phụ cấp − khấu trừ. Chưa có xử lý đưa kết quả âm về 0. Hàm này do thành viên phụ trách bảo mật và chốt lương cung cấp, phần tính lương gọi dùng chung.

## 5. Tham số sai bị bắt ở SQL Server

Chạy riêng từng trường hợp, đổi ba biến để thử tháng 13, năm 2019 hoặc ngày công chuẩn bằng 0.

```sql
DECLARE @Thang INT = 13, @Nam INT = 2026, @CongChuan INT = 26;
BEGIN TRY
    DECLARE @Ma INT;
    EXEC dbo.sp_TinhBangLuongThang
        @Thang, @Nam, @CongChuan, @Ma OUTPUT;
END TRY
BEGIN CATCH
    SELECT ERROR_NUMBER() AS MaLoi, ERROR_MESSAGE() AS ThongBao;
END CATCH;
```

**Giải thích:** Procedure kiểm tra tháng 1–12, năm 2020–2100 và công chuẩn > 0. Lỗi tham số xảy ra trước bước mở transaction của procedure, nên ví dụ này không chứng minh rollback lỗi giữa giao dịch.

## 6. Tính lại và kiểm tra không trùng

Gọi lại khối tính lương ở mục 2 cho kỳ nháp, sau đó chạy:

```sql
SELECT MaBangLuong, Thang, Nam, TrangThai
FROM dbo.BANGLUONG
WHERE Thang = 10 AND Nam = 2026;

SELECT Thang, Nam, COUNT(*) AS SoKy
FROM dbo.BANGLUONG
GROUP BY Thang, Nam HAVING COUNT(*) > 1;

SELECT MaBangLuong, MaNV, COUNT(*) AS SoDong
FROM dbo.CHITIETBANGLUONG
GROUP BY MaBangLuong, MaNV HAVING COUNT(*) > 1;
```

**Kỳ vọng:** Kỳ nháp dùng lại mã kỳ; hai truy vấn tìm trùng không trả dòng. Procedure thay chi tiết trong cùng transaction. UNIQUE tháng/năm và UNIQUE mã kỳ/mã nhân viên bảo vệ chống trùng.

## 7. Xem view tổng khấu trừ

```sql
SELECT MaNV, HoTen, Thang, Nam, TongKhauTru
FROM dbo.vw_TongKhauTruThang
WHERE Thang = 10 AND Nam = 2026;
```

**Có thể hỏi:** Update trực tiếp qua view được không?

View này dùng `SUM` và `GROUP BY`, không thể UPDATE trực tiếp giá trị tổng như một bảng thường. Cần sửa dòng nguồn `KHAUTRUNHANVIEN` qua nghiệp vụ phù hợp. Source không có INSTEAD OF trigger cho view này. Procedure tính lương hiện lấy tổng bằng `fn_TongKhauTru`, không đọc trực tiếp view.

## 8. Xem dữ liệu công nào được tính

```sql
SELECT MaNV,
       COUNT(*) AS SoDongChamCong,
       SUM(CASE WHEN TrangThai IN ('CO_MAT', 'DI_TRE', 'VE_SOM')
                THEN 1 ELSE 0 END) AS NgayCongTinhLuong
FROM dbo.CHAMCONG
WHERE NgayChamCong >= '20261001'
  AND NgayChamCong < '20261101'
GROUP BY MaNV;
```

**Giải thích:** Hiện tại đi trễ và về sớm vẫn tính một ngày công. `VANG` không được tính. Chưa có công thức tự quy đổi theo số giờ hoặc tự phạt tiền trong bước đếm công này.

## 9. Đối chiếu snapshot và lịch sử lương

```sql
SELECT nv.MaNV, nv.HoTen,
       nv.LuongCoBan AS LuongHienTai,
       ct.LuongCoBan AS LuongDaDungDeTinh,
       bl.Thang, bl.Nam
FROM dbo.CHITIETBANGLUONG ct
JOIN dbo.NHANVIEN nv ON nv.MaNV = ct.MaNV
JOIN dbo.BANGLUONG bl ON bl.MaBangLuong = ct.MaBangLuong
WHERE bl.Thang = 10 AND bl.Nam = 2026;

SELECT * FROM dbo.LICHSULUONG
ORDER BY MaNV, TuThang DESC;
```

**Giải thích:** Với kỳ quá khứ đã có chi tiết, source ưu tiên lương trong chi tiết cũ; nếu thiếu mới lấy lịch sử hiệu lực tại đầu kỳ, sau đó đến lương hiện tại. Không khẳng định tự phục hồi chính xác lịch sử đã thiếu. Điều kiện “kỳ quá khứ” trong source dựa vào ngày hiện tại của SQL Server.

## 10. Kích hoạt trigger chặn sửa kỳ đã chốt

Dùng tài khoản có quyền UPDATE để thấy lỗi trigger, thay vì chỉ thấy lỗi thiếu quyền. Khối này thử sửa rồi rollback trên database demo. Chưa có kỳ chốt thì báo dừng; chuẩn bị kỳ riêng trước buổi trình bày.

```sql
DECLARE @Ma INT = (
    SELECT TOP (1) MaBangLuong
    FROM dbo.BANGLUONG WHERE TrangThai = 'DA_CHOT'
    ORDER BY MaBangLuong DESC
);
IF @Ma IS NULL
    THROW 51000, N'Chua co ky da chot de demo.', 1;

BEGIN TRY
    BEGIN TRAN;
    UPDATE dbo.BANGLUONG
    SET NgayCongChuan = NgayCongChuan + 1
    WHERE MaBangLuong = @Ma;
    IF @@TRANCOUNT > 0 ROLLBACK;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    SELECT ERROR_MESSAGE() AS ThongBao;
END CATCH;
SELECT MaBangLuong, NgayCongChuan, TrangThai
FROM dbo.BANGLUONG WHERE MaBangLuong = @Ma;
```

**Kỳ vọng:** Thông báo không được sửa hoặc xóa kỳ đã chốt; dữ liệu không đổi.

**Giải thích:** `trg_BangLuong_KhongSuaKhiDaChot` là INSTEAD OF UPDATE, DELETE; xét tập `inserted`, `deleted`. Có ngoại lệ mở lại hợp lệ: DA_CHOT → CHUA_CHOT, NgayChot → NULL, các trường còn lại không đổi. Kiểm soát ai được mở lại dựa thêm vào quyền SQL và procedure.

## 11. Tính lại kỳ đã chốt bị từ chối

```sql
DECLARE @Thang INT, @Nam INT, @Ma INT;
SELECT TOP (1) @Thang = Thang, @Nam = Nam
FROM dbo.BANGLUONG WHERE TrangThai = 'DA_CHOT'
ORDER BY MaBangLuong DESC;
IF @Thang IS NULL
    THROW 51000, N'Chua co ky da chot de demo.', 1;
BEGIN TRY
    EXEC dbo.sp_TinhBangLuongThang @Thang, @Nam, 26, @Ma OUTPUT;
END TRY
BEGIN CATCH
    SELECT ERROR_MESSAGE() AS ThongBao;
END CATCH;
```

**Giải thích:** Lần này lỗi xuất phát từ kiểm tra trạng thái trong procedure; khác với UPDATE trực tiếp để kích hoạt trigger ở mục 10.

## 12. Đo truy vấn sử dụng index

Bật Actual Execution Plan bằng Ctrl+M trong SSMS trước khi chạy. Chọn một nhân viên có khoản khấu trừ trong kỳ.

```sql
DECLARE @MaNV INT = (
    SELECT TOP (1) MaNV FROM dbo.KHAUTRUNHANVIEN
    WHERE Thang = 10 AND Nam = 2026 ORDER BY MaNV
);
IF @MaNV IS NULL
    THROW 51000, N'Chua co khau tru trong ky de do.', 1;
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
SELECT SUM(SoTien) AS TongKhauTru
FROM dbo.KHAUTRUNHANVIEN
WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026;
SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
```

**Giải thích:** Index `IX_KHAUTRU_MaNV_ThangNam` có khóa `(MaNV, Thang, Nam)` và INCLUDE `SoTien`, có thể cover truy vấn. Xem logical reads trong Messages và kế hoạch thực tế. Bảng nhỏ có thể được optimizer chọn scan.

## 13. Disable và khôi phục index

Chỉ thao tác trên database demo riêng. Chạy từng lệnh, chạy lại cùng truy vấn mục 12 sau mỗi bước. Nếu việc đo lỗi, vẫn phải chạy REBUILD để khôi phục.

```sql
ALTER INDEX IX_KHAUTRU_MaNV_ThangNam
ON dbo.KHAUTRUNHANVIEN DISABLE;
```

Sau khi đo trường hợp không dùng index này:

```sql
ALTER INDEX IX_KHAUTRU_MaNV_ThangNam
ON dbo.KHAUTRUNHANVIEN REBUILD;
```

Kiểm tra đã bật lại:

```sql
SELECT name, is_disabled
FROM sys.indexes
WHERE object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
  AND name = N'IX_KHAUTRU_MaNV_ThangNam';
```

**Kỳ vọng:** `is_disabled = 0` sau REBUILD. Đây là nonclustered index; không disable clustered index hay khóa chính để làm ví dụ này.

## 14. Những đoạn transaction cần chỉ vào trong procedure

Mở `database/04_Module_TinhLuong_TV4.sql`, tìm các từ khóa sau:

| Đoạn code | Cần giải thích |
| --- | --- |
| `@InitialTranCount = @@TRANCOUNT` | Ghi nhận bên gọi đã mở giao dịch hay chưa. |
| `BEGIN TRANSACTION` / `@OwnTransaction = 1` | Procedure tự mở giao dịch thì tự chịu trách nhiệm commit hoặc rollback. |
| `SAVE TRANSACTION TV4_TinhBangLuong` | Đặt savepoint khi tham gia giao dịch của bên gọi. |
| `WITH (UPDLOCK, HOLDLOCK)` trên BANGLUONG | Khóa kỳ trong transaction để phối hợp các phiên tính lương đồng thời. |
| `INSERT INTO @NhanVienNguon` | Giữ tập nhân viên và mức lương nguồn trước khi thay chi tiết. |
| `DELETE FROM dbo.CHITIETBANGLUONG` | Xóa kết quả cũ khi tính lại kỳ nháp, trong cùng transaction. |
| `IF @OwnTransaction = 1 COMMIT TRANSACTION` | Chỉ tự commit giao dịch do procedure mở. |
| `XACT_STATE() = 1` và rollback savepoint | Chỉ rollback cục bộ nếu giao dịch còn committable. |
| `SET @MaBangLuong = NULL` trong CATCH | Không trả mã kỳ như thể lần tính thất bại đã thành công. |

Không dùng riêng `BEGIN TRAN; EXEC ...; ROLLBACK;` để khẳng định đã chứng minh rollback nội bộ khi lỗi giữa chừng. Kịch bản tạo lỗi giữa quá trình ghi và kiểm tra dữ liệu cũ nằm trong `database/tests/TV4_Payroll_E2E.sql`; cần đọc và tập chạy trên database test trước.

## 15. Java gọi procedure như thế nào

File: `src/main/java/com/dao/BangLuongDAO.java`, phương thức `tinhBangLuongThang`.

```java
public int tinhBangLuongThang(int thang, int nam, int ngayCongChuan)
        throws SQLException {
    String sql = "{CALL dbo.sp_TinhBangLuongThang(?, ?, ?, ?)}";
    try (Connection conn = DatabaseConnection.getConnection();
         CallableStatement cs = conn.prepareCall(sql)) {
        cs.setInt(1, thang);
        cs.setInt(2, nam);
        cs.setInt(3, ngayCongChuan);
        cs.registerOutParameter(4, Types.INTEGER);
        cs.execute();
        return cs.getInt(4);
    }
}
```

**Giải thích:** `prepareCall` tạo lời gọi stored procedure. `setInt` truyền đầu vào, `registerOutParameter` đăng ký đầu ra, `execute` chạy và `getInt(4)` lấy mã kỳ. Try-with-resources đóng statement và connection.

File Service: `src/main/java/com/service/PayrollService.java`.

```java
Session.getInstance().requireRoles("DB_Admin", "Payroll_Officer");
validateKyLuong(thang, nam, ngayCongChuan);
```

**Giải thích:** Kiểm tra quyền và tham số ở tầng ứng dụng; SQL Server tiếp tục kiểm tra ở procedure và quyền database. Giao diện gọi qua `BangLuongPanel.xuLyTinhLuong`, sau đó đến Service rồi DAO.

## 16. Định nghĩa SQL gốc để đọc khi được hỏi

Các khối tiếp theo trích từ module tính lương trong source. Dùng để giải thích; không cần chạy CREATE OR ALTER lại trong lúc demo.

### Function tính tiền công

```sql
CREATE OR ALTER FUNCTION dbo.fn_TinhTienCong
(
    @LuongCoBan      DECIMAL(18,2),
    @NgayCongChuan   INT,
    @NgayCongThucTe  INT
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @KetQua DECIMAL(18,2) = 0;

    IF (@LuongCoBan IS NULL OR @LuongCoBan <= 0)
        RETURN 0;

    IF (@NgayCongChuan IS NULL OR @NgayCongChuan <= 0)
        RETURN 0;

    IF (@NgayCongThucTe IS NULL OR @NgayCongThucTe <= 0)
        RETURN 0;

    SET @KetQua = ROUND((@LuongCoBan / CAST(@NgayCongChuan AS DECIMAL(18,2))) * @NgayCongThucTe, 2);
    RETURN @KetQua;
END;
```

### View tổng khấu trừ

```sql
IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
   AND OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NOT NULL
BEGIN
    EXEC(N'
        CREATE OR ALTER VIEW dbo.vw_TongKhauTruThang
        AS
        SELECT
            ktn.MaNV,
            nv.HoTen,
            ktn.Thang,
            ktn.Nam,
            CAST(SUM(ktn.SoTien) AS DECIMAL(18,2)) AS TongKhauTru
        FROM dbo.KHAUTRUNHANVIEN ktn
        INNER JOIN dbo.NHANVIEN nv ON ktn.MaNV = nv.MaNV
        GROUP BY ktn.MaNV, nv.HoTen, ktn.Thang, ktn.Nam;
    ');
END
ELSE
BEGIN
    PRINT N'Bo qua view vw_TongKhauTruThang vi chua co bang KHAUTRUNHANVIEN hoac NHANVIEN.';
END;
```

### Index khấu trừ

```sql
IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
   AND NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = N'IX_KHAUTRU_MaNV_ThangNam'
          AND object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
   )
BEGIN
    CREATE NONCLUSTERED INDEX IX_KHAUTRU_MaNV_ThangNam
    ON dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam)
    INCLUDE (SoTien);
END;
```

### Trigger bảo vệ kỳ chốt

```sql
CREATE OR ALTER TRIGGER dbo.trg_BangLuong_KhongSuaKhiDaChot
ON dbo.BANGLUONG
INSTEAD OF UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Ky da chot la bat bien. Ngoai le duy nhat la thao tac mo lai co kiem soat:
    -- DA_CHOT -> CHUA_CHOT, xoa NgayChot va khong thay doi bat ky du lieu nao khac.
    -- Quyen UPDATE truc tiep tren bang khong duoc cap cho Payroll Officer; thao tac nay
    -- duoc thuc hien qua dbo.sp_HuyChotBangLuong.
    IF EXISTS (
        SELECT 1
        FROM deleted d
        LEFT JOIN inserted i ON i.MaBangLuong = d.MaBangLuong
        WHERE d.TrangThai = 'DA_CHOT'
          AND (
                i.MaBangLuong IS NULL
                OR i.TrangThai <> 'CHUA_CHOT'
                OR i.NgayChot IS NOT NULL
                OR i.Thang <> d.Thang
                OR i.Nam <> d.Nam
                OR i.NgayCongChuan <> d.NgayCongChuan
                OR i.NgayTao <> d.NgayTao
          )
    )
    BEGIN
        RAISERROR(N'Khong duoc sua hoac xoa bang luong da chot.', 16, 1);
        RETURN;
    END;

    IF EXISTS (SELECT 1 FROM inserted)
    BEGIN
        UPDATE bl
        SET
            bl.Thang = i.Thang,
            bl.Nam = i.Nam,
            bl.NgayCongChuan = i.NgayCongChuan,
            bl.TrangThai = i.TrangThai,
            bl.NgayTao = i.NgayTao,
            bl.NgayChot = i.NgayChot
        FROM dbo.BANGLUONG bl
        INNER JOIN inserted i ON bl.MaBangLuong = i.MaBangLuong;

        RETURN;
    END;

    DELETE bl
    FROM dbo.BANGLUONG bl
    INNER JOIN deleted d ON bl.MaBangLuong = d.MaBangLuong;
END;
```

## 17. Các câu hỏi yêu cầu thao tác trực tiếp trên SQL Server

Phần này bổ sung 16 tình huống để tập vấn đáp. Với đoạn đã có, mở đúng mục được dẫn; các truy vấn mới nằm ngay dưới câu hỏi. Mỗi khối SQL chạy riêng trong SSMS trên database demo.

### 17.1. Gọi procedure và nhận mã bảng lương như thế nào?

Chạy [mục 2](#2-gọi-stored-procedure-tính-lương-và-lấy-output). Chỉ vào tham số `OUTPUT`, sau đó dùng mã nhận được để đọc `BANGLUONG` và `CHITIETBANGLUONG`.

### 17.2. Function trả gì với dữ liệu bình thường, ngày công bằng 0 và NULL?

Chạy [mục 3](#3-gọi-function-tính-tiền-công). Mở định nghĩa `fn_TinhTienCong` tại mục 16 để giải thích các điều kiện `RETURN 0` và dòng `ROUND`.

### 17.3. Truy vấn tổng thực nhận và số nhân viên của một kỳ lương

```sql
DECLARE @Thang INT = 10, @Nam INT = 2026;
SELECT bl.MaBangLuong, bl.Thang, bl.Nam,
       COUNT(ct.MaNV) AS SoNhanVien,
       ISNULL(SUM(ct.ThucNhan), 0) AS TongThucNhan
FROM dbo.BANGLUONG bl
LEFT JOIN dbo.CHITIETBANGLUONG ct
       ON ct.MaBangLuong = bl.MaBangLuong
WHERE bl.Thang = @Thang AND bl.Nam = @Nam
GROUP BY bl.MaBangLuong, bl.Thang, bl.Nam;
```

**Cần giải thích:** `LEFT JOIN` vẫn giữ kỳ chưa có chi tiết. `COUNT(ct.MaNV)` đếm các dòng chi tiết có nhân viên; `ISNULL` đưa tổng về 0 khi không có chi tiết. Nếu kỳ chưa tồn tại, truy vấn không trả dòng.

### 17.4. Tìm nhân viên thực nhận cao nhất và giữ tất cả người đồng hạng

```sql
DECLARE @Thang INT = 10, @Nam INT = 2026;
SELECT TOP (1) WITH TIES
       nv.MaNV, nv.HoTen, ct.ThucNhan
FROM dbo.CHITIETBANGLUONG ct
JOIN dbo.BANGLUONG bl ON bl.MaBangLuong = ct.MaBangLuong
JOIN dbo.NHANVIEN nv ON nv.MaNV = ct.MaNV
WHERE bl.Thang = @Thang AND bl.Nam = @Nam
ORDER BY ct.ThucNhan DESC;
```

**Cần giải thích:** `WITH TIES` lấy thêm các dòng có cùng giá trị sắp xếp với dòng cao nhất. Không thêm `MaNV` vào `ORDER BY` này, vì sẽ làm thay đổi tiêu chí đồng hạng.

### 17.5. Viết truy vấn kiểm tra trùng kỳ hoặc trùng nhân viên trong kỳ

Chạy hai truy vấn `GROUP BY ... HAVING COUNT(*) > 1` ở [mục 6](#6-tính-lại-và-kiểm-tra-không-trùng). Giải thích thêm hai ràng buộc `UQ_BANGLUONG_ThangNam` và `UQ_CTBL_BangLuong_NhanVien`.

### 17.6. Tính lại kỳ nháp có tạo thêm kỳ mới không?

Ghi lại `MaBangLuong`, gọi lại procedure cùng tháng/năm theo mục 2, rồi kiểm tra theo mục 6. Cần chứng minh mã kỳ được giữ, mỗi nhân viên chỉ có một dòng trong kỳ; chi tiết được thay trong cùng transaction.

### 17.7. Tính lại kỳ đã chốt bị bắt lỗi ở đâu?

Chạy [mục 11](#11-tính-lại-kỳ-đã-chốt-bị-từ-chối). Trong procedure, chỉ vào điều kiện `@TrangThaiKy = 'DA_CHOT'` và câu phát lỗi ngay sau đó.

### 17.8. UPDATE hoặc DELETE kỳ đã chốt để chứng minh trigger

UPDATE đã có ở [mục 10](#10-kích-hoạt-trigger-chặn-sửa-kỳ-đã-chốt). Dưới đây là trường hợp DELETE, dùng tài khoản đủ quyền để kiểm tra trigger:

```sql
DECLARE @Ma INT = (
    SELECT TOP (1) MaBangLuong FROM dbo.BANGLUONG
    WHERE TrangThai = 'DA_CHOT' ORDER BY MaBangLuong DESC
);
IF @Ma IS NULL
    THROW 51000, N'Chua co ky da chot de demo.', 1;

BEGIN TRY
    BEGIN TRAN;
    DELETE FROM dbo.BANGLUONG WHERE MaBangLuong = @Ma;
    IF @@TRANCOUNT > 0 ROLLBACK;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    SELECT ERROR_MESSAGE() AS ThongBao;
END CATCH;
SELECT MaBangLuong, TrangThai
FROM dbo.BANGLUONG WHERE MaBangLuong = @Ma;
```

**Kỳ vọng:** Trigger báo không được sửa/xóa kỳ đã chốt; kỳ vẫn tồn tại. Đây là kiểm tra trigger, không phải lệnh xóa kỳ nháp trong quy trình nghiệp vụ.

### 17.9. UPDATE nhiều dòng có cả kỳ đã chốt thì sao?

Chuẩn bị sẵn một kỳ nháp và một kỳ đã chốt trên database demo; dùng tài khoản có quyền UPDATE.

```sql
DECLARE @DaChot INT = (
    SELECT TOP (1) MaBangLuong FROM dbo.BANGLUONG
    WHERE TrangThai = 'DA_CHOT' ORDER BY MaBangLuong DESC
);
DECLARE @Nhap INT = (
    SELECT TOP (1) MaBangLuong FROM dbo.BANGLUONG
    WHERE TrangThai = 'CHUA_CHOT' ORDER BY MaBangLuong DESC
);
IF @DaChot IS NULL OR @Nhap IS NULL
    THROW 51000, N'Can mot ky da chot va mot ky nhap.', 1;

SELECT N'Truoc' AS Moc, MaBangLuong, NgayCongChuan, TrangThai
FROM dbo.BANGLUONG WHERE MaBangLuong IN (@DaChot, @Nhap);
BEGIN TRY
    BEGIN TRAN;
    UPDATE dbo.BANGLUONG
    SET NgayCongChuan = CASE WHEN NgayCongChuan = 26 THEN 25 ELSE 26 END
    WHERE MaBangLuong IN (@DaChot, @Nhap);
    IF @@TRANCOUNT > 0 ROLLBACK;
END TRY
BEGIN CATCH
    -- Doc truoc ROLLBACK de thay trigger da tu choi ca tap dong.
    SELECT ERROR_MESSAGE() AS ThongBao;
    SELECT N'Sau khi trigger tu choi' AS Moc,
           MaBangLuong, NgayCongChuan, TrangThai
    FROM dbo.BANGLUONG WHERE MaBangLuong IN (@DaChot, @Nhap);
    IF @@TRANCOUNT > 0 ROLLBACK;
END CATCH;
```

**Cần giải thích:** Trigger dùng `IF EXISTS` trên cả tập `deleted` và `inserted`. Chỉ cần một dòng đã chốt bị sửa không hợp lệ, trigger phát lỗi và `RETURN` trước phần UPDATE thật, nên không thực hiện cập nhật cho cả tập dòng.

### 17.10. Viết SELECT tương đương view mà không dùng view

```sql
SELECT ktn.MaNV, nv.HoTen, ktn.Thang, ktn.Nam,
       CAST(SUM(ktn.SoTien) AS DECIMAL(18,2)) AS TongKhauTru
FROM dbo.KHAUTRUNHANVIEN ktn
JOIN dbo.NHANVIEN nv ON nv.MaNV = ktn.MaNV
WHERE ktn.Thang = 10 AND ktn.Nam = 2026
GROUP BY ktn.MaNV, nv.HoTen, ktn.Thang, ktn.Nam;
```

Đối chiếu với truy vấn view ở [mục 7](#7-xem-view-tổng-khấu-trừ). Hai truy vấn phải cho cùng tập kết quả khi dữ liệu nguồn không thay đổi; thứ tự dòng không được bảo đảm nếu không có `ORDER BY`.

### 17.11. Thử UPDATE tổng khấu trừ qua view

```sql
BEGIN TRY
    BEGIN TRAN;
    EXEC sys.sp_executesql N'
        UPDATE dbo.vw_TongKhauTruThang
        SET TongKhauTru = 0
        WHERE Thang = 10 AND Nam = 2026;';
    IF @@TRANCOUNT > 0 ROLLBACK;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    SELECT ERROR_MESSAGE() AS ThongBao;
END CATCH;
```

**Kỳ vọng theo định nghĩa view hiện tại:** Không cập nhật được cột tổng hợp. Dùng `sp_executesql` để lỗi biên dịch của câu UPDATE bên trong được bắt tại CATCH bên ngoài. Dùng tài khoản đủ quyền để tránh nhầm lỗi thiếu quyền với lỗi view không cập nhật được.

### 17.12. Disable index, đo lại rồi khôi phục

Chạy [mục 12](#12-đo-truy-vấn-sử-dụng-index) và [mục 13](#13-disable-và-khôi-phục-index). Giữ cùng câu truy vấn và tham số khi so sánh. Sau khi đo phải REBUILD và xác nhận `is_disabled = 0`.

### 17.13. Tạo lỗi giữa transaction và chứng minh rollback

Ví dụ dưới dùng bảng tạm để minh họa cơ chế transaction; không phải bằng chứng procedure tính lương đã vượt kiểm thử rollback.

```sql
CREATE TABLE #DemoRollbackLuong (
    MaNV INT PRIMARY KEY,
    ThucNhan DECIMAL(18,2) NOT NULL
);
BEGIN TRY
    BEGIN TRAN;
    INSERT INTO #DemoRollbackLuong VALUES (1, 730000);
    -- Co tinh trung khoa chinh o thao tac thu hai.
    INSERT INTO #DemoRollbackLuong VALUES (1, 900000);
    COMMIT;
END TRY
BEGIN CATCH
    SELECT ERROR_MESSAGE() AS ThongBao;
    IF @@TRANCOUNT > 0 ROLLBACK;
END CATCH;
SELECT COUNT(*) AS SoDongSauRollback FROM #DemoRollbackLuong;
DROP TABLE #DemoRollbackLuong;
```

**Kỳ vọng:** Số dòng bằng 0; dòng đầu cũng bị hoàn tác. Nếu cô yêu cầu chứng minh trực tiếp trên procedure tính lương, dùng tình huống tạo lỗi giữa chừng và kiểm tra dữ liệu cũ trong `database/tests/TV4_Payroll_E2E.sql`.

### 17.14. Rollback về savepoint giữ phần dữ liệu nào?

```sql
CREATE TABLE #DemoSavepointLuong (
    MaNV INT PRIMARY KEY,
    ThucNhan DECIMAL(18,2) NOT NULL
);
BEGIN TRAN;
INSERT INTO #DemoSavepointLuong VALUES (1, 730000);
SAVE TRANSACTION MocTruocNhanVien2;
INSERT INTO #DemoSavepointLuong VALUES (2, 900000);

ROLLBACK TRANSACTION MocTruocNhanVien2;
SELECT * FROM #DemoSavepointLuong;
SELECT @@TRANCOUNT AS SoCapTransaction, XACT_STATE() AS TrangThai;

ROLLBACK;
DROP TABLE #DemoSavepointLuong;
```

**Kỳ vọng trong cửa sổ không có transaction mở sẵn:** Sau rollback savepoint chỉ còn dòng nhân viên 1; `@@TRANCOUNT = 1`, `XACT_STATE() = 1`. Giao dịch vẫn mở, nên cuối ví dụ có `ROLLBACK` toàn bộ để kết thúc. Liên hệ với savepoint của procedure tại mục 14.

### 17.15. Hai cửa sổ SSMS cùng tính một kỳ thì phiên nào chờ?

Mở hai kịch bản có sẵn trong source: `database/tests/TV4_Payroll_Concurrency_SessionA.sql` và `database/tests/TV4_Payroll_Concurrency_SessionB.sql`. Đọc phần chuẩn bị trong file, dùng cùng database test và cùng kỳ; bắt đầu phiên A rồi chạy phiên B trong lúc A còn giữ khóa.

**Cần chứng minh:** Phiên B chờ khóa khi xung đột với phiên A; sau khi khóa được giải phóng, kiểm tra lại số kỳ và các cặp mã kỳ/mã nhân viên theo mục 6. Chỉ nói thời gian chờ theo kết quả thực tế; không kết luận đã có kiểm thử đồng thời chỉ từ việc chạy hai phiên lần lượt sau khi phiên trước hoàn tất.

### 17.16. Lấy mức lương lịch sử có hiệu lực tại đầu kỳ

```sql
DECLARE @DauKy DATE = DATEFROMPARTS(2026, 10, 1);
SELECT nv.MaNV, nv.HoTen,
       h.TuThang, h.LuongCoBan AS LuongLichSuTaiDauKy
FROM dbo.NHANVIEN nv
OUTER APPLY (
    SELECT TOP (1) hl.TuThang, hl.LuongCoBan
    FROM dbo.LICHSULUONG hl
    WHERE hl.MaNV = nv.MaNV AND hl.TuThang <= @DauKy
    ORDER BY hl.TuThang DESC
) h
ORDER BY nv.MaNV;
```

**Cần giải thích:** Lọc lịch sử có hiệu lực không muộn hơn đầu kỳ, rồi lấy mốc gần nhất. `OUTER APPLY` vẫn giữ nhân viên chưa có lịch sử và trả NULL ở cột lịch sử. Đây chỉ là truy vấn lịch sử; quy tắc chọn lương đầy đủ của procedure còn ưu tiên snapshot cũ khi tính lại kỳ quá khứ, như mục 9.
