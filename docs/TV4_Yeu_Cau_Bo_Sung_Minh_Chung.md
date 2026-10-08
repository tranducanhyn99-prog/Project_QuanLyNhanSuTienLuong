# TV4 – Minh chứng kiểm thử và ảnh

- **Thành viên:** Nguyễn Quang Vinh (MSSV 24110385)
- **Rà soát:** 08/10/2026
- **Phân hệ:** Tính lương, transaction, concurrency và benchmark.

## Kết quả hiện có

Lần chạy tích hợp ngày 08/10/2026 trên database QA ngẫu nhiên đã PASS E2E TV4 10/10 và benchmark; runner đã xóa QA database sau đó. Log đầy đủ: `build/sql-verification/20261008_101955_11b4e24c/TV4_Payroll_E2E.log` và `benchmark_TV4_Payroll_Benchmark.log`.

Benchmark của lần chạy đó ghi 547 logical reads cho clustered scan và 4 cho covering seek trên 128 dòng khấu trừ, cùng tổng `133356.00`. Đây là một lần đo fixture, phụ thuộc dữ liệu/cache/máy; ảnh benchmark trước đây phải ghi ngày và phạm vi riêng, không đại diện cho kết quả mới nếu không gắn với log mới.

Hai session tính cùng kỳ có kết quả lịch sử ngày 07/10/2026: Session B chờ 20 631 ms (khoảng 20 giây); hai session nhận cùng `MaBangLuong`, một header, không có chi tiết trùng và fixture được cleanup. Log: `build/fix-verification/Concurrency_A.log` và `Concurrency_B.log`. Đây không phải concurrency case chạy trong log tích hợp ngày 08/10.

Trong lần tích hợp 08/10, bốn race đóng kỳ với sửa attendance, allowance, deduction và payroll detail đều đợi khoảng 1,0–1,1 giây, bị từ chối, nguồn không đổi và cleanup PASS. Kết quả này có trong `build/sql-verification/20261008_101955_11b4e24c/Closed_Source_Concurrency.log`.

## Ảnh đề xuất

| Tên gợi ý | Nội dung | Cách ghi phạm vi |
|---|---|---|
| `TV4_Benchmark_StatisticsIO.png` | Kết quả scan 547 reads và covering seek 4 reads | Ghi log/lần chạy 08/10/2026; benchmark fixture 128 dòng. |
| `TV4_Benchmark_ExecutionPlan.png` | Actual plan của truy vấn benchmark cùng lần chạy | Chỉ dùng nếu ảnh được chụp trong lần chạy có log nêu trên. |
| `TV4_Concurrency_2Sessions.png` | Session A/B giữ và chờ khóa kỳ | Nếu dùng ảnh cũ, ghi ngày 07/10/2026 và 20 631 ms (khoảng 20 giây); không trình bày như lần chạy 08/10. |
| `TV4_E2E_10Steps_Pass.png` | Mười bước E2E | Có thể dùng Messages từ lần chạy QA 08/10; đối chiếu với log trước khi đưa vào báo cáo. |
| `TV4_Closed_Source_Races.png` | Bốn nguồn dữ liệu bị chặn khi đóng kỳ | Ghi thời gian chờ và log 08/10; đây là race test riêng. |

Ảnh `screenshots/08_BangLuong_TinhLuong.png` và các ảnh dùng tên chung như `Benchmark_*`, `Concurrency_2Sessions.png`, `E2E_10Steps_Pass.png`, `Trigger_KhoaKyDaChot.png` đã có sẵn; thời điểm/fixture của ảnh chung chưa xác minh. Chúng không chứng minh trạng thái SQL hoặc UI hiện tại. Chụp mới thì ghi ngày, database QA và log tương ứng trong chú thích.

## Chạy lại để chụp ảnh

Chạy trong PowerShell từ thư mục dự án với SQL Server QA đã được chuẩn bị. Luôn truyền database có tên khớp `PRJ_Fix_QA_...`; runner từ chối database khác. Kết nối dùng Windows auth hoặc hai biến môi trường `TEST_SQL_USER` và `TEST_SQL_PASSWORD`. Không ghi mật khẩu vào command line hay tài liệu.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\run_payroll_tests.ps1 `
  -ServerInstance localhost `
  -Database PRJ_Fix_QA_<ten_database> `
  -WindowsAuthentication
```

Thêm `-SkipBenchmark` nếu chỉ cần E2E. Runner này không tạo database QA; nó chỉ dùng database đã chỉ định và chỉ kết nối tới host được truyền rõ ràng. Nếu bỏ `-Database`, cấu hình ứng dụng có thể được đọc để lấy tên database, nhưng giá trị đó vẫn phải qua chặn prefix QA; database mặc định `QuanLyNhanSuTienLuong` bị từ chối. Script concurrency hai session là thủ công và cần database QA riêng; kiểm tra marker/điều kiện sẵn có trong scripts trước khi chạy. Không chạy lệnh chụp/test trên database dự án gốc.
