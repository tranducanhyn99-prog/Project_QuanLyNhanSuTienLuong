# TV2 — CẨM NANG CÂU LỆNH SQL THỰC CHIẾN (KIỂM TRA & KÍCH HOẠT CHỨC NĂNG)

**Dành riêng cho:** Phạm Minh Quân (TV2 · MSSV: 24110311)  
**Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284) — TS. Phan Thị Thể  
**Phân hệ đảm nhiệm:** Quản lý chấm công (`CHAMCONG`), Ràng buộc toàn vẹn, Trigger kiểm tra giờ & trạng thái nhân viên, Stored Procedure ghi nhận chấm công, Function phụ cấp (phối hợp TV3), View tổng hợp tháng & Chỉ mục tối ưu hiệu năng.

---

> [!IMPORTANT]
> ### THÔNG TIN DATABASE & TÀI KHOẢN ĐANG CÓ SẴN TRÊN MÁY BẠN
> - **Database kiểm tra chính thức:** `QuanLyNhanSuTienLuong` (đây là DB chuẩn mà ứng dụng Java PeopleOS đang kết nối đồng bộ trực tiếp).
> - **Kỳ lương đang mở (Chưa chốt):** Tháng 10/2026 (`MaBangLuong = 1009`, `CHUA_CHOT`).
> - **Nhân viên test tiêu chuẩn:** `Email = 'gui-b@example.invalid'` (Mã NV: `1009` — Trần Thị Bảo Bình, đang làm việc `DANG_LAM_VIEC`).
> - **Nguyên tắc an toàn tuyệt đối:** Toàn bộ các test case có thao tác ghi dữ liệu (`INSERT`, `UPDATE`) đều được bọc trong `BEGIN TRANSACTION ... ROLLBACK TRANSACTION` và có khối đối chiếu sau `GO`. Dữ liệu sẽ hoàn trả 100% nguyên trạng ngay sau khi thực thi.

---

## HƯỚNG DẪN THAO TÁC TỪNG BƯỚC TRÊN SSMS

1. **Bước 1:** Mở SSMS, kết nối bằng tài khoản có quyền quản trị (Windows Authentication hoặc `sa`).
2. **Bước 2:** Chạy trọn vẹn khối **B0 — BƯỚC CHUẨN BỊ** để khởi tạo bảng tạm `#TV2_CTX` (chứa sẵn `MaNV = 1009` và `NgayTest` hợp lệ trong tháng 10/2026).
3. **Bước 3:** Khi Giảng viên đặt câu hỏi, tra cứu mã test case tương ứng ở bảng bên dưới.
4. **Bước 4:** Copy toàn bộ khối SQL của test case đó vào cửa sổ SSMS (cùng cửa sổ với B0), bôi đen và nhấn **F5**.
5. **Bước 5:** Quan sát kết quả tại tab **Results** và tab **Messages** để giải thích cho cô.
6. **Bước 6:** Khi kết thúc toàn bộ phần báo cáo, chạy **TC25 — Kiểm tra cuối buổi** để chứng minh CSDL hoàn toàn sạch và không còn transaction mở.

---

## BẢNG TRA CỨU NHANH — CÔ HỎI GÌ THÌ CHẠY GÌ?

| Nhóm chức năng / Vị trí Slide | Giảng viên hỏi / Tình huống kiểm tra | Test Case cần chạy |
| :--- | :--- | :--- |
| **Slide 6 — Toàn vẹn dữ liệu** | Giờ ra <= giờ vào, Trigger khi INSERT | **TC08** |
| | Giờ ra <= giờ vào, Trigger khi UPDATE | **TC09** |
| | Nhân viên đã nghỉ việc, Trigger khi INSERT | **TC10** |
| | Nhân viên đã nghỉ việc, Trigger khi UPDATE | **TC11** |
| | Một người không được chấm hai lần trong ngày | **TC02** (qua SP) / **TC17** (qua UNIQUE CSDL) |
| | Mã nhân viên không tồn tại trong hệ thống | **TC06** (qua SP) / **TC18** (qua FK CSDL) |
| | Không cho chấm công ngày trong tương lai | **TC03** (qua SP) / **TC19** (qua CHECK CSDL) |
| | Chỉ chấp nhận 4 mã trạng thái chấm công hợp lệ | **TC05** (qua SP) / **TC20** (qua CHECK CSDL) |
| **Slide 7 — Thủ tục, Hàm, View** | SP ghi nhận chấm công thành công & trả OUTPUT mã vừa tạo | **TC01** |
| | SP từ chối giờ ra không hợp lệ | **TC04** |
| | SP từ chối nhân viên đã nghỉ việc | **TC07** |
| | SP kiểm tra xử lý tham số bắt buộc bị NULL | **TC21** |
| | SP cho phép `GioRa = NULL` (khi nhân viên chưa kết thúc ca) | **TC22** |
| | Hàm `fn_TongPhuCap` khi nhân viên không có phụ cấp | **TC12** |
| | Hàm `fn_TongPhuCap` cộng dồn các khoản phụ cấp (100k + 200k) | **TC13** |
| | Xem View tổng hợp công theo tháng | **TC14** |
| | View tự động cập nhật số công theo transaction | **TC23** |
| **Slide 8 — Chỉ mục & Hiệu năng** | Xem cấu trúc Covering Index `IX_CHAMCONG_MaNV_Ngay` | **TC15** |
| | Đo Logical Reads với `STATISTICS IO` trên dataset hiện tại | **TC24** |
| | Benchmark 30.000 dòng trước/sau Index (574 reads -> 5 reads) | Chạy file: `database/test_benchmark_index_TV2.sql` |
| **Giao dịch (Transaction) chuyên sâu** | Nhập chấm công hàng loạt đảm bảo tính nguyên tử (All-or-Nothing) | **TC26** |
| | Điểm lưu giao dịch một phần (SAVE TRANSACTION / Savepoints) | **TC27** |
| | Cơ chế XACT_STATE() = -1 (Doomed Transaction) khi Trigger tự ROLLBACK | **TC28** |
| | Kiểm soát tranh chấp đồng thời (Concurrency Locking) giữa 2 phiên | **TC29** |
| **Hậu kiểm an toàn** | Mở định nghĩa SQL của SP/Trigger/Function/View | **B1** |
| | Kiểm tra nhanh trạng thái sau từng ca demo | **TC16** |
| | Đối chiếu tổng thể dữ liệu & transaction cuối buổi | **TC25** |

---

## PHÂN BIỆT 4 ĐỐI TƯỢNG SQL ĐỂ TRẢ LỜI CÔ

1. **TRIGGER (`trg_ChamCong_KiemTraGio`, `trg_ChamCong_KiemTraNhanVien`):**
   - Là đối tượng tự động kích hoạt bởi SQL Server khi có sự kiện `INSERT` hoặc `UPDATE` vào bảng `CHAMCONG`.
   - **Không thể gọi trực tiếp bằng lệnh `EXEC`**. Phải test bằng cách phát lệnh `INSERT` hoặc `UPDATE` trực tiếp vào bảng.
   - Khi phát hiện vi phạm, Trigger thực hiện `RAISERROR` kèm `ROLLBACK TRANSACTION`.
2. **STORED PROCEDURE (`sp_GhiNhanChamCong`):**
   - Là thủ tục nghiệp vụ, được gọi tường minh bằng lệnh `EXEC dbo.sp_GhiNhanChamCong ...`.
   - SP thực hiện tiền kiểm tra (validation) các tham số đầu vào trước khi thực hiện câu lệnh `INSERT`.
   - SP sử dụng `SCOPE_IDENTITY()` để lấy mã vừa sinh trả về qua tham số `OUTPUT`.
3. **SCALAR FUNCTION (`fn_TongPhuCap`):**
   - Là hàm trả về một giá trị duy nhất (tổng tiền phụ cấp của nhân viên trong tháng).
   - Được gọi bằng câu lệnh `SELECT dbo.fn_TongPhuCap(MaNV, Thang, Nam)`.
   - Không tự động chạy khi ghi dữ liệu, mà được gọi từ câu lệnh SELECT hoặc từ SP tính lương.
