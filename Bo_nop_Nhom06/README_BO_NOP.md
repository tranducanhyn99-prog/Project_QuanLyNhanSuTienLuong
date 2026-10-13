# Bộ nộp Nhóm 06 — đã nhận TV4 và TV5

Source Java/SQL được đóng gói ngày **11/10/2026**, từ `main` commit **f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5**. TV5: **Trần Đức Anh — 24110155**. Ngày **13/10/2026** nhận bộ TV4 **Nguyễn Quang Vinh — 24110385** từ PR #41 (đã merge); source dùng thu minh chứng TV4 ở `62223d5` có Java/SQL khớp snapshot hiện tại. Bộ này có source, JDBC, SQL tái tạo và evidence TV4/TV5. **Bộ nộp nhóm chưa hoàn tất**: còn chờ ERD TV1, benchmark/4 actual plans CHAMCONG TV2, slide/evidence TV1–TV3 và rehearsal chung. Hai deck TV4/TV5 gồm tổng cộng 6 slide riêng, chưa thay deck nhóm ≤15 slide.

| Thư mục | Nội dung |
|---|---|
| [source](source/README.md) | Java main/test, template URL, JDBC JAR, launcher/provision/test runner |
| [database](database/README.md) | Module 01→05, seed 06 tùy chọn, demo account manifest và script kiểm thử |
| [evidence/TV5](evidence/TV5/README_EVIDENCE.md) | Log/plan gốc đã chọn, phạm vi đo và danh mục SHA256 |
| [bộ TV4](evidence/TV4/README_EVIDENCE.md) | Word phần TV4 đã sửa bảng, slide 10–12 và 13 file minh chứng; giữ cấu trúc `Minh_chung/` để tham chiếu trong tài liệu mở được |
| [evidence](evidence/README_EVIDENCE.md) | Trạng thái bàn giao từng thành viên |
| [report/Word](report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx), [rà soát báo cáo](report/TV5_Ra_Soat_Bao_Cao.md) | Bản Word sao nguyên byte và kết quả đối chiếu; chưa cung cấp PDF từ bản Word cuối |
| [slide TV5 13–15](slides/TV5_Slide_13-15.pptx), [README slide](slides/README_SLIDES.md) | Deck editable 3 slide riêng TV5; deck nhóm ≤15 slide còn chờ |
| [TV5_TAP_DEMO](TV5_TAP_DEMO.md) | Kịch bản tập và bảng kết quả trống; chưa nhận là rehearsal đã PASS |

## Cài và chạy trên máy khác

Yêu cầu Windows PowerShell 5.1, **JDK 11+** có `java`/`javac` trong PATH, SQL Server (lượt QA lưu bằng chứng dùng **SQL Server 2025 RTM 17.0.1000.7, Standard Developer Edition**), SQLCMD và SSMS nếu xem/lưu actual plan. JDBC **mssql-jdbc-12.6.1.jre11.jar** đã có tại [source/lib](source/lib/). SQL Server cần bật SQL authentication, TCP/IP và cổng **1433** cho launcher GUI/identity local. Windows account chạy provisioning phải là SQL Server `sysadmin`; đây không phải password được cung cấp trong package.

Mở terminal tại **thư mục `source/` của package**. Các lệnh dưới dùng thư mục database bên cạnh source; không phụ thuộc repository hay thư mục build cũ trên máy QA.

```powershell
Copy-Item .\src\resources\config.properties.template .\src\resources\config.properties
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_sql_verification.ps1 -ServerInstance localhost -KeepDatabase
```

