# Project Quản Lý Nhân Sự và Tiền Lương

Đồ án cuối kỳ học phần **Hệ quản trị cơ sở dữ liệu (DBMS330284)**.

Mục tiêu của dự án là xây dựng một ứng dụng quản lý nhân sự và tiền lương kết nối trực tiếp với **Microsoft SQL Server**, đồng thời thể hiện đầy đủ các nội dung trọng tâm của môn học như ràng buộc dữ liệu, Trigger, View, Index, Stored Procedure, Function, Transaction, phân quyền và xử lý đồng thời.

## Công nghệ sử dụng

- **Java Swing**: xây dựng giao diện desktop.
- **JDBC**: kết nối ứng dụng Java với SQL Server.
- **Microsoft SQL Server**: hệ quản trị cơ sở dữ liệu chính.
- **Git + GitHub**: quản lý mã nguồn, tài liệu và lịch sử đóng góp.

## Chạy bản sửa hiện tại

Ứng dụng đăng nhập bằng **SQL login riêng cho từng người**, được DBA mapping vào tài khoản ứng dụng. Cấu hình chỉ chứa `db.url`; không dùng tài khoản SQL chung hoặc `sa` làm danh tính mặc định. Cài schema 01→05, cấp login qua `com.tools.ProvisionIdentity`, rồi chạy `CHAY_UNG_DUNG.bat` hoặc `start_app.ps1`. Seed 06/07 dùng khi cần dữ liệu demo thực tế.

Để trình bày đồ án, dùng [tài khoản và mật khẩu demo](docs/DEMO_ACCOUNTS.md), [sổ tay hướng dẫn báo cáo chi tiết](docs/TV5_Huong_Dan_Bao_Cao_Chi_Tiet.md) và launcher `CHAY_UNG_DUNG.bat` (hoặc `start_app.ps1` / `start_gui_qa.ps1`). Trên máy khác, cài QA rồi chạy `setup_gui_accounts.ps1` để cấp cùng bộ SQL login/mapping.

