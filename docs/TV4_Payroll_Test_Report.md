# TV4 – Báo cáo kiểm thử và benchmark tính lương

**Thành viên:** Nguyễn Quang Vinh – MSSV 24110385

**Ngày thực thi ban đầu:** 30/09/2026
**Rà soát/cập nhật:** 08/10/2026

**Phạm vi:** tầng SQL của luồng tính lương tháng, transaction/rollback, khóa sau chốt, concurrency cùng kỳ và benchmark truy vấn nguồn.

## 1. Phạm vi kết quả

Các số liệu bên dưới ghi lần chạy ban đầu ngày 30/09/2026 nếu không nêu ngày khác. Chúng là kết quả lịch sử của fixture và môi trường khi đó, không chứng minh trạng thái runtime mới hơn.

Lần tích hợp mới nhất ngày 08/10/2026 chạy trên database `PRJ_Fix_QA_20261008_101955_11b4e24c` và kết thúc thành công. Runner đã drop database QA. E2E TV4 đạt 10/10; benchmark đã chạy; bốn race đóng kỳ với attendance/allowance/deduction/payroll detail bị từ chối, chờ 1,033–1,101 ms và giữ nguyên dữ liệu nguồn. Log: `build/sql-verification/20261008_101955_11b4e24c/TV4_Payroll_E2E.log`, `benchmark_TV4_Payroll_Benchmark.log`, `Closed_Source_Concurrency.log`, `cleanup.log`.

## 2. Kết luận của lần đo lịch sử

Các hạng mục kiểm thử của TV4 đã được chạy bằng fixture độc lập và đều đạt:

- E2E dữ liệu nguồn → tính lương → tạo chi tiết → đối chiếu công thức: **PASS 10/10**.
- Tính lại kỳ `CHUA_CHOT` giữ nguyên một header và thay chi tiết nguyên tử.
- Lỗi khi chèn chi tiết khôi phục chính xác snapshot header/chi tiết; lần retry kế tiếp thành công.
- Kỳ `DA_CHOT` bị khóa sửa/xóa; `sp_HuyChotBangLuong` chỉ mở lại theo transition được kiểm soát.
- Hai session tính cùng kỳ được tuần tự hóa; lần chạy ngày 07/10 đo Session B bị block 20 631 ms (khoảng 20 giây) và không tạo dữ liệu trùng (xem phần 6).
- Benchmark ngày 30/09 đo covering seek khấu trừ giảm từ 547 xuống 4 logical reads; lần đo mới hơn 08/10 ghi 547 và 4 trên fixture 128 dòng. Số đo không phải ngưỡng PASS hay bảo đảm production.
- Mọi fixture E2E, benchmark và concurrency đều được cleanup; identity của các bảng benchmark không thay đổi.

Ghi chú về phạm vi chỉnh sửa chỉ áp dụng cho lượt TV4 được ghi ngày 30/09; lần tích hợp mới hơn cũng cài và kiểm tra module 05 như dependency, không suy ra từ đó rằng toàn bộ dự án chỉ thay đổi trong phạm vi TV4.

## 3. Hành vi hiện thực trong source

### 3.1 Transaction không được chiếm quyền của caller

Hai procedure TV4 `sp_TinhBangLuongThang` và `sp_HuyChotBangLuong` nay phân biệt hai trường hợp:

- không có transaction ngoài: procedure tự `BEGIN TRANSACTION`, tự `COMMIT` hoặc rollback toàn bộ khi lỗi;
- đã có transaction của caller: procedure tạo savepoint, chỉ rollback về savepoint và không commit/rollback transaction của caller.

`XACT_ABORT` được xử lý theo phạm vi procedure để lỗi bên trong không làm mất transaction do caller sở hữu. E2E kiểm tra cả lỗi validation trước transaction nội bộ và lỗi sau khi đã tạo savepoint; `@@TRANCOUNT`, `XACT_STATE()` và cấu hình `XACT_ABORT` của caller vẫn được giữ đúng.

### 3.2 Một lần tính dùng tập nguồn nhất quán

