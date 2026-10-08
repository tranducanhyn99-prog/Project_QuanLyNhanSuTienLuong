# Cài đặt SQL Server

Chạy các module theo thứ tự **01 → 05**. Hướng dẫn cấu hình TLS, login cá nhân, migration và kiểm thử ở [SECURE_SETUP](../docs/SECURE_SETUP.md).

| Module | Nội dung chính |
|---|---|
| 01_Module_NhanSu_TV1.sql | Nhân sự/danh mục/tài khoản, LICHSULUONG và ngày nghỉ, procedure thêm nhân viên |
| 02_Module_ChamCong_TV2.sql | Chấm công, ràng buộc/trigger/view/index, fn_TinhSoNgayCong sau khi có CHAMCONG |
| 03_phucap_khautru_TV3.sql | Phụ cấp/khấu trừ, fn_TongKhauTru và fn_TongPhuCap, xóa kỳ nháp |
| 04_Module_TinhLuong_TV4.sql | Header/chi tiết lương, tính và tính lại theo lịch sử/snapshot, transaction payroll |
| 05_Security_Payroll_TV5.sql | Chốt/mở kỳ, khóa nguồn/chi tiết sau chốt, SQL roles/grants, view lương cá nhân, procedure mapping/admin |
| 06_Demo_Data.sql | **Tùy chọn:** dữ liệu demo sau đủ schema; không cấp login/mật khẩu và không DELETE tháng hiện có |

Các module có lệnh USE database dự án. Với QA, tạo bản script thay mọi USE bằng tên QA đã kiểm tra, không chỉ đổi toolbar SSMS. Chạy bằng DBA, giữ SET options đầu file, kiểm tra Messages không có lỗi. Chạy lại 01→05 cập nhật schema/procedure/grants; xác minh trên database riêng trước khi migration dữ liệu thật. Schema không provision login tự động.

DBA cấp SQL login cá nhân bằng `com.tools.ProvisionIdentity` sau cài schema; Employee phải có hồ sơ nhân viên. SQL user chỉ được cấp role đúng với profile. DB_Admin kế thừa db_owner trong database; quản trị login server cần quyền DBA riêng. Không dùng shared sa credentials để vận hành ứng dụng.

Để chạy đồ án, `demo_accounts.json` là bộ năm SQL login/password/role được nhóm chủ động lưu trong Git. `setup_gui_accounts.ps1 -Database PRJ_Fix_QA_...` tạo hồ sơ A/B và cấp mapping trên QA đã cài schema, từ chối ghi đè principal cũ; không sửa dữ liệu database gốc. Cách chuẩn bị và chạy ở [DEMO_ACCOUNTS](../docs/DEMO_ACCOUNTS.md). Module 06 vẫn chỉ seed dữ liệu nghiệp vụ, không tự cấp các login này.

## Kiểm thử

Chỉ chạy script có ghi fixture trên database riêng `PRJ_Fix_QA_...`. Không chạy test/benchmark trên database đang sử dụng.

- `tests/TV4_Payroll_E2E.sql`: Oracle công thức/rollback/chốt/mở kỳ và cleanup.
- `tests/Fix_Regression.sql`: Lịch sử nhân viên/mức lương, bất biến các nguồn và chi tiết sau chốt, function sum/empty và cleanup.
- `test_security_roles_TV5.sql`: Ma trận grants và lệnh bị từ chối thực tế với test users WITHOUT LOGIN; **không** thay thế test login cá nhân/ORIGINAL_LOGIN.
- `tests/TV4_Payroll_Concurrency_SessionA.sql` + `SessionB.sql`: Chạy hai session theo chỉ dẫn trong file. Cần cả hai kết quả và cleanup; routing file `test_concurrency_demo_TV5.sql` không báo PASS.
- `tests/Closed_Source_Concurrency.ps1 -Database PRJ_Fix_QA_...`: Race giữa chốt và sửa công/phụ cấp/khấu trừ/chi tiết; hai connection thật, kiểm tra wait/reject/dữ liệu/transaction/cleanup. SQL verification runner có chạy case này.
- `test_module_nhansu_TV1.sql`, `tests_TV2/test_module_chamcong_TV2.sql`, `tests_TV3/test_module_phucap_khautru_TV3.sql`: Bộ SQL từng module; lỗi assertion phải làm process FAIL.
- Benchmark phải giữ STATISTICS IO/TIME và execution plan thực; không dùng số liệu trong ảnh dựng.

`run_sql_verification.ps1 -ServerInstance localhost -DemoData -Benchmarks` tạo QA ngẫu nhiên, chạy install/rerun có dữ liệu và các test module/security/payroll, kiểm tra seed/UDF rồi tự dọn DB trong finally. `run_identity_tests.ps1` dùng Windows DBA localhost, tạo login QA ngẫu nhiên mật khẩu và kiểm tra SQL identity thật; không ghi đè fixture cũ. Hai runner này giữ log riêng trong build/. Concurrency vẫn chạy hai session riêng.

`run_payroll_tests.ps1` chạy E2E và benchmark trừ khi `-SkipBenchmark`, từ chối database ngoài prefix QA. Dùng `-WindowsAuthentication` cho DBA QA hoặc biến môi trường TEST_SQL_USER/TEST_SQL_PASSWORD; không lấy mật khẩu từ config của ứng dụng. Chứng chỉ tự ký local có thể bật `-TrustLocalCertificate` một cách tường minh. Runner này không chạy concurrency hoặc tất cả test chuyên đề.

Log xác minh nằm trong build/, được gitignore. Xem [FIX_TASKLIST](../docs/FIX_TASKLIST.md) để phân biệt source đã sửa, test đã chạy và phần còn chờ xác minh. Các ảnh tracked cũ không phải kết quả của lần chạy hiện tại.
