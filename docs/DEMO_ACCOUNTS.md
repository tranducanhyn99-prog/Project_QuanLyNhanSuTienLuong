# Tài khoản demo cho đồ án

Cập nhật 08/10/2026. Theo lựa chọn của nhóm, mật khẩu của năm tài khoản QA được lưu rõ trong [database/demo_accounts.json](../database/demo_accounts.json) để thành viên cùng chạy và trình bày đồ án. Đây là bộ tài khoản đã được tạo trên SQL Server local; không phải mật khẩu DBA dùng để cài đặt.

| Username | Vai trò ứng dụng | SQL database role | Hồ sơ nhân viên |
|---|---|---|---|
| gui_admin | DB_Admin | role_DBAdmin | Không gắn nhân viên |
| gui_hr | HR_Manager | role_HRManager | Không gắn nhân viên |
| gui_payroll | Payroll_Officer | role_PayrollOfficer | Không gắn nhân viên |
| gui_a | Employee | role_Employee | gui-a@example.invalid |
| gui_b | Employee | role_Employee | gui-b@example.invalid |

Mở file JSON để lấy trường `Password` tương ứng với `User`. Mỗi tài khoản có mật khẩu riêng. Database lưu hash PBKDF2 của ứng dụng; SQL Server xác thực mật khẩu SQL login. Việc đưa mật khẩu demo vào repository không thay đổi cơ chế kiểm tra quyền của đồ án.

## Trạng thái trên máy hiện tại

- Server `localhost:1433`; database `PRJ_Fix_QA_20261007_01`.
- SQL login → database user cùng SID → database role → `TAIKHOAN.SqlLogin` đã khớp cho cả năm tài khoản.
- `gui_a` đang gắn MaNV **1008**, `gui_b` gắn MaNV **1009**. Trên máy khác phải tra theo email; không dùng cố định hai ID này.
- Cả năm đã PASS đăng nhập thực qua AuthService và từ chối mật khẩu sai. Log mapping: `build/gui-qa/mapping_verification.log`.
- Hai hồ sơ `TEST Giao dien A/B` đã có sẵn, lương cơ bản 26.000.000, ngày vào làm 2026-08-01. Chưa tự tạo công, phụ cấp, khấu trừ hoặc bảng lương để người dùng thao tác thử.
- DB_Admin có quyền quản trị database, không được tự cấp server role hoặc `ALTER ANY LOGIN`. Reset/khóa SQL login cần DBA có quyền server tương ứng; xem [SECURE_SETUP](SECURE_SETUP.md).

## Chạy giao diện

Tại thư mục repository:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1
```

Đăng nhập bằng username ở bảng và mật khẩu trong JSON. Chip demo `admin/hr_manager/payroll_officer/employee01` là nhóm tên khác, chỉ hiện khi bật `app.demo`; bộ `gui_*` được nhập trực tiếp tại LoginFrame.

## Chuẩn bị trên máy khác

Clone source không tạo tài khoản trên SQL Server. Cần SQL Server bật SQL authentication và TCP, JDK 11+, JDBC JAR, Windows SQL DBA và database QA đã cài module 01→05.

Có thể dùng `run_sql_verification.ps1 -ServerInstance localhost -KeepDatabase` để tạo/cài QA mới. Lấy đúng tên `QA target` trong output, rồi chạy:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\setup_gui_accounts.ps1 -Database PRJ_Fix_QA_TEN_VUA_TAO
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1 -Database PRJ_Fix_QA_TEN_VUA_TAO
```

Thay tên ví dụ bằng tên QA thực. Script cấp cùng bộ mật khẩu trong JSON, tạo hai hồ sơ theo email và mapping role trong transaction; từ chối ghi đè login/user/profile hoặc hồ sơ đã tồn tại. Không chạy lại chế độ tạo trên máy hiện tại. Có thể dùng `-VerifyOnly` để kiểm tra lại đăng nhập mà không tạo tài khoản.

Xem [kịch bản test giao diện](GUI_TEST_GUIDE.md) và [trạng thái các test tự động](FIX_TASKLIST.md).
