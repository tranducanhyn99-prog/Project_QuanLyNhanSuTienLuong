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
| `1` | Nguyễn Văn An | PB01 | CV01 | 15.000.000 | `DANG_LAM` | Nhân viên hợp lệ đang làm việc bình thường |
| `2` | Lê Thị Bình | PB01 | CV02 | 10.000.000 | `DANG_LAM` | Nhân viên hợp lệ đang làm việc bình thường |
| `3` | Trần Văn Cường | PB02 | CV02 | 12.000.000 | `DA_NGHI` | Nhân viên đã thôi việc (dùng test chặn chấm công) |
| `999` | (Không tồn tại) | — | — | — | — | Mã nhân viên không có trong CSDL (dùng test khóa ngoại) |

### 2.2 Dữ liệu ngày và ca làm việc giả định

- **Kỳ kiểm thử:** Tháng 09/2026.
- **Ngày làm việc mẫu:** `2026-09-21`, `2026-09-22`, `2026-09-23`.
- **Khung giờ ca chuẩn:** 08:00:00 (vào ca) đến 17:00:00 (hết ca).
- **Mốc thời gian tương lai dùng test chặn:** `2099-01-01`.

---

## 3. Bảng danh mục Test Cases chi tiết

### Nhóm 1: Ràng buộc tính hợp lệ & Toàn vẹn dữ liệu (Constraint / Schema)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Mức độ |
|---|---|---|---|---|---|---|
| **TC-CC-01** | Chấm công đơn lẻ hợp lệ | `MaNV=1` có trạng thái `DANG_LAM`; ngày chưa được chấm công | `MaNV = 1`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'`, `TrangThai = N'CO_MAT'` | 1. Nhập thông tin trên form hoặc chạy lệnh INSERT.<br>2. Xác nhận lưu dữ liệu. | Dữ liệu được thêm mới thành công vào bảng `CHAMCONG`. Hệ thống sinh mã `MaChamCong` tự tăng. | Nghiêm trọng (P1) |
| **TC-CC-02** | Ngăn chặn chấm công trùng lặp trong ngày | `MaNV=1` đã có bản ghi chấm công ngày `2026-09-21` | `MaNV = 1`, `Ngay = '2026-09-21'`, `GioVao = '08:30:00'`, `GioRa = '17:30:00'` | 1. Thực hiện INSERT bản ghi thứ 2 cho cùng nhân viên trong cùng ngày. | SQL Server chặn thực thi, ném lỗi vi phạm ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay`. Không thêm bản ghi thứ 2. | Nghiêm trọng (P1) |
| **TC-CC-03** | Chặn chấm công cho nhân viên không tồn tại | `MaNV=999` không có trong bảng `NHANVIEN` | `MaNV = 999`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Thực hiện lệnh INSERT với `MaNV=999`. | Thao tác thất bại do vi phạm khóa ngoại `FK_CHAMCONG_NHANVIEN`. Ứng dụng thông báo mã nhân viên không hợp lệ. | Nghiêm trọng (P1) |
| **TC-CC-04** | Chặn chấm công với ngày trong tương lai | Hệ thống hoạt động bình thường | `MaNV = 1`, `Ngay = '2099-01-01'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Nhập ngày công lớn hơn ngày hiện tại.<br>2. Thực hiện ghi nhận. | Thao tác thất bại do vi phạm ràng buộc kiểm tra `CHK_CHAMCONG_Ngay`. Thông báo lỗi ngày công không hợp lệ. | Cao (P2) |
| **TC-CC-05** | Chặn chấm công thiếu trường bắt buộc | Dữ liệu thiếu `GioVao` hoặc thiếu `NgayChamCong` | `MaNV = 1`, `Ngay = NULL`, `GioVao = NULL` | 1. Gửi request dữ liệu thiếu trường NOT NULL. | SQL Server từ chối lệnh INSERT; tầng DAO ném ngoại lệ thông báo không được để trống trường bắt buộc. | Cao (P2) |
| **TC-CC-06** | Chặn giá trị trạng thái không hợp lệ | Hệ thống hoạt động bình thường | `MaNV = 1`, `Ngay = '2026-09-22'`, `GioVao = '08:00:00'`, `TrangThai = N'KHONG_HOP_LE'` | 1. Thử lưu với giá trị trạng thái ngoài danh mục `CHK_CHAMCONG_TrangThai`. | Thao tác thất bại do vi phạm ràng buộc CHECK. Dữ liệu không được ghi vào DB. | Trung bình (P3) |

