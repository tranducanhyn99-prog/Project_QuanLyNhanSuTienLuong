# TV5 — danh mục bằng chứng thực tế

Phụ trách **Trần Đức Anh, 24110155**; đóng gói 11/10/2026 từ `main f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`. Log SQL/identity/plan là **lượt QA 07–08/10/2026**, được sao byte gốc; không phải lượt chạy lại SQL ngày đóng gói. Timestamp file là giờ máy QA (UTC+07:00 theo môi trường làm việc); timestamp không thay commit nguồn của lượt chạy.

## Bổ sung ngày 13/10

- [verification-20261013](verification-20261013/summary.txt): chín case tiền, ba overflow 8115 và payroll rollback; benchmark 10.000 dòng, 833 kết quả, **126→13 reads**, hint scan/seek, warm cache/MAXDOP 1. Có Messages, Results đầy đủ, hai actual plans, hash source đã chạy và cleanup. `database-kept.txt` ghi bước giữ QA tạm; `cleanup.log` xác nhận đã dọn sau kiểm GUI.
- [sql-regression-20261013](sql-regression-20261013/cleanup.log): schema environment, các module, Fix Regression, money, object security, payroll E2E và bốn race nguồn PASS. Không nhận đây là identity suite 50 checks hoặc mọi chức năng GUI đã chạy lại.
- [gui-20261013](gui-20261013/write-outage-fixed.png): mất kết nối TCP relay riêng lúc login/đổi role, kiểm lại hộp thoại sau sửa và SQL profile unchanged; đổi role thành công sau phục hồi, đăng nhập thật sau đổi và từ chối khóa khi thiếu quyền server. Có log/ảnh/đối chiếu SQL/cleanup. Query timeout probe riêng ở JDBC, không phải GUI query timeout. Chưa kiểm đầy đủ khóa/mở/reset, report outage, in-flight transaction, DPI và cửa sổ nhỏ.
- [package-verification-20261013](package-verification-20261013/summary.txt): SQL/runner lấy từ package chạy ở thư mục sạch không có Git trên cùng máy, money/benchmark và cleanup PASS; [GuiQa](package-verification-20261013/GuiQaRegressionTest.log) 62 + [Security](package-verification-20261013/SecurityRegressionTest.log) 104 + [LightTheme](package-verification-20261013/LightThemeRegressionTest.log) 45 + [LoginChip](package-verification-20261013/LoginChipRegressionTest.log) 20 = **231 assertions**, compile 54 nguồn Java 11. Không thay kiểm cài/chạy GUI trên máy khác.
- [layout-20261013](layout-20261013/README.md): ba lượt render Swing ở cửa sổ tối thiểu/Java scale 1, 1.25, 1.5; 21 assertions bounds và chín ảnh. Chỉ kiểm layout với session fixture, report chưa có dữ liệu; không gán là Windows DPI thật hoặc live GUI SQL.

[EVIDENCE_20261013.csv](EVIDENCE_20261013.csv) ghi file gốc, byte/SHA-256 và phạm vi của phần mới. Các mục bên dưới giữ dữ liệu lịch sử 07–11/10, gồm benchmark 142→15; không trộn số đo hoặc đổi ngày chạy.

## Follow-up GUI và birthday 13/10

[GUI follow-up](gui-followup-20261013/README.md):87 core assertions tự động, focusedphone7 riêng/native modal đầy đủ, các module/role/lifecycle/outage/session; cleanup và quyền tạm được verify. [Age boundaries](age-boundaries-20261013/README.md):9 cases/18 SQL operations +7 TV1 callers trên fresh QA; create-only. Current offline257 (Gui88/Security104 rerun, Light45/Login20 unchanged), không sửa số231 lịch sử.

## Quyền và danh tính thật

