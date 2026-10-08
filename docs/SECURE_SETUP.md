# Cài đặt và migration danh tính SQL

Cập nhật 08/10/2026. Ứng dụng Swing dùng **SQL login cá nhân**: tên/mật khẩu ở LoginFrame được SQL Server xác thực, rồi `sp_LayTaiKhoanHienTai` tra mapping `TAIKHOAN.SqlLogin = ORIGINAL_LOGIN()`. Session Java phục vụ giao diện; thay Session không đổi quyền của connection SQL. [ORIGINAL_LOGIN](https://learn.microsoft.com/en-us/sql/t-sql/functions/original-login-transact-sql?view=sql-server-ver17) giữ danh tính đăng nhập ban đầu.

## 1. Cài schema

Để demo môn học, nhóm lưu rõ bộ mật khẩu QA trong [database/demo_accounts.json](../database/demo_accounts.json). [DEMO_ACCOUNTS](DEMO_ACCOUNTS.md) hướng dẫn cấp cùng mapping trên máy khác; [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md) hướng dẫn thao tác từng màn hình. Bộ fixture này không chứa mật khẩu DBA và không thay thế SQL role/mapping.

Dùng JDK 11 trở lên, SQL Server và JDBC JAR cho Java 11 trong `lib/` hoặc biến `MSSQL_JDBC_JAR`. Không cần thêm framework.

1. Sao lưu database đang sử dụng. Thử migration trên bản sao riêng trước khi áp dụng vào dữ liệu thật.
2. Với database dự án `QuanLyNhanSuTienLuong`, chạy `database/01_Module_NhanSu_TV1.sql` → `02_Module_ChamCong_TV2.sql` → `03_phucap_khautru_TV3.sql` → `04_Module_TinhLuong_TV4.sql` → `05_Security_Payroll_TV5.sql`. Giữ các tùy chọn SET đầu file. Không chạy song song các module.
3. Với database QA có tên `PRJ_Fix_QA_...`, tạo database riêng và chạy bản script đã thay **mọi** lệnh `USE QuanLyNhanSuTienLuong` bằng đúng tên QA. Một số file chứa lệnh USE nên chỉ chọn database trên toolbar SSMS chưa đủ để cách ly. Kiểm tra `SELECT DB_NAME()` trước mỗi nhóm test.
4. Kiểm tra Messages không có lỗi. Schema không tạo login/mật khẩu demo và không xóa phụ cấp/khấu trừ theo tháng. Chạy lại schema theo đúng thứ tự để cập nhật procedure/view/trigger/grants; không coi lần chạy có lỗi là cài đặt thành công.
5. Chỉ chạy `database/06_Demo_Data.sql` khi cần dữ liệu minh họa, sau 01→05. Script này không cấp SQL login, không có mật khẩu mặc định và không xóa dữ liệu hiện có.

Module 01 thêm `LICHSULUONG` và `NHANVIEN.NgayNghiViec`; module 05 mở rộng `MatKhau` thành VARCHAR(255), thêm `SqlLogin` và index mapping duy nhất. Lịch sử lương ban đầu chỉ có thể lấy mức lương đang lưu. Dữ liệu mức lương/ngày nghỉ trong quá khứ bị thiếu phải được phục hồi từ hồ sơ đáng tin cậy; migration không thể tự tái tạo lịch sử đã mất. Snapshot chi tiết kỳ cũ được giữ khi tính lại theo quy tắc trong module 04.

## 2. Cấu hình ứng dụng

Sao chép `src/resources/config.properties.template` thành `config.properties`. Cấu hình chỉ chứa URL:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=true;trustServerCertificate=false
```

Có thể override bằng biến `DB_URL` hoặc `-Ddb.url=...`. Thứ tự ưu tiên là system property → biến môi trường → file cấu hình. URL không được chứa user/password/integratedSecurity/authentication. `db.user`, `db.password`, `DB_USER`, `DB_PASSWORD` và `-Ddb.user`/`-Ddb.password` không cấp danh tính cho ứng dụng.

Máy phát triển dùng chứng chỉ SQL tự ký có thể đặt `encrypt=true;trustServerCertificate=true` cho **môi trường local đó**. Khi triển khai, cài chứng chỉ được client tin cậy và dùng `trustServerCertificate=false`. [Hướng dẫn TLS của Microsoft JDBC](https://learn.microsoft.com/en-us/sql/connect/jdbc/connecting-with-ssl-encryption?view=sql-server-ver17).

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_app.ps1
```

Script chỉ biên dịch `src/main/java` với `--release 11` vào `build/app`, rồi mở LoginFrame. Không dùng các class cũ trong `bin/` để chạy ứng dụng.

## 3. DBA cấp tài khoản cá nhân

Tạo hồ sơ nhân viên trước nếu vai trò là Employee. HR chỉ tạo được tài khoản ứng dụng vai trò Employee; tài khoản đó chưa đăng nhập được cho đến khi DBA cấp SQL login/mapping. Các tài khoản quản trị, HR và payroll cũng phải có mapping.

Biên dịch bằng `start_app.ps1` hoặc `run_verification.ps1`, rồi chạy công cụ trong terminal tương tác (JAR minh họa phải đổi đúng tên đang có):

```powershell
java -cp "build/app;src/resources;lib/mssql-jdbc-12.6.1.jre11.jar" com.tools.ProvisionIdentity
```

Hoặc dùng `build/verification/classes` thay `build/app` sau runner kiểm thử. Công cụ yêu cầu SQL login DBA, mật khẩu DBA, tên login mới, vai trò, MaNV và mật khẩu cá nhân xác nhận hai lần. Mật khẩu được nhập ẩn qua `Console.readPassword`, không đặt trên command line. Tool cần terminal thật, không chạy qua pipeline/IDE console không hỗ trợ `System.console()`.

`sp_DBAProvisionIdentity` yêu cầu DBA server (`sysadmin`) và tạo SQL login/user/role/mapping trong transaction. Tên login dài 1–50 ký tự thuộc `a-z A-Z 0-9 _ . -`; mật khẩu 8–128 ký tự và còn phải đáp ứng SQL password policy. Tool từ chối SQL login/user đã tồn tại hoặc tài khoản đã được mapping; không tự ghi đè principal cũ. SQL login cá nhân không được cấp quyền server bởi công cụ này. Employee cần MaNV tồn tại; quản trị hệ thống có thể không gắn nhân viên.

Mapping vai trò:

| Vai trò ứng dụng | Role SQL | Phạm vi |
|---|---|---|
| DB_Admin | role_DBAdmin | Quản trị database; role này kế thừa db_owner |
| HR_Manager | role_HRManager | Nhân sự/danh mục/chấm công/phụ cấp/khấu trừ; không tính/chốt/xóa kỳ lương |
| Payroll_Officer | role_PayrollOfficer | Đọc nhân sự/công; CRUD phụ cấp/khấu trừ; tính/chốt/mở lại/xóa kỳ nháp |
| Employee | role_Employee | Đọc `vw_PhieuLuongCaNhan` của người được SQL login mapping |

Chỉ cấp một role ứng dụng cho mỗi user. DBA phải kiểm tra các grants/role cũ, tránh để HR/Payroll/Employee có thêm db_owner, sysadmin hoặc quyền đọc rộng từ role khác. Database role DB_Admin không đồng nghĩa quyền quản trị SQL Server. Reset mật khẩu/khóa-mở SQL login cần quyền server tương ứng (thường `ALTER ANY LOGIN`; với principal đặc quyền còn có yêu cầu cao hơn), theo [ALTER LOGIN](https://learn.microsoft.com/en-us/sql/t-sql/statements/alter-login-transact-sql?view=sql-server-ver17). Schema/tool không tự cấp quyền này. Dùng DBA được ủy quyền để thực hiện nếu application admin thiếu quyền; transaction phải rollback cả SQL login và profile khi thao tác lỗi.

## 4. Migration tài khoản cũ

- Tài khoản ứng dụng chưa có SQL login: DBA chạy ProvisionIdentity với cùng TenDangNhap/MaNV/vai trò đã được kiểm tra và mật khẩu mới mạnh. Tool cập nhật profile chưa mapping cùng tên và thay hash bằng PBKDF2 trong transaction.
- SQL login/user đã tồn tại: công cụ từ chối. DBA rà soát SID, membership/grants, trạng thái và mapping trước khi chuyển. Sao lưu profile, bảo đảm `TenDangNhap` khớp login mà AuthService dùng và `SqlLogin` trỏ đúng principal. Không dùng tài khoản chung/sa làm mapping cho nhiều người. Các lệnh ALTER LOGIN/ALTER ROLE và cập nhật profile phải được thực hiện cùng transaction; kiểm thử bằng kết nối mới của chính login đó trước khi đưa vào sử dụng.
- Profile cũ SHA-256 đã được mapping và SQL password còn khớp: sau một lần xác thực SQL và app hash thành công, AuthService chuyển hash của **chính người đăng nhập** sang PBKDF2 bằng `sp_MigrateMatKhau`. Không thể migrate chỉ nhờ sửa Session hoặc biết hash cũ.
- PBKDF2 lưu phiên bản, SHA-256, 600.000 vòng, salt ngẫu nhiên 16 byte và khóa 32 byte. Hai người dùng cùng mật khẩu có hash khác nhau. [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

Tài khoản được khóa qua admin procedure sẽ đồng bộ trạng thái profile và SQL login; đổi vai trò đồng bộ SQL role và profile. Dùng connection mới để xác minh kết quả. SQL session đã mở có thể vẫn tồn tại; DBA cần kết thúc các session đang hoạt động khi thu hồi quyền ngay lập tức. Không cập nhật riêng `TAIKHOAN.VaiTro` để đổi quyền.

## 5. Kiểm thử và minh chứng

Bộ GUI QA đã có năm login `gui_admin/gui_hr/gui_payroll/gui_a/gui_b`, hai Employee gắn hồ sơ riêng. Trên máy hiện tại chạy `start_gui_qa.ps1`; kiểm tra lại bằng `setup_gui_accounts.ps1 -VerifyOnly`. Database QA cũ hiện có hai nhân viên và năm profile, không còn là database chỉ có danh mục. Các log cleanup của runner trước đó chỉ mô tả fixture do chính từng lần chạy tạo.

```powershell
# Compile Java 11 + regression offline; không cần SQL login.
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1

# Kiểm tra chip demo bằng JFrame thật được giữ ẩn, không kết nối SQL.
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1 -TestClass LoginChipRegressionTest

# Live read-only auth/schema/HR smoke: dùng DB_Admin QA đã mapping.
# Đặt TEST_SQL_USER/TEST_SQL_PASSWORD bằng môi trường của terminal, không commit.
# DB_URL phải chọn PRJ_Fix_QA_...; không để URL chỉ tới DB đang sử dụng.
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1 -LiveSql

# SQL E2E/benchmark có ghi fixture; runner từ chối database ngoài prefix QA.
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_payroll_tests.ps1 -ServerInstance . -Database PRJ_Fix_QA_example -WindowsAuthentication -TrustLocalCertificate

# Toàn bộ module SQL trên QA tên ngẫu nhiên: cài sạch, rerun giữ dữ liệu, seed hai lần.
# Tự dọn database đã tạo trong finally kể cả khi FAIL; -KeepDatabase là tùy chọn tường minh.
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_sql_verification.ps1 -ServerInstance localhost -DemoData -Benchmarks

# Login thật bốn role, lương cá nhân, reset/khóa/đổi role/migration và DAO ngày.
# Windows SQL sysadmin trên localhost; cần SQL auth + TCP 1433 và JDBC JAR trong lib/.
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_identity_tests.ps1
```

Exit code: 0 = tất cả kiểm tra được runner yêu cầu đã chạy và PASS; 1 = FAIL; 2 = SKIPPED vì thiếu driver/credentials live hoặc công cụ. Có credential nhưng lỗi certificate/connectivity/schema/wrong role là FAIL, không được xem là xác nhận chặn quyền. Java smoke không thay thế SQL E2E/concurrency/identity. `run_payroll_tests.ps1` chạy TV4 E2E và benchmark trừ khi `-SkipBenchmark`; concurrency tính lương chạy hai script SessionA/SessionB riêng. `run_sql_verification.ps1` dùng Windows DBA, tin chứng chỉ local và tự tạo database QA duy nhất; không nhận database đang sử dụng làm target; chạy thêm race chốt với chấm công/phụ cấp/khấu trừ/chi tiết bằng `Closed_Source_Concurrency.ps1`. PS5 integrated run `build/sql-verification/20261008_101955_11b4e24c/` đã exit 0, gồm bốn race PASS với nguồn không đổi và cleanup fixture. Runner chỉ dọn QA database do chính lần chạy đó tạo; QA cũ `PRJ_Fix_QA_20261007_01` vẫn được giữ. `run_identity_tests.ps1` dùng QA identity riêng, mật khẩu ngẫu nhiên trong môi trường process, từ chối ghi đè login/DB đã tồn tại và dọn các principal mình tạo; không in mật khẩu. Live identity được kiểm tra bằng login thật; `EXECUTE AS USER ... WITHOUT LOGIN` chỉ chứng minh grants, không chứng minh cách lọc ORIGINAL_LOGIN.

Log runner nằm trong `build/test-results/`; log lần sửa này nằm trong `build/fix-verification/`. Các ảnh tracked cũ trong `screenshots/` có dữ liệu hardcode, **không phải minh chứng cho lần xác minh này**. Công cụ tạo ảnh chỉ chạy khi bật `-Dapp.mockScreenshots=true`, có watermark minh họa và xuất riêng vào `build/mock-screenshots/`. Để nghiệm thu hiệu năng, dùng log `STATISTICS IO/TIME` và execution plan của lần thực thi thật; không lấy số vẽ trên ảnh làm benchmark.
