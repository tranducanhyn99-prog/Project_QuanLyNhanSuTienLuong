# TV2 — NỘI DUNG SLIDE 4–6 (BẢN HANDOFF CẬP NHẬT CHÍNH THỨC)

- **Người trình bày:** Phạm Minh Quân (TV2 — MSSV 24110311)
- **Ngày cập nhật:** 13/10/2026 — căn cứ source `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`
- **Phạm vi:** Slide 4–6 của deck nhóm (TV5 tổng hợp, trần ≤15 slide toàn nhóm).
- **Trạng thái số liệu benchmark:** **ĐÃ XÁC THỰC HOÀN TẤT** — Lượt đo đồng nhất `run_20261013_140800` trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81`.
- **Ghi chú công cụ:** Markdown slides và speaker notes được hoàn thiện đầy đủ. Deck PPTX nhóm do TV5 tổng hợp; module Python `python-pptx` không có sẵn trên môi trường cục bộ và quy chế cấm tự ý cài đặt dependency mới, do đó nội dung được chuẩn hóa dạng Markdown chuyển giao sẵn sàng.

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

*Nguồn kiểm chứng:* `database/02_Module_ChamCong_TV2.sql:26–50`, schema live QA `PRJ_Fix_QA_20261013_001534_ae185d81`.

### Khối 2: Quy tắc nghiệp vụ ➔ Vật thể thực thi

| Quy tắc | Nội dung nghiệp vụ | Vật thể thực thi tại Database |
|---|---|---|
| **BR-CC-01** | Một nhân viên – một ngày: duy nhất một bản ghi công | `UQ_CHAMCONG_MaNV_Ngay` (Chốt chặn cuối tại CSDL) + kiểm tra sớm trong SP |
| **BR-CC-02** | `GioRa > GioVao` khi `GioRa` có giá trị | `sp_GhiNhanChamCong` + `trg_ChamCong_KiemTraGio` (AFTER INSERT, UPDATE) |
| **BR-CC-03** | Chỉ nhân viên `DANG_LAM_VIEC` mới được chấm công | SP + `trg_ChamCong_KiemTraNhanVien` (Quy tắc cross-table ➔ Trigger) |
| **BR-CC-04** | Ngày chấm công không vượt quá ngày hiện tại | `CHK_CHAMCONG_Ngay` + SP validate sớm |
| **BR-CC-05** | Trạng thái công thuộc 4 giá trị định sẵn | `CHK_CHAMCONG_TrangThai` (`CO_MAT, DI_TRE, VE_SOM, VANG`) |

### Khối 3: Thông điệp chính — Kiến trúc kiểm soát đa tầng (Multi-tier Defense)

- **Chuỗi kiểm soát:** `UI Form` ➔ `ChamCongService.validateChamCong` (Java) ➔ `sp_GhiNhanChamCong` (7 bước tiền kiểm tra) ➔ `Constraint + Trigger` (Lớp CSDL).
- **Ý nghĩa kỹ thuật (Rubric điểm cộng):** Mỗi ràng buộc đều có ý nghĩa nghiệp vụ thực tế, không hình thức. Khi có lệnh ghi trực tiếp vào SQL Server (bỏ qua ứng dụng), CSDL vẫn từ chối dữ liệu rác.

### Lưu ý khi trình bày (Finding A1):
- Giao diện UI (`ChamCongPanel.java:109`, `DieuChinhChamCongDialog.java:138`) nạp và hiển thị trực tiếp 4 mã nghiệp vụ `CO_MAT / DI_TRE / VE_SOM / VANG`. Khi trình bày, đọc rõ mã kỹ thuật và diễn giải nghĩa tiếng Việt tương ứng bằng lời (`Có mặt / Đi trễ / Về sớm / Vắng`), không tuyên bố giao diện nạp tiếng Việt.

---

## SLIDE 5 — Stored Procedure, Trigger và Giao dịch nhập lô (ROLLBACK)

### Khối 1: `dbo.sp_GhiNhanChamCong`

- **Tham số:** 6 tham số đầu vào (`@MaNV, @NgayChamCong, @GioVao, @GioRa, @TrangThai, @GhiChu`) + 1 tham số đầu ra `@MaChamCong INT OUTPUT` nhận `SCOPE_IDENTITY()`.
- **7 nhóm tiền kiểm tra:** Tham số bắt buộc ➔ Ngày ≤ hiện tại ➔ Miền trạng thái ➔ Độ dài ghi chú ≤ 255 ➔ `GioRa > GioVao` ➔ Nhân viên tồn tại & `DANG_LAM_VIEC` ➔ Chặn trùng lặp `(MaNV, NgayChamCong)` ➔ `INSERT`.
- **Thiết kế không sở hữu Transaction:** SP cố ý không mở transaction nội bộ (`BEGIN TRAN`) để caller tầng ứng dụng (Java Service) toàn quyền điều phối atomicity cho toàn bộ lô.

### Khối 2: Ba Trigger bảo vệ dữ liệu công

| Trigger | Bảng / Sự kiện | Vai trò và Hành vi | Phân định Ownership |
|---|---|---|---|
| `trg_ChamCong_KiemTraGio` | `CHAMCONG` (AFTER INSERT, UPDATE) | Quét set-based trên bảng `inserted`: phát hiện `GioRa <= GioVao` ➔ `RAISERROR 16` + `ROLLBACK` | **TV2 sở hữu** (Module 02) |
| `trg_ChamCong_KiemTraNhanVien` | `CHAMCONG` (AFTER INSERT, UPDATE) | JOIN `inserted` với `NHANVIEN`: nếu trạng thái ≠ `DANG_LAM_VIEC` ➔ `RAISERROR 16` + `ROLLBACK` | **TV2 sở hữu** (Module 02) |
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
  - `SoNgayDiLam` = `COUNT` các ngày có trạng thái `CO_MAT, DI_TRE, VE_SOM`.
  - `TongSoGioLam` = `SUM(DATEDIFF(MINUTE, GioVao, GioRa)) / 60.0` (chỉ tính dòng `GioRa > GioVao`).
- **Ứng dụng:** Tab "Tổng hợp theo tháng" đọc view này qua `ChamCongDAO.getTongHopTheoThang` phục vụ đối soát công của nhân sự.

### Khối 2: Non-clustered Covering Index `IX_CHAMCONG_MaNV_Ngay`

```sql
CREATE NONCLUSTERED INDEX IX_CHAMCONG_MaNV_Ngay
ON dbo.CHAMCONG (MaNV, NgayChamCong)
INCLUDE (GioVao, GioRa, TrangThai);
```

- **Vấn đề kỹ thuật của Baseline:** Ràng buộc `UQ_CHAMCONG_MaNV_Ngay` đã có sẵn index seek theo khóa `(MaNV, NgayChamCong)`, nhưng thiếu các cột cần chiếu ➔ Buộc SQL Server thực hiện **Key Lookup** về Clustered Index cho từng dòng.
- **Giải pháp:** Mệnh đề `INCLUDE` đưa `GioVao, GioRa, TrangThai` lên trang lá ➔ Chuyển plan thành **Pure Index Seek (Zero Key Lookup)**.
- **Số liệu đo lường thực nghiệm chính thức (Run ID: `run_20261013_140800`):**
  - **Truy vấn chi tiết (1.1 ➔ 3.1, 273 dòng):** Logical Reads giảm từ **574 xuống 5** (Giảm **99.13%** theo Messages; từ 522 xuống 5 theo XML operator `RunTimeCountersPerThread`). CPU: 15ms ➔ 0ms, Elapsed: 31ms ➔ 2ms.
  - **Truy vấn tổng hợp (1.2 ➔ 3.2, 9 dòng):** Logical Reads giảm từ **574 xuống 5** (Giảm **99.13%**).
  - **Giải trình độ chênh 574 vs 522:** 574 bao gồm trang dẫn đường (b-tree navigation) qua 273 lần Key Lookup ở tầng Storage Engine; 522 là số trang lá dữ liệu được đọc tại toán tử execution tree. Khi có covering index, cả hai phương thức đều đo chính xác **5 logical reads** (0 lookup).
  - **Đánh đổi kiến trúc:** Tăng dung lượng lưu trữ đĩa và thêm chi phí duy trì chỉ mục khi ghi dữ liệu (INSERT/UPDATE).

### Khối 3: Phối hợp TV2–TV3: Hàm `dbo.fn_TongPhuCap(@MaNV, @Thang, @Nam)`

- **Quyền sở hữu (Finding A3):** Theo ma trận phân công dự án, đây là **Function do TV2 đảm nhiệm**, được triển khai trong phân hệ `03_phucap_khautru_TV3.sql:214–224` (phạm vi dữ liệu bảng `PHUCAPNHANVIEN` của TV3).
- **Hành vi:** Tính `ISNULL(SUM(SoTien), 0)` các khoản phụ cấp đúng nhân viên/tháng/năm. Thủ tục tính lương `sp_TinhBangLuongThang` (TV4) gọi hàm này qua dynamic SQL có kiểm tra `OBJECT_ID`.

### Khối 4: Quy trình tính ngày công của phân hệ Lương (Tránh nhầm lẫn)

- Thủ tục `sp_TinhBangLuongThang` đếm **trực tiếp** bảng `CHAMCONG`: quét khoảng ngày nửa mở `[DATEFROMPARTS(nam, thang, 1), DATEADD(month, 1, ...))` với điều kiện `TrangThai IN ('CO_MAT', 'DI_TRE', 'VE_SOM')` kèm khóa `WITH (HOLDLOCK)` (`04_Module_TinhLuong_TV4.sql:376–389`).
- **Không gọi `fn_TinhSoNgayCong`:** Hàm `fn_TinhSoNgayCong` do **TV1 sở hữu** (dù đặt trong script 02).
- **Phân quyền bảo mật:** `role_PayrollOfficer` chỉ được quyền `SELECT` trên `CHAMCONG`, bị `DENY INSERT, UPDATE, DELETE` (`05:367, :373`). UI Chấm công tự động vô hiệu hóa toàn bộ nút ghi/xóa khi đăng nhập tài khoản Payroll.

---

## Tóm tắt chuyển giao cho TV5 ghép Slide tổng thể:
1. Thay thế toàn bộ số liệu benchmark bằng bảng số liệu chính thức `574 ➔ 5` (Giảm 99.13%).
2. Giữ nguyên định dạng 3 slide (Slide 4, 5, 6) để đảm bảo tổng số slide toàn nhóm ≤ 15 slide.
3. Không sử dụng 5 ảnh chụp màn hình cũ; nếu đưa hình vào slide, trích xuất sơ đồ hoặc dùng 4 file `.sqlplan` và Messages log chính thức từ thư mục bằng chứng.
