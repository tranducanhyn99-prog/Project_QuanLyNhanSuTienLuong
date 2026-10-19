# Trạng thái evidence nhóm

TV5 và TV1 cập nhật 15/10/2026: Cả 5 thành viên [TV1](TV1/README_EVIDENCE.md), [TV2](TV2/README_TV5_RECEIPT.md), [TV3](TV3/README_EVIDENCE.md), [TV4](TV4/README_EVIDENCE.md) và [TV5](TV5/README_EVIDENCE.md) đã hoàn tất bộ bàn giao, phạm vi và SHA-256.

| Thành viên | Trạng thái |
|---|---|
| [TV1](TV1/README_EVIDENCE.md) — Nguyễn Minh Trí, 24110359 | Đã nhận ERD 10 bảng (gồm LICHSULUONG, NgayNghiViec, SqlLogin), 8 ảnh minh chứng, log test nhân sự 7/7 PASS, log benchmark 75→10 logical reads |
| [TV2](TV2/README_TV5_RECEIPT.md) — Phạm Minh Quân, 24110311 | Đã nhận 52 file từ PR44 head `7efd9dd`; bốn actual plans, Results/Messages/summary và slide 4–6. Canonical v3: Messages 574→5, XML 522→5. [Hiệu đính tiếp nhận của TV5](TV2/README_TV5_RECEIPT.md#hiệu-đính-sử-dụng-của-tv5--14102026) thay các tham chiếu cũ trong handoff v2; bản TV2 gốc vẫn nguyên byte |
| [TV3](TV3/README_EVIDENCE.md) — Trần Tiến Đạt, 24110198 | Đã nhận 13 file qua PR #43/#48: Word 5 trang, slide 7–9, hai log, một actual plan và tám ảnh lịch sử. Reads 642→2; Messages 37/27 ms và XML 4/0 ms tách nguồn. Notes đã tích hợp deck chung |
| [TV4](TV4/README_EVIDENCE.md) — Nguyễn Quang Vinh, 24110385 | Đã nhận Word, slide 10–12, 5 ảnh, 5 log, 2 actual plans và metadata; E2E 10/10, benchmark 547→4, concurrency B 21072 ms, GUI 31 checks. Vẫn cần tập demo tay |
| [TV5](TV5/README_EVIDENCE.md) — Trần Đức Anh, 24110155 | Có object security, identity thật, A/B, bốn race, plan benchmark 4/5 và regression offline của bản package |

Chỉ file thật đã đọc/đối chiếu mới được ghi PASS. Không lấy ảnh tracked cũ làm bằng chứng runtime hiện hành. Rehearsal chung và mở package trên máy khác còn chờ.

**Cập nhật TV5 ngày 16/10:** PR #51 TV1 đã cung cấp bộ ERD/log/benchmark/deck và được đối chiếu; PR #52 TV2 đã cung cấp kết quả Option B cuối trong [thư mục evidence](TV2/optionb-20261016/). Word hai bản đã đồng bộ byte; notes slide 2/3/5 ghi nguồn TV1 và kết quả TV2. Chưa có xác nhận rehearsal hoặc cài đặt trên máy nhận.