Xem [hướng dẫn cài đặt/migration](docs/SECURE_SETUP.md).

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_app.ps1
```

Runner biên dịch Java 11 và chạy regression offline. `-LiveSql` chạy thêm các smoke check chỉ đọc bằng tài khoản QA đã mapping, lấy `TEST_SQL_USER`/`TEST_SQL_PASSWORD` từ môi trường và database từ `DB_URL`. Exit 0 = PASS, 1 = FAIL, 2 = SKIPPED. Payroll E2E, concurrency, grants và login thật có các test SQL/identity riêng; smoke không thay thế các test đó. Log ở `build/test-results/` và `build/fix-verification/`.

## Chức năng chính

- Đăng nhập và phân quyền theo vai trò (RBAC + Row-Level Security).
- Quản lý hồ sơ nhân viên và lịch sử lương.
- Quản lý phòng ban và chức vụ.
- Chấm công đơn lẻ và điểm danh hàng loạt (Batch Transaction).
- Quản lý phụ cấp và khấu trừ.
- Tính bảng lương theo tháng (Savepoint Transaction + Historical Salary Snapshot).
- Chốt bảng lương, mở lại bảng lương và khóa dữ liệu nguồn kỳ đã chốt.
- Tra cứu, thống kê và báo cáo phiếu lương cá nhân.

## Mục tiêu kỹ thuật theo rubric

Dự án đáp ứng đầy đủ các yêu cầu chính của học phần:

- 10 bảng có quan hệ và chuẩn hóa đến 3NF.
- Hơn 20 Constraint có ý nghĩa nghiệp vụ.
- 9 Trigger kiểm toàn vẹn và khóa sổ kỳ lương đã chốt.
- 6 View (bao gồm View phân quyền dòng `vw_PhieuLuongCaNhan`).
- 5 Covering Index kèm minh chứng hiệu năng (`STATISTICS IO, TIME` & Execution Plan).
- 14 Stored Procedure có `TRY...CATCH`.
- 5 User-Defined Function.
- 6 nghiệp vụ sử dụng Transaction & Savepoint.
- Minh họa xử lý đồng thời (Concurrency Locking `UPDLOCK, HOLDLOCK`).
- 4 Database Role + 5 SQL Login cá nhân với chính sách `GRANT`/`REVOKE`/`DENY`.

## Cấu trúc repository

```text
Project_QuanLyNhanSuTienLuong/
├── src/            # Mã nguồn Java (Swing, Service, DAO, Tests)
├── database/       # Script SQL Server (01->05 Schema, 06-07 Seed, Tests)
├── docs/           # Báo cáo Word, Sổ tay báo cáo và Kịch bản SQL vấn đáp
├── Bo_nop_Nhom06/  # Gói nộp bài hoàn chỉnh (Source, DB, Report, Slides, Evidence)
├── .github/        # CODEOWNERS, PR template và ruleset
├── README.md
└── CONTRIBUTING.md
```

## Tài liệu dự án

### 1. Báo cáo & Thiết kế chung
- [Báo cáo cuối kỳ đầy đủ - bản Word](docs/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx)
- [Kế hoạch phân công Project DBMS - bản Word](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.docx)
- [Phân tích và thiết kế hệ thống - bản Word](docs/Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx)
- [Hướng dẫn cài đặt bảo mật & SQL Identity](docs/SECURE_SETUP.md)
- [Tài khoản và mật khẩu Demo](docs/DEMO_ACCOUNTS.md)
- [Sổ tay hướng dẫn báo cáo & đối chiếu mã nguồn chi tiết](docs/TV5_Huong_Dan_Bao_Cao_Chi_Tiet.md)

### 2. Sổ tay SQL Kiểm tra Chức năng Vấn đáp (SSMS)
- [TV1 – Quản lý Hồ sơ Nhân sự & Danh mục](docs/SQL_Kiem_Tra_Chuc_Nang/TV1_SQL_Kiem_Tra_Chuc_Nang.md)
- [TV2 – Quản lý Chấm công & Tổng hợp công](docs/SQL_Kiem_Tra_Chuc_Nang/TV2_SQL_Kiem_Tra_Chuc_Nang.md)
- [TV3 – Quản lý Phụ cấp & Khấu trừ](docs/SQL_Kiem_Tra_Chuc_Nang/TV3_SQL_Kiem_Tra_Chuc_Nang.md)
- [TV4 – Quản lý Tính lương & Bảng lương](docs/SQL_Kiem_Tra_Chuc_Nang/TV4_SQL_Kiem_Tra_Chuc_Nang.md)
- [TV5 – Bảo mật, Concurrency & Chốt kỳ lương](docs/SQL_Kiem_Tra_Chuc_Nang/TV5_SQL_Kiem_Tra_Chuc_Nang.md)

### 3. Scripts Khởi chạy, Đóng gói & Kiểm thử
- `CHAY_UNG_DUNG.bat` / `start_app.ps1`: Biên dịch và mở giao diện ứng dụng kết nối `QuanLyNhanSuTienLuong`.
- `DONG_GOI_JAR.bat` / `build_jar.ps1`: Đóng gói file thực thi `QuanLyNhanSuTienLuong.jar` (Fat JAR kèm JDBC driver).
- `run_verification.ps1` / `run_tests_cli.ps1`: Biên dịch Java 11 và chạy kiểm tra offline; tùy chọn live SQL.
- `test_roles.ps1`: Chạy regression Session/service/sidebar offline.
- `run_tuan3_tv1.ps1`: Smoke chỉ đọc Java HR; SQL transaction/trigger/benchmark chạy riêng.
- `run_tuan3_tv5.ps1`: Regression offline và smoke login/schema live.
- `run_payroll_tests.ps1`: TV4 E2E/benchmark trên database riêng có prefix `PRJ_Fix_QA_`; concurrency chạy SessionA/SessionB riêng.
- `run_sql_verification.ps1`: Tạo QA tên ngẫu nhiên, install/rerun/seed/module/security/payroll, benchmark tùy chọn và cleanup ngay cả khi lỗi.
- `run_tv5_verification.ps1`: Tạo QA riêng, kiểm miền tiền/overflow và thu đầy đủ benchmark TV5 (Messages, Results, hai actual plans, SHA-256 source); mặc định dọn QA.
- `run_identity_tests.ps1`: Tạo login cá nhân QA, kiểm tra danh tính/quyền/thay mật khẩu/trạng thái/vai trò/ngày DAO rồi dọn fixture.
- `setup_gui_accounts.ps1`: Cấp bộ năm tài khoản demo được lưu trong `database/demo_accounts.json`; `-VerifyOnly` kiểm tra đăng nhập đã có.
- `start_gui_qa.ps1`: Mở giao diện bằng database QA tường minh; hỗ trợ `-Database` cho máy khác.
- `database/test_benchmark_index_TV1.sql`: Đo Scan/Seek trên bảng tạm, không chèn 5.000 nhân viên vào bảng thật.
- `database/test_module_nhansu_TV1.sql`: Kiểm thử tự động 6 đối tượng CSDL của TV1 (SP, Transaction, Trigger, Function, View, Index).

## Giao diện sáng PeopleOS

Ứng dụng dùng theme sáng thống nhất cho đăng nhập, điều hướng và toàn bộ phân hệ. Bảng dữ liệu có thanh cuộn ngang, trạng thái và vai trò hiển thị bằng tiếng Việt; mã dữ liệu và phân quyền giữ nguyên. Hồ sơ nhân viên được chia thành tab danh sách và tab chỉnh sửa, hỗ trợ nhấp đúp để mở hồ sơ.

- Khởi chạy: `powershell -ExecutionPolicy Bypass -File .\start_app.ps1` (hoặc chạy `CHAY_UNG_DUNG.bat`).
- Kiểm tra màu chữ/nền, trạng thái nút, badge và focus bàn phím, không kết nối SQL: `powershell -ExecutionPolicy Bypass -File .\run_verification.ps1 -TestClass LightThemeRegressionTest`.
- Kiểm tra lỗi QA về email đuôi miền dài, nhãn tài khoản theo vai trò và thông báo đăng nhập: `powershell -ExecutionPolicy Bypass -File .\run_verification.ps1 -TestClass GuiQaRegressionTest` (cần desktop; không kết nối SQL).

## Quy trình Git/GitHub

Branch `main` đã được bảo vệ bằng GitHub Ruleset.

- Không được push trực tiếp vào `main`.
- Mỗi công việc phải thực hiện trên một branch riêng.
- Thành viên push branch lên GitHub và tạo Pull Request vào `main`.
- Pull Request phải được Code Owner review trước khi merge.
- Code Owner của repository: **@tranducanhyn99-prog**.
- Chỉ thay đổi đã qua review mới được đưa vào `main`.

Xem chi tiết tại [CONTRIBUTING.md](CONTRIBUTING.md).

## Cấu hình và tài khoản đồ án

Nhóm chủ động lưu rõ mật khẩu của bộ tài khoản demo trong `database/demo_accounts.json` để chia sẻ và trình bày đồ án. Thông tin role/mapping và cách cấp trên máy khác ở [DEMO_ACCOUNTS](docs/DEMO_ACCOUNTS.md). File cấu hình riêng, mật khẩu DBA, token và backup vẫn không thuộc bộ fixture demo; `.env`, `config.properties` và `*.bak` tiếp tục được gitignore.

---

**Repository:** `tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong`
