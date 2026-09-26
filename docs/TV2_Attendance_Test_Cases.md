# TV2 – Bộ Test Case Kiểm Thử Module Chấm Công
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Phạm Minh Quân (TV2, MSSV 24110311)<br>
**Ngày:** 24/09/2026<br>
**Phiên bản:** 1.0 (Tuần 1)

---

## 1. Mục tiêu và phạm vi kiểm thử

Bộ tài liệu này thiết kế các kịch bản kiểm thử (Test Cases) nhằm xác minh chất lượng, tính toàn vẹn dữ liệu và độ tin cậy của **Module Chấm công** trước khi triển khai và nghiệm thu ở Tuần 2 và Tuần 3:

1. **Kiểm tra ràng buộc Schema & Constraint:** Đảm bảo các ràng buộc toàn vẹn khóa chính, khóa ngoại, tính duy nhất (`UNIQUE`) và miền giá trị (`CHECK`) hoạt động chính xác.
2. **Kiểm tra Trigger nghiệp vụ:** Đảm bảo Trigger ngăn chặn triệt để hành vi chấm công cho nhân viên đã nghỉ việc (`trg_ChamCong_KiemTraNhanVien`) và kiểm soát tính hợp lý về giờ làm việc.
3. **Kiểm tra Transaction nhập chấm công theo lô:** Xác minh tính nguyên tố (Atomicity) theo nguyên tắc All-or-Nothing; kiểm tra việc `ROLLBACK` toàn bộ giao dịch khi phát sinh bất kỳ lỗi dữ liệu nào trong lô.
4. **Kiểm tra View tổng hợp và Index hiệu năng:** Kiểm tra tính chính xác của dữ liệu tổng hợp tháng từ `vw_TongHopChamCongThang` và minh chứng cải thiện hiệu năng truy vấn của `IX_CHAMCONG_MaNV_Ngay`.
5. **Kiểm tra Bảo mật và Phân quyền (Security):** Kiểm tra chính sách phân quyền trên SQL Server đối với 4 nhóm vai trò (`DB_Admin`, `HR_Manager`, `Payroll_Officer`, `Employee`).

---

## 2. Môi trường và dữ liệu giả định kiểm thử

### 2.1 Dữ liệu nhân viên mẫu phục vụ kiểm thử

| Mã NV (`MaNV`) | Họ và tên | Mã PB | Mã CV | Lương cơ bản | Trạng thái (`TrangThai`) | Ý nghĩa kiểm thử |
|---|---|---|---|---|---|---|
| `1` | Nguyễn Văn An | PB01 | CV01 | 15.000.000 | `DANG_LAM_VIEC` | Nhân viên hợp lệ đang làm việc bình thường |
| `2` | Lê Thị Bình | PB01 | CV02 | 10.000.000 | `DANG_LAM_VIEC` | Nhân viên hợp lệ đang làm việc bình thường |
| `3` | Trần Văn Cường | PB02 | CV02 | 12.000.000 | `NGHI_VIEC` | Nhân viên đã thôi việc (dùng test chặn chấm công) |
| `999` | (Không tồn tại) | — | — | — | — | Mã nhân viên không có trong CSDL (dùng test khóa ngoại) |

### 2.2 Dữ liệu ngày và ca làm việc giả định

- **Kỳ kiểm thử:** Tháng 09/2026.
- **Ngày làm việc mẫu:** `2026-09-21`, `2026-09-22`, `2026-09-23`.
- **Khung giờ ca chuẩn:** 08:00:00 (vào ca) đến 17:00:00 (hết ca).
- **Mốc thời gian tương lai dùng test chặn:** `2099-01-01`.

---

## 3. Bảng danh mục Test Cases chi tiết