Runner SQL tạo một database mới `PRJ_Fix_QA_<ngày_giờ>_<mã>`, thay tên database trong bản SQL do runner tạo, cài tuần tự **01→05**, chạy kiểm tra và giữ database do `-KeepDatabase`. Lấy **đúng tên `QA target`** trong output; chỉ tiếp tục khi runner exit **0**. Lệnh này có tạo/xóa fixture test trong QA mới, không phải bước restore backup và không tự cấp login GUI. SQLCMD phải có trong PATH hoặc đúng đường dẫn fallback được runner ghi. Chỉ đặt module lên database mới; không dùng lệnh trên để migration database đang sử dụng.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\setup_gui_accounts.ps1 -Database PRJ_Fix_QA_TEN_THUC_TE
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1 -Database PRJ_Fix_QA_TEN_THUC_TE
```

Thay `PRJ_Fix_QA_TEN_THUC_TE` bằng tên vừa nhận. Provision tạo 5 SQL login/database user/profile, 4 role và 2 nhân viên A/B trong transaction; từ chối ghi đè nếu login/user/profile/nhân viên đã có. Trên instance đã có các login `gui_*`, không chạy lại chế độ tạo; dùng `-VerifyOnly` với đúng database đã mapping. Đăng nhập bằng `User` và `Password` trong [demo_accounts.json](database/demo_accounts.json): `gui_admin`, `gui_hr`, `gui_payroll`, `gui_a`, `gui_b`. Các mật khẩu này là **fixture QA môn học do nhóm chủ động chia sẻ**, không phải credential DBA/Git/GitHub. MaNV được tra theo email khi cấp tài khoản, không cố định ID máy cũ.

Fresh QA sau provision **chưa có công, phụ cấp, khấu trừ hoặc bảng lương của GUI demo tháng 10/2026**. Fixture đã có trên máy QA gốc là trường hợp riêng; không khẳng định clone/cài mới sẽ cho ngay A 730.000đ/B 0đ. [06_Demo_Data.sql](database/06_Demo_Data.sql) tạo `employee01@example.invalid`, lương 12.000.000đ và phụ cấp 730.000đ **tháng 9/2026** theo marker `DEMO_SEED_V1`; nó không tạo login, không phải seed GUI A/B tháng 10 và không tự chạy trong các lệnh trên. Nếu dùng 06 trên QA, phải thay mọi tên database trong bản sao bằng đúng QA trước khi chạy, vì file chứa `USE QuanLyNhanSuTienLuong`.

Cấu hình chỉ giữ `db.url`. Template mặc định dùng TLS `encrypt=true;trustServerCertificate=false`; launcher GUI local dùng `encrypt=true;trustServerCertificate=true` cho chứng chỉ local tự ký. Với máy triển khai dùng chứng chỉ tin cậy và `trustServerCertificate=false`. URL không chứa user/password/integratedSecurity/authentication; mỗi người đăng nhập bằng SQL login cá nhân. `DB_URL` hoặc `-Ddb.url` ưu tiên hơn template.

Với database dự án mới `QuanLyNhanSuTienLuong`, có thể cài thủ công [01](database/01_Module_NhanSu_TV1.sql) → [02](database/02_Module_ChamCong_TV2.sql) → [03](database/03_phucap_khautru_TV3.sql) → [04](database/04_Module_TinhLuong_TV4.sql) → [05](database/05_Security_Payroll_TV5.sql) bằng DBA, giữ SET options và kiểm Messages. Schema không tự cấp login. Biên dịch bằng `run_verification.ps1`, rồi dùng terminal tương tác:

```powershell
java -cp "build/verification/classes;src/resources;lib/mssql-jdbc-12.6.1.jre11.jar" com.tools.ProvisionIdentity
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_app.ps1
```

Tool yêu cầu SQL login/password DBA, login mới, role, MaNV và password cá nhân; nhập password ẩn. Employee cần MaNV đã tồn tại; mapping là SQL login → user cùng SID → role → `TAIKHOAN.SqlLogin`. DB_Admin có quyền database, không tự có quyền server reset/khóa login. Dữ liệu lịch sử lương thiếu trước migration không được tự phục hồi. Không coi tạo profile Java là đã cấp SQL login.

## Kiểm tra và đọc bằng chứng

[README bằng chứng TV5](evidence/TV5/README_EVIDENCE.md) tách rõ quyền object bằng `WITHOUT LOGIN`, đăng nhập thật theo `ORIGINAL_LOGIN`, concurrency A/B và bốn race nguồn. Plan TV5 4/5 đo **142→15 logical reads**, cùng **833 dòng** trên bảng tạm **10.000 dòng**, có index hint; không thay benchmark CHAMCONG TV2. Không dùng ảnh lịch sử trong `screenshots/TV5` làm chứng minh source/runtime mới.

Để kiểm tra byte file sau sao chép hoặc checkout Git, đối chiếu [SHA256_MANIFEST.csv](SHA256_MANIFEST.csv) cho toàn package, trừ chính manifest. [SHA256_INVENTORY.csv](source/SHA256_INVENTORY.csv) chỉ bao phủ README/source/database/evidence. [SOURCE_PROVENANCE.csv](source/SOURCE_PROVENANCE.csv) ghi nguồn và SHA256 trước/sau của launcher có đổi đường dẫn; [EVIDENCE_INVENTORY.csv](evidence/TV5/EVIDENCE_INVENTORY.csv) ghi source path, timestamp gốc, hash và phạm vi từng log/plan. Bản sao local giữ nguyên bytes, encoding và LastWriteTime gốc; Git giữ byte nhưng không bảo toàn thời gian sửa file khi clone/checkout, nên ngày chạy phải đọc từ log và inventory. Log PowerShell có thể dùng UTF-16, mở bằng SSMS/PowerShell hoặc trình soạn thảo hỗ trợ BOM.

Launcher có sửa **chỉ đường dẫn database** trong bản package của `setup_gui_accounts.ps1`, `run_sql_verification.ps1`, `run_identity_tests.ps1`, `run_payroll_tests.ps1`; source Java, SQL, JAR và evidence được sao byte gốc. Không mang `.git`, `bin`, class đã biên dịch, config local hay mật khẩu `sa` local vào bộ nộp.

**Kiểm chứng lượt đóng gói:** đã đối chiếu hash/timestamp gốc, mở XML actual plan và kiểm runtime counters, kiểm liên kết nội bộ và chạy regression Java offline từ source trong package. **Chưa cài thành công trên máy mới**, chưa chạy lại SQL/provision/GUI/identity/rehearsal hoặc backup/restore trong lượt này; PASS SQL ở đây là kết quả các log QA 07–08/10/2026, không phải lượt chạy 11/10. Runner mặc định tự dọn QA nó tạo; với `-KeepDatabase`, người cài quản lý đúng database vừa giữ lại. Cleanup cũ không chứng minh mọi QA/SQL login trên instance đã được xóa.
