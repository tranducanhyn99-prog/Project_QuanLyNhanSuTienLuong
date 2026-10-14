# TV4 — Bộ bàn giao đã nhận

Trạng thái nhóm 14/10: benchmark TV2 và deck chung 15 slide đã tích hợp qua PR #44–#46. Các câu “còn chờ” bên dưới ghi đúng mốc bàn giao TV4 ngày 13/10; hiện còn xác nhận TV1/TV3, rehearsal và thử máy nhận.

Nguyễn Quang Vinh — 24110385. Nhận từ [PR #41](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/41), đã merge vào main ngày 13/10/2026 (`5dc4ba4`). Source của các lượt chạy là `62223d5`; Java/SQL khớp snapshot source trong bộ nộp nhóm. Các kết quả dưới đây là lượt 10–11/10, không phải chạy lại ngày nhận bàn giao.

## Tài liệu và slide

- [Word phần TV4](TV4_Phan_Bao_Cao.docx): 4 trang; ngày 13/10 sửa độ rộng và font 11pt riêng hai bảng schema, bật lặp header bảng. Tên trường, kiểu dữ liệu, NULL và mặc định không bị ngắt giữa chữ; nội dung đoạn/bảng giữ nguyên và đã render, kiểm cả 4 trang. Đây là phần bàn giao để đối chiếu/ghép, không thay báo cáo toàn nhóm 55 trang nội dung.
- [Slide 10–12](TV4_Slides_10-12.pptx): 3 slide và notes, giữ nguyên byte PR #41; đã xem bản render PowerPoint. Đã ghép vào [deck chung 15 slide](../../slides/Nhom06_ThuyetTrinh_15_Slides.pptx). Giữ deck gốc cạnh `Minh_chung/` để tham chiếu trong notes đúng vị trí.

## Minh chứng

Thông tin nguồn, môi trường, ngày chạy và ngày chụp trong [THONG_TIN_MINH_CHUNG.txt](Minh_chung/THONG_TIN_MINH_CHUNG.txt). SQL chạy trên SQL Server 2022 Developer 16.0.1000.6, khác máy SQL Server 2025 của bộ TV5.

| Hạng mục | File | Kết quả và phạm vi |
|---|---|---|
| E2E SQL 10/10, 20:53 | [log](Minh_chung/TV4_Payroll_E2E.log), [ảnh](Minh_chung/TV4_E2E_10Steps_Pass.png) | 10/10 PASS; QA riêng, kỳ 09/2026; công thức, tính lại, rollback, chốt/mở lại và cleanup. Kỳ 09/2026 chỉ là fixture test, không thay demo chung 10/2026 |
| Benchmark SQL 10/10, 20:55 | [Messages/Results](Minh_chung/TV4_Payroll_Benchmark.log), [scan plan](Minh_chung/TV4_Payroll_Benchmark_FORCED_CLUSTERED_SCAN.sqlplan), [seek plan](Minh_chung/TV4_Payroll_Benchmark_FORCED_COVERING_SEEK.sqlplan), [IO ảnh](Minh_chung/TV4_Benchmark_StatisticsIO.png), [plan ảnh](Minh_chung/TV4_Benchmark_ExecutionPlan.png) | 30.000 dòng fixture; scan 547/seek 4 logical reads; cùng 128 khoản, tổng 133356.00; actual counters đúng và cleanup PASS. Có hint ép scan/seek, không suy ra tốc độ toàn ứng dụng; không thay benchmark CHAMCONG TV2 |
| Concurrency SQL 10/10, 20:49 | [A](Minh_chung/Concurrency_A.log), [B](Minh_chung/Concurrency_B.log), [ảnh](Minh_chung/TV4_Concurrency_2Sessions.png) | A/B 72/73, cùng header 6, 1 detail, 0 nhóm trùng; B 21072 ms; cleanup PASS. Ảnh 11/10 là log mở cạnh nhau trong SSMS, không phải ảnh hai query đang chạy |
| GUI 11/10, 04:03 | [log](Minh_chung/TV4_GUI_Live.log), [ảnh](Minh_chung/TV4_GUI_TinhLuong_102026.png) | 31 kiểm tra tự động qua Swing/SQL trên QA riêng; 10/2026, A 730.000đ/B 0đ; từ chối tính lại kỳ chốt; kết thúc CHUA_CHOT/NgayChot NULL, nguồn không đổi. QA/login đã dọn theo metadata. Không phải rehearsal tay hoặc thử Employee A/B |

TV4 21072 ms ngày 10/10 và TV5 20631 ms ngày 07/10 là hai lượt riêng. Giữ cả nguồn/ngày/môi trường, không thay số trong Word/slide TV5 bằng số của TV4. GUI 31 checks không cộng vào 224 assertions Java offline hoặc 20 ca GUI ngày 09/10.

## Toàn vẹn và phần còn chờ

[SHA256_TV4.csv](SHA256_TV4.csv) đối chiếu 15 file với blob PR #41: 14 file giữ nguyên byte, Word là bản sửa định dạng có ghi transformation. Log/plan/ảnh không chỉnh sửa. Git không giữ timestamp khi checkout; đọc ngày chạy/chụp từ log và metadata. Manifest toàn package bao phủ cả README và inventory.

Chưa chạy lại SQL/GUI, chưa tập demo tay hay cài trên máy khác trong lượt tích hợp TV4. Tại mốc 13/10, TV1–TV3 và benchmark TV2 còn chờ; từ PR #44–#46, benchmark TV2 đã nhận. Nhóm vẫn cần xác nhận TV1/TV3, rehearsal và thử máy nhận.
