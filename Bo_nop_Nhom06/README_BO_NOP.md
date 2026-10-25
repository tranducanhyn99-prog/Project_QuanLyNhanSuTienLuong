# Bộ nộp Nhóm 06 — đã nhận đủ 5 thành viên (TV1, TV2, TV3, TV4, TV5); có deck chung 15 slide

Source Java/SQL được đóng gói từ `main`, tích hợp đầy đủ 5 thành viên (TV1, TV2, TV3, TV4, TV5). TV5 (Trần Đức Anh — 24110155) phụ trách kiến trúc, bảo mật 2 tầng, chốt lương, xử lý đồng thời và đóng gói. Đã tích hợp [bộ TV1](evidence/TV1/) (ERD 10 bảng, log test 7/7 PASS, benchmark 75→10), [bộ TV2](evidence/TV2/), [bộ TV3](evidence/TV3/), [bộ TV4](evidence/TV4/) và [bộ TV5](evidence/TV5/). [Deck chung](slides/Nhom06_ThuyetTrinh_15_Slides.pptx) và [Báo cáo cuối kỳ Word](report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) đã tích hợp đầy đủ nội dung của cả nhóm.

| Thư mục | Nội dung |
|---|---|
| [source](source/README.md) | Java main/test, template URL, JDBC JAR, launcher/provision/test runner |
| [database](database/README.md) | Module 01→05, seed 06–07 tùy chọn, demo account manifest và script kiểm thử |
| [evidence/TV1](evidence/TV1/) | ERD 10 bảng hiện hành, 8 ảnh minh chứng, log test nhân sự 7/7 PASS, log benchmark 75→10 logical reads |
| [evidence/TV2](evidence/TV2/) | Minh chứng CHAMCONG v3 (574→5 reads), 4 actual plans, log Option B JDBC & SQL 21/21 PASS, slide 4–6 |
| [evidence/TV3](evidence/TV3/) | Word phần TV3, slide 7–9, log test module, execution plan và ảnh minh chứng |
| [evidence/TV4](evidence/TV4/) | Word phần TV4, slide 10–12, log E2E 10/10 PASS, concurrency và benchmark |
| [evidence/TV5](evidence/TV5/) | Log/plan benchmark (142→15 / 126→13 reads), identity test, concurrency race và GUI QA |
| [report/Word](report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) | Báo cáo tổng kết cuối kỳ đầy đủ của Nhóm 06 (69 trang tổng / 56 trang nội dung) |
| [slides](slides/README_SLIDES.md) | Deck thuyết trình chung [Nhom06_ThuyetTrinh_15_Slides.pptx](slides/Nhom06_ThuyetTrinh_15_Slides.pptx) và slide thành viên |

## Cài và chạy trên máy khác

Yêu cầu Windows PowerShell 5.1, **JDK 11+** có `java`/`javac` trong PATH, SQL Server, SQLCMD và SSMS nếu xem/lưu actual plan. JDBC **mssql-jdbc-12.6.1.jre11.jar** đã có tại [source/lib](source/lib/). SQL Server cần bật SQL authentication, TCP/IP và cổng **1433** cho launcher GUI/identity local. Windows account chạy provisioning phải là SQL Server `sysadmin`.

Mở terminal tại **thư mục `source/` của package**. Các lệnh dưới dùng thư mục database bên cạnh source:

```powershell
Copy-Item .\src\resources\config.properties.template .\src\resources\config.properties
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_verification.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_sql_verification.ps1 -ServerInstance localhost -KeepDatabase
```

Runner SQL tạo một database mới `PRJ_Fix_QA_<ngày_giờ>_<mã>`, cài tuần tự **01→05**, chạy kiểm tra và giữ database do `-KeepDatabase`. Lấy **đúng tên `QA target`** trong output; chỉ tiếp tục khi runner exit **0**:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\setup_gui_accounts.ps1 -Database PRJ_Fix_QA_TEN_THUC_TE
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1 -Database PRJ_Fix_QA_TEN_THUC_TE
```

Đăng nhập bằng `User` và `Password` trong [demo_accounts.json](database/demo_accounts.json): `gui_admin`, `gui_hr`, `gui_payroll`, `gui_a`, `gui_b`.

Với database dự án chính `QuanLyNhanSuTienLuong`, có thể cài tuần tự [01](database/01_Module_NhanSu_TV1.sql) → [02](database/02_Module_ChamCong_TV2.sql) → [03](database/03_phucap_khautru_TV3.sql) → [04](database/04_Module_TinhLuong_TV4.sql) → [05](database/05_Security_Payroll_TV5.sql) (và [07](database/07_Real_Enterprise_Seed.sql) / [06](database/06_Demo_Data.sql) nếu cần dữ liệu demo), cấp tài khoản qua `setup_gui_accounts.ps1` hoặc `com.tools.ProvisionIdentity`, rồi chạy:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_app.ps1
```