| Bằng chứng | Ngày/môi trường | Case/kết quả và giới hạn |
|---|---|---|
| [test_security_roles_TV5.log](sql-verification/test_security_roles_TV5.log), [script đã chạy](sql-verification/test_security_roles_TV5.sql) | 08/10, `PRJ_Fix_QA_20261008_101955_11b4e24c`, Windows DBA | **22 permission checks, 3 thao tác bị từ chối, cleanup PASS**. Bốn user `WITHOUT LOGIN`, `EXECUTE AS USER`; chứng minh object grants/deny, chưa chứng minh own-row theo ORIGINAL_LOGIN |
| [install 05 log](sql-verification/install_05_Security_Payroll_TV5.log), [rerun 05 log](sql-verification/rerun_05_Security_Payroll_TV5.log), [SQL 05 đã cài](sql-verification/install_05_Security_Payroll_TV5.sql) | 08/10, cùng QA SQL verification | Có GRANT role nghiệp vụ, REVOKE SELECT global payroll view của Employee, DENY bảng/thao tác ngoài role; log cài/rerun không có lỗi. Security test kiểm trạng thái cuối; không phải thí nghiệm riêng thay grant rồi revoke một quyền ở runtime |
| [identity.log](identity-agent/identity.log) | 07/10, `localhost:1433`, `PRJ_Fix_QA_20261007_Identity`; bốn role và A/B login thật | **41 checks PASS, 0 skipped**: SQL identity từng role; HR/Payroll nghiệp vụ theo quyền; Employee A đọc own DAO/view, không đọc B/global; giả Session không vượt quyền; reset/lock/role/migrate/logout và ngày DAO |
| [provision.log](identity-agent/provision.log), [config.log](identity-agent/config.log), [cleanup.log](identity-agent/cleanup.log) | 07/10, cùng QA identity | **2 + 6 + 1 PASS**: provisioning sai không để side effect, URL chứa auth override bị từ chối trước connection, database/logins do lượt này tạo được dọn. Tổng identity suite **50**, không cộng vào 224 regression Java offline |

[schema_environment.log](sql-verification/schema_environment.log) ghi tên QA, Windows SQL identity và **SQL Server 2025 RTM 17.0.1000.7 X64, Standard Developer Edition, Windows build 26200** cho lượt SQL verification 08/10. Bản [cleanup.log SQL](sql-verification/cleanup.log) ghi PASS dọn **QA do runner đó sở hữu**. Lượt identity/A-B không có log `@@VERSION` riêng; không gán môi trường 08/10 là metadata được ghi trực tiếp trong các log 07/10. [run.ps1 lịch sử identity](identity-agent/run.ps1) giúp truy xuất fixture và kết nối; dùng launcher hiện tại trong source để chạy mới, không chạy file lưu trữ này như installer.

## Concurrency

| Bằng chứng | Ngày/phạm vi | Kết quả |
|---|---|---|
| [Concurrency_A.log](fix-verification/Concurrency_A.log), [Concurrency_B.log](fix-verification/Concurrency_B.log) | **07/10 22:33**, QA `PRJ_Fix_QA_20261007_01`; hai session tính cùng kỳ **01/2020**, fixture của script SessionA/B | A SPID77 giữ lock; B SPID83 hoàn thành sau **20631ms**. Cùng MaBangLuong8, **1 header/1 chi tiết/1 nhân viên/0 nhóm trùng**. A kiểm **0 payroll demo/0 công fixture còn lại**, cleanup PASS. Đây là log thật của fixture 2020, không phải concurrency trên GUI fixture 10/2026 |
| [Closed_Source_Concurrency.log](sql-verification/Closed_Source_Concurrency.log) | **08/10**, QA riêng SQL verification; hai connection thật, chốt đối đầu attendance/allowance/deduction/payroll-detail | Bốn race chờ **1101/1046/1042/1033ms**, đều bị từ chối, **source unchanged**, fixture cleanup PASS. Chứng minh các race đã chạy; không đo mọi workload, deadlock recovery hoặc backup/restore |

Script hiện tại tương ứng nằm trong [database/tests](../../database/tests/): [A](../../database/tests/TV4_Payroll_Concurrency_SessionA.sql), [B](../../database/tests/TV4_Payroll_Concurrency_SessionB.sql), [race nguồn](../../database/tests/Closed_Source_Concurrency.ps1). Giới hạn là fixture/đường thao tác nêu trong log, không kết luận mọi thao tác GUI đều đã thử. Không gọi timeout mô phỏng là đã cancel câu SQL đang chạy trong GUI.

## Benchmark TV5: chọn đúng plan 4/5

