# Project Quản Lý Nhân Sự và Tiền Lương

Đồ án cuối kỳ học phần **Hệ quản trị cơ sở dữ liệu (DBMS330284)**.

Mục tiêu của dự án là xây dựng một ứng dụng quản lý nhân sự và tiền lương kết nối trực tiếp với **Microsoft SQL Server**, đồng thời thể hiện đầy đủ các nội dung trọng tâm của môn học như ràng buộc dữ liệu, Trigger, View, Index, Stored Procedure, Function, Transaction, phân quyền và xử lý đồng thời.

## Công nghệ sử dụng

- **Java Swing**: xây dựng giao diện desktop.
- **JDBC**: kết nối ứng dụng Java với SQL Server.
- **Microsoft SQL Server**: hệ quản trị cơ sở dữ liệu chính.
- **Git + GitHub**: quản lý mã nguồn, tài liệu và lịch sử đóng góp.

## Chạy bản sửa hiện tại

Ứng dụng đăng nhập bằng **SQL login riêng cho từng người**, được DBA mapping vào tài khoản ứng dụng. Cấu hình chỉ chứa `db.url`; không dùng tài khoản SQL chung hoặc `sa` làm danh tính mặc định. Cài schema 01→05, cấp login qua `com.tools.ProvisionIdentity`, rồi chạy `start_app.ps1`. Seed 06 chỉ dùng khi chủ động cần dữ liệu demo.

Để trình bày đồ án, dùng [tài khoản và mật khẩu demo](docs/DEMO_ACCOUNTS.md), [kịch bản test giao diện](docs/GUI_TEST_GUIDE.md) và launcher `start_gui_qa.ps1`. Trên máy khác, cài QA rồi chạy `setup_gui_accounts.ps1` để cấp cùng bộ SQL login/mapping.

