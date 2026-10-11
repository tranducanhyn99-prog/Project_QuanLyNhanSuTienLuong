# Source chạy trong bộ nộp

Đọc [README_BO_NOP](../README_BO_NOP.md) để cài SQL, cấu hình và provision đúng thứ tự. Chạy terminal tại thư mục này; `src/`, `lib/` và launcher cùng ở đây, SQL ở `../database/`.

- [src/main/java](src/main/java/) và [src/test/java](src/test/java/): bản sao Java gốc tại commit ghi trong README bộ nộp.
- [config.properties.template](src/resources/config.properties.template): chỉ URL; tạo `config.properties` tại máy cài.
- [start_app.ps1](start_app.ps1), [start_gui_qa.ps1](start_gui_qa.ps1): compile main rồi mở LoginFrame.
- [setup_gui_accounts.ps1](setup_gui_accounts.ps1): cấp fixture QA hoặc kiểm login bằng `-VerifyOnly`.
- [run_verification.ps1](run_verification.ps1): compile Java 11 và regression offline; exit 0 PASS, 1 FAIL, 2 SKIPPED.
- [run_sql_verification.ps1](run_sql_verification.ps1): tạo QA riêng/cài/kiểm SQL; `-Benchmarks` chỉ chạy TV1/TV3/TV4, không thu benchmark TV2/TV5 hoặc actual plan.
- [run_identity_tests.ps1](run_identity_tests.ps1): SQL login thật trên QA identity riêng, mật khẩu ngẫu nhiên và cleanup; không dùng để cấp GUI fixture.
- [run_payroll_tests.ps1](run_payroll_tests.ps1): E2E/benchmark trên QA được chỉ định; không chạy concurrency A/B.
- [SOURCE_PROVENANCE.csv](SOURCE_PROVENANCE.csv) và [SHA256_INVENTORY.csv](SHA256_INVENTORY.csv): nguồn sao chép và kiểm toàn vẹn của bộ này.

Các runner SQL có ghi fixture và chỉ được dùng trên QA riêng. Source không kèm config local hoặc class cũ. Chưa kiểm cài mới trên máy khác.
