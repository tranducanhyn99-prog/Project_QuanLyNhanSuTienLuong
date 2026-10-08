# TV3 – Kết quả rà soát và hoàn thiện

- **Thành viên:** Trần Tiến Đạt (MSSV 24110198)
- **Phân hệ:** Phụ cấp, khấu trừ và nghiệp vụ liên quan
- **Rà soát cập nhật:** 08/10/2026
- **Bối cảnh:** Tài liệu này ghi lại các điểm từng được nêu khi rà PR #28 ngày 02/10/2026 và trạng thái đối chiếu với source hiện tại. Các bước checkout/commit/push cũ đã bị loại bỏ vì không phải hướng dẫn áp dụng cho checkout hiện tại.

## Kết quả đối chiếu source

| Hạng mục từng được nêu | Trạng thái theo source hiện tại |
|---|---|
| TV3 khai báo lặp `sp_GhiNhanChamCong` hoặc trigger kiểm tra giờ chấm công | `database/03_phucap_khautru_TV3.sql` không định nghĩa hai object này. Chấm công thuộc module TV2. |
| Thiếu hàm tổng phụ cấp dùng cho tính lương | `dbo.fn_TongPhuCap` được định nghĩa trong module 03; `sp_TinhBangLuongThang` gọi hàm này. |
| Guard phụ cấp/khấu trừ/chấm công cho kỳ đã chốt | Các trigger nằm trong module 05. Chúng đọc dòng `BANGLUONG` với `UPDLOCK, HOLDLOCK`, xét kỳ ở cả `inserted` và `deleted`, và rollback khi kỳ bị chốt. |
| Xóa kỳ lương chưa chốt không an toàn | `sp_XoaKyLuongChuaChot` khóa dòng kỳ bằng `UPDLOCK, HOLDLOCK`, xóa chi tiết trước header trong transaction, và từ chối kỳ đã chốt. |
| Test TV3 lỗi hoặc ghi nhãn PASS quá rộng | Lần tích hợp 08/10 xác nhận function/view với dữ liệu và kết quả rỗng, ràng buộc tiền/kỳ, xóa kỳ nháp, từ chối kỳ chốt và cleanup fixture. Chi tiết tại log được dẫn bên dưới. |
| Seed làm mất dữ liệu có sẵn | `database/06_Demo_Data.sql` không xóa phụ cấp/khấu trừ theo tháng; chỉ thêm dữ liệu còn thiếu và chạy trong transaction. Modules 01–05 không tự nạp seed. |
| Nút Thêm/Xóa TV3 | Source hiện tại dùng `createSuccessButton` cho Thêm và `UITheme.styleDangerButton` cho Xóa; ảnh cũ chỉ là minh chứng tại thời điểm chụp, không phải kiểm thử giao diện mới. |

## Kết quả kiểm chứng hiện tại

- Lần chạy SQL tích hợp ngày 08/10/2026: testcase TV3 PASS và cleanup fixture PASS. Log: `build/sql-verification/20261008_101955_11b4e24c/test_module_phucap_khautru_TV3.log`.
- Benchmark fixture 30.000 dòng ngày 08/10: 642 logical reads trước index và 2 sau index trên temporary table; số đo phụ thuộc dữ liệu/cache/máy. Log: `build/sql-verification/20261008_101955_11b4e24c/benchmark_test_benchmark_index_TV3.log`.
- Lần chạy tích hợp kiểm tra bốn race đóng kỳ với attendance, allowance, deduction và payroll detail. Cả bốn mutation đợi khoảng 1,0–1,1 giây, bị từ chối, nguồn không đổi; fixture cleanup PASS. Log: `build/sql-verification/20261008_101955_11b4e24c/Closed_Source_Concurrency.log`.

Các kết quả trên dùng database QA ngẫu nhiên do runner tạo và xóa; chúng không chứng minh trạng thái database dự án gốc. Hình ảnh cũ và các phép đo trước đây cần được ghi kèm ngày/phạm vi của lần đo, không dùng thay cho log 08/10. Schema không tạo tài khoản hoặc mật khẩu demo mặc định; SQL login được DBA provision riêng.