Procedure khóa kỳ bằng `UPDLOCK, HOLDLOCK`, sau đó giữ khóa trên tập nhân viên đang làm và dữ liệu nguồn của kỳ (`CHAMCONG`, `PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN`) trong cùng transaction. Ngày công được đếm trực tiếp bằng khoảng ngày nửa mở:

```sql
NgayChamCong >= DATEFROMPARTS(@Nam, @Thang, 1)
AND NgayChamCong < DATEADD(MONTH, 1, DATEFROMPARTS(@Nam, @Thang, 1))
```

Cách này tránh `MONTH()`/`YEAR()` trên cột ngày và cho phép optimizer dùng index theo khoảng ngày.

Procedure cũng giữ nhân viên nghỉ việc nếu người đó có đủ điều kiện trong kỳ (ví dụ ngày nghỉ việc từ đầu kỳ trở đi hoặc có attendance/allowance/deduction trong kỳ). Khi tính kỳ quá khứ, mức lương ưu tiên lấy từ chi tiết đã lưu của kỳ đó, sau đó lấy lịch sử gần nhất từ `LICHSULUONG` có hiệu lực trước đầu kỳ, rồi mới dùng mức lương hiện tại. Test `Fix_Regression.sql` ngày 08/10 xác nhận kỳ cũ vẫn giữ nhân viên đã nghỉ và mức lương cũ; log: `build/sql-verification/20261008_101955_11b4e24c/Fix_Regression.log`. Mức lương lịch sử chưa được lưu từ trước migration không thể tự khôi phục.

### 3.3 Trigger khóa sau chốt và nghiệp vụ mở lại

`trg_BangLuong_KhongSuaKhiDaChot` vẫn chặn sửa/xóa kỳ đã chốt, nhưng cho phép đúng transition mà procedure mở lại cần:

- `DA_CHOT` → `CHUA_CHOT`;
- `NgayChot` → `NULL`;
- không đổi `Thang`, `Nam`, `NgayCongChuan`, `NgayTao`.

Nhờ đó `sp_HuyChotBangLuong` hoạt động mà không làm yếu quy tắc bất biến của kỳ đã chốt.

### 3.4 Runner và quyền SQL

`run_payroll_tests.ps1` yêu cầu database có prefix `PRJ_Fix_QA_`; database gốc `QuanLyNhanSuTienLuong` bị từ chối. Truyền rõ `-ServerInstance` và `-Database`. Kết nối dùng `-WindowsAuthentication` hoặc biến môi trường `TEST_SQL_USER`/`TEST_SQL_PASSWORD`; script không lấy mật khẩu từ `config.properties` và không ghi mật khẩu vào log. Nếu bỏ `-Database`, runner có thể đọc tên trong cấu hình ứng dụng, nhưng vẫn áp dụng chặn QA prefix. Khi truyền server trên command line, runner không tự chuyển sang instance khác. Đây là runner TV4; `run_sql_verification.ps1` là runner khác, tự tạo và dọn QA database ngẫu nhiên bằng SQLCMD/Windows auth.

## 4. Môi trường và cách chạy

| Thành phần | Giá trị của lần đo |
|---|---|
| SQL Server | Microsoft SQL Server 2022 RTM, 16.0.1000.6 |
| Database ghi trong báo cáo cũ | `QuanLyNhanSuTienLuong` (chỉ là thông tin lịch sử ngày 30/09; không dùng làm đích chạy lại) |
| Kết nối thực thi thành công | Local Shared Memory (`.`) |
| Java/Javac | 25.0.2 |
| JDBC driver dùng để compile/test ứng dụng | `mssql-jdbc-12.6.1.jre11.jar` |
| Database runner cho phép ghi | Chỉ tên bắt đầu `PRJ_Fix_QA_` |

Chạy E2E và benchmark:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\run_payroll_tests.ps1 `
  -ServerInstance localhost `
  -Database PRJ_Fix_QA_<ten_database> `
  -WindowsAuthentication
```

