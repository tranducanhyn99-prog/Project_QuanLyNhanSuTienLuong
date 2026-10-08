# Source Java Swing

Yêu cầu JDK **11 trở lên**, JDBC SQL Server JAR cho Java 11 trong lib/ hoặc biến MSSQL_JDBC_JAR. Biên dịch dùng `javac --release 11 -encoding UTF-8`.

## Cài và chạy

1. Cài database theo 01→05 trong [database/README](../database/README.md). Seed 06 chỉ dùng demo.
2. Sao chép resources/config.properties.template thành resources/config.properties; chỉ cấu hình db.url. Cấu hình thật được gitignore.
3. DBA cấp login cá nhân/mapping theo [SECURE_SETUP](../docs/SECURE_SETUP.md). Nhập chính SQL login/password này tại LoginFrame.
4. Chạy `powershell -NoProfile -ExecutionPolicy Bypass -File .\start_app.ps1` từ root.

Với bộ demo đã mapping, chạy `start_gui_qa.ps1` để chọn QA tường minh. Username/password được nhóm lưu trong [database/demo_accounts.json](../database/demo_accounts.json); xem [DEMO_ACCOUNTS](../docs/DEMO_ACCOUNTS.md) và [GUI_TEST_GUIDE](../docs/GUI_TEST_GUIDE.md). `setup_gui_accounts.ps1 -Database PRJ_Fix_QA_...` cấp cùng fixture trên máy khác; `-VerifyOnly` xác minh tài khoản đã có.

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=true;trustServerCertificate=false
```

DB_URL hoặc -Ddb.url override file. db.user/db.password không được dùng để xác thực ứng dụng; URL cũng không được chứa thông tin xác thực. Local tự ký có thể tường minh trustServerCertificate=true, giữ encrypt=true. Triển khai dùng chứng chỉ tin cậy.

Presentation → service → DAO/JDBC → SQL Server. Session Java kiểm tra thao tác trước JDBC; SQL login/role/mapping bảo vệ các đường DAO và SQL trực tiếp. Employee chỉ đọc view cá nhân theo ORIGINAL_LOGIN. HR tạo app account Employee nhưng DBA vẫn phải provision SQL login. PasswordUtil tạo PBKDF2-SHA256 với salt riêng; SHA-256 cũ chỉ được đọc để migrate sau đăng nhập thành công.

## Kiểm thử

`run_verification.ps1` biên dịch main + test với Java 11 vào build/verification/classes và chạy regression offline. Thêm -LiveSql cùng DB_URL chọn QA và TEST_SQL_USER/TEST_SQL_PASSWORD của DB_Admin QA đã mapping để chạy login/schema/HR read-only smoke. Exit 0 PASS, 1 FAIL, 2 SKIPPED thiếu driver/credentials. Smoke không chứng minh tất cả nghiệp vụ SQL; E2E/concurrency/identity phải chạy riêng. Bản chạy start_app chỉ biên dịch src/main, không dùng bin/ hoặc test class cũ.

Công cụ ảnh dựng chỉ bật tường minh -Dapp.mockScreenshots=true, có watermark và xuất build/mock-screenshots/. Không dùng ảnh dựng hoặc ảnh tracked cũ làm kết quả kiểm thử.