4. **VIEW (`vw_TongHopChamCongThang`):**
   - Là khung nhìn tổng hợp ảo, gom nhóm theo `MaNV`, `Thang`, `Nam` để tính tổng số ngày làm, số lần đi trễ, về sớm, vắng và tổng giờ làm.
   - Dữ liệu luôn cập nhật trực tiếp theo thời gian thực từ bảng `CHAMCONG` nguồn.

---

## B0 — BƯỚC CHUẨN BỊ: BẮT BUỘC CHẠY TRƯỚC KHI DEMO BẤT KỲ TC NÀO

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **MỤC ĐÍCH** | Chọn đúng database QuanLyNhanSuTienLuong, xác nhận đầy đủ đối tượng SQL và chọn nhân viên B cùng ngày chưa có công thuộc kỳ chưa chốt. |
| **THAY ĐỔI** | Chỉ tạo bảng TẠM #TV2_CTX trong phiên SSMS, KHÔNG thêm công. |
| **CÁCH LÀM** | Bôi đen từ USE [QuanLyNhanSuTienLuong] đến GO kết thúc B0 rồi F5. |
| **CẦN THẤY** | Kết quả 'B0 - SAN SANG' cùng MaNV, NgayTest, ThangTest, NamTest. |
| **NHỚ** | Phải giữ nguyên cửa sổ Query này cho các test tiếp theo. |

```sql
USE [QuanLyNhanSuTienLuong];
GO
SET NOCOUNT ON;
SET XACT_ABORT OFF;
IF DB_NAME() <> N'QuanLyNhanSuTienLuong'
    THROW 51001, N'SAI DATABASE! Chi duoc chay tren QA duoc chi dinh.', 1;
IF @@TRANCOUNT <> 0
    THROW 51002, N'Cua so query dang co transaction mo. ROLLBACK/COMMIT theo ngữ canh truoc khi test.', 1;
IF OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.PHUCAPNHANVIEN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.BANGLUONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.sp_GhiNhanChamCong', N'P') IS NULL
   OR OBJECT_ID(N'dbo.trg_ChamCong_KiemTraGio', N'TR') IS NULL
   OR OBJECT_ID(N'dbo.trg_ChamCong_KiemTraNhanVien', N'TR') IS NULL
   OR OBJECT_ID(N'dbo.fn_TongPhuCap', N'FN') IS NULL
   OR OBJECT_ID(N'dbo.vw_TongHopChamCongThang', N'V') IS NULL
    THROW 51003, N'Thieu mot hoac nhieu doi tuong SQL can test trong database QuanLyNhanSuTienLuong.', 1;

IF OBJECT_ID('tempdb..#TV2_CTX') IS NOT NULL DROP TABLE #TV2_CTX;
CREATE TABLE #TV2_CTX
(
    MaNV INT NOT NULL,
    NgayTest DATE NOT NULL,
    ThangTest INT NOT NULL,
    NamTest INT NOT NULL
);

DECLARE @MaNV_B INT, @NgayTest DATE;
SELECT @MaNV_B = MaNV
FROM dbo.NHANVIEN
WHERE Email = 'gui-b@example.invalid' AND TrangThai = N'DANG_LAM_VIEC';
IF @MaNV_B IS NULL
    THROW 51004, N'Khong tim thay nhan vien QA B dang lam viec. Dung test.', 1;

;WITH ds AS
(
    SELECT TOP (180)
        DATEADD(DAY, -CONVERT(INT, ROW_NUMBER() OVER (ORDER BY (SELECT NULL))), CONVERT(DATE, GETDATE())) AS Ngay
    FROM sys.all_objects
)
SELECT TOP (1) @NgayTest = ds.Ngay
FROM ds
JOIN dbo.NHANVIEN nv ON nv.MaNV = @MaNV_B
WHERE ds.Ngay >= nv.NgayVaoLam
  AND NOT EXISTS
      (SELECT 1 FROM dbo.CHAMCONG cc
       WHERE cc.MaNV = @MaNV_B AND cc.NgayChamCong = ds.Ngay)
  AND NOT EXISTS
      (SELECT 1 FROM dbo.BANGLUONG bl
       WHERE bl.Thang = MONTH(ds.Ngay) AND bl.Nam = YEAR(ds.Ngay)
         AND bl.TrangThai = 'DA_CHOT')
ORDER BY ds.Ngay DESC;

IF @NgayTest IS NULL
    THROW 51005, N'Khong tim thay ngay QA chua cham cong trong ky chua chot. Dung test.', 1;
INSERT INTO #TV2_CTX VALUES (@MaNV_B, @NgayTest, MONTH(@NgayTest), YEAR(@NgayTest));
SELECT N'B0 - SAN SANG' AS TinhTrang, DB_NAME() AS DatabaseDangDung,
       MaNV, NgayTest, ThangTest, NamTest
FROM #TV2_CTX;
PRINT N'DA CHUAN BI: Bay gio chon va chay lan luot tung test TC01...';
GO
```

---

## B1 — TÙY CHỌN: MỞ MÃ NGUỒN SP, TRIGGER, FUNCTION VÀ VIEW

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **CÔ HỎI** | 'Em mở định nghĩa SQL của đối tượng cho cô xem được không?' |
| **CÁCH LÀM** | Bôi đen toàn bộ câu SELECT dưới đây đến GO rồi nhấn F5. |
| **THAY ĐỔI** | CHỈ ĐỌC, không thêm/sửa/xóa dữ liệu. |
| **CẦN THẤY** | Cột DinhNghiaSQL chứa CREATE PROCEDURE, CREATE TRIGGER, CREATE FUNCTION và CREATE VIEW thực tế. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong'
    THROW 51009, N'SAI DATABASE - B1 phai chay tren QA.', 1;
SELECT v.TenDoiTuong,
       OBJECT_DEFINITION(OBJECT_ID(v.TenDoiTuong)) AS DinhNghiaSQL
FROM (VALUES
 (N'dbo.sp_GhiNhanChamCong'),
 (N'dbo.trg_ChamCong_KiemTraGio'),
 (N'dbo.trg_ChamCong_KiemTraNhanVien'),
 (N'dbo.fn_TongPhuCap'),
 (N'dbo.vw_TongHopChamCongThang')
) v(TenDoiTuong);
GO
```

---

## TC01 — Stored Procedure ghi chấm công hợp lệ, trả về OUTPUT

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 |
| **CÔ HỎI** | Thủ tục ghi chấm công như thế nào? Mã chấm công được trả về ra sao? |
| **LỆNH SẼ CHẠY** | EXEC dbo.sp_GhiNhanChamCong; đọc tham số @MaChamCong OUTPUT. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Thêm tạm 1 bản ghi chấm công nhân viên B, sau đó ROLLBACK. |
| **KẾT QUẢ MONG ĐỢI** | Trước rollback có mã công > 0; sau rollback không còn bản ghi. |
| **CÁCH TRẢ LỜI CÔ** | SP kiểm tra hợp lệ, INSERT dữ liệu rồi dùng SCOPE_IDENTITY() để trả mã mới qua OUTPUT. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51101, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV01 INT = (SELECT MaNV FROM #TV2_CTX),
        @D01 DATE = (SELECT NgayTest FROM #TV2_CTX),
        @ID01 INT = NULL;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV = @NV01, @NgayChamCong = @D01,
        @GioVao = '08:00', @GioRa = '17:00',
        @TrangThai = N'CO_MAT', @GhiChu = N'TV2 TC01 SP hop le',
        @MaChamCong = @ID01 OUTPUT;
    IF @ID01 IS NULL OR NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaChamCong=@ID01)
        THROW 51201, N'SP khong tra OUTPUT hoac khong tao dong.', 1;
    SELECT N'TC01 PASS - SP tao duoc ban ghi (TRUOC ROLLBACK)' AS KetQua,
           @ID01 AS MaChamCongOutput, cc.*
    FROM dbo.CHAMCONG cc WHERE cc.MaChamCong = @ID01;
    ROLLBACK TRANSACTION;
    SELECT N'TC01 SAU ROLLBACK - phai bang 0' AS KiemTra,
           COUNT(*) AS SoDongConLai
    FROM dbo.CHAMCONG WHERE MaNV = @NV01 AND NgayChamCong = @D01;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC01 FAIL - SP hop le nhung bi loi' AS KetQua, ERROR_MESSAGE() AS Loi;
END CATCH;
GO
```

