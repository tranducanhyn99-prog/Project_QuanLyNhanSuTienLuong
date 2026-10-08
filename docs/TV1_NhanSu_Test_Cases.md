# TV1 – BỘ TEST CASE KIỂM THỬ MODULE NHÂN SỰ
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Nguyễn Minh Trí (TV1, MSSV: 24110359)  
**Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284) – GVHD: TS. Phan Thị Thể  
**Module:** Quản lý Phòng Ban, Chức Vụ, Hồ Sơ Nhân Viên, Tích Hợp Tài Khoản  
**Phiên bản:** 2.0 (Nghiệm thu Tuần 2 & Tuần 3)  
**Ngày cập nhật:** 08/10/2026

> Đây là bộ kịch bản thiết kế ban đầu, không phải báo cáo PASS hiện hành. Schema và phân quyền đã thay đổi: `fn_TinhSoNgayCong` được cài ở module 02; `TAIKHOAN.MatKhau` là `VARCHAR(255)` chứa hash PBKDF2 versioned và `SqlLogin` được DBA mapping riêng. HR chỉ tạo hồ sơ tài khoản Employee; hồ sơ chờ DBA provision SQL login. Không chạy khối SQL minh họa cũ ở mục 4 trên DB thật. Kết quả regression đã chạy xem [FIX_TASKLIST](FIX_TASKLIST.md).

---

## 1. Mục tiêu và phạm vi kiểm thử

Tài liệu này đặc tả toàn bộ các kịch bản kiểm thử (Test Cases) nhằm xác minh tính đúng đắn, tính toàn vẹn dữ liệu và độ tin cậy của **Module Nhân sự** do **Nguyễn Minh Trí (TV1)** phụ trách, bao gồm:
1. **Ràng buộc toàn vẹn Schema & Constraints:** Kiểm tra các ràng buộc mức cột/bảng trên `PHONGBAN`, `CHUCVU`, `NHANVIEN` (`CHECK`, `UNIQUE`, `DEFAULT`, `FOREIGN KEY`).
2. **Trigger nghiệp vụ (`trg_NhanVien_KhongXoaKhiDaPhatSinhLuong`):** Kiểm tra cơ chế ngăn chặn xóa cứng (Hard Delete) đối với nhân viên đã phát sinh dữ liệu chấm công hoặc lương; kiểm tra quy tắc xóa hợp lệ khi chưa có dữ liệu phụ thuộc.
3. **Stored Procedure & Transaction (`sp_ThemNhanVien`):** Kịch bản lịch sử kiểm tra tính nguyên tử khi tạo nhân viên và hồ sơ tài khoản ứng dụng. HR chỉ được tạo role Employee; SQL login và mapping là bước DBA riêng.
4. **Function (`fn_TinhSoNgayCong`) & View (`vw_NhanVien_PhongBan_ChucVu`):** Function được cài ở module 02 sau bảng chấm công. Procedure tính payroll hiện đếm trực tiếp từ `CHAMCONG` trong transaction, không gọi function này.
5. **Giao diện người dùng Java Swing & Phân quyền:** Xác minh các màn hình `NhanVienPanel`, `DanhMucPanel` và ma trận phân quyền người dùng tương ứng với vai trò `HR_Manager`, `DB_Admin`, `Payroll_Officer`, `Employee`.
6. **Chỉ mục hiệu năng Index (`IX_NHANVIEN_HoTen`):** Chuẩn bị kịch bản đo lường `SET STATISTICS IO, TIME ON` và Execution Plan để chứng minh tối ưu hóa truy vấn.

---

## 2. Môi trường và dữ liệu giả định kiểm thử

### 2.1 Dữ liệu danh mục nền tảng mẫu

| Bảng | Mã | Tên danh mục | Thuộc tính đặc trưng | Trạng thái |
|---|:---:|---|---|:---:|
| `PHONGBAN` | 1 | Ban Giám Đốc | SĐT: `0283896864` | `HOAT_DONG` |
| `PHONGBAN` | 2 | Phòng Nhân Sự | SĐT: `0283896865` | `HOAT_DONG` |
| `PHONGBAN` | 3 | Phòng Kế Toán | SĐT: `0283896866` | `HOAT_DONG` |
| `PHONGBAN` | 4 | Phòng Kỹ Thuật | SĐT: `0283896867` | `HOAT_DONG` |
| `CHUCVU` | 1 | Giám Đốc | Phụ cấp: 5.000.000 VNĐ | — |
| `CHUCVU` | 2 | Trưởng Phòng | Phụ cấp: 3.000.000 VNĐ | — |
| `CHUCVU` | 3 | Phó Phòng | Phụ cấp: 1.500.000 VNĐ | — |
| `CHUCVU` | 4 | Chuyên Viên | Phụ cấp: 500.000 VNĐ | — |
| `CHUCVU` | 5 | Nhân Viên | Phụ cấp: 0 VNĐ | — |

