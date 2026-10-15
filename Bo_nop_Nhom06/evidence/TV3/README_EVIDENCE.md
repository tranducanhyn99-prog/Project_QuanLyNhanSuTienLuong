# TV3 bộ bàn giao đã nhận

Trần Tiến Đạt — 24110198. TV5 tích hợp ngày 15/10/2026 từ [PR #43](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/43) head `72fbd95`, merge `665113c`; Word dùng bản hiệu đính [PR #48](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/48) commit `50b372d`, merge `a98371d`. Đây là các commit bàn giao tài liệu, không tự chứng minh commit SQL đã chạy trên máy TV3.

## Tài liệu và slide

- [Word TV3](Minh_chung/TV3_Phan_Bao_Cao.docx): 5 trang, có cột `KHAUTRUNHANVIEN.LyDo`; TV5 sửa hai đoạn ở trang 4 về đường dẫn ảnh và base PR. Bản này giữ nguyên byte PR #48, dùng để đối chiếu phần TV3 trong báo cáo nhóm.
- [Slide TV3 7–9](Minh_chung/TV3_Slides_7-9.pptx): ba slide và notes đúng Đạt từ PR #43. Notes đã được nhận vào [deck chung 15 slide](../../slides/Nhom06_ThuyetTrinh_15_Slides.pptx); đường dẫn trong notes của deck chung được đổi theo vị trí package. Nội dung hiển thị của deck chung giữ nguyên.

Word bàn giao dùng tiền tố đường dẫn repo `Bo_nop_TV3_TranTienDat/`. Trong package độc lập, đọc tiền tố này thành `evidence/TV3/`; toàn bộ phần `Minh_chung/` phía sau được giữ đúng cấu trúc. Các liên kết trong biên nhận này mở trực tiếp bản nằm trong package.

## Minh chứng và phạm vi

| File | Kết quả có thể sử dụng | Giới hạn |
|---|---|---|
| [Log module](Minh_chung/Log_SQLPlan/test_module_phucap_khautru_TV3.log) | Hoàn tất 13/10/2026 00:45:21 +07; bốn thông báo PASS: hàm/view, CHECK tiền/tháng, xóa header nháp 5/2020 và từ chối kỳ chốt 6/2020, cleanup | Có kỳ rỗng 12/2020; không chèn chi tiết lương, không kiểm riêng trigger hoặc SP thêm phụ cấp OUTPUT |
| [Log benchmark](Minh_chung/Log_SQLPlan/benchmark_test_benchmark_index_TV3.log) | Hoàn tất 13/10/2026 00:32:54 +07; bảng tạm 30.000 dòng, reads 642→2; CPU 0/0 ms, elapsed 37/27 ms; cleanup | Fixture riêng, không xóa cache server; không suy ra tốc độ GUI hoặc mọi workload |
| [Actual plan](Minh_chung/Log_SQLPlan/TV3_ActualPlan.sqlplan) | SQL Server 17.0.1000.7, ShowPlan XML 1.599; scan 642 reads → seek 2 reads, cùng 7 dòng nguồn và gom nhóm một dòng; có runtime counters | QueryTimeStats 4/0 ms khác nguồn đo với Messages. Không có metadata đủ chứng minh plan và Messages được thu cùng session; không gộp hai thời gian |
| [Tám ảnh](Minh_chung/Screenshots/) | Trùng byte ảnh trong `screenshots/TV3/` của repo; tư liệu lịch sử 30/09–06/10 | Ảnh IO 643→2 và GUI kỳ 9/2026 không thay log/plan ngày 13/10 hoặc demo hiện tại |

`b2e046c` chỉ là base của PR #43. Log không ghi source commit; version trong actual plan cũng không xác nhận toàn bộ DDL của database lúc chạy. Các giới hạn này được giữ khi tổng hợp Word và slide, không yêu cầu chạy lại bằng chứng hợp lệ chỉ để đổi ngày.

## Hash và tiếp nhận

[SHA256_TV3.csv](SHA256_TV3.csv) đối chiếu 13 file với snapshot nguồn trong repo: 12 file nguyên byte PR #43, Word đúng bản hiệu đính PR #48. Cột `OriginalPR43SHA256` giữ hash Word trước hiệu đính; `SourceSHA256` và `PackageSHA256` ghi bản đang dùng. `ArtifactCommit` là nguồn file bàn giao, không phải xác nhận source của lần chạy SQL. Manifest nhóm bao phủ thêm biên nhận và CSV này.

Bộ bàn giao riêng đã nhận; TV1 còn chờ ERD/log/plan cuối. Rehearsal chung, máy nhận khác và Windows DPI thật vẫn cần kiểm thực tế. Lượt tích hợp này không chạy lại SQL/GUI.
