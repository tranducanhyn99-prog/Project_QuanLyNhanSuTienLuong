# Bộ nộp Nhóm 06 — đã nhận đủ 5 thành viên (TV1, TV2, TV3, TV4, TV5); có deck chung 15 slide

Source Java/SQL được đóng gói ngày **11/10/2026**, từ `main` commit **f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5**, có các bản cập nhật TV5 13/10 ghi bên dưới. TV5: **Trần Đức Anh — 24110155**. Đã tích hợp [bộ TV1](evidence/TV1/README_EVIDENCE.md) (ERD 10 bảng, log test 7/7 PASS, benchmark 75→10), [bộ TV2](evidence/TV2/README_TV5_RECEIPT.md), [bộ TV3](evidence/TV3/README_EVIDENCE.md) và [bộ TV4](evidence/TV4/README_EVIDENCE.md). [Deck chung 15 slide](slides/Nhom06_ThuyetTrinh_15_Slides.pptx) đã tích hợp đầy đủ khung và notes của các thành viên.

**Cập nhật TV5 13/10:** source module 05/function tiền, benchmark, runner và thông báo lỗi GUI đã sửa từ working tree dựa trên `a7e1096`; Word đồng bộ mô tả tiền. SHA-256 trong provenance xác định file cụ thể, chưa gán thay đổi này cho một commit mới. Có [money/benchmark mới](evidence/TV5/verification-20261013/summary.txt), [kiểm package standalone](evidence/TV5/package-verification-20261013/summary.txt) và [GUI QA](evidence/TV5/gui-20261013/write-outage-fixed.png). Source TV4 giữ nguyên; evidence TV4 thu trước thay đổi tiền TV5, không tự nhận đã kiểm miền tiền mới. PR #43 đã merge; Word TV3 đã hiệu đính qua PR #48, log/plan/ảnh và PPTX gốc giữ nguyên. Biên nhận TV3 phân biệt commit bàn giao với commit SQL chưa xác minh.

**Follow-up TV5 đến 16/10:** [GUI87 + focusedphone7](evidence/TV5/gui-followup-20261013/README.md), [age fresh QA](evidence/TV5/age-boundaries-20261013/README.md), Java offline 257=88+104+45+20; TV2 Option B mới có 55 nguồn biên dịch, suite kiểm soát 179/179, SQL 21/21, concurrency 4/4 và JDBC 105 assertions. Bản Word trong bộ nộp và `docs/` đã đồng bộ byte (69 trang tổng/56 nội dung); chỉ trang 11 và 33 đổi so với PDF QA trước. Deck 15 slide giữ nguyên phần hiển thị; notes 2/3/5 cập nhật và kiểm bằng Artifact Tool. TV1 handoff PR #51 đã được kiểm; ba slide nhóm vẫn là tóm tắt ngắn đối chiếu với bộ TV1. Windows DPI thật, cài máy nhận và rehearsal chưa kiểm. [Biên nhận TV2](evidence/TV2/README_TV5_RECEIPT.md) giữ nguyên bàn giao gốc; evidence Option B ngày 16/10 được thêm riêng. CHECK tuổi module01 create-only, không tự migrate constraint database cũ.

| Thư mục | Nội dung |
|---|---|
| [source](source/README.md) | Java main/test, template URL, JDBC JAR, launcher/provision/test runner |
| [database](database/README.md) | Module 01→05, seed 06 tùy chọn, demo account manifest và script kiểm thử |
| [bộ TV1](evidence/TV1/README_EVIDENCE.md) | ERD 10 bảng hiện hành, 8 ảnh minh chứng, log test nhân sự 7/7 PASS, log benchmark 75→10 logical reads |
| [bộ TV2](evidence/TV2/README_TV5_RECEIPT.md) | 52 file bàn giao: benchmark CHAMCONG v3 (574→5 reads), 4 actual plans, slide 4–6 |
| [bộ TV3](evidence/TV3/README_EVIDENCE.md) | 13 file bàn giao: Word hiệu đính PR #48, slide 7–9, hai log, một plan và tám ảnh lịch sử; nguồn/hash trong SHA256_TV3.csv |
| [bộ TV4](evidence/TV4/README_EVIDENCE.md) | Word phần TV4 đã sửa bảng, slide 10–12 và 13 file minh chứng; giữ cấu trúc `Minh_chung/` để mở tham chiếu |
| [evidence/TV5](evidence/TV5/README_EVIDENCE.md) | Log/plan gốc đã chọn, phạm vi đo và danh mục SHA256 |
| [evidence](evidence/README_EVIDENCE.md) | Trạng thái bàn giao từng thành viên |
| [report/Word](report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx), [rà soát báo cáo](report/TV5_Ra_Soat_Bao_Cao.md) | Bản trong package và `docs/` đồng nhất byte; 69/56 trang, cập nhật TRY…CATCH và kiểm bố cục lại trang 11/33; PDF cuối chỉ tạo nếu được yêu cầu |
| [slide TV5 13–15](slides/TV5_Slide_13-15.pptx), [README slide](slides/README_SLIDES.md) | Deck riêng TV5 và deck chung 15 slide; notes cập nhật theo TV1/TV2, phần hiển thị và 15 slide được giữ nguyên |
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