### 2.2 Dữ liệu nhân viên mẫu kiểm thử

| Mã NV | Họ và tên | Ngày sinh | Giới tính | CCCD | SĐT | Email | Lương cơ bản | Mã PB | Mã CV | Trạng thái |
|:---:|---|:---:|:---:|:---:|:---:|---|---:|:---:|:---:|:---:|
| **1** | Nguyễn Văn An | 15/05/1990 | Nam | `079090000001` | `0901234567` | `an.nguyen@company.com` | 15.000.000 | 2 | 2 | `DANG_LAM_VIEC` |
| **2** | Lê Thị Bình | 20/08/1995 | Nữ | `079095000002` | `0912345678` | `binh.le@company.com` | 12.000.000 | 3 | 4 | `DANG_LAM_VIEC` |
| **3** | Trần Văn Cường | 10/12/1988 | Nam | `079088000003` | `0923456789` | `cuong.tran@company.com` | 10.000.000 | 4 | 5 | `NGHI_VIEC` |
| **4** | Phạm Thị Dung | 01/01/2000 | Nữ | `079000000004` | `0934567890` | `dung.pham@company.com` | 9.000.000 | 2 | 5 | `DANG_LAM_VIEC` |

---

## 3. Bảng danh mục Test Cases chi tiết

### Nhóm 1: Ràng buộc tính hợp lệ & Toàn vẹn dữ liệu (Constraint / Schema)

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Trạng thái | Mức độ |
|---|---|---|---|---|---|:---:|:---:|
| **TC-NS-01** | Thêm nhân viên hợp lệ qua SQL INSERT | Phòng ban (MaPB=2), Chức vụ (MaCV=4) tồn tại | `HoTen = N'Hoàng Minh Đức'`, `NgaySinh = '1998-04-12'`, `GioiTinh = N'Nam'`, `CCCD = '079098000005'`, `SDT = '0945678901'`, `Email = 'duc.hoang@company.com'`, `LuongCoBan = 11000000` | 1. Chạy câu lệnh `INSERT INTO NHANVIEN`.<br>2. Kiểm tra bản ghi sinh ra. | Bản ghi được thêm thành công, `MaNV` tự tăng, `NgayVaoLam` mặc định là ngày hiện tại (`GETDATE()`), `TrangThai` là `DANG_LAM_VIEC`. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-02** | Chặn nhân viên chưa đủ 18 tuổi | Hệ thống hoạt động bình thường | `NgaySinh = DATEADD(YEAR, -17, GETDATE())` (17 tuổi), các trường khác hợp lệ | 1. Thực hiện lệnh `INSERT INTO NHANVIEN`. | Vi phạm ràng buộc `CHK_NHANVIEN_DoTuoi` (`DATEDIFF >= 18`). Lệnh INSERT bị SQL Server từ chối. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-03** | Chặn CCCD không đúng 12 chữ số | Hệ thống hoạt động bình thường | `CCCD = '12345'` (5 số) hoặc `'07909800000A'` (chứa chữ) | 1. Thực hiện lệnh INSERT với CCCD sai định dạng. | Vi phạm ràng buộc `CHK_NHANVIEN_CCCD`. Câu lệnh bị chặn, báo lỗi vi phạm CHECK. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-04** | Chặn CCCD trùng lặp | NV1 đã có CCCD `'079090000001'` | Nhập nhân viên mới với `CCCD = '079090000001'` | 1. Thực hiện lệnh INSERT với CCCD đã tồn tại. | Vi phạm ràng buộc `UQ_NHANVIEN_CCCD`. Thao tác bị SQL Server chặn ngay lập tức. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-05** | Chặn Số điện thoại không hợp lệ | Hệ thống hoạt động bình thường | `SoDienThoai = '1234567890'` (không bắt đầu bằng 0) hoặc `'090123'` (dưới 10 số) | 1. Chạy câu lệnh INSERT với SĐT không chuẩn format Việt Nam. | Vi phạm ràng buộc `CHK_NHANVIEN_SDT` (`LEN = 10 AND LIKE '0%'`). Câu lệnh bị từ chối. | Thiết kế (chưa xác minh) | Cao (P2) |
| **TC-NS-06** | Chặn Email sai định dạng | Hệ thống hoạt động bình thường | `Email = 'duc.hoangcompany.com'` (thiếu `@`) | 1. Chạy câu lệnh INSERT với Email sai format. | Vi phạm ràng buộc `CHK_NHANVIEN_Email` (`LIKE '%_@__%.__%'`). Thao tác bị chặn. | Thiết kế (chưa xác minh) | Cao (P2) |
| **TC-NS-07** | Chặn Lương cơ bản $\le 0$ | Hệ thống hoạt động bình thường | `LuongCoBan = 0` hoặc `-5000000` | 1. Chạy câu lệnh INSERT với lương không dương. | Vi phạm ràng buộc `CHK_NHANVIEN_LuongCoBan` (`LuongCoBan > 0`). Thao tác bị từ chối. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-08** | Chặn trùng tên Phòng ban | Phòng ban `Phòng Nhân Sự` đã tồn tại | Thêm mới `PHONGBAN` với `TenPB = N'Phòng Nhân Sự'` | 1. Chạy lệnh `INSERT INTO PHONGBAN`. | Vi phạm ràng buộc `UQ_PHONGBAN_TenPB`. SQL Server từ chối câu lệnh. | Thiết kế (chưa xác minh) | Cao (P2) |

