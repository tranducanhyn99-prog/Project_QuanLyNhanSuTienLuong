# TV2 — NỘI DUNG SLIDE 4–6 (BẢN REVIEWED V2 ĐÃ KIỂM ĐỊNH)

- **Người trình bày:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Ngày cập nhật:** 13/10/2026 — căn cứ source `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`
- **Phạm vi:** Slide 4–6 trong tổng thể deck nhóm (TV5 tổng hợp, trần ≤15 slide toàn nhóm).
- **Trạng thái số liệu benchmark:** **ĐÃ XÁC THỰC TOÀN DIỆN (Remediation Pass Verified)** — Lượt đo đồng nhất `run_20261013_140800` trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81`.
- **Ghi chú công cụ & Chuyển giao:** Markdown slides và speaker notes được hoàn thiện đầy đủ. File PowerPoint gốc chỉnh sửa được [TV2_Slides_4_6.pptx](../final/TV2_Slides_4_6.pptx) đã được khởi tạo trực tiếp qua Microsoft PowerPoint COM.

---

## SLIDE 4 — Nghiệp vụ chấm công và ràng buộc cơ sở dữ liệu

### Khối 1: Bảng `dbo.CHAMCONG` (chuẩn hóa 3NF)

| Cột | Kiểu dữ liệu | Nullable / Mặc định | Ràng buộc vật thể |
|---|---|---|---|
| `MaChamCong` | `INT IDENTITY(1,1)` | `NOT NULL` | `PK_CHAMCONG` (`PRIMARY KEY CLUSTERED`) |
| `MaNV` | `INT` | `NOT NULL` | `FK_CHAMCONG_NHANVIEN` ➔ `NHANVIEN(MaNV)` |
| `NgayChamCong` | `DATE` | `NOT NULL` | `CHK_CHAMCONG_Ngay` (`<= CAST(GETDATE() AS DATE)`) |
| `GioVao` | `TIME(0)` | `NOT NULL` | Định dạng giờ chuẩn, SP + Trigger kiểm soát |
| `GioRa` | `TIME(0)` | `NULL` | Cho phép chưa checkout; logic `GioRa > GioVao` do Trigger kiểm tra |
| `TrangThai` | `NVARCHAR(20)` | `NOT NULL, DEFAULT N'CO_MAT'` | `CHK_CHAMCONG_TrangThai` (4 mã nghiệp vụ) |
| `GhiChu` | `NVARCHAR(255)` | `NULL` | Ghi chú bổ sung (tối đa 255 ký tự) |
| *(Tổ hợp khóa)* | `(MaNV, NgayChamCong)` | — | `UQ_CHAMCONG_MaNV_Ngay` (`UNIQUE NONCLUSTERED`) |

*Nguồn kiểm chứng:* `database/02_Module_ChamCong_TV2.sql:26–50`, schema live QA `PRJ_Fix_QA_20261013_001534_ae185d81` (xuất chi tiết tại `schema-inspection/TV2_live_schema_inspection.log`).

### Khối 2: Quy tắc nghiệp vụ ➔ Vật thể thực thi

| Quy tắc | Nội dung nghiệp vụ | Vật thể thực thi tại Database |
|---|---|---|
| **BR-CC-01** | Một nhân viên – một ngày: duy nhất một bản ghi công | `UQ_CHAMCONG_MaNV_Ngay` (Chốt chặn cuối tại CSDL) + kiểm tra sớm trong SP |
| **BR-CC-02** | `GioRa > GioVao` khi `GioRa` có giá trị | `sp_GhiNhanChamCong` + `trg_ChamCong_KiemTraGio` (AFTER INSERT, UPDATE) — Testcase `TC-CC-07` |
| **BR-CC-03** | Chỉ nhân viên `DANG_LAM_VIEC` mới được chấm công | SP + `trg_ChamCong_KiemTraNhanVien` (Quy tắc cross-table ➔ Trigger) — Testcase `TC-CC-06` |
| **BR-CC-04** | Ngày chấm công không vượt quá ngày hiện tại | `CHK_CHAMCONG_Ngay` + SP validate sớm — Testcase `TC-CC-04` |
| **BR-CC-05** | Trạng thái công thuộc 4 giá trị định sẵn | `CHK_CHAMCONG_TrangThai` (`CO_MAT, DI_TRE, VE_SOM, VANG`) |

### Khối 3: Thông điệp chính — Kiến trúc kiểm soát đa tầng (Multi-tier Defense)

- **Chuỗi kiểm soát:** `UI Form` ➔ `ChamCongService.validateChamCong` (Java) ➔ `sp_GhiNhanChamCong` (7 bước tiền kiểm tra) ➔ `Constraint + Trigger` (Lớp CSDL).
- **Ý nghĩa kỹ thuật (Phạm vi bảo vệ thực tế):** Ràng buộc `CHECK`, `UNIQUE`, `FOREIGN KEY` và Trigger bảo đảm thực thi đúng các điều kiện toàn vẹn cụ thể ngay cả khi ghi bằng SQL ngoài app; câu lệnh SQL trực tiếp hợp lệ vẫn thực thi bình thường; cơ chế này bảo vệ theo phạm vi ràng buộc đã định nghĩa và không thay thế hoàn toàn quyền quản trị của DBA cấp cao.
- **Lưu ý khi trình bày (Finding A1):** Giao diện UI (`ChamCongPanel.java:109`, `DieuChinhChamCongDialog.java:138`) nạp và hiển thị trực tiếp 4 mã nghiệp vụ `CO_MAT / DI_TRE / VE_SOM / VANG`. Khi trình bày, đọc rõ mã kỹ thuật và diễn giải nghĩa tiếng Việt tương ứng bằng lời (`Có mặt / Đi trễ / Về sớm / Vắng`), không tuyên bố giao diện nạp tiếng Việt.

---

## SLIDE 5 — Stored Procedure, Trigger và Giao dịch nhập lô (ROLLBACK)

### Khối 1: `dbo.sp_GhiNhanChamCong`

- **Tham số:** 6 tham số đầu vào (`@MaNV, @NgayChamCong, @GioVao, @GioRa, @TrangThai, @GhiChu`) + 1 tham số đầu ra `@MaChamCong INT OUTPUT` nhận `SCOPE_IDENTITY()`.
- **7 nhóm tiền kiểm tra:** Tham số bắt buộc ➔ Ngày ≤ hiện tại ➔ Miền trạng thái ➔ Độ dài ghi chú ≤ 255 ➔ `GioRa > GioVao` ➔ Nhân viên tồn tại & `DANG_LAM_VIEC` ➔ Chặn trùng lặp `(MaNV, NgayChamCong)` ➔ `INSERT`.
- **Thiết kế không sở hữu Transaction:** SP cố ý không mở transaction nội bộ (`BEGIN TRAN`) để caller tầng ứng dụng (Java Service) toàn quyền điều phối atomicity cho toàn bộ lô.

### Khối 2: Ba Trigger bảo vệ dữ liệu công

| Trigger | Bảng / Sự kiện | Vai trò và Hành vi | Phân định Ownership |
|---|---|---|---|
| `trg_ChamCong_KiemTraGio` | `CHAMCONG` (AFTER INSERT, UPDATE) | Quét set-based trên bảng `inserted`: phát hiện `GioRa <= GioVao` ➔ `RAISERROR 16` + `ROLLBACK` (Testcase `TC-CC-07`) | **TV2 sở hữu** (Module 02) |
| `trg_ChamCong_KiemTraNhanVien` | `CHAMCONG` (AFTER INSERT, UPDATE) | JOIN `inserted` với `NHANVIEN`: nếu trạng thái ≠ `DANG_LAM_VIEC` ➔ `RAISERROR 16` + `ROLLBACK` (Testcase `TC-CC-06`) | **TV2 sở hữu** (Module 02) |
| `trg_ChamCong_KhongSuaKhiDaChotLuong` | `CHAMCONG` (AFTER INSERT, UPDATE, DELETE) | Khóa `BANGLUONG` bằng `UPDLOCK, HOLDLOCK`; hợp tập (UNION) kỳ của cả `inserted` lẫn `deleted` ➔ Chặn thêm/sửa/xóa/đổi ngày vào kỳ đã chốt | **TV5 sở hữu** (Module 05) — TV2 phối hợp |

### Khối 3: Giao dịch nhập lô All-or-Nothing — `ChamCongService.nhapChamCongTheoLo`

- **Trình tự xử lý:**
  1. `validateChamCong` toàn bộ danh sách ở tầng Java trước khi gửi request tới CSDL.
  2. Mở 1 `Connection` duy nhất; thiết lập `conn.setAutoCommit(false)`.
  3. Lặp gọi `ChamCongDAO.insertInTransaction(conn, cc)` thực thi `sp_GhiNhanChamCong` trên cùng connection đó.
  4. Mọi dòng hợp lệ ➔ `conn.commit()` lưu trọn vẹn lô.
  5. Bất kỳ dòng nào gặp lỗi (vi phạm ràng buộc / trigger) ➔ `conn.rollback()` hủy toàn bộ các dòng đã chèn trước đó trong lô, bảo toàn 0 bản ghi rác.
  6. Khối `finally` đảm bảo khôi phục `conn.setAutoCommit(true)`.
- **Minh chứng kiểm thử:** Testcase `TC-CC-08` (commit thành công) và `TC-CC-09` (rollback trọn vẹn khi lỗi giữa chừng) đạt **21/21 PASS** (log `test_module_chamcong_TV2.log`).

---

## SLIDE 6 — View tổng hợp tháng, Covering Index, fn_TongPhuCap và Nối sang Lương

### Khối 1: `dbo.vw_TongHopChamCongThang`

- **Cấu trúc:** `GROUP BY (MaNV, HoTen, Thang, Nam)`.
- **9 cột đầu ra:** `MaNV`, `HoTen`, `Thang`, `Nam`, `SoNgayDiLam`, `SoLanDiTre`, `SoLanVeSom`, `SoNgayVang`, `TongSoGioLam`.
- **Công thức:**
  - `SoNgayDiLam` = `COUNT(CASE WHEN TrangThai IN ('CO_MAT', 'DI_TRE', 'VE_SOM') THEN 1 END)`. (Lưu ý: Mệnh đề `CASE` không có `ELSE` sẽ trả về `NULL` cho dòng không thỏa, SQL Server phát ra cảnh báo chuẩn *"Null value is eliminated by an aggregate or other SET operation"* tại `COUNT`, hoàn toàn đúng chuẩn ANSI SQL).
  - `TongSoGioLam` = `SUM(CASE WHEN GioRa > GioVao THEN DATEDIFF(MINUTE, GioVao, GioRa) ELSE 0 END) / 60.0`.
- **Ứng dụng:** Tab "Tổng hợp theo tháng" đọc view này qua `ChamCongDAO.getTongHopTheoThang` phục vụ đối soát công của nhân sự.

### Khối 2: Đo lường Covering Index thực nghiệm (Run ID: `run_20261013_140800`)

```sql
-- Chỉ mục mô phỏng trên bảng tạm chuẩn bị benchmark:
CREATE NONCLUSTERED INDEX IX_Bench_CHAMCONG_MaNV_Ngay
ON #CHAMCONG_BENCHMARK (MaNV, NgayChamCong)
INCLUDE (GioVao, GioRa, TrangThai);
```

- **Mục tiêu kỹ thuật:** Baseline ban đầu (`UQ_Bench_MaNV_Ngay`) có sẵn Index Seek theo `(MaNV, NgayChamCong)` nhưng thiếu các cột cần chiếu ➔ Buộc SQL Server thực hiện **Key Lookup** về Clustered Index cho từng dòng. Mệnh đề `INCLUDE` đưa `GioVao, GioRa, TrangThai` lên trang lá ➔ Chuyển plan thành **Pure Index Seek (0 Key Lookup)**.
- **Bảng số liệu thực nghiệm chính thức:**
  - **Truy vấn chi tiết (1.1 ➔ 3.1, 273 dòng):**
    - Logical Reads (Messages `SET STATISTICS IO`): Giảm từ **574 xuống 5** (Giảm **99.1289%**).
    - Logical Reads (XML Plan `RunTimeCountersPerThread`): Giảm từ **522 xuống 5** (Giảm **99.0421%**).
    - Thời gian thực thi (Messages `SET STATISTICS TIME`): CPU 15ms ➔ 0ms; Elapsed: 31ms ➔ 2ms.
    - QueryTimeStats trong XML Plan: CPU 0ms ➔ 0ms; Elapsed: 0ms ➔ 0ms.
  - **Truy vấn tổng hợp (1.2 ➔ 3.2, 9 dòng):**
    - Logical Reads (Messages `SET STATISTICS IO`): Giảm từ **574 xuống 5** (Giảm **99.1289%**).
    - Logical Reads (XML Plan `RunTimeCountersPerThread`): Giảm từ **522 xuống 5** (Giảm **99.0421%**).
    - Thời gian thực thi: Messages không xuất riêng dòng thời gian kết thúc cho khối aggregate; trong XML Plan `QueryTimeStats`: Before ghi nhận CPU 1ms, Elapsed 1ms; After ghi nhận CPU 0ms, Elapsed 0ms (ở độ phân giải dưới 1ms, nhóm minh bạch ghi nhận không tuyên bố kết luận về tốc độ thời gian cho khối aggregate).
- **Giải trình khoa học về độ chênh 574 vs 522:**
  - Con số 574 đo từ `SET STATISTICS IO` trong Messages phản ánh tổng số trang đọc ở tầng Storage Engine buffer pool.
  - Con số 522 ghi nhận từ bộ đếm thời gian chạy từng toán tử (`RunTimeCountersPerThread`) trong XML Execution Plan.
  - Nguyên nhân cụ thể của 52 trang chênh lệch chưa có căn cứ kỹ thuật nội bộ của SQL Server để khẳng định (nguyên nhân là UNKNOWN). Điểm cốt lõi: Khi có covering index, **cả hai phương thức đo đều ghi nhận chính xác 5 trang đọc logic và 0 Key Lookup**. Không đưa ra tuyên bố đo lường về cắt giảm đọc đĩa vật lý.
- **Đánh đổi kiến trúc:** Tăng dung lượng lưu trữ đĩa và thêm chi phí cập nhật index khi thực hiện thao tác ghi dữ liệu (INSERT/UPDATE).

### Khối 3: Phối hợp TV2–TV3: Hàm `dbo.fn_TongPhuCap(@MaNV, @Thang, @Nam)`

- **Quyền sở hữu (Finding A3):** Theo ma trận phân công dự án, đây là **Function do TV2 đảm nhiệm**, được triển khai trong phân hệ `03_phucap_khautru_TV3.sql:214–224` (phạm vi dữ liệu bảng `PHUCAPNHANVIEN` của TV3).
- **Hành vi:** Tính `ISNULL(SUM(SoTien), 0)` các khoản phụ cấp đúng nhân viên/tháng/năm. Thủ tục tính lương `sp_TinhBangLuongThang` (TV4) gọi hàm này qua dynamic SQL có kiểm tra `OBJECT_ID`.

### Khối 4: Quy trình tính ngày công của phân hệ Lương (Tránh nhầm lẫn)

- Thủ tục `sp_TinhBangLuongThang` đếm **trực tiếp** bảng `CHAMCONG`: quét khoảng ngày nửa mở `[DATEFROMPARTS(nam, thang, 1), DATEADD(month, 1, ...))` với điều kiện `TrangThai IN ('CO_MAT', 'DI_TRE', 'VE_SOM')` kèm khóa `WITH (HOLDLOCK)` (`04_Module_TinhLuong_TV4.sql:376–389`).
- **Không gọi `fn_TinhSoNgayCong`:** Hàm `fn_TinhSoNgayCong` do **TV1 sở hữu** (dù đặt trong script 02).
- **Phân quyền bảo mật:** `role_PayrollOfficer` chỉ được quyền `SELECT` trên `CHAMCONG`, bị `DENY INSERT, UPDATE, DELETE` (`05:367, :373`). UI Chấm công tự động vô hiệu hóa toàn bộ nút ghi/xóa khi đăng nhập tài khoản Payroll.