Từ thư mục `source/`, chạy `powershell -NoProfile -ExecutionPolicy Bypass -File ..\run_tv5_verification.ps1` để thu kiểm biên và benchmark TV5 trên QA mới. Runner đặt ở root package vì dùng `database/` cạnh nó; không cần Git để chạy standalone. Mặc định runner dọn đúng QA nó tạo; chỉ dùng `-KeepDatabase` khi cần giữ tạm và tự quản lý cleanup. Benchmark yêu cầu QA có tên `PRJ_Fix_QA_*`.

Ngày 13/10 đã kiểm bản SQL/runner lấy từ package trong thư mục sạch cùng máy, không có `.git`: money/benchmark PASS, 833 dòng, 126→13 reads, cleanup PASS. Biên dịch 54 nguồn từ package và bốn regression Java đạt **231 assertions** (62+104+45+20). TV2 cập nhật 16/10: **55 nguồn compile**, controlled suite **179/179**, SQL **21/21**, concurrency **4/4**, JDBC **105 assertions**; log mới nằm ở [evidence TV2 Option B](evidence/TV2/optionb-20261016/). Đây chưa phải cài/chạy đầy đủ trên máy khác. Số 142→15 trong các đoạn lịch sử bên dưới thuộc lượt cũ, không bị thay bằng số đo mới.

[README bằng chứng TV5](evidence/TV5/README_EVIDENCE.md) tách rõ quyền object bằng `WITHOUT LOGIN`, đăng nhập thật theo `ORIGINAL_LOGIN`, concurrency A/B và bốn race nguồn. Plan TV5 4/5 đo **142→15 logical reads**, cùng **833 dòng** trên bảng tạm **10.000 dòng**, có index hint; không thay benchmark CHAMCONG TV2. Không dùng ảnh lịch sử trong `screenshots/TV5` làm chứng minh source/runtime mới.

Để kiểm tra byte file sau sao chép hoặc checkout Git, đối chiếu [SHA256_MANIFEST.csv](SHA256_MANIFEST.csv) cho toàn package, trừ chính manifest. Bản manifest cập nhật 16/10 liệt kê 573 file; đã xác minh đủ hash/byte và rà 309 liên kết Markdown, các tham chiếu thiếu trong handoff TV2 được ghi chú rõ tại biên nhận. [SHA256_INVENTORY.csv](source/SHA256_INVENTORY.csv) chỉ bao phủ README/source/database/evidence. [SOURCE_PROVENANCE.csv](source/SOURCE_PROVENANCE.csv) ghi nguồn và SHA256 trước/sau của launcher có đổi đường dẫn; [EVIDENCE_INVENTORY.csv](evidence/TV5/EVIDENCE_INVENTORY.csv) ghi source path, timestamp gốc, hash và phạm vi từng log/plan. Bản sao local giữ nguyên bytes, encoding và LastWriteTime gốc; Git giữ byte nhưng không bảo toàn thời gian sửa file khi clone/checkout, nên ngày chạy phải đọc từ log và inventory. Log PowerShell có thể dùng UTF-16, mở bằng SSMS/PowerShell hoặc trình soạn thảo hỗ trợ BOM.

Launcher có sửa **chỉ đường dẫn database** trong bản package của `setup_gui_accounts.ps1`, `run_sql_verification.ps1`, `run_identity_tests.ps1`, `run_payroll_tests.ps1`; source Java, SQL, JAR và evidence được sao byte gốc. Không mang `.git`, `bin`, class đã biên dịch, config local hay mật khẩu `sa` local vào bộ nộp.

**Kiểm chứng lượt đóng gói:** đã đối chiếu hash/timestamp gốc, mở XML actual plan và kiểm runtime counters, kiểm liên kết nội bộ và chạy regression Java offline từ source trong package. **Chưa cài thành công trên máy mới**, lượt 11/10 chưa chạy lại SQL/provision/GUI/identity/rehearsal hoặc backup/restore; follow-up13/10 ở trên có QA riêng; PASS SQL ở đây là kết quả các log QA 07–08/10/2026, không phải lượt chạy 11/10. Runner mặc định tự dọn QA nó tạo; với `-KeepDatabase`, người cài quản lý đúng database vừa giữ lại. Cleanup cũ không chứng minh mọi QA/SQL login trên instance đã được xóa.
