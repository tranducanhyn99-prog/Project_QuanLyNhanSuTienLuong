# Kiểm thử bằng giao diện Swing

Cập nhật 08/10/2026. Kịch bản dưới đây dùng database QA và bộ [tài khoản demo](DEMO_ACCOUNTS.md). Hai nhân viên A/B đã có sẵn; không thêm lại cùng CCCD/email. Đây là hướng dẫn thao tác cần người dùng thực hiện, không phải báo cáo rằng toàn bộ luồng đã được chạy thủ công.

## 1. Mở ứng dụng và kiểm tra đăng nhập

Tại thư mục repository:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1
```

Đăng nhập `gui_admin` bằng mật khẩu trong `database/demo_accounts.json`. Trước đó có thể thử mật khẩu sai: ứng dụng phải ở màn hình đăng nhập, báo lỗi và bật lại nút/ô nhập. Đăng nhập đúng phải mở MainFrame với tên/vai trò tương ứng.

Trên máy khác dùng `-Database` theo [DEMO_ACCOUNTS](DEMO_ACCOUNTS.md). Database cần schema mới và mapping trước khi đăng nhập. `start_app.ps1` mặc định dùng database dự án nếu chưa cấu hình `DB_URL`; launcher QA chọn database riêng tường minh.

## 2. Hồ sơ nhân viên

Đăng nhập `gui_hr`, vào **Hồ sơ Nhân viên**, chọn `TEST Giao dien A`.

1. Kiểm tra lương cơ bản 26.000.000 và ngày vào làm 2026-08-01.
2. Đổi ngày vào làm thành `2026-08-02`, địa chỉ thành `GUI QA da cap nhat`; bấm **Cập Nhật**.
3. Bấm **Tải lại danh sách**, chọn lại A: cả ngày, địa chỉ, phòng ban và chức vụ phải được giữ đúng.
4. Thử ngày `2026-02-30` hoặc lương `-1`: phải báo lỗi, dữ liệu cũ không đổi.
5. Nếu thử thêm nhân viên kèm hồ sơ tài khoản, HR chỉ được chọn Employee. Hồ sơ mới vẫn cần DBA cấp SQL login; nút cấp hồ sơ không tự tạo login.

## 3. Chấm công

Vào **Nhật ký Chấm công**, chọn A, ngày `2026-09-01`, giờ vào `08:00`, giờ ra `17:00`, trạng thái `CO_MAT`, rồi bấm **Ghi nhận chấm công**. Lọc tháng **9**, năm **2026** để xem kết quả.

- Ghi cùng nhân viên/ngày lần nữa phải bị từ chối.
- Giờ ra trước giờ vào phải bị từ chối.
- Mở **Tổng hợp Công tháng**: với duy nhất dòng vừa nhập, A có 1 ngày đi làm.
- Nếu thử điều chỉnh ngày công, tải lại và kiểm tra ngày mới được lưu, không tạo bản ghi thứ hai.

## 4. Phụ cấp và khấu trừ

Vào **Phụ cấp & Khấu trừ**, chọn **9/2026**:

1. Tab **Phụ Cấp Nhân Viên**: thêm A một khoản `GUI QA phu cap`, số tiền `500000`.
2. Tab **Khấu Trừ Nhân Viên**: thêm A một khoản `GUI QA khau tru`, số tiền `100000`.
3. Bấm **Lọc dữ liệu kỳ** và kiểm tra đúng A, kỳ và số tiền.
4. Thử số tiền âm: phải bị từ chối.

Nếu chạy lại kịch bản, kiểm tra dữ liệu hiện có trước khi thêm khoản; mỗi lần bấm Thêm hợp lệ có thể tạo thêm một khoản và làm thay đổi tổng.

## 5. Tính và chốt lương

Đăng xuất HR, đăng nhập `gui_payroll`. Vào **Tính toán Bảng lương**, chọn **9/2026**, ngày công chuẩn **26**, bấm **Tính / Cập nhật lương**, xác nhận và xem chi tiết A.

Với duy nhất 1 ngày công, phụ cấp 500.000 và khấu trừ 100.000:

| Chỉ tiêu | Kết quả mong đợi |
|---|---:|
| Tiền công | 26.000.000 / 26 × 1 = 1.000.000 |
| Tổng phụ cấp | 500.000 |
| Tổng khấu trừ | 100.000 |
| Thực nhận | 1.400.000 |

Tính lại kỳ chưa chốt phải giữ một kỳ lương và một chi tiết mỗi nhân viên. Vào **Báo cáo & Chốt lương**, chọn kỳ **9/2026**, bấm **Chốt bảng lương** và xác nhận.

Sau chốt, kiểm tra các thao tác sau bị chặn và dữ liệu giữ nguyên:

- Payroll tính lại hoặc xóa kỳ.
- HR thêm/sửa/xóa chấm công trong tháng 9.
- HR thêm/xóa phụ cấp hoặc khấu trừ tháng 9.

Payroll bấm **Mở lại bảng lương (Hủy chốt)**. HR phải sửa nguồn được trở lại; sau đó Payroll tính lại để cập nhật chi tiết lương. Xóa kỳ chỉ được thử với kỳ chưa chốt trên QA.

## 6. Quyền và báo cáo

| Người đăng nhập | Điều cần xác minh |
|---|---|
| gui_admin | Thấy Quản trị Tài khoản và các phân hệ nghiệp vụ |
| gui_hr | Được sửa hồ sơ/công/khoản phát sinh; không tính/chốt/xóa kỳ, không cấp role quản trị |
| gui_payroll | Được tính/chốt/mở lại lương; không sửa hồ sơ hoặc chấm công |
| gui_a | Báo cáo chỉ có phiếu lương A, không có lương B hay bảng lương toàn công ty |
| gui_b | Báo cáo chỉ có phiếu lương B |

Tạo thêm kỳ **8/2026** để kiểm tra dropdown báo cáo. Đổi nhanh giữa 8/2026 và 9/2026: bảng, tổng tiền và trạng thái phải cùng kỳ đang chọn. Trong lúc tải dữ liệu, cửa sổ vẫn phản hồi; thao tác ghi không được gửi trùng. Đăng xuất/đăng nhập người khác không được hiển thị kết quả cũ.

Reset mật khẩu và khóa SQL login cần quyền server bổ sung. `gui_admin` hiện chỉ có quyền database, nên thiếu quyền server phải báo lỗi và rollback; đó không phải lỗi mapping. DBA có thể thực hiện các bài quản trị login theo [SECURE_SETUP](SECURE_SETUP.md).

## 7. Ghi nhận kết quả

Ghi username, kỳ, thao tác, kết quả mong đợi/thực tế và ảnh khi gặp lỗi. Các PASS tự động đã có ở [FIX_TASKLIST](FIX_TASKLIST.md); ảnh cũ không thay thế kết quả thao tác trên bản mới.