---

## TC02 — Stored Procedure từ chối chấm công trùng ngày

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | Một nhân viên có được ghi hai ngày công trong cùng một ngày không? |
| **LỆNH SẼ CHẠY** | Gọi SP hai lần cùng MaNV và NgayChamCong. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Lần 1 thêm tạm; lần 2 bị chặn; ROLLBACK cả lần 1. |
| **KẾT QUẢ MONG ĐỢI** | Thông báo trùng ngày; sau rollback còn 0 dòng. |
| **CÁCH TRẢ LỜI CÔ** | SP có kiểm tra trùng trước INSERT; UNIQUE trong CSDL bảo vệ thêm nếu ghi trực tiếp. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51102, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV02 INT = (SELECT MaNV FROM #TV2_CTX), @D02 DATE = (SELECT NgayTest FROM #TV2_CTX),
        @ID02 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV02, @NgayChamCong=@D02,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC02 dong 1', @MaChamCong=@ID02 OUTPUT;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV02, @NgayChamCong=@D02,
         @GioVao='09:00', @GioRa='18:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC02 dong 2', @MaChamCong=@ID02 OUTPUT;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC02 FAIL - SP khong chan trung ngay' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC02 - SP trung ngay' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
SELECT N'TC02 DOI CHIEU - phai 0' AS KiemTra, COUNT(*) AS SoDongConLai
FROM dbo.CHAMCONG WHERE MaNV=@NV02 AND NgayChamCong=@D02;
GO
```

---

## TC03 — Stored Procedure từ chối ngày trong tương lai

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | Nếu nhập ngày chấm công là ngày mai thì sao? |
| **LỆNH SẼ CHẠY** | EXEC SP với ngày mai. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công; có khởi tạo và hoàn tác transaction kiểm thử. |
| **KẾT QUẢ MONG ĐỢI** | SP từ chối trước khi INSERT. |
| **CÁCH TRẢ LỜI CÔ** | SP kiểm tra @NgayChamCong không được lớn hơn ngày hiện tại; TC19 kiểm tra CHECK tại CSDL. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51103, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV03 INT=(SELECT MaNV FROM #TV2_CTX), @ID03 INT,
        @NgayTuongLai03 DATE = DATEADD(DAY, 1, CONVERT(DATE, GETDATE()));
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV03,
         @NgayChamCong=@NgayTuongLai03,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID03 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC03 FAIL - SP cho phep ngay tuong lai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC03 - SP ngay tuong lai' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
```

---

## TC04 — Stored Procedure từ chối giờ ra không hợp lệ

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | Nếu giờ ra bằng hoặc nhỏ hơn giờ vào thì sao? |
| **LỆNH SẼ CHẠY** | EXEC SP với GioVao=17:00, GioRa=08:00. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | SP báo giờ ra phải lớn hơn giờ vào. |
| **CÁCH TRẢ LỜI CÔ** | Đây là kiểm tra của SP, không phải bằng chứng Trigger đã chạy; muốn xem Trigger dùng TC08/09. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51104, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV04 INT=(SELECT MaNV FROM #TV2_CTX), @D04 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID04 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV04, @NgayChamCong=@D04,
         @GioVao='17:00', @GioRa='08:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID04 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC04 FAIL - SP nhan gio sai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC04 - SP GioRa <= GioVao' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
```

---

## TC05 — Stored Procedure từ chối trạng thái sai

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | Nếu nhập trạng thái NGHI_PHEP thì sao? |
| **LỆNH SẼ CHẠY** | EXEC SP với TrangThai=NGHI_PHEP. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | SP báo trạng thái chấm công không hợp lệ. |
| **CÁCH TRẢ LỜI CÔ** | SP chỉ cho phép CO_MAT, DI_TRE, VE_SOM, VANG; CHECK CSDL được thử riêng ở TC20. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51105, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV05 INT=(SELECT MaNV FROM #TV2_CTX), @D05 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID05 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV05, @NgayChamCong=@D05,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'NGHI_PHEP',
         @MaChamCong=@ID05 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC05 FAIL - SP nhan trang thai la' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC05 - SP trang thai khong hop le' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
```

---

## TC06 — Stored Procedure từ chối nhân viên không tồn tại

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | Nếu MaNV không có trong NHANVIEN thì sao? |
| **LỆNH SẼ CHẠY** | EXEC SP với MaNV không tồn tại. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | SP báo nhân viên không tồn tại. |
| **CÁCH TRẢ LỜI CÔ** | SP kiểm tra trước khi INSERT; khóa ngoại được kiểm tra độc lập ở TC18. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51106, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @D06 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID06 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=99999999, @NgayChamCong=@D06,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID06 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC06 FAIL - SP cho nhan vien khong ton tai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC06 - SP nhan vien khong ton tai' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
```

---

## TC07 — Stored Procedure từ chối nhân viên đã nghỉ việc

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | SP có ngăn chấm công cho nhân viên nghỉ việc không? |
| **LỆNH SẼ CHẠY** | Đổi tạm nhân viên B sang NGHI_VIEC rồi EXEC SP. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | UPDATE trạng thái nhân viên B TẠM THỜI; sau đó ROLLBACK. |
| **KẾT QUẢ MONG ĐỢI** | SP từ chối; nhân viên B trở về DANG_LAM_VIEC. |
| **CÁCH TRẢ LỜI CÔ** | SP đọc trạng thái NHANVIEN, chỉ cho phép DANG_LAM_VIEC. |
| **LƯU Ý** | Chỉ chạy TRỌN KHỐI, tuyệt đối không tách riêng lệnh UPDATE. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51107, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV07 INT=(SELECT MaNV FROM #TV2_CTX), @D07 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID07 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC' WHERE MaNV=@NV07;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV07, @NgayChamCong=@D07,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID07 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC07 FAIL - SP cho NV nghi viec' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC07 - SP nhan vien nghi viec' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
SELECT N'TC07 KIEM TRA NV SAU ROLLBACK' AS KiemTra, MaNV, TrangThai
FROM dbo.NHANVIEN WHERE MaNV=@NV07;
GO
```

---

## TC08 — Trigger kiểm tra giờ khi INSERT trực tiếp

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Trigger có chạy khi INSERT dữ liệu bỏ qua SP không? |
| **LỆNH SẼ CHẠY** | INSERT trực tiếp giờ vào 17:00, giờ ra 08:00. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Trigger tự hủy toàn transaction; không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | Thông báo giờ ra sai; đối chiếu sau đó có 0 bản ghi. |
| **CÁCH TRẢ LỜI CÔ** | Trigger AFTER INSERT đọc bảng inserted, phát hiện GioRa <= GioVao rồi ROLLBACK. |
| **LƯU Ý** | SQL Server có thể in thêm lỗi 3609; CHẠY CẢ BATCH đối chiếu sau GO. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51108, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV08 INT=(SELECT MaNV FROM #TV2_CTX), @D08 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV08,@D08,'17:00','08:00',N'CO_MAT',N'TV2 TC08 trigger INSERT gio');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC08 FAIL - trigger khong chan gio sai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC08 - KIEM TRA Messages: loi GioRa <= GioVao la mong doi' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51180, N'Chay B0 truoc.', 1;
SELECT N'TC08 DOI CHIEU: 0 DONG' AS KiemTra, COUNT(*) AS SoDongConLai
FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest;
GO
```

---

## TC09 — Trigger kiểm tra giờ khi UPDATE trực tiếp

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Trigger có chạy khi UPDATE một bản ghi đang hợp lệ không? |
| **LỆNH SẼ CHẠY** | Ghi 1 công hợp lệ tạm rồi UPDATE GioRa thành 07:30. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Trigger ROLLBACK cả bản ghi thêm tạm và câu UPDATE. |
| **KẾT QUẢ MONG ĐỢI** | Báo giờ ra sai; hậu kiểm còn 0 công. |
| **CÁCH TRẢ LỜI CÔ** | AFTER INSERT, UPDATE nghĩa là Trigger bảo vệ cả ghi mới lẫn sửa. |
| **LƯU Ý** | CHẠY CẢ BATCH đối chiếu sau GO. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51109, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV09 INT=(SELECT MaNV FROM #TV2_CTX), @D09 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID09 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV09, @NgayChamCong=@D09,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC09 seed', @MaChamCong=@ID09 OUTPUT;
    UPDATE dbo.CHAMCONG SET GioRa='07:30' WHERE MaChamCong=@ID09;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC09 FAIL - trigger khong chan UPDATE gio sai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC09 - KIEM TRA Messages: loi gio khi UPDATE la mong doi' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51190, N'Chay B0 truoc.', 1;
SELECT N'TC09 DOI CHIEU: 0 DONG' AS KiemTra, COUNT(*) AS SoDongConLai
FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest;
GO
```

---

## TC10 — Trigger chặn nhân viên nghỉ việc khi INSERT

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Nếu INSERT trực tiếp công cho nhân viên nghỉ việc thì sao? |
| **LỆNH SẼ CHẠY** | Tạm chuyển B thành NGHI_VIEC rồi INSERT trực tiếp công. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Trigger hủy INSERT và hoàn tác luôn UPDATE trạng thái B. |
| **KẾT QUẢ MONG ĐỢI** | Báo không được chấm công nhân viên nghỉ việc; B vẫn đang làm việc. |
| **CÁCH TRẢ LỜI CÔ** | Trigger JOIN inserted với NHANVIEN để kiểm tra trạng thái, không phụ thuộc SP. |
| **LƯU Ý** | Chạy cả phần đối chiếu sau GO, không chạy riêng UPDATE nhân viên. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51110, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV10 INT=(SELECT MaNV FROM #TV2_CTX), @D10 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC' WHERE MaNV=@NV10;
    INSERT INTO dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV10,@D10,'08:00','17:00',N'CO_MAT',N'TV2 TC10 trigger NV');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC10 FAIL - trigger cho NV nghi viec' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC10 - KIEM TRA Messages: khong duoc cham cong NV nghi viec' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51111, N'Chay B0 truoc.', 1;
SELECT N'TC10 DOI CHIEU' AS KiemTra, n.MaNV, n.TrangThai,
       (SELECT COUNT(*) FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
        WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest) AS SoDongTest
FROM dbo.NHANVIEN n JOIN #TV2_CTX c ON n.MaNV=c.MaNV;
GO
```

---

## TC11 — Trigger chặn nhân viên nghỉ việc khi UPDATE

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Trigger có chặn UPDATE bản ghi công của nhân viên nghỉ việc không? |
| **LỆNH SẼ CHẠY** | Tạo tạm 1 công, chuyển B sang NGHI_VIEC, UPDATE GhiChu công. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Trigger ROLLBACK cả công tạm và cập nhật trạng thái B. |
| **KẾT QUẢ MONG ĐỢI** | Báo nhân viên nghỉ việc; công 0 dòng; B trở về đang làm việc. |
| **CÁCH TRẢ LỜI CÔ** | Bảng inserted chứa dữ liệu MỚI của câu UPDATE; Trigger kiểm tra với NHANVIEN. |
| **LƯU Ý** | Chạy cả phần đối chiếu sau GO, không tách riêng câu UPDATE. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51112, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV11 INT=(SELECT MaNV FROM #TV2_CTX), @D11 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID11 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV11, @NgayChamCong=@D11,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC11 seed', @MaChamCong=@ID11 OUTPUT;
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC' WHERE MaNV=@NV11;
    UPDATE dbo.CHAMCONG SET GhiChu=N'TV2 TC11 force UPDATE' WHERE MaChamCong=@ID11;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC11 FAIL - trigger cho UPDATE cong cua NV nghi viec' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC11 - KIEM TRA Messages: khong duoc cham cong NV nghi viec' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51113, N'Chay B0 truoc.', 1;
SELECT N'TC11 DOI CHIEU' AS KiemTra, n.MaNV, n.TrangThai,
       (SELECT COUNT(*) FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
        WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest) AS SoDongTest
FROM dbo.NHANVIEN n JOIN #TV2_CTX c ON n.MaNV=c.MaNV;
GO
```

---

## TC12 — Function fn_TongPhuCap trả về số tiền hiện tại

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 / phần TV2 phối hợp TV3 |
| **CÔ HỎI** | Function tính tổng phụ cấp được gọi như thế nào? |
| **LỆNH SẼ CHẠY** | SELECT dbo.fn_TongPhuCap(MaNV,Thang,Nam), so sánh SUM(SoTien). |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | CHỈ ĐỌC. |
| **KẾT QUẢ MONG ĐỢI** | Kết quả Function bằng SUM, không có khoản thì trả 0. |
| **CÁCH TRẢ LỜI CÔ** | Function là hàm được gọi tường minh bằng SELECT hoặc từ SP tính lương; không phải Trigger. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51114, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC12 - HAM TONG PHU CAP (DU LIEU HIEN CO)' AS KiemTra,
       c.MaNV, c.ThangTest AS Thang, c.NamTest AS Nam,
       dbo.fn_TongPhuCap(c.MaNV,c.ThangTest,c.NamTest) AS KetQuaFunction,
       (SELECT ISNULL(SUM(pc.SoTien),0)
        FROM dbo.PHUCAPNHANVIEN pc
        WHERE pc.MaNV=c.MaNV AND pc.Thang=c.ThangTest AND pc.Nam=c.NamTest) AS KetQuaSUM
FROM #TV2_CTX c;
SELECT N'TC12 - NHAN VIEN KHONG CO PHU CAP' AS KiemTra,
       dbo.fn_TongPhuCap(99999999,1,2020) AS MongDoiBang0;
GO
```

---

## TC13 — Function cộng hai khoản phụ cấp

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 / phần TV2 phối hợp TV3 |
| **CÔ HỎI** | Em chứng minh Function thực sự cộng các khoản bằng cách nào? |
| **LỆNH SẼ CHẠY** | INSERT tạm 100.000 và 200.000, gọi Function, ROLLBACK. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Thêm tạm 2 dòng PHUCAPNHANVIEN; không lưu sau rollback. |
| **KẾT QUẢ MONG ĐỢI** | Tổng trong transaction tăng 300.000; sau rollback trở về như cũ. |
| **CÁCH TRẢ LỜI CÔ** | Function dùng SUM(SoTien) theo nhân viên, tháng, năm. |
| **LƯU Ý** | Không chạy cùng lúc người khác đang demo tính lương/phụ cấp. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51115, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV13 INT=(SELECT MaNV FROM #TV2_CTX),
        @Thang13 INT=(SELECT ThangTest FROM #TV2_CTX),
        @Nam13 INT=(SELECT NamTest FROM #TV2_CTX),
        @Truoc13 DECIMAL(18,2), @Sau13 DECIMAL(18,2);
SET @Truoc13=dbo.fn_TongPhuCap(@NV13,@Thang13,@Nam13);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien,GhiChu)
    VALUES (@NV13,@Thang13,@Nam13,N'TV2 Test PC 1',100000,N'TC13 rollback'),
           (@NV13,@Thang13,@Nam13,N'TV2 Test PC 2',200000,N'TC13 rollback');
    SET @Sau13=dbo.fn_TongPhuCap(@NV13,@Thang13,@Nam13);
    SELECT N'TC13 - DELTA PHAI BANG 300000' AS KiemTra,
           @Truoc13 AS TongTruoc, @Sau13 AS TongTrongTransaction,
           @Sau13-@Truoc13 AS PhuCapMoi;
    ROLLBACK TRANSACTION;
    SELECT N'TC13 - SAU ROLLBACK PHAI BANG TongTruoc' AS KiemTra,
           dbo.fn_TongPhuCap(@NV13,@Thang13,@Nam13) AS TongSauRollback,
           @Truoc13 AS TongTruoc;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC13 FAIL / BI CHAN BOI OBJECT KHAC' AS KetQua, ERROR_MESSAGE() AS Loi;
END CATCH;
GO
```

---

## TC14 — View tổng hợp công theo tháng (chỉ đọc)

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 |
| **CÔ HỎI** | View tháng trả về những chỉ tiêu gì? |
| **LỆNH SẼ CHẠY** | SELECT trên vw_TongHopChamCongThang của nhân viên B. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | CHỈ ĐỌC. |
| **KẾT QUẢ MONG ĐỢI** | Nếu B chưa có công thì không có dòng, KHÔNG phải một dòng 0 công. |
| **CÁCH TRẢ LỜI CÔ** | View GROUP BY nhân viên/tháng/năm, tổng hợp công, đi trễ, về sớm, vắng và giờ làm. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51116, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC14 - VIEW TONG HOP THANG (NEU CHUA CO CONG SE 0 DONG)' AS KiemTra,
       v.*
FROM dbo.vw_TongHopChamCongThang v
JOIN #TV2_CTX c ON v.MaNV=c.MaNV AND v.Thang=c.ThangTest AND v.Nam=c.NamTest;
GO
```

---

## TC15 — Kiểm tra định nghĩa Covering Index

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 8 |
| **CÔ HỎI** | Index của em tên gì và gồm những cột nào? |
| **LỆNH SẼ CHẠY** | Đọc metadata sys.indexes, sys.index_columns, sys.columns. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | CHỈ ĐỌC thông tin cấu trúc Index. |
| **KẾT QUẢ MONG ĐỢI** | Hai cột khóa MaNV rồi NgayChamCong; INCLUDE GioVao, GioRa, TrangThai. |
| **CÁCH TRẢ LỜI CÔ** | Cột INCLUDE hỗ trợ truy vấn bao phủ và hạn chế Key Lookup; đây chưa phải benchmark. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51117, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC15 - COVERING INDEX TV2' AS KiemTra,
       i.name AS TenIndex, i.type_desc,
       ic.key_ordinal AS ViTriKhoa,
       col.name AS TenCot,
       ic.is_included_column AS LaCotINCLUDE
FROM sys.indexes i
JOIN sys.index_columns ic ON ic.object_id=i.object_id AND ic.index_id=i.index_id
JOIN sys.columns col ON col.object_id=ic.object_id AND col.column_id=ic.column_id
WHERE i.object_id=OBJECT_ID(N'dbo.CHAMCONG')
  AND i.name=N'IX_CHAMCONG_MaNV_Ngay'
ORDER BY ic.key_ordinal, ic.index_column_id;
GO
```

---

## TC16 — Kiểm tra nhanh dữ liệu sau từng ca

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Cuối mỗi phần demo |
| **CÔ HỎI** | Có dữ liệu test nào bị lưu lại không? |
| **LỆNH SẼ CHẠY** | SELECT trạng thái nhân viên B, COUNT công tại ngày test, @@TRANCOUNT. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | CHỈ ĐỌC. |
| **KẾT QUẢ MONG ĐỢI** | Nhân viên B đang làm việc; 0 bản ghi ngày test; 0 transaction còn mở. |
| **CÁCH TRẢ LỜI CÔ** | Dùng để phát hiện dữ liệu tạm chưa được hoàn tác. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51118, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC16 - CHECK CUOI' AS KiemTra,
       c.MaNV, c.NgayTest,
       n.TrangThai AS TrangThaiNhanVien,
       (SELECT COUNT(*) FROM dbo.CHAMCONG cc
        WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest) AS BanGhiChamCongConLai,
       @@TRANCOUNT AS SoTransactionDangMo
FROM #TV2_CTX c
JOIN dbo.NHANVIEN n ON n.MaNV=c.MaNV;
GO
```

---

## TC17 — UNIQUE tự chặn ghi trùng, không đi qua SP

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Nếu người ta INSERT trực tiếp bỏ qua SP, có chấm công trùng được không? |
| **LỆNH SẼ CHẠY** | INSERT trực tiếp 2 dòng cùng MaNV và NgayChamCong. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Vi phạm UNIQUE; test hoàn tác. |
| **KẾT QUẢ MONG ĐỢI** | Lỗi SQL Server 2627 hoặc 2601; không có công bị lưu lại. |
| **CÁCH TRẢ LỜI CÔ** | UQ_CHAMCONG_MaNV_Ngay bảo vệ toàn vẹn ngay trong SQL Server, khác TC02 của SP. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51119, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV17 INT=(SELECT MaNV FROM #TV2_CTX), @D17 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT dbo.CHAMCONG (MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV17,@D17,'08:00','17:00',N'CO_MAT',N'TC17 A'),
           (@NV17,@D17,'09:00','18:00',N'DI_TRE',N'TC17 B');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC17 FAIL - UNIQUE khong chan trung ngay' AS KetQua;
END TRY
BEGIN CATCH
    DECLARE @Err17 INT=ERROR_NUMBER(), @Msg17 NVARCHAR(4000)=ERROR_MESSAGE();
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT CASE WHEN @Err17 IN (2601,2627) THEN N'TC17 PASS - UNIQUE chan trung'
                ELSE N'TC17 CAN KIEM TRA - loi khac' END AS KetQua,
           @Err17 AS MaLoi, @Msg17 AS ThongBao;
END CATCH;
SELECT N'TC17 SAU ROLLBACK - PHAI 0' AS KiemTra, COUNT(*) AS SoDongTest
FROM dbo.CHAMCONG WHERE MaNV=@NV17 AND NgayChamCong=@D17;
GO
```

---

## TC18 — FOREIGN KEY chặn mã nhân viên không tồn tại

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Khóa ngoại có thật sự ngăn chấm công cho mã nhân viên không có không? |
| **LỆNH SẼ CHẠY** | INSERT trực tiếp với MaNV không tồn tại. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | Lỗi 547, nhắc FK_CHAMCONG_NHANVIEN. |
| **CÁCH TRẢ LỜI CÔ** | FOREIGN KEY bảo đảm bản ghi CHAMCONG tham chiếu NHANVIEN hợp lệ. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51120, N'Chay B0 truoc va dam bao dung QA.', 1;
IF EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=-1)
    THROW 51121, N'MaNV=-1 dang ton tai, khong the test FK an toan.', 1;
DECLARE @D18 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT dbo.CHAMCONG (MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (-1,@D18,'08:00','17:00',N'CO_MAT',N'TC18 FK');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC18 FAIL - FK khong chan MaNV khong ton tai' AS KetQua;
END TRY
BEGIN CATCH
    DECLARE @Err18 INT=ERROR_NUMBER(), @Msg18 NVARCHAR(4000)=ERROR_MESSAGE();
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT CASE WHEN @Err18=547 AND @Msg18 LIKE N'%FK_CHAMCONG_NHANVIEN%'
                THEN N'TC18 PASS - FK chan MaNV khong ton tai'
                ELSE N'TC18 CAN KIEM TRA - loi khac' END AS KetQua,
           @Err18 AS MaLoi, @Msg18 AS ThongBao;
END CATCH;
GO
```

---

## TC19 — CHECK ngăn ngày tương lai, không đi qua SP

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Nếu INSERT trực tiếp một ngày trong tương lai thì sao? |
| **LỆNH SẼ CHẠY** | INSERT trực tiếp với NgayChamCong là ngày mai. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | Lỗi 547, nhắc CHK_CHAMCONG_Ngay. |
| **CÁCH TRẢ LỜI CÔ** | CHECK dùng GETDATE() và CAST(... AS DATE) để giới hạn ngày. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51122, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV19 INT=(SELECT MaNV FROM #TV2_CTX),
        @D19 DATE=DATEADD(DAY,1,CONVERT(DATE,GETDATE()));
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT dbo.CHAMCONG (MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV19,@D19,'08:00','17:00',N'CO_MAT',N'TC19 CHK ngay');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC19 FAIL - CHECK khong chan ngay tuong lai' AS KetQua;
END TRY
BEGIN CATCH
    DECLARE @Err19 INT=ERROR_NUMBER(), @Msg19 NVARCHAR(4000)=ERROR_MESSAGE();
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT CASE WHEN @Err19=547 AND @Msg19 LIKE N'%CHK_CHAMCONG_Ngay%'
                THEN N'TC19 PASS - CHECK ngay tuong lai'
                ELSE N'TC19 CAN KIEM TRA - loi khac' END AS KetQua,
           @Err19 AS MaLoi, @Msg19 AS ThongBao;
END CATCH;
GO
```

---

## TC20 — CHECK ngăn trạng thái không hợp lệ

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6 |
| **CÔ HỎI** | Nếu INSERT trực tiếp trạng thái NGHI_PHEP thì sao? |
| **LỆNH SẼ CHẠY** | INSERT trực tiếp TrangThai=NGHI_PHEP. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | Lỗi 547, nhắc CHK_CHAMCONG_TrangThai. |
| **CÁCH TRẢ LỜI CÔ** | CHECK giới hạn TrangThai thuộc đúng bốn giá trị được thiết kế. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51123, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV20 INT=(SELECT MaNV FROM #TV2_CTX), @D20 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT dbo.CHAMCONG (MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV20,@D20,'08:00','17:00',N'NGHI_PHEP',N'TC20 CHK trang thai');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC20 FAIL - CHECK chap nhan trang thai sai' AS KetQua;
END TRY
BEGIN CATCH
    DECLARE @Err20 INT=ERROR_NUMBER(), @Msg20 NVARCHAR(4000)=ERROR_MESSAGE();
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT CASE WHEN @Err20=547 AND @Msg20 LIKE N'%CHK_CHAMCONG_TrangThai%'
                THEN N'TC20 PASS - CHECK trang thai'
                ELSE N'TC20 CAN KIEM TRA - loi khac' END AS KetQua,
           @Err20 AS MaLoi, @Msg20 AS ThongBao;
END CATCH;
GO
```

---

## TC21 — SP kiểm tra ba tham số bắt buộc NULL

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 |
| **CÔ HỎI** | SP có cho phép MaNV, NgayChamCong hoặc GioVao bằng NULL không? |
| **LỆNH SẼ CHẠY** | EXEC SP 3 lần, lần lượt cho mỗi tham số bắt buộc bằng NULL. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu công. |
| **KẾT QUẢ MONG ĐỢI** | Ba thông báo lỗi ứng với mỗi tham số bị NULL. |
| **CÁCH TRẢ LỜI CÔ** | SP có 6 tham số đầu vào và 1 OUTPUT; GioRa, GhiChu là tùy chọn. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51124, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV21 INT=(SELECT MaNV FROM #TV2_CTX), @D21 DATE=(SELECT NgayTest FROM #TV2_CTX),
        @ID21 INT, @I21 INT=1, @Msg21 NVARCHAR(4000);
DECLARE @KQ21 TABLE (Truong NVARCHAR(40), KetQua NVARCHAR(70), ThongBao NVARCHAR(4000));
WHILE @I21<=3
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;
        IF @I21=1
            EXEC dbo.sp_GhiNhanChamCong @MaNV=NULL, @NgayChamCong=@D21, @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT', @MaChamCong=@ID21 OUTPUT;
        ELSE IF @I21=2
            EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV21, @NgayChamCong=NULL, @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT', @MaChamCong=@ID21 OUTPUT;
        ELSE
            EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV21, @NgayChamCong=@D21, @GioVao=NULL, @GioRa='17:00', @TrangThai=N'CO_MAT', @MaChamCong=@ID21 OUTPUT;
        IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
        INSERT @KQ21 VALUES (CASE @I21 WHEN 1 THEN N'MaNV NULL' WHEN 2 THEN N'NgayChamCong NULL' ELSE N'GioVao NULL' END,N'FAIL - SP khong chan NULL',N'');
    END TRY
    BEGIN CATCH
        SET @Msg21=ERROR_MESSAGE();
        IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
        INSERT @KQ21 VALUES
        (CASE @I21 WHEN 1 THEN N'MaNV NULL' WHEN 2 THEN N'NgayChamCong NULL' ELSE N'GioVao NULL' END,
         CASE WHEN (@I21=1 AND @Msg21 LIKE N'%Mã nhân viên%')
                   OR (@I21=2 AND @Msg21 LIKE N'%Ngày chấm công%')
                   OR (@I21=3 AND @Msg21 LIKE N'%Giờ vào%')
              THEN N'PASS - SP tu choi NULL' ELSE N'CAN KIEM TRA - loi khac' END,
         @Msg21);
    END CATCH;
    SET @I21+=1;
END;
SELECT N'TC21 - BA TRUONG NULL' AS Nhom, * FROM @KQ21;
GO
```

---

## TC22 — SP cho phép GioRa = NULL

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 |
| **CÔ HỎI** | Nhân viên chưa kết thúc ca, GioRa chưa có, có ghi công được không? |
| **LỆNH SẼ CHẠY** | EXEC SP với @GioRa=NULL. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | INSERT tạm một công, sau đó ROLLBACK. |
| **KẾT QUẢ MONG ĐỢI** | SP trả mã mới; dòng tạm có GioRa=NULL; sau đó không còn dòng. |
| **CÁCH TRẢ LỜI CÔ** | Trigger chỉ so sánh giờ khi GioRa IS NOT NULL. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51125, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV22 INT=(SELECT MaNV FROM #TV2_CTX), @D22 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID22 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV22, @NgayChamCong=@D22,
         @GioVao='08:00', @GioRa=NULL, @TrangThai=N'CO_MAT',
         @GhiChu=N'TC22 chua co gio ra', @MaChamCong=@ID22 OUTPUT;
    SELECT CASE WHEN @ID22 IS NOT NULL AND EXISTS
         (SELECT 1 FROM dbo.CHAMCONG WHERE MaChamCong=@ID22 AND GioRa IS NULL)
         THEN N'TC22 PASS - GioRa NULL hop le'
         ELSE N'TC22 FAIL - khong tim thay dong' END AS KetQua,
         @ID22 AS MaChamCong;
    ROLLBACK TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    SELECT N'TC22 FAIL - loi khong mong doi' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
```

---

## TC23 — View thay đổi khi có dữ liệu chấm công

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 |
| **CÔ HỎI** | View có cập nhật số ngày công khi thêm bản ghi mới không? |
| **LỆNH SẼ CHẠY** | Đọc View trước; ghi tạm 1 công qua SP; đọc View; rollback và đọc lại. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Thêm tạm 1 công CHAMCONG rồi ROLLBACK. |
| **KẾT QUẢ MONG ĐỢI** | Ngày công tăng 1 trong transaction; sau rollback trở lại giá trị ban đầu. |
| **CÁCH TRẢ LỜI CÔ** | View thường tính kết quả từ bảng nguồn, không phải bản sao cố định. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51126, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV23 INT=(SELECT MaNV FROM #TV2_CTX), @D23 DATE=(SELECT NgayTest FROM #TV2_CTX),
        @Thang23 INT=(SELECT ThangTest FROM #TV2_CTX), @Nam23 INT=(SELECT NamTest FROM #TV2_CTX),
        @ID23 INT, @Truoc23 INT=0, @Sau23 INT=0, @Rollback23 INT=0;
SELECT @Truoc23=SoNgayDiLam FROM dbo.vw_TongHopChamCongThang
WHERE MaNV=@NV23 AND Thang=@Thang23 AND Nam=@Nam23;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV23, @NgayChamCong=@D23,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TC23 view trong transaction', @MaChamCong=@ID23 OUTPUT;
    SELECT @Sau23=SoNgayDiLam FROM dbo.vw_TongHopChamCongThang
    WHERE MaNV=@NV23 AND Thang=@Thang23 AND Nam=@Nam23;
    ROLLBACK TRANSACTION;
    SELECT @Rollback23=SoNgayDiLam FROM dbo.vw_TongHopChamCongThang
    WHERE MaNV=@NV23 AND Thang=@Thang23 AND Nam=@Nam23;
    SELECT CASE WHEN @Sau23=@Truoc23+1 AND @Rollback23=@Truoc23
             THEN N'TC23 PASS - View thay doi theo du lieu nguon'
             ELSE N'TC23 CAN KIEM TRA' END AS KetQua,
           @Truoc23 AS SoNgayTruoc, @Sau23 AS SoNgayTrongTransaction,
           @Rollback23 AS SoNgaySauRollback;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    SELECT N'TC23 FAIL - loi khong mong doi' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
```

---

## TC24 — Đo logical reads trên dữ liệu QA hiện tại

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 8 |
| **CÔ HỎI** | Cách bật STATISTICS IO và Actual Execution Plan? |
| **LỆNH SẼ CHẠY** | Nhấn Ctrl+M trong SSMS, chạy SELECT với STATISTICS IO ON. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | CHỈ ĐỌC; không thêm/xóa Index. |
| **KẾT QUẢ MONG ĐỢI** | Messages có logical reads; Execution Plan do SQL Server lựa chọn. |
| **CÁCH TRẢ LỜI CÔ** | Đây KHÔNG phải phép đo 574→5 của bảng tạm 30.000 dòng; để chứng minh mốc đó dùng file benchmark riêng. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51127, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV24 INT=(SELECT MaNV FROM #TV2_CTX), @D24 DATE=(SELECT NgayTest FROM #TV2_CTX);
SET STATISTICS IO ON;
SELECT MaNV,NgayChamCong,GioVao,GioRa,TrangThai
FROM dbo.CHAMCONG
WHERE MaNV=@NV24
  AND NgayChamCong >= DATEFROMPARTS(YEAR(@D24),MONTH(@D24),1)
  AND NgayChamCong < DATEADD(MONTH,1,DATEFROMPARTS(YEAR(@D24),MONTH(@D24),1));
SET STATISTICS IO OFF;
PRINT N'TC24: xem Messages va Actual Execution Plan. Khong so sanh voi 574/5 tren dataset QA nho.';
GO
```

---

## TC25 — Kiểm tra cuối buổi để đảm bảo sạch dữ liệu

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Kết thúc demo |
| **CÔ HỎI** | Em có bảo đảm các thao tác thử không để lại dữ liệu không? |
| **LỆNH SẼ CHẠY** | SELECT trạng thái B, đếm công ngày test, kiểm tra @@TRANCOUNT. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | CHỈ ĐỌC. |
| **KẾT QUẢ MONG ĐỢI** | B vẫn DANG_LAM_VIEC; 0 công ngày test; 0 transaction còn mở. |
| **CÁCH TRẢ LỜI CÔ** | Nếu kết quả không khớp, dừng các ca ghi và kiểm tra nguyên nhân. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51128, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC25 - DOI CHIEU CUOI CUNG' AS KiemTra,
       DB_NAME() AS DatabaseDangDung,
       c.MaNV, n.TrangThai AS TrangThaiNV,
       c.NgayTest,
       (SELECT COUNT(*) FROM dbo.CHAMCONG x
        WHERE x.MaNV=c.MaNV AND x.NgayChamCong=c.NgayTest) AS SoDongConLai,
       @@TRANCOUNT AS SoTransactionDangMo
FROM #TV2_CTX c JOIN dbo.NHANVIEN n ON n.MaNV=c.MaNV;
GO
```

---


---

## TC26 — Tính nguyên tử (Atomicity - All-or-Nothing) khi nhập chấm công hàng loạt

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 / Giao dịch Chấm công (liên kết phương thức `ChamCongService.nhapChamCongTheoLo`) |
| **CÔ HỎI** | Nếu nhập danh sách chấm công cho nhiều người, các người đầu hợp lệ nhưng người cuối bị lỗi thì CSDL xử lý thế nào? Chứng minh tính nguyên tử (Atomicity) của Transaction. |
| **LỆNH SẼ CHẠY** | Mở Transaction, gọi `sp_GhiNhanChamCong` cho 2 người hợp lệ (1008, 1009) và 1 người đã nghỉ việc (1029). |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Giao dịch tự động hoàn tác toàn bộ (All-or-Nothing), không lưu công nào. |
| **KẾT QUẢ MONG ĐỢI** | Bị bắt lỗi ở dòng thứ 3; số dòng công lưu lại của cả 3 nhân viên bằng 0. |
| **CÁCH TRẢ LỜI CÔ** | Trong phương thức `nhapChamCongTheoLo()` ở Java, ứng dụng tắt auto-commit (`conn.setAutoCommit(false)`). CSDL thực thi trong 1 transaction duy nhất; chỉ cần 1 dòng vi phạm, toàn bộ tiến trình sẽ ROLLBACK, không bao giờ lưu dữ liệu chắp vá nửa vời. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51129, N'Chay B0 truoc va dam bao dung QuanLyNhanSuTienLuong.', 1;

DECLARE @D26 DATE = (SELECT NgayTest FROM #TV2_CTX), @Out26 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    
    -- Dòng 1: NV 1008 hợp lệ
    EXEC dbo.sp_GhiNhanChamCong @MaNV = 1008, @NgayChamCong = @D26,
         @GioVao = '08:00', @GioRa = '17:00', @TrangThai = N'CO_MAT',
         @GhiChu = N'TC26 Batch 1', @MaChamCong = @Out26 OUTPUT;

    -- Dòng 2: NV 1009 hợp lệ
    EXEC dbo.sp_GhiNhanChamCong @MaNV = 1009, @NgayChamCong = @D26,
         @GioVao = '08:30', @GioRa = '17:30', @TrangThai = N'DI_TRE',
         @GhiChu = N'TC26 Batch 2', @MaChamCong = @Out26 OUTPUT;

    -- Dòng 3: NV 1029 (Hồ Viết Thắng - Đã nghỉ việc) -> Gây lỗi
    EXEC dbo.sp_GhiNhanChamCong @MaNV = 1029, @NgayChamCong = @D26,
         @GioVao = '08:00', @GioRa = '17:00', @TrangThai = N'CO_MAT',
         @GhiChu = N'TC26 Batch 3', @MaChamCong = @Out26 OUTPUT;

    COMMIT TRANSACTION;
    SELECT N'TC26 FAIL: Không chặn được nhân viên nghỉ việc' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 OR XACT_STATE() <> 0 
        ROLLBACK TRANSACTION;

    SELECT 
        N'TC26 PASS - Atomicity All-or-Nothing' AS KetQua,
        ERROR_MESSAGE() AS ThongBaoLoi,
        (SELECT COUNT(*) FROM dbo.CHAMCONG 
         WHERE NgayChamCong = @D26 AND MaNV IN (1008, 1009, 1029)) AS SoCongLuuLaiSauRollback;
END CATCH;
GO
```

---

## TC27 — Điểm lưu giao dịch một phần (SAVE TRANSACTION / Savepoints)

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 7 / Kỹ thuật nâng cao Transaction |
| **CÔ HỎI** | Nếu muốn nhập công theo lô, nhưng gặp dòng lỗi thì chỉ bỏ qua người đó mà vẫn lưu công cho những người hợp lệ trước đó thì làm thế nào? |
| **LỆNH SẼ CHẠY** | Dùng `SAVE TRANSACTION SavePoint_NV1008` và `SAVE TRANSACTION SavePoint_NV1029`. Dòng lỗi chỉ rollback về Savepoint tương ứng. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Trong transaction, dòng hợp lệ vẫn tồn tại, dòng lỗi bị hủy. Cuối bài test ROLLBACK toàn cục để giữ sạch DB. |
| **KẾT QUẢ MONG ĐỢI** | `SoCongNV1008 = 1` và `SoCongNV1029 = 0`. |
| **CÁCH TRẢ LỜI CÔ** | Dùng `SAVE TRANSACTION <TenSavepoint>` để tạo các mốc lưu tạm. Khi có lỗi cục bộ, khối CATCH chỉ ROLLBACK về điểm lưu đó (`ROLLBACK TRANSACTION <TenSavepoint>`) mà không hủy bỏ các bản ghi đã thực hiện thành công. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51130, N'Chay B0 truoc va dam bao dung QuanLyNhanSuTienLuong.', 1;

DECLARE @D27 DATE = (SELECT NgayTest FROM #TV2_CTX), @Out27 INT;
BEGIN TRANSACTION;

-- Dòng 1: NV 1008 (Hợp lệ)
SAVE TRANSACTION SavePoint_NV1008;
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong @MaNV = 1008, @NgayChamCong = @D27,
         @GioVao = '08:00', @GioRa = '17:00', @TrangThai = N'CO_MAT',
         @GhiChu = N'TC27 Savepoint 1008', @MaChamCong = @Out27 OUTPUT;
END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION SavePoint_NV1008;
END CATCH;

-- Dòng 2: NV 1029 (Đã nghỉ việc -> Gây lỗi)
SAVE TRANSACTION SavePoint_NV1029;
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong @MaNV = 1029, @NgayChamCong = @D27,
         @GioVao = '08:00', @GioRa = '17:00', @TrangThai = N'CO_MAT',
         @GhiChu = N'TC27 Savepoint 1029', @MaChamCong = @Out27 OUTPUT;
END TRY
BEGIN CATCH
    -- Chỉ hoàn tác về Savepoint của dòng lỗi, giữ nguyên dòng 1008
    ROLLBACK TRANSACTION SavePoint_NV1029;
END CATCH;

-- Kiểm tra trong transaction
SELECT 
    N'TC27 PASS - Partial Rollback thành công' AS KetQua,
    (SELECT COUNT(*) FROM dbo.CHAMCONG WHERE NgayChamCong = @D27 AND MaNV = 1008) AS SoCongNV1008_GiuLai,
    (SELECT COUNT(*) FROM dbo.CHAMCONG WHERE NgayChamCong = @D27 AND MaNV = 1029) AS SoCongNV1029_BiHuy;

-- Dọn dẹp sạch sẽ sau test
ROLLBACK TRANSACTION;
GO
```

---

## TC28 — Phân tích XACT_STATE() = -1 (Doomed Transaction) khi Trigger tự ROLLBACK

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 / Cơ chế phối hợp giữa Trigger và Transaction |
| **CÔ HỎI** | Khi một Trigger phát lệnh ROLLBACK TRANSACTION bên trong nó, trạng thái Transaction bên ngoài bị ảnh hưởng thế nào? Tại sao không được gọi COMMIT trong khối CATCH? |
| **LỆNH SẼ CHẠY** | Cố tình INSERT giờ ra sai để Trigger tự ROLLBACK, kiểm tra hàm `XACT_STATE()` trong khối CATCH. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Không lưu dữ liệu. |
| **KẾT QUẢ MONG ĐỢI** | `XACT_STATE() = -1` (Doomed Transaction / Uncommittable). |
| **CÁCH TRẢ LỜI CÔ** | Khi Trigger tự phát lệnh ROLLBACK, Transaction bị chuyển thành trạng thái Doomed (`XACT_STATE() = -1`). Lúc này giao dịch bị khóa cứng, không thể tiếp tục ghi hay COMMIT (nếu gọi COMMIT sẽ bị lỗi nghiêm trọng Msg 3998/3930). Vì vậy code chuẩn luôn phải kiểm tra `IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;` để giải phóng transaction an toàn. |
| **CÁCH THAO TÁC** | Bôi đen TỪ IF DB_NAME() ngay dưới đây đến GO cuối TC. |

```sql
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51131, N'Chay B0 truoc va dam bao dung QuanLyNhanSuTienLuong.', 1;

DECLARE @NV28 INT = (SELECT MaNV FROM #TV2_CTX), @D28 DATE = (SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    -- Kích hoạt trigger trg_ChamCong_KiemTraGio gây ROLLBACK tự động
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@NV28, @D28, '17:00', '08:00', 'CO_MAT');

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    SELECT 
        N'TC28 PASS - XACT_STATE() sau khi Trigger Rollback' AS KetQua,
        XACT_STATE() AS TrangThaiXACT_STATE,
        CASE XACT_STATE()
            WHEN -1 THEN N'Doomed Transaction: Giao dịch bị hỏng do Trigger, bắt buộc phải ROLLBACK toàn bộ!'
            WHEN 1 THEN N'Transaction hợp lệ, có thể COMMIT hoặc ROLLBACK'
            ELSE N'Không có transaction nào đang mở'
        END AS GiaiThichKyThuat,
        ERROR_MESSAGE() AS ThongBaoTrigger;

    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
END CATCH;
GO
```

---

## TC29 — Mô phỏng kiểm soát tranh chấp đồng thời (Concurrency Locking) trên Chấm công

| Thuộc tính | Nội dung chi tiết |
| :--- | :--- |
| **VỊ TRÍ THUYẾT TRÌNH** | Slide 6–7 / Concurrency Control |
| **CÔ HỎI** | Nếu hai quản lý cùng mở máy và cùng bấm ghi nhận chấm công cho một nhân viên tại cùng một thời điểm thì CSDL chống xung đột tranh chấp (Race Condition / Lost Update) như thế nào? |
| **LỆNH SẼ CHẠY** | Mở 2 cửa sổ Query trong SSMS: Phiên 1 chèn công nhưng chưa commit; Phiên 2 gọi `sp_GhiNhanChamCong` cho cùng nhân viên trong ngày. |
| **DỮ LIỆU BỊ TÁC ĐỘNG** | Thao tác tạm trong transaction, sau đó ROLLBACK an toàn. |
| **KẾT QUẢ MONG ĐỢI** | Phiên 2 bị chặn hoặc bị bẫy lỗi trùng ngày từ chối ngay lập tức, ngăn chặn hoàn toàn Double Check-in. |
| **CÁCH TRẢ LỜI CÔ** | Hệ thống bảo vệ 2 tầng: Tầng 1 là kiểm tra `EXISTS` trong SP; Tầng 2 là ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay`. Khi 2 giao dịch đồng thời xảy ra, SQL Server tự động cấp Exclusive Lock hoặc quăng lỗi trùng lặp (Msg 2627), đảm bảo tính cô lập (Isolation) và không bị ghi đè. |
| **CÁCH THAO TÁC** | Mở 2 cửa sổ SSMS và chạy lần lượt theo chỉ dẫn dưới đây. |

### Cửa sổ 1 (Phiên A - Giữ giao dịch đang xử lý):
```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @NV INT = 1009, @D DATE = '2026-10-22', @Out INT;
BEGIN TRANSACTION;
EXEC dbo.sp_GhiNhanChamCong @MaNV = @NV, @NgayChamCong = @D,
     @GioVao = '08:00', @GioRa = '17:00', @TrangThai = N'CO_MAT',
     @GhiChu = N'Phiên A đang ghi nhận...', @MaChamCong = @Out OUTPUT;

PRINT N'Phiên A đã ghi công nhưng CHƯA COMMIT... Đang giữ Transaction.';
-- Giữ nguyên cửa sổ này, sang Cửa sổ 2 chạy lệnh.
-- Sau khi xem kết quả ở Cửa sổ 2, quay lại đây bôi đen dòng dưới và F5:
-- ROLLBACK TRANSACTION;
```

### Cửa sổ 2 (Phiên B - Thử ghi nhận đồng thời trên cùng nhân viên và ngày):
```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @NV INT = 1009, @D DATE = '2026-10-22', @Out INT;
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong @MaNV = @NV, @NgayChamCong = @D,
         @GioVao = '08:30', @GioRa = '17:30', @TrangThai = N'DI_TRE',
         @GhiChu = N'Phiên B cố gắng ghi nhận...', @MaChamCong = @Out OUTPUT;
END TRY
BEGIN CATCH
    SELECT N'TC29 PASS: Phiên B bị chặn thành công, ngăn ngừa Double Check-in' AS KetQua,
           ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