---

### Nhóm 2: Kiểm thử Trigger nghiệp vụ

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Mức độ |
|---|---|---|---|---|---|---|
| **TC-CC-07** | Chặn chấm công cho nhân viên đã nghỉ việc (`INSERT`) | `MaNV=3` có `TrangThai = 'DA_NGHI'` trong bảng `NHANVIEN` | `MaNV = 3`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Thực hiện INSERT bản ghi chấm công cho `MaNV=3`. | Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt, thực thi `RAISERROR` với thông điệp: "Không được phép ghi nhận chấm công cho nhân viên đã nghỉ việc" và `ROLLBACK TRANSACTION`. | Nghiêm trọng (P1) |
| **TC-CC-08** | Chặn đổi chấm công sang nhân viên đã nghỉ việc (`UPDATE`) | Bản ghi chấm công đã tồn tại với `MaNV=1`; `MaNV=3` đã thôi việc | UPDATE `CHAMCONG SET MaNV = 3 WHERE MaChamCong = 1` | 1. Thực thi câu lệnh UPDATE đổi `MaNV` sang nhân viên đã nghỉ. | Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt trên sự kiện UPDATE, gửi thông báo lỗi và ROLLBACK. | Nghiêm trọng (P1) |
| **TC-CC-09** | Cho phép chấm công với nhân viên đang làm việc | `MaNV=2` có `TrangThai = 'DANG_LAM'` | `MaNV = 2`, `Ngay = '2026-09-21'`, `GioVao = '08:00:00'`, `GioRa = '17:00:00'` | 1. Thực hiện INSERT chấm công cho `MaNV=2`. | Trigger kiểm tra thành công, bản ghi được lưu hợp lệ vào CSDL. | Nghiêm trọng (P1) |
| **TC-CC-10** | Chặn giờ ra nhỏ hơn hoặc bằng giờ vào | Nhân viên hợp lệ `MaNV=1` | `MaNV = 1`, `Ngay = '2026-09-22'`, `GioVao = '17:00:00'`, `GioRa = '08:00:00'` | 1. Nhập giờ ra sớm hơn giờ vào.<br>2. Nhấn lưu chấm công. | Bị chặn bởi cơ chế kiểm tra giờ (Trigger hoặc Constraint / Service); thông báo lỗi giờ ra phải lớn hơn giờ vào. Không lưu dữ liệu sai lệch. *(Ghi chú: xem mục Pending Decisions)*. | Nghiêm trọng (P1) |

---

### Nhóm 3: Kiểm thử Transaction nhập chấm công theo lô (Batch Processing)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Mức độ |
|---|---|---|---|---|---|---|
| **TC-CC-11** | Nhập lô toàn bộ hợp lệ (Commit thành công) | CSDL sạch chưa có dữ liệu ngày `2026-09-23` cho các nhân viên trong lô | Danh sách 3 nhân viên:<br>- Dòng 1: NV 1, ngày 23/09, 08:00-17:00<br>- Dòng 2: NV 2, ngày 23/09, 08:15-17:00<br>- Dòng 3: NV 1, ngày 22/09, 08:00-17:00 | 1. Khởi chạy Transaction nhập lô từ `ChamCongService`.<br>2. Gửi batch lệnh xuống SQL Server.<br>3. Kiểm tra kết quả thực thi. | Toàn bộ 3 bản ghi được lưu thành công vào CSDL. Lệnh `COMMIT` được gọi. Số lượng bản ghi trong DB tăng thêm đúng 3. | Nghiêm trọng (P1) |
| **TC-CC-12** | Nhập lô thất bại do có nhân viên đã nghỉ việc (Rollback toàn bộ) | Dữ liệu kiểm thử có dòng vi phạm; NV3 đã nghỉ việc | Danh sách 3 dòng:<br>- Dòng 1: NV 1, ngày 24/09 (hợp lệ)<br>- Dòng 2: NV 3, ngày 24/09 (NV đã nghỉ)<br>- Dòng 3: NV 2, ngày 24/09 (hợp lệ) | 1. Bắt đầu Transaction nhập lô.<br>2. Ghi nhận dòng 1, dòng 2.<br>3. Đến dòng 2, Trigger `trg_ChamCong_KiemTraNhanVien` kích hoạt lỗi. | Toàn bộ Transaction bị `ROLLBACK`. Kiểm tra bảng `CHAMCONG`: không có bản ghi nào của ngày 24/09 được lưu (kể cả dòng 1 của NV 1). Đảm bảo tính nguyên tố (All-or-Nothing). | Nghiêm trọng (P1) |
| **TC-CC-13** | Nhập lô thất bại do có dòng trùng ngày (Rollback toàn bộ) | NV 1 đã có bản ghi chấm công ngày 21/09/2026 | Danh sách 2 dòng:<br>- Dòng 1: NV 2, ngày 21/09 (hợp lệ)<br>- Dòng 2: NV 1, ngày 21/09 (bị trùng) | 1. Bắt đầu Transaction nhập lô.<br>2. Thực thi batch insert. | Gặp lỗi vi phạm `UQ_CHAMCONG_MaNV_Ngay` tại dòng 2. Transaction tự động `ROLLBACK`. Không lưu bản ghi dòng 1 của NV 2. | Nghiêm trọng (P1) |
| **TC-CC-14** | Nhập lô thất bại do lỗi logic giờ làm việc (Rollback toàn bộ) | CSDL sẵn sàng | Danh sách 2 dòng:<br>- Dòng 1: NV 1, ngày 25/09 (hợp lệ)<br>- Dòng 2: NV 2, ngày 25/09, Vào: 17:00, Ra: 08:00 | 1. Bắt đầu Transaction nhập lô.<br>2. Thực thi xử lý dữ liệu. | Phát hiện lỗi giờ không hợp lệ tại dòng 2. Transaction bị hủy bỏ và rollback toàn bộ. Không dòng nào được lưu. | Cao (P2) |