Có thể thêm `-SkipBenchmark` để chỉ chạy E2E. Thay `<ten_database>` bằng database QA đã chuẩn bị; runner không tự tạo database. Hai script concurrency phải chạy thủ công trong hai session riêng; runner không tuyên bố đã chạy concurrency.

## 5. Kết quả E2E và rollback

Script: `database/tests/TV4_Payroll_E2E.sql`

| Bước | Assertion chính | Kết quả |
|---:|---|:---:|
| 1 | Tạo fixture nguồn cô lập | PASS |
| 2 | Header/chi tiết và công thức độc lập đều đúng | PASS |
| 3 | Tính lại `CHUA_CHOT`, giữ nguyên `MaBangLuong` | PASS |
| 4 | Force lỗi insert, khôi phục toàn bộ snapshot | PASS |
| 5 | Bỏ force-error và retry thành công | PASS |
| 6 | Chốt bảng lương | PASS |
| 7 | UPDATE/DELETE header đã chốt đều bị khóa | PASS |
| 8 | UPDATE/DELETE chi tiết đã chốt đều bị khóa | PASS |
| 9 | Mở lại đúng transition và giữ nguyên chi tiết | PASS |
| 10 | Cleanup dữ liệu và trigger fixture | PASS |

Oracle công thức không gọi lại chính function cần kiểm thử. `TienCong` được tính độc lập bằng `(LuongCoBan / NgayCongChuan) × NgayCongThucTe`, còn `ThucNhan` được đối chiếu bằng `TienCong + TongPhuCap - TongKhauTru`.

Trước khi force lỗi, script snapshot đầy đủ header (`MaBangLuong`, kỳ, ngày công chuẩn, trạng thái, ngày tạo, ngày chốt) và toàn bộ chi tiết. Sau lỗi, hai phép `EXCEPT` hai chiều xác nhận không có dòng bị thêm, mất hoặc sửa. Trigger khóa được xác minh theo đúng error number/message nghiệp vụ, nên lỗi quyền hay lỗi khóa ngoại không thể bị tính nhầm là PASS.

## 6. Kết quả concurrency

Scripts:

- `database/tests/TV4_Payroll_Concurrency_SessionA.sql`
- `database/tests/TV4_Payroll_Concurrency_SessionB.sql`

Session A sở hữu fixture, khóa chính xác kỳ `01/2020`, tính lương trong outer transaction rồi giữ khóa 20 giây. Chỉ sau thông báo `SESSION A DA GIU LOCK` mới chạy Session B.

| Chỉ số | Kết quả thực đo |
|---|---:|
| Thời gian Session B từ lúc gọi đến hoàn tất | 20 631 ms (khoảng 20 giây; lần chạy lịch sử 07/10) |
| Ngưỡng blocking được assertion tự động kiểm tra | ≥ 15.000 ms |
| Header cho cùng tháng/năm | 1 |
| `MaBangLuong` của A và B | Giống nhau |
| Số chi tiết | > 0 |
| Nhóm trùng `(MaBangLuong, MaNV)` | 0 |
| Fixture còn lại sau cleanup | 0 |

Lần đo lịch sử ngày 07/10/2026 dùng hai kết nối `SqlClient` độc lập, tương đương hai session SSMS. Output đã lược bỏ thông tin kết nối:

```text
[A] SESSION A DA GIU LOCK trong 20 giay. CHAY SESSION B NGAY BAY GIO.
[B] Hoan tat sau 20631 ms. Blocking da duoc chung minh.
```

Session A chỉ cleanup các khóa do chính nó tạo. Nếu kỳ demo đã tồn tại hoặc còn marker của lần chạy bị ngắt, script dừng an toàn thay vì xóa dữ liệu không rõ chủ sở hữu.

## 7. Benchmark truy vấn nguồn và index khấu trừ

Script: `database/tests/TV4_Payroll_Benchmark.sql`