Hai [plan 4](fix-verification/TV5_ActualPlan_4.sqlplan) và [plan 5](fix-verification/TV5_ActualPlan_5.sqlplan) có XML runtime counters thật, cùng temp table/fixture, thống kê cập nhật **07/10/2026 22:42:45**, XML Build `17.0.1000.7`. Query chọn `MaNV, HoTen, LuongCoBan, TrangThai` với `MaPB=2 AND MaCV=1` trên `#BenchmarkNhanVien`; fixture **10.000 dòng**, có hint chọn index. Script hiện tại là [test_benchmark_index_TV5.sql](../../database/test_benchmark_index_TV5.sql).

| Plan | Hint/operator | ActualRows | ActualRowsRead | ActualLogicalReads | QueryTimeStats CPU/elapsed |
|---|---|---:|---:|---:|---:|
| 4 | PK_Bench_MaNV / Clustered Index Scan | 833 | 10.000 | **142** | 1ms / 3ms |
| 5 | IX_Bench_MaPB_MaCV / Index Seek | 833 | 833 | **15** | 0ms / 2ms |

Các giá trị trên đọc trực tiếp từ plan, không dựng lại từ ảnh. Cùng predicate và count 833; **chưa có file Results để đối chiếu toàn bộ giá trị từng dòng**, chưa có Messages IO/TIME độc lập hoặc log cleanup benchmark TV5 trong nguồn được chọn. Vì vậy dùng 142→15 với giới hạn plan/fixture/hint, không tuyên bố đủ mọi đầu ra benchmark. Plan6 truy vấn dbo.NHANVIEN trả **0 dòng** không được chọn làm plan cải thiện và không có trong package. Plan TV5 không thay benchmark index CHAMCONG của TV2; query time rất nhỏ không đủ kết luận tốc độ ổn định của workload thực.

## Nguồn và kiểm toàn vẹn

[EVIDENCE_INVENTORY.csv](EVIDENCE_INVENTORY.csv) liệt kê **31 file gốc**: đường dẫn trong package, đường dẫn repository nguồn, timestamp gốc (nanoseconds + giờ local), SHA256 trước/sau, phạm vi. Bản sao local giữ nguyên encoding/BOM và LastWriteTime. Git không bảo toàn LastWriteTime sau clone/checkout; sử dụng timestamp trong inventory và log để xác định ngày chạy. File `.sql` được lưu để biết schema QA đã cài; không sửa tên database trong evidence.

[SOURCE_SCOPE.csv](SOURCE_SCOPE.csv) được đối chiếu lại ngày 13/10: module02–04 giữ logic tương ứng; module01/05 trong evidence cũ **không còn khớp** CHECK tuổi/precision tiền source package mới. Giữ nguyên bản SQL/log lịch sử, không suy ra chúng đã kiểm miền tiền mới; proof mới ở verification/package-verification-20261013. Các khác biệt BOM/newline/tên QA/whitespace được ghi riêng trong CSV. Hash source hiện tại ở [SOURCE_PROVENANCE.csv](../../source/SOURCE_PROVENANCE.csv); log cũ không nhúng commit nguồn, không gán PASS vô điều kiện cho phiên bản mới.

[SHA256_INVENTORY.csv](../../source/SHA256_INVENTORY.csv) bao phủ các file đóng gói của lượt TV5, kể cả README và log kiểm package mới. Ảnh lịch sử `screenshots/TV5` không được chép vào tập chứng cứ này.

## Kiểm mới ở lượt đóng gói 11/10

Chạy Java từ **source trong package** với JDK21, `--release 11`: compile **54 Java files**; [Security](package-verification/SecurityRegressionTest.log) **104**, [LightTheme](package-verification/LightThemeRegressionTest.log) **45**, [LoginChip](package-verification/LoginChipRegressionTest.log) **20**, [GuiQa](package-verification/GuiQaRegressionTest.log) **55 assertions PASS**, tổng **224**, không dùng SQL live. Parser PowerShell của 7 launcher hợp lệ; đường dẫn SQL/JAR/template tồn tại; XML plan, hash/timestamp gốc và link nội bộ được đối chiếu. Class/build sinh từ kiểm package được chuyển ra thư mục build của repository, không nằm trong bản giao.

**Lượt 11/10 chưa kiểm cài mới trên máy khác hoặc chạy lại SQL/provision/GUI/rehearsal/backup-restore.** Phần kiểm mới ngày 13/10 ghi riêng ở đầu tài liệu; bộ nhóm vẫn chưa hoàn tất. Các phần chờ được ghi ở [trạng thái evidence nhóm](../README_EVIDENCE.md).