---

### Nhóm 4: Kiểm thử View tổng hợp & Index hiệu năng

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Mức độ |
|---|---|---|---|---|---|---|
| **TC-CC-15** | Đối soát số liệu View `vw_TongHopChamCongThang` | Đã có dữ liệu chấm công tháng 09/2026 của NV 1 (2 ngày bình thường, 1 ngày đi trễ) | Tháng = 9, Năm = 2026 | 1. Chạy câu truy vấn: `SELECT * FROM vw_TongHopChamCongThang WHERE MaNV = 1 AND Thang = 9 AND Nam = 2026`. | Trả về chính xác: `SoNgayDiLam = 3`, `SoLanDiTre = 1`, `TongSoGioLam` được tính đúng theo tổng giờ giữa GioRa và GioVao. | Cao (P2) |
| **TC-CC-16** | Đo kiểm hiệu năng Index `IX_CHAMCONG_MaNV_Ngay` | Bảng `CHAMCONG` có dữ liệu lớn (chuẩn bị dữ liệu mẫu ở Tuần 3) | Lọc theo `MaNV = 1` và khoảng ngày trong tháng | 1. Bật `SET STATISTICS IO, TIME ON`.<br>2. Chạy truy vấn lọc khi không có Index.<br>3. Tạo Index `IX_CHAMCONG_MaNV_Ngay` kèm `INCLUDE`.<br>4. Chạy lại truy vấn và đối chiếu Actual Execution Plan. | Khi có Index, phương thức truy cập chuyển từ `Clustered Index Scan` / `Table Scan` sang `Index Seek`. Số lượng logical reads giảm rõ rệt. | Cao (P2) |

---