Fixture của lần đo ban đầu ngày 30/09/2026 được ghi là 2.465 dòng chấm công và 30.000 dòng mỗi loại allowance/deduction. Lần chạy mới ngày 08/10/2026 báo 2.473 dòng chấm công và 30.000 dòng mỗi loại; mục tiêu allowance/deduction có 128 dòng. Cả hai đều là dữ liệu fixture, được rollback, không phải production workload. Benchmark kiểm tra chữ ký index `IX_KHAUTRU_MaNV_ThangNam`, `STATISTICS IO/TIME/XML`, kết quả truy vấn và access paths.

| Truy vấn đo | Dòng đích | Logical reads |
|---|---:|---:|
| Kiểm tra chấm công của kỳ | 30 (lịch sử) / 8 (08/10) | 8 |
| Đếm ngày công theo nhân viên + khoảng ngày | 30 (lịch sử) / 8 (08/10) | 2 |
| Tổng phụ cấp nguồn | 128 | 7 (lịch sử) / 8 (08/10) |
| Khấu trừ – forced clustered scan | 128 | 547 |
| Khấu trừ – forced covering seek | 128 | 4 |
| Khấu trừ – access path tự nhiên của truy vấn payroll | 128 | 4 |

Ngày 08/10, scan và seek trả cùng 128 dòng, tổng khấu trừ `133356.00`; truy vấn phụ cấp trả 128 dòng với tổng `261356.00`. Seek có 4 logical reads so với 547 của scan trong lần chạy này. Mức giảm khoảng 99,27% chỉ mô tả phép đo đó.

Các con số IO/thời gian phụ thuộc dữ liệu, cache và máy chạy, nên không được dùng làm ngưỡng PASS cố định. Tiêu chí tự động là cấu trúc index đúng, truy vấn nguồn trả đúng dữ liệu, scan/seek tương đương về kết quả, và rollback sạch. Sau rollback, số fixture bằng 0 và identity của cả ba bảng nguồn giữ nguyên.

## 8. Phạm vi và dependency còn lại

- Bộ tự động xác minh tầng SQL/transaction/index; không tự động thao tác UI `BangLuongPanel` hay xác minh hiển thị Java.
- `vw_TongKhauTruThang` là object tra cứu/báo cáo, không nằm trên đường thực thi hiện tại của `sp_TinhBangLuongThang`.
- Runner E2E cần tài khoản kiểm thử có quyền tạo/xóa trigger fixture, dùng `IDENTITY_INSERT` và DML trên các bảng liên quan; nó không phải bài kiểm thử quyền của role nghiệp vụ.

## 9. Kịch bản demo cá nhân

1. Chạy runner và chỉ ra 10 dòng PASS của E2E cùng result set cuối.
2. Giải thích snapshot hai chiều và trigger theo session dùng để chứng minh rollback/retry.
3. Mở hai cửa sổ SSMS, chạy Session A rồi Session B sau thông báo giữ lock; chỉ ra thời gian block, một header và không duplicate.
4. Mở Messages/Actual Execution Plan của benchmark; so sánh 547 với 4 logical reads và đối chiếu tổng tiền bằng nhau.
5. Trình bày savepoint khi procedure tham gia transaction của caller và trigger mở lại có kiểm soát.
6. Chạy lại hoặc truy vấn cleanup để chứng minh không còn fixture.

## 10. Artifact bàn giao

| Artifact | Mục đích |
|---|---|
| `database/04_Module_TinhLuong_TV4.sql` | Function/view/index/trigger/procedure và transaction TV4 |
| `database/tests/TV4_Payroll_E2E.sql` | E2E, công thức, rollback/retry, khóa và cleanup |
| `database/tests/TV4_Payroll_Benchmark.sql` | Benchmark chấm công/phụ cấp/khấu trừ + IO/TIME/XML |
| `database/tests/TV4_Payroll_Concurrency_SessionA.sql` | Session giữ lock, verify và cleanup có ownership |
| `database/tests/TV4_Payroll_Concurrency_SessionB.sql` | Session cạnh tranh cùng kỳ và đo blocking |
| `run_payroll_tests.ps1` | Runner E2E + benchmark, chọn đúng database và sinh log |
| `docs/TV4_Tien_Do_Thuc_Hien.md` | Tiến độ, đặc tả và các testcase chính của TV4 |