---

### Nhóm 2: Kiểm thử Trigger nghiệp vụ Soft Delete

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Trạng thái | Mức độ |
|---|---|---|---|---|---|:---:|:---:|
| **TC-NS-09** | Chặn xóa nhân viên đã có dữ liệu chấm công | NV1 đã phát sinh bản ghi trong bảng `CHAMCONG` | `DELETE FROM NHANVIEN WHERE MaNV = 1` | 1. Thực thi câu lệnh `DELETE FROM NHANVIEN WHERE MaNV = 1`. | Trigger `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong` kích hoạt, báo lỗi: `Không được phép xóa nhân viên đã có dữ liệu chấm công hoặc lương. Vui lòng chuyển trạng thái sang NGHI_VIEC!` và `ROLLBACK TRANSACTION`. Bản ghi NV1 vẫn tồn tại nguyên vẹn. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-10** | Chặn xóa nhân viên đã có chi tiết bảng lương | NV2 đã có bản ghi trong `CHITIETBANGLUONG` | `DELETE FROM NHANVIEN WHERE MaNV = 2` | 1. Thực thi câu lệnh `DELETE FROM NHANVIEN WHERE MaNV = 2`. | Trigger kích hoạt, phát hiện dữ liệu lương và `ROLLBACK TRANSACTION`. Dữ liệu nhân viên không bị mất mát. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-11** | Cho phép xóa nhân viên mới chưa có bất kỳ dữ liệu phát sinh | NV mới tạo (chưa có chấm công, chưa có lương, có tài khoản liên kết) | `DELETE FROM NHANVIEN WHERE MaNV = @NewMaNV` | 1. Tạo nhân viên thử nghiệm kèm tài khoản.<br>2. Chạy lệnh `DELETE FROM NHANVIEN`. | Trigger kiểm tra không có dữ liệu lương/chấm công; thực hiện xóa tài khoản cascade trong `TAIKHOAN` rồi xóa bản ghi trong `NHANVIEN`. Thao tác thành công. | Thiết kế (chưa xác minh) | Cao (P2) |

---

### Nhóm 3: Kiểm thử Stored Procedure & Transaction tạo nhân viên + tài khoản

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Trạng thái | Mức độ |
|---|---|---|---|---|---|:---:|:---:|
| **TC-NS-12** | Thêm nhân viên đơn lẻ (không tạo tài khoản) qua SP | Phòng ban và chức vụ hợp lệ | `TaoTaiKhoan = 0`, thông tin cá nhân đầy đủ | 1. Thực thi `sp_ThemNhanVien` với `@TaoTaiKhoan = 0`.<br>2. Đọc giá trị `@NewMaNV`. | Thêm thành công bản ghi vào `NHANVIEN`, trả về `@NewMaNV > 0`. Không có bản ghi nào tạo mới trong `TAIKHOAN`. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-13** | Thêm nhân viên kèm hồ sơ tài khoản ứng dụng | Tên đăng nhập chưa tồn tại; actor được phép tạo Employee | `TaoTaiKhoan = 1`, `VaiTro = 'Employee'`, hash versioned hợp lệ | 1. Gọi procedure theo signature hiện hành trên QA.<br>2. Kiểm tra nhân viên và profile. | Transaction tạo profile Employee; SQL login chưa tồn tại cho đến khi DBA provision/mapping. | Kịch bản; xem FIX_TASKLIST cho evidence | Nghiêm trọng (P1) |
| **TC-NS-14** | Rollback toàn bộ khi tên đăng nhập bị trùng | Username test đã tồn tại trong QA | `TaoTaiKhoan = 1`, thông tin nhân viên hợp lệ | Gọi procedure đúng signature trên QA; kiểm tra cả `NHANVIEN` và `TAIKHOAN`. | Procedure báo lỗi duplicate và rollback nhân viên/profile mới. | Kịch bản; xem FIX_TASKLIST cho evidence | Nghiêm trọng (P1) |
| **TC-NS-15** | Rollback khi Phòng ban hoặc Chức vụ không tồn tại | `MaPB = 9999` không có trong `PHONGBAN` | `MaPB = 9999`, `MaCV = 1` | 1. Gọi `sp_ThemNhanVien`. | SP kiểm tra và báo lỗi `Phòng ban không tồn tại hoặc đã ngừng hoạt động!`, Transaction tự động Rollback. | Thiết kế (chưa xác minh) | Cao (P2) |