### Nhóm 1: Ràng buộc tính hợp lệ & Toàn vẹn dữ liệu (Constraint / Schema)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Bằng chứng thực thi thực tế | Mức độ |
|---|---|---|---|---|---|---|---|
| **TC-CC-01** | Chấm công đơn lẻ hợp lệ | `MaNV=1` có trạng thái `DANG_LAM_VIEC`; ngày chưa được chấm công | `MaNV = 1`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'`, `TrangThai = N'CO_MAT'` | 1. Nhập thông tin trên form hoặc chạy lệnh INSERT.<br>2. Xác nhận lưu dữ liệu. | Dữ liệu được thêm mới thành công vào bảng `CHAMCONG`. Hệ thống sinh mã `MaChamCong` tự tăng. | **PASS** (Bridge job `2026-09-25-001`, `2026-09-25-004`, `2026-09-26-001`: INSERT thành công qua `sp_GhiNhanChamCong`, sinh mã tự tăng hợp lệ) | Nghiêm trọng (P1) |
| **TC-CC-02** | Ngăn chặn chấm công trùng lặp trong ngày | `MaNV=1` đã có bản ghi chấm công ngày `2026-09-21` | `MaNV = 1`, `Ngay = '2026-09-21'`, `GioVao = '08:30:00'`, `GioRa = '17:30:00'` | 1. Thực hiện INSERT bản ghi thứ 2 cho cùng nhân viên trong cùng ngày. | SQL Server chặn thực thi, ném lỗi vi phạm ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay`. Không thêm bản ghi thứ 2. | **PASS** (Bridge job `2026-09-25-001`: Vi phạm ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay`, thao tác bị từ chối) | Nghiêm trọng (P1) |
| **TC-CC-03** | Chặn chấm công cho nhân viên không tồn tại | `MaNV=999` không có trong bảng `NHANVIEN` | `MaNV = 999`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Thực hiện lệnh INSERT với `MaNV=999`. | Thao tác thất bại do vi phạm khóa ngoại `FK_CHAMCONG_NHANVIEN`. Ứng dụng thông báo mã nhân viên không hợp lệ. | **PASS** (Bridge job `2026-09-25-001`: Vi phạm khóa ngoại `FK_CHAMCONG_NHANVIEN`, câu lệnh bị chặn) | Nghiêm trọng (P1) |
| **TC-CC-04** | Chặn chấm công với ngày trong tương lai | Hệ thống hoạt động bình thường | `MaNV = 1`, `Ngay = '2099-01-01'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Nhập ngày công lớn hơn ngày hiện tại.<br>2. Thực hiện ghi nhận. | Thao tác thất bại do vi phạm ràng buộc kiểm tra `CHK_CHAMCONG_Ngay`. Thông báo lỗi ngày công không hợp lệ. | **PASS** (Bridge job `2026-09-25-001`, `2026-09-26-002`: Vi phạm ràng buộc CHECK `CHK_CHAMCONG_Ngay`, không lưu ngày tương lai) | Cao (P2) |
| **TC-CC-05** | Chặn chấm công thiếu trường bắt buộc | Dữ liệu thiếu `GioVao` hoặc thiếu `NgayChamCong` | `MaNV = 1`, `Ngay = NULL`, `GioVao = NULL` | 1. Gửi request dữ liệu thiếu trường NOT NULL. | SQL Server từ chối lệnh INSERT; tầng DAO ném ngoại lệ thông báo không được để trống trường bắt buộc. | Chưa thực thi / chưa có log | Cao (P2) |
| **TC-CC-06** | Chặn giá trị trạng thái không hợp lệ | Hệ thống hoạt động bình thường | `MaNV = 1`, `Ngay = '2026-09-22'`, `GioVao = '08:00:00'`, `TrangThai = N'KHONG_HOP_LE'` | 1. Thử lưu với giá trị trạng thái ngoài danh mục `CHK_CHAMCONG_TrangThai`. | Thao tác thất bại do vi phạm ràng buộc CHECK. Dữ liệu không được ghi vào DB. | **PASS** (Bridge job `2026-09-25-001`: Vi phạm ràng buộc CHECK `CHK_CHAMCONG_TrangThai`, bị từ chối) | Trung bình (P3) |

---

### Nhóm 2: Kiểm thử Trigger nghiệp vụ

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Bằng chứng thực thi thực tế | Mức độ |
|---|---|---|---|---|---|---|---|
| **TC-CC-07** | Chặn chấm công cho nhân viên đã nghỉ việc (`INSERT`) | `MaNV=3` có `TrangThai = 'NGHI_VIEC'` trong bảng `NHANVIEN` | `MaNV = 3`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Thực hiện INSERT bản ghi chấm công cho `MaNV=3`. | Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt, thực thi `RAISERROR`: "Không thể ghi nhận chấm công cho nhân viên đã nghỉ việc!" và `ROLLBACK TRANSACTION`. | **PASS** (Bridge job `2026-09-26-001`: Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt chặn INSERT cho nhân viên `NGHI_VIEC` và ROLLBACK) | Nghiêm trọng (P1) |
| **TC-CC-08** | Chặn đổi chấm công sang nhân viên đã nghỉ việc (`UPDATE`) | Bản ghi chấm công đã tồn tại với `MaNV=1`; `MaNV=3` có `TrangThai = 'NGHI_VIEC'` | UPDATE `CHAMCONG SET MaNV = 3 WHERE MaChamCong = 1` | 1. Thực thi câu lệnh UPDATE đổi `MaNV` sang nhân viên đã nghỉ. | Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt trên sự kiện UPDATE, gửi thông báo lỗi và ROLLBACK. | **PASS** (Bridge job `2026-09-26-001`: Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt chặn UPDATE sang nhân viên `NGHI_VIEC` và ROLLBACK) | Nghiêm trọng (P1) |
| **TC-CC-09** | Cho phép chấm công với nhân viên đang làm việc | `MaNV=2` có `TrangThai = 'DANG_LAM_VIEC'` | `MaNV = 2`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Thực hiện INSERT chấm công cho `MaNV=2`. | Trigger kiểm tra thành công, bản ghi được lưu hợp lệ vào CSDL. | **PASS** (Bridge job `2026-09-26-001`: Chấm công cho nhân viên `DANG_LAM_VIEC` thành công, trigger kiểm tra hợp lệ) | Nghiêm trọng (P1) |
| **TC-CC-10** | Chặn giờ ra nhỏ hơn hoặc bằng giờ vào | Nhân viên hợp lệ `MaNV=1` | `MaNV = 1`, `Ngay = '2026-09-22'`, `GioVao = '17:00:00'`, `GioRa = '08:00:00'` | 1. Nhập giờ ra sớm hơn giờ vào.<br>2. Nhấn lưu chấm công. | Bị chặn bởi Trigger `trg_ChamCong_KiemTraGio` với thông báo lỗi: `N'Lỗi: Giờ ra về phải lớn hơn giờ vào làm.'` và ROLLBACK. | **PASS** (Bridge job `2026-09-25-004`: Trigger `trg_ChamCong_KiemTraGio` kích hoạt chặn `GioRa <= GioVao` và ROLLBACK) | Nghiêm trọng (P1) |

---

### Nhóm 3: Kiểm thử Transaction nhập chấm công theo lô (Batch Processing)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Bằng chứng thực thi thực tế | Mức độ |
|---|---|---|---|---|---|---|---|
| **TC-CC-11** | Nhập lô toàn bộ hợp lệ (Commit thành công) | CSDL sạch chưa có dữ liệu ngày `2026-09-23` cho các nhân viên trong lô | Danh sách 3 nhân viên:<br>- Dòng 1: NV 1, ngày 23/09, 08:00-17:00<br>- Dòng 2: NV 2, ngày 23/09, 08:15-17:00<br>- Dòng 3: NV 1, ngày 22/09, 08:00-17:00 | 1. Khởi chạy Transaction nhập lô từ `ChamCongService`.<br>2. Gửi batch lệnh xuống SQL Server.<br>3. Kiểm tra kết quả thực thi. | Toàn bộ 3 bản ghi được lưu thành công vào CSDL. Lệnh `COMMIT` được gọi. Số lượng bản ghi trong DB tăng thêm đúng 3. | **PASS** (Bridge job `2026-09-25-003`, `2026-09-25-005`, `2026-09-26-001`: Batch commit thành công toàn bộ bản ghi hợp lệ) | Nghiêm trọng (P1) |
| **TC-CC-12** | Nhập lô thất bại do có nhân viên đã nghỉ việc (Rollback toàn bộ) | Dữ liệu kiểm thử có dòng vi phạm; NV3 có `TrangThai = 'NGHI_VIEC'` | Danh sách 3 dòng:<br>- Dòng 1: NV 1, ngày 24/09 (hợp lệ)<br>- Dòng 2: NV 3, ngày 24/09 (NV đã nghỉ)<br>- Dòng 3: NV 2, ngày 24/09 (hợp lệ) | 1. Bắt đầu Transaction nhập lô.<br>2. Ghi nhận dòng 1, dòng 2.<br>3. Đến dòng 2, Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt lỗi. | Toàn bộ Transaction bị `ROLLBACK`. Kiểm tra bảng `CHAMCONG`: không có bản ghi nào của ngày 24/09 được lưu (kể cả dòng 1 của NV 1). Đảm bảo tính nguyên tố (All-or-Nothing). | **PASS** (Bridge job `2026-09-26-001`: Lô có nhân viên `NGHI_VIEC` bị trigger chặn, toàn bộ transaction được rollback, 0 dòng ghi nhận) | Nghiêm trọng (P1) |
| **TC-CC-13** | Nhập lô thất bại do có dòng trùng ngày (Rollback toàn bộ) | NV 1 đã có bản ghi chấm công ngày 21/09/2026 | Danh sách 2 dòng:<br>- Dòng 1: NV 2, ngày 21/09 (hợp lệ)<br>- Dòng 2: NV 1, ngày 21/09 (bị trùng) | 1. Bắt đầu Transaction nhập lô.<br>2. Thực thi batch insert. | Gặp lỗi vi phạm `UQ_CHAMCONG_MaNV_Ngay` tại dòng 2. Transaction tự động `ROLLBACK`. Không lưu bản ghi dòng 1 của NV 2. | Chưa thực thi / chưa có log | Nghiêm trọng (P1) |
| **TC-CC-14** | Nhập lô thất bại do lỗi logic giờ làm việc (Rollback toàn bộ) | CSDL sẵn sàng | Danh sách 2 dòng:<br>- Dòng 1: NV 1, ngày 25/09 (hợp lệ)<br>- Dòng 2: NV 2, ngày 25/09, Vào: 17:00, Ra: 08:00 | 1. Bắt đầu Transaction nhập lô.<br>2. Thực thi xử lý dữ liệu. | Phát hiện lỗi giờ không hợp lệ tại dòng 2. Transaction bị hủy bỏ và rollback toàn bộ. Không dòng nào được lưu. | Chưa thực thi / chưa có log | Cao (P2) |

---

### Nhóm 4: Kiểm thử View tổng hợp & Index hiệu năng

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Bằng chứng thực thi thực tế | Mức độ |
|---|---|---|---|---|---|---|---|
| **TC-CC-15** | Đối soát số liệu View `vw_TongHopChamCongThang` | Đã có dữ liệu chấm công tháng 09/2026 của NV 1 (2 ngày bình thường, 1 ngày đi trễ) | Tháng = 9, Năm = 2026 | 1. Chạy câu truy vấn: `SELECT * FROM vw_TongHopChamCongThang WHERE MaNV = 1 AND Thang = 9 AND Nam = 2026`. | Trả về chính xác: `SoNgayDiLam = 3`, `SoLanDiTre = 1`, `TongSoGioLam` được tính đúng theo tổng giờ giữa GioRa và GioVao. | **PASS** (Bridge job `2026-09-26-002`, `2026-09-26-003`: View `vw_TongHopChamCongThang` đối soát trả về kết quả chính xác theo các chỉ số công, giờ làm và đi trễ) | Cao (P2) |
| **TC-CC-16** | Đo kiểm hiệu năng Index `IX_CHAMCONG_MaNV_Ngay` | Bảng `CHAMCONG` có dữ liệu lớn (chuẩn bị dữ liệu mẫu ở Tuần 3) | Lọc theo `MaNV = 1` và khoảng ngày trong tháng | 1. Bật `SET STATISTICS IO, TIME ON`.<br>2. Chạy truy vấn lọc khi không có Index.<br>3. Tạo Index `IX_CHAMCONG_MaNV_Ngay` kèm `INCLUDE`.<br>4. Chạy lại truy vấn và đối chiếu Actual Execution Plan. | Khi có Index, phương thức truy cập chuyển từ `Clustered Index Scan` / `Table Scan` sang `Index Seek`. Số lượng logical reads giảm rõ rệt. | Chưa thực thi / chưa có log | Cao (P2) |

---

### Nhóm 5: Kiểm thử Bảo mật & Phân quyền (Security Matrix)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Bằng chứng thực thi thực tế | Mức độ |
|---|---|---|---|---|---|---|---|
| **TC-CC-17** | Phân quyền vai trò `HR_Manager` | Đăng nhập bằng `login_HRManager` (`role_HRManager`) | Thao tác trên bảng `CHAMCONG` | 1. Thực hiện SELECT dữ liệu chấm công.<br>2. Thực hiện INSERT/UPDATE chấm công. | Cả hai thao tác SELECT và INSERT/UPDATE đều được SQL Server cho phép thực thi bình thường. | Chưa thực thi / chưa có log | Nghiêm trọng (P1) |
| **TC-CC-18** | Chặn quyền sửa/xóa đối với vai trò `Payroll_Officer` | Đăng nhập bằng `login_PayrollOfficer` (`role_PayrollOfficer`) | Thao tác trên bảng `CHAMCONG` | 1. Thực hiện SELECT chấm công để xem dữ liệu.<br>2. Thử thực hiện lệnh INSERT hoặc DELETE. | Thao tác SELECT thành công. Lệnh INSERT hoặc DELETE bị SQL Server từ chối ngay lập tức với lỗi: `The INSERT/DELETE permission was denied on the object 'CHAMCONG'`. | Chưa thực thi / chưa có log | Nghiêm trọng (P1) |
| **TC-CC-19** | Chặn truy cập bảng chung đối với vai trò `Employee` | Đăng nhập bằng `login_Employee` (`role_Employee`) | Lệnh truy vấn trực tiếp bảng | 1. Chạy câu lệnh `SELECT * FROM CHAMCONG`. | SQL Server từ chối truy cập (DENY SELECT on CHAMCONG). Nhân viên không được xem thông tin chấm công của người khác. | Chưa thực thi / chưa có log | Cao (P2) |
| **TC-CC-20** | Kiểm tra toàn quyền vai trò `DB_Admin` | Đăng nhập bằng `login_DBAdmin` (`role_DBAdmin`) | Thao tác quản trị | 1. Thực hiện đầy đủ SELECT, INSERT, UPDATE, DELETE, tạo Index trên `CHAMCONG`. | Mọi thao tác đều thực thi thành công do thuộc role `db_owner`. | Chưa thực thi / chưa có log | Cao (P2) |

---

## 4. Tình trạng các điểm kỹ thuật ảnh hưởng đến kiểm thử (Pending Decisions & Resolution)

Qua quá trình triển khai mã nguồn và kịch bản CSDL tại Tuần 2, các vấn đề kỹ thuật trước đây đã được giải quyết cụ thể:

1. **Thủ tục `sp_GhiNhanChamCong` (TC-CC-01):**
   - **Trạng thái:** [ĐÃ GIẢI QUYẾT / RESOLVED].
   - **Kết quả:** Đã cài đặt chính thức trong `database/02_Module_ChamCong_TV2.sql` với 7 tham số (`@MaNV`, `@NgayChamCong`, `@GioVao`, `@GioRa`, `@TrangThai`, `@GhiChu`, `@MaChamCong OUTPUT`). Đã kiểm thử thành công, trả về đúng mã chấm công tự tăng qua JDBC `CallableStatement`.
2. **Trigger kiểm tra giờ `trg_ChamCong_KiemTraGio` (TC-CC-10):**
   - **Trạng thái:** [ĐÃ GIẢI QUYẾT / RESOLVED].
   - **Kết quả:** Đã cài đặt chính thức trong `database/02_Module_ChamCong_TV2.sql` (`AFTER INSERT, UPDATE`), ném lỗi qua `RAISERROR (N'Lỗi: Giờ ra về phải lớn hơn giờ vào làm.', 16, 1)` và gọi `ROLLBACK TRANSACTION`. Đã kiểm thử thành công qua Job `2026-09-25-004`.
3. **Cơ chế Transaction nhập lô (TC-CC-11 đến TC-CC-14):**
   - **Trạng thái:** [ĐÃ GIẢI QUYẾT / RESOLVED].
   - **Kết quả:** Đã chốt thực thi giao dịch tại tầng Java JDBC `ChamCongService` (`conn.setAutoCommit(false)`, lặp gọi `insertInTransaction()`, `conn.commit()` và `conn.rollback()`). Đã kiểm thử tính nguyên tố thành công qua Job `2026-09-25-003`, `2026-09-25-005` và `2026-09-26-001`.
4. **Định dạng file nhập lô (Pending Decision duy nhất còn lại):**
   - **Trạng thái:** [CHƯA CHỐT / UNRESOLVED].
   - **Nội dung:** Quyết định giữa định dạng `.csv` (parser chuẩn Java SE) và bảng tính `.xlsx` (Apache POI) đang chờ thống nhất toàn nhóm ở Tuần 3.

---

## 5. Bảng ghi nhận kết quả thực thi kiểm thử thực tế (Test Execution Log)

Cập nhật kết quả kiểm thử thực tế dựa trên lịch sử thực thi các công việc kiểm thử tự động và bán tự động tại Tuần 2:

| Mã TC | Ngày kiểm thử | Phạm vi / Build | Trạng thái thực tế | Bằng chứng & Log đính kèm |
|---|---|---|---|---|
| **TC-CC-01** | 2026-09-25 / 2026-09-26 | Tuần 2 (Task 2.4A, 2.5) | **PASS** | Bridge job `2026-09-25-001`, `004`, `2026-09-26-001`: INSERT bản ghi chấm công đơn lẻ hợp lệ thành công qua `sp_GhiNhanChamCong`, sinh mã tự tăng. |
| **TC-CC-02** | 2026-09-25 | Tuần 2 (Task 2.1) | **PASS** | Bridge job `2026-09-25-001`: INSERT trùng lặp cặp `(MaNV, NgayChamCong)` bị SQL Server từ chối bởi ràng buộc `UQ_CHAMCONG_MaNV_Ngay`. |
| **TC-CC-03** | 2026-09-25 | Tuần 2 (Task 2.1) | **PASS** | Bridge job `2026-09-25-001`: INSERT với `MaNV=999` không tồn tại bị từ chối bởi khóa ngoại `FK_CHAMCONG_NHANVIEN`. |
| **TC-CC-04** | 2026-09-25 / 2026-09-26 | Tuần 2 (Task 2.1, 2.6) | **PASS** | Bridge job `2026-09-25-001`, `2026-09-26-002`: Chặn ngày công tương lai `2099-01-01` bởi ràng buộc CHECK `CHK_CHAMCONG_Ngay`. |
| **TC-CC-05** | — | — | Chưa thực thi / chưa có log | Chưa có log chạy kiểm thử độc lập cho trường hợp thiếu trường NOT NULL trên kịch bản tự động. |
| **TC-CC-06** | 2026-09-25 | Tuần 2 (Task 2.1) | **PASS** | Bridge job `2026-09-25-001`: INSERT với trạng thái không nằm trong danh mục bị từ chối bởi ràng buộc CHECK `CHK_CHAMCONG_TrangThai`. |
| **TC-CC-07** | 2026-09-26 | Tuần 2 (Task 2.5) | **PASS** | Bridge job `2026-09-26-001`: Trigger `trg_ChamCong_KiemTraNhanVien` chặn INSERT bản ghi chấm công cho nhân viên `NGHI_VIEC`, thực hiện ROLLBACK. |
| **TC-CC-08** | 2026-09-26 | Tuần 2 (Task 2.5) | **PASS** | Bridge job `2026-09-26-001`: Trigger `trg_ChamCong_KiemTraNhanVien` chặn UPDATE `MaNV` sang nhân viên `NGHI_VIEC`, thực hiện ROLLBACK. |
| **TC-CC-09** | 2026-09-26 | Tuần 2 (Task 2.5) | **PASS** | Bridge job `2026-09-26-001`: Chấm công cho nhân viên hoạt động `DANG_LAM_VIEC` thành công, trigger kiểm tra hợp lệ. |
| **TC-CC-10** | 2026-09-25 | Tuần 2 (Task 2.4A) | **PASS** | Bridge job `2026-09-25-004`: Trigger `trg_ChamCong_KiemTraGio` chặn `GioRa <= GioVao`, ném lỗi tiếng Việt và ROLLBACK. |
| **TC-CC-11** | 2026-09-25 / 2026-09-26 | Tuần 2 (Task 2.4B, 2.5) | **PASS** | Bridge job `2026-09-25-003`, `005` (Java), `2026-09-26-001` (SQL): Nhập lô hợp lệ commit thành công toàn bộ các dòng vào CSDL. |
| **TC-CC-12** | 2026-09-26 | Tuần 2 (Task 2.5) | **PASS** | Bridge job `2026-09-26-001`: Nhập lô có nhân viên `NGHI_VIEC` bị trigger chặn, toàn bộ giao dịch rollback về trạng thái ban đầu (0 bản ghi được lưu). |
| **TC-CC-13** | — | — | Chưa thực thi / chưa có log | Kịch bản kiểm thử batch rollback cho vi phạm ràng buộc duy nhất chưa có log ghi nhận độc lập. |
| **TC-CC-14** | — | — | Chưa thực thi / chưa có log | Kịch bản kiểm thử batch rollback cho lỗi logic giờ chưa có log ghi nhận độc lập. |
| **TC-CC-15** | 2026-09-26 | Tuần 2 (Task 2.6) | **PASS** | Bridge job `2026-09-26-002`, `2026-09-26-003`: View `vw_TongHopChamCongThang` đối soát số liệu chính xác cho nhiều nhân viên và nhiều tháng. |
| **TC-CC-16** | — | — | Chưa thực thi / chưa có log | Kế hoạch đo kiểm hiệu năng Covering Index `IX_CHAMCONG_MaNV_Ngay` trên tập dữ liệu lớn vào Tuần 3. |
| **TC-CC-17** | — | — | Chưa thực thi / chưa có log | Kế hoạch kiểm thử phân quyền đăng nhập vai trò `HR_Manager` vào Tuần 3. |
| **TC-CC-18** | — | — | Chưa thực thi / chưa có log | Kế hoạch kiểm thử phân quyền đăng nhập vai trò `Payroll_Officer` vào Tuần 3. |
| **TC-CC-19** | — | — | Chưa thực thi / chưa có log | Kế hoạch kiểm thử phân quyền đăng nhập vai trò `Employee` vào Tuần 3. |
| **TC-CC-20** | — | — | Chưa thực thi / chưa có log | Kế hoạch kiểm thử phân quyền quản trị vai trò `DB_Admin` vào Tuần 3. |