### Nhóm 5: Kiểm thử Bảo mật & Phân quyền (Security Matrix)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Mức độ |
|---|---|---|---|---|---|---|
| **TC-CC-17** | Phân quyền vai trò `HR_Manager` | Đăng nhập bằng `login_HRManager` (`role_HRManager`) | Thao tác trên bảng `CHAMCONG` | 1. Thực hiện SELECT dữ liệu chấm công.<br>2. Thực hiện INSERT/UPDATE chấm công. | Cả hai thao tác SELECT và INSERT/UPDATE đều được SQL Server cho phép thực thi bình thường. | Nghiêm trọng (P1) |
| **TC-CC-18** | Chặn quyền sửa/xóa đối với vai trò `Payroll_Officer` | Đăng nhập bằng `login_PayrollOfficer` (`role_PayrollOfficer`) | Thao tác trên bảng `CHAMCONG` | 1. Thực hiện SELECT chấm công để xem dữ liệu.<br>2. Thử thực hiện lệnh INSERT hoặc DELETE. | Thao tác SELECT thành công. Lệnh INSERT hoặc DELETE bị SQL Server từ chối ngay lập tức với lỗi: `The INSERT/DELETE permission was denied on the object 'CHAMCONG'`. | Nghiêm trọng (P1) |
| **TC-CC-19** | Chặn truy cập bảng chung đối với vai trò `Employee` | Đăng nhập bằng `login_Employee` (`role_Employee`) | Lệnh truy vấn trực tiếp bảng | 1. Chạy câu lệnh `SELECT * FROM CHAMCONG`. | SQL Server từ chối truy cập (DENY SELECT on CHAMCONG). Nhân viên không được xem thông tin chấm công của người khác. | Cao (P2) |
| **TC-CC-20** | Kiểm tra toàn quyền vai trò `DB_Admin` | Đăng nhập bằng `login_DBAdmin` (`role_DBAdmin`) | Thao tác quản trị | 1. Thực hiện đầy đủ SELECT, INSERT, UPDATE, DELETE, tạo Index trên `CHAMCONG`. | Mọi thao tác đều thực thi thành công do thuộc role `db_owner`. | Cao (P2) |

---

## 4. Các điểm chưa chốt ảnh hưởng đến kiểm thử (Pending Decisions)

Trong quá trình thiết kế test case Tuần 1, TV2 ghi nhận các yếu tố ảnh hưởng trực tiếp đến kịch bản kiểm thử nhưng hiện **CHƯA ĐƯỢC CHỐT (UNRESOLVED)**:

1. **Kịch bản kiểm thử thủ tục `sp_GhiNhanChamCong`:**
   - Trường hợp thủ tục này do TV3 cài đặt: Cần văn bản thống nhất danh sách tham số (Parameters) và mã lỗi trả về (Error Codes/Return Values) trước khi viết test case tích hợp cho tầng DAO.
   - Trạng thái: **CHƯA CHỐT (UNRESOLVED)**.
2. **Kịch bản kiểm thử kiểm tra giờ `trg_ChamCong_KiemTraGio` (TC-CC-10):**
   - Trường hợp trigger này do TV3 cài đặt: Cần thống nhất thông điệp lỗi chính xác từ `RAISERROR` để tầng UI/Service có thể bắt đúng ngoại lệ và hiển thị tiếng Việt thân thiện.
   - Trạng thái: **CHƯA CHỐT (UNRESOLVED)**.
3. **Cơ chế giả lập kiểm thử Transaction nhập lô (TC-CC-11 đến TC-CC-14):**
   - Phụ thuộc vào quyết định thực thi transaction qua SP hay qua JDBC Connection (`setAutoCommit(false)`). Cả hai kịch bản đều đã được thiết kế sẵn sàng để áp dụng theo quyết định cuối cùng của nhóm.
   - Trạng thái: **CHƯA CHỐT (UNRESOLVED)**.

---

## 5. Biểu mẫu ghi nhận kết quả kiểm thử thực tế (Test Execution Log Template)

Biểu mẫu sẽ được cập nhật trong giai đoạn chạy thử nghiệm tại Tuần 3:

```text
================================================================================
BIÊN BẢN GHI NHẬN KẾT QUẢ KIỂM THỬ MODULE CHẤM CÔNG (TUẦN 3)
Người thực hiện: Phạm Minh Quân (TV2)
Môi trường test: SQL Server 2019 / JDK 17 / NetBeans / SSMS
================================================================================
| Mã TC    | Ngày test  | Phiên bản build | Trạng thái (PASS/FAIL) | Ghi chú & Log đính kèm |
|----------|------------|-----------------|------------------------|------------------------|
| TC-CC-01 | ...        | Build v0.2       | [Chờ chạy Tuần 3]       |                        |
| TC-CC-02 | ...        | Build v0.2       | [Chờ chạy Tuần 3]       |                        |
| TC-CC-07 | ...        | Build v0.2       | [Chờ chạy Tuần 3]       |                        |
| TC-CC-11 | ...        | Build v0.2       | [Chờ chạy Tuần 3]       |                        |
| TC-CC-12 | ...        | Build v0.2       | [Chờ chạy Tuần 3]       |                        |
| TC-CC-16 | ...        | Build v0.2       | [Chờ chạy Tuần 3]       |                        |
================================================================================
```