---

### Nhóm 4: Kiểm thử Function & View của TV1

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Trạng thái | Mức độ |
|---|---|---|---|---|---|:---:|:---:|
| **TC-NS-16** | Kiểm tra Function `fn_TinhSoNgayCong` | Có fixture tháng được tạo riêng trong QA | Mã nhân viên/tháng/năm của fixture | Chạy function sau khi module 02 cài đặt xong; so sánh với số ngày có trạng thái đi làm hợp lệ | Đếm `CO_MAT`, `DI_TRE`, `VE_SOM`; không dùng dữ liệu MaNV=1 hoặc số liệu giả định làm bằng chứng hiện hành. | Kịch bản; xem FIX_TASKLIST cho evidence | Cao (P2) |
| **TC-NS-17** | Function trả về 0 khi fixture không có công | Employee fixture không có chấm công trong tháng test | Các tham số từ fixture QA | Gọi function trên QA riêng | Trả về 0; không khẳng định dữ liệu cố định trong production. | Kịch bản; xem FIX_TASKLIST cho evidence | Trung bình (P3) |
| **TC-NS-18** | Đối soát dữ liệu View `vw_NhanVien_PhongBan_ChucVu` | Đã có dữ liệu `NHANVIEN`, `PHONGBAN`, `CHUCVU`, `TAIKHOAN` | `SELECT * FROM vw_NhanVien_PhongBan_ChucVu WHERE MaNV = @FixtureMaNV` | 1. Truy vấn View và đối chiếu các trường `TenPB`, `TenCV`, `PhuCapChucVu`, `TenDangNhap`, `VaiTro`. | Khớp 100% thông tin liên kết giữa các bảng. Nhân viên chưa có tài khoản vẫn hiển thị (nhờ `LEFT JOIN TAIKHOAN`). | Thiết kế (chưa xác minh) | Cao (P2) |

---

### Nhóm 5: Kiểm thử Giao diện & Phân quyền tầng Ứng dụng

| Mã TC | Tên kịch bản | Điều kiện tiên quyết | Dữ liệu đầu vào | Các bước thực hiện | Kết quả mong đợi | Trạng thái | Mức độ |
|---|---|---|---|---|---|:---:|:---:|
| **TC-NS-19** | Quyền quản lý nhân sự của `HR_Manager` | Đăng nhập với tài khoản có vai trò `HR_Manager` | Thao tác trên giao diện | 1. Mở menu **Nhân viên** và **Danh mục**.<br>2. Thêm mới, chỉnh sửa thông tin nhân sự. | Các menu và nút chức năng hoạt động bình thường, lưu thành công vào CSDL. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |
| **TC-NS-20** | Chặn truy cập menu Nhân sự đối với `Payroll_Officer` và `Employee` | Đăng nhập với vai trò `Payroll_Officer` hoặc `Employee` | Xem thanh menu trên `MainFrame` | 1. Quan sát giao diện chính sau khi đăng nhập. | Menu **Nhân viên** và **Danh mục** bị ẩn hoàn toàn khỏi giao diện người dùng theo đúng ma trận phân quyền. | Thiết kế (chưa xác minh) | Nghiêm trọng (P1) |

---

## 4. Chạy kiểm thử hiện hành

Các khối SQL minh họa đời đầu đã bị loại khỏi tài liệu vì dùng username/hash cố định, MaNV giả định và có thể in PASS giả. Chạy các runner QA hiện hành trong [FIX_TASKLIST](FIX_TASKLIST.md) và [SECURE_SETUP](SECURE_SETUP.md); không chạy test ghi dữ liệu trên database dự án.

## 5. Kết luận tài liệu

- Bộ này là kịch bản thiết kế lịch sử, không chứng minh 20/20 PASS hiện hành. Các kết quả runtime theo từng suite, database QA và giới hạn của chúng được ghi tại [FIX_TASKLIST](FIX_TASKLIST.md).
- Module cài hiện có các thay đổi schema/bảo mật sau phiên bản tài liệu gốc; ưu tiên SQL scripts và hướng dẫn cài/migration hiện hành khi triển khai.
