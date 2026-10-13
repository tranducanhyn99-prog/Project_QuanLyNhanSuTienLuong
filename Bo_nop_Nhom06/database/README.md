# SQL trong bộ nộp

Cài tuần tự **01→05** bằng DBA trên database mới theo [README_BO_NOP](../README_BO_NOP.md). Module chứa tên `QuanLyNhanSuTienLuong`; với QA dùng [run_sql_verification.ps1](../source/run_sql_verification.ps1) hoặc bản sao đã thay mọi tên database bằng đúng QA.

| File | Nội dung |
|---|---|
| [01_Module_NhanSu_TV1.sql](01_Module_NhanSu_TV1.sql) | Nhân sự, danh mục, tài khoản, lịch sử lương |
| [02_Module_ChamCong_TV2.sql](02_Module_ChamCong_TV2.sql) | Công, constraint/trigger/view/index và fn_TinhSoNgayCong |
| [03_phucap_khautru_TV3.sql](03_phucap_khautru_TV3.sql) | Khoản thu/trừ, function và xóa kỳ nháp |
| [04_Module_TinhLuong_TV4.sql](04_Module_TinhLuong_TV4.sql) | Tính/tính lại và snapshot lương, transaction |
| [05_Security_Payroll_TV5.sql](05_Security_Payroll_TV5.sql) | Chốt/mở, khóa nguồn, GRANT/REVOKE/DENY, own view và provisioning |
| [06_Demo_Data.sql](06_Demo_Data.sql) | Tùy chọn seed riêng tháng 9/2026; không cấp login và không tạo fixture GUI A/B tháng 10 |
| [demo_accounts.json](demo_accounts.json) | Password QA môn học do nhóm cung cấp; dùng qua setup_gui_accounts, không phải password DBA |

[test_security_roles_TV5.sql](test_security_roles_TV5.sql) chỉ kiểm quyền object dưới `WITHOUT LOGIN`; login thật chạy bằng [runner identity](../source/run_identity_tests.ps1). Concurrency tính lương phải mở hai session và chạy [SessionA](tests/TV4_Payroll_Concurrency_SessionA.sql) rồi [SessionB](tests/TV4_Payroll_Concurrency_SessionB.sql) theo chỉ dẫn; cả hai phải trỏ đúng QA riêng. [Closed_Source_Concurrency.ps1](tests/Closed_Source_Concurrency.ps1) kiểm chốt đối đầu sửa bốn nguồn, có cleanup fixture.

[test_benchmark_index_TV5.sql](test_benchmark_index_TV5.sql) dùng 10.000 dòng tạm và hint. [test_benchmark_index_TV2.sql](test_benchmark_index_TV2.sql) là phép đo riêng CHAMCONG 30.000 dòng tạm, chưa có bộ evidence mới trong package. Chạy benchmark có `GO` cùng session SSMS, bật actual plan, lưu Messages/Results/plan thật; không đo trực tiếp trên database đang sử dụng. Các script module/benchmark TV1–TV4 đã kèm để thành viên hoàn thiện phần mình, không đồng nghĩa evidence của họ đã bàn giao.