Xem [hướng dẫn cài đặt/migration](docs/SECURE_SETUP.md), [tasklist và trạng thái xác minh](docs/FIX_TASKLIST.md), [review ban đầu](docs/SOURCE_REVIEW.md).

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_app.ps1
```

Runner biên dịch Java 11 và chạy regression offline. `-LiveSql` chạy thêm các smoke check chỉ đọc bằng tài khoản QA đã mapping, lấy `TEST_SQL_USER`/`TEST_SQL_PASSWORD` từ môi trường và database từ `DB_URL`. Exit 0 = PASS, 1 = FAIL, 2 = SKIPPED. Payroll E2E, concurrency, grants và login thật có các test SQL/identity riêng; smoke không thay thế các test đó. Log ở `build/test-results/` và `build/fix-verification/`. Ảnh tracked cũ có số liệu dựng, không là minh chứng cho bản sửa hiện tại.

## Chức năng chính dự kiến

- Đăng nhập và phân quyền theo vai trò.
- Quản lý hồ sơ nhân viên.
- Quản lý phòng ban và chức vụ.
- Chấm công.
- Quản lý phụ cấp và khấu trừ.
- Tính bảng lương theo tháng.
- Chốt bảng lương.
- Tra cứu, thống kê và báo cáo.

## Mục tiêu kỹ thuật theo rubric

Dự án hướng tới đáp ứng các yêu cầu chính của học phần:

- Tối thiểu 8 bảng có quan hệ và chuẩn hóa đến ít nhất 3NF.
- Tối thiểu 5 Constraint có ý nghĩa nghiệp vụ.
- Tối thiểu 5 Trigger.
- Tối thiểu 5 View.
- Tối thiểu 5 Index và có minh chứng hiệu năng.
- Tối thiểu 5 Stored Procedure.
- Tối thiểu 5 User-Defined Function.
- Tối thiểu 5 nghiệp vụ sử dụng Transaction.
- Có minh họa xử lý concurrency hoặc recovery.
- Có ít nhất 4 Role/Login với chính sách GRANT/REVOKE/DENY.

> Lưu ý: Đây là các mục tiêu triển khai của dự án. Trạng thái hoàn thành thực tế sẽ được cập nhật theo tiến độ nhóm.

## Cấu trúc repository

```text
Project_QuanLyNhanSuTienLuong/
├── src/        # Mã nguồn Java
├── database/   # Script SQL Server
├── docs/       # Tài liệu, báo cáo, sơ đồ và kế hoạch
├── .github/    # CODEOWNERS, PR template và ruleset
├── README.md
└── CONTRIBUTING.md
```

## Tài liệu dự án

### 1. Kế hoạch & Kiến trúc chung
- [Kế hoạch phân công Project DBMS (Nhóm 06)](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Kế hoạch phân công - bản Word](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.docx)
- [Phân tích và thiết kế hệ thống - bản Word](docs/Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx)
- [Hướng dẫn làm việc với Git/GitHub](docs/GIT_WORKFLOW.md)
- [Thiết kế Kiến trúc Hệ thống 4 tầng](docs/TV5_Architecture.md)
- [Checklist Tích hợp trước khi merge PR](docs/TV5_Integration_Checklist.md)

### 2. Báo cáo Tiến độ & Thiết kế 5 Phân hệ
- [TV1 – Quản lý Hồ sơ Nhân sự & Danh mục](docs/TV1_Tien_Do_Thuc_Hien.md)
- [TV2 – Quản lý Chấm công & Tổng hợp công](docs/TV2_Tien_Do_Thuc_Hien.md)
- [TV3 – Quản lý Phụ cấp & Khấu trừ](docs/TV3_Tien_Do_Thuc_Hien.md)
- [TV4 – Quản lý Tính lương & Bảng lương](docs/TV4_Tien_Do_Thuc_Hien.md)
- [TV5 – Bảo mật, Concurrency & Chốt kỳ lương](docs/TV5_Tien_Do_Thuc_Hien.md)

### 3. Báo cáo Chuyên đề Tổng kết & Minh chứng Thực nghiệm
- [TV1 – Thiết kế CSDL Chuyên sâu, Chuẩn hóa 3NF & Benchmark Index](docs/TV1_BaoCao_ChuyenDe_NhanSu_CuoiKy.md)
- [Tasklist và minh chứng xác minh hiện tại](docs/FIX_TASKLIST.md)
- [TV1 – Bộ 20 Test Cases Kiểm thử Module Nhân sự](docs/TV1_NhanSu_Test_Cases.md)
- [TV5 – Chương 3 – Kiến trúc Bảo mật 2 tầng, Concurrency & Benchmark Index](docs/Chuong3_Bao_Mat_Va_Concurrency_TV5.md)

### 4. Scripts Kiểm thử & Tự động hóa
- `run_verification.ps1` / `run_tests_cli.ps1`: Biên dịch Java 11 và chạy kiểm tra offline; tùy chọn live SQL.
- `test_roles.ps1`: Chạy regression Session/service/sidebar offline.
- `run_tuan3_tv1.ps1`: Smoke chỉ đọc Java HR; SQL transaction/trigger/benchmark chạy riêng.
- `run_tuan3_tv5.ps1`: Regression offline và smoke login/schema live; không tạo ảnh dựng.
- `run_payroll_tests.ps1`: TV4 E2E/benchmark trên database riêng có prefix `PRJ_Fix_QA_`; concurrency chạy SessionA/SessionB riêng.
- `run_sql_verification.ps1`: Tạo QA tên ngẫu nhiên, install/rerun/seed/module/security/payroll, benchmark tùy chọn và cleanup ngay cả khi lỗi.
- `run_identity_tests.ps1`: Tạo login cá nhân QA, kiểm tra danh tính/quyền/thay mật khẩu/trạng thái/vai trò/ngày DAO rồi dọn fixture.
- `setup_gui_accounts.ps1`: Cấp bộ năm tài khoản demo được lưu trong `database/demo_accounts.json`; `-VerifyOnly` kiểm tra đăng nhập đã có.
- `start_gui_qa.ps1`: Mở giao diện bằng database QA tường minh; hỗ trợ `-Database` cho máy khác.
- `database/test_benchmark_index_TV1.sql`: Đo Scan/Seek trên bảng tạm, không chèn 5.000 nhân viên vào bảng thật.
- `database/test_module_nhansu_TV1.sql`: Kiểm thử tự động 6 đối tượng CSDL của TV1 (SP, Transaction, Trigger, Function, View, Index).

## Giao diện sáng PeopleOS

Ứng dụng dùng theme sáng thống nhất cho đăng nhập, điều hướng và toàn bộ phân hệ. Bảng dữ liệu có thanh cuộn ngang, trạng thái và vai trò hiển thị bằng tiếng Việt; mã dữ liệu và phân quyền giữ nguyên. Hồ sơ nhân viên được chia thành tab danh sách và tab chỉnh sửa, hỗ trợ nhấp đúp để mở hồ sơ.

- Khởi chạy: `powershell -ExecutionPolicy Bypass -File .\start_app.ps1` (dùng cấu hình SQL hiện có).
- Kiểm tra màu chữ/nền, trạng thái nút, badge và focus bàn phím, không kết nối SQL: `powershell -ExecutionPolicy Bypass -File .\run_verification.ps1 -TestClass LightThemeRegressionTest`.
- Kiểm tra lỗi QA về email đuôi miền dài, nhãn tài khoản theo vai trò và thông báo đăng nhập: `powershell -ExecutionPolicy Bypass -File .\run_verification.ps1 -TestClass GuiQaRegressionTest` (cần desktop; không kết nối SQL).
- Xem trước: [Đăng nhập](docs/screenshots/peopleos-light/login.png) · [Dashboard](docs/screenshots/peopleos-light/dashboard.png).

## Quy trình Git/GitHub

Branch `main` đã được bảo vệ bằng GitHub Ruleset.

- Không được push trực tiếp vào `main`.
- Mỗi công việc phải thực hiện trên một branch riêng.
- Thành viên push branch lên GitHub và tạo Pull Request vào `main`.
- Pull Request phải được Code Owner review trước khi merge.
- Code Owner của repository: **@tranducanhyn99-prog**.
- Chỉ thay đổi đã qua review mới được đưa vào `main`.

Quy ước branch:

- `feature/<ten-chuc-nang>`: tính năng mới.
- `fix/<ten-loi>`: sửa lỗi.
- `db/<noi-dung>`: thay đổi cơ sở dữ liệu.
- `docs/<noi-dung>`: tài liệu.
- `test/<noi-dung>`: kiểm thử.

Xem chi tiết tại [CONTRIBUTING.md](CONTRIBUTING.md).

## Quy ước commit

Sử dụng commit message ngắn gọn, mô tả đúng nội dung thay đổi:

- `feat:` thêm tính năng mới.
- `fix:` sửa lỗi.
- `db:` thay đổi liên quan đến database.
- `docs:` cập nhật tài liệu.
- `test:` thêm hoặc cập nhật kiểm thử.
- `refactor:` tái cấu trúc code nhưng không thay đổi hành vi.
- `chore:` cấu hình hoặc công việc phụ trợ.

Ví dụ:

```text
feat: thêm màn hình quản lý nhân viên
db: thêm stored procedure tính bảng lương
fix: rollback transaction khi tính lương thất bại
docs: cập nhật ERD
```

## Cấu hình và tài khoản đồ án

Nhóm chủ động lưu rõ mật khẩu của bộ tài khoản demo trong `database/demo_accounts.json` để chia sẻ và trình bày đồ án. Thông tin role/mapping và cách cấp trên máy khác ở [DEMO_ACCOUNTS](docs/DEMO_ACCOUNTS.md). File cấu hình riêng, mật khẩu DBA, token và backup vẫn không thuộc bộ fixture demo; `.env`, `config.properties` và `*.bak` tiếp tục được gitignore.

---

**Repository:** `tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong`
