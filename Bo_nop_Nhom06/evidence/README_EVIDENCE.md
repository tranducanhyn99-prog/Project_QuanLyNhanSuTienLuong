# Trạng thái evidence nhóm

TV5 tích hợp cập nhật 15/10/2026: [TV2](TV2/README_TV5_RECEIPT.md), [TV3](TV3/README_EVIDENCE.md), [TV4](TV4/README_EVIDENCE.md) và [TV5](TV5/README_EVIDENCE.md) đã có bộ bàn giao, phạm vi và SHA-256. TV1 vẫn chờ bộ cuối; marker chờ không được tính là evidence.

| Thành viên | Trạng thái |
|---|---|
| [TV1](TV1/CHO_BAN_GIAO.md) — Nguyễn Minh Trí, 24110359 | Chờ ERD hiện hành đủ 10 bảng và bộ log/plan do TV1 lựa chọn |
| [TV2](TV2/README_TV5_RECEIPT.md) — Phạm Minh Quân, 24110311 | Đã nhận 52 file từ PR44 head `7efd9dd`; bốn actual plans, Results/Messages/summary và slide 4–6. Canonical v3: Messages 574→5, XML 522→5. [Hiệu đính tiếp nhận của TV5](TV2/README_TV5_RECEIPT.md#hiệu-đính-sử-dụng-của-tv5--14102026) thay các tham chiếu cũ trong handoff v2; bản TV2 gốc vẫn nguyên byte |
| [TV3](TV3/README_EVIDENCE.md) — Trần Tiến Đạt, 24110198 | Đã nhận 13 file qua PR #43/#48: Word 5 trang, slide 7–9, hai log, một actual plan và tám ảnh lịch sử. Reads 642→2; Messages 37/27 ms và XML 4/0 ms tách nguồn. Notes đã tích hợp deck chung |
| [TV4](TV4/README_EVIDENCE.md) — Nguyễn Quang Vinh, 24110385 | Đã nhận Word, slide 10–12, 5 ảnh, 5 log, 2 actual plans và metadata; E2E 10/10, benchmark 547→4, concurrency B 21072 ms, GUI 31 checks. Vẫn cần tập demo tay |
| [TV5](TV5/README_EVIDENCE.md) — Trần Đức Anh, 24110155 | Có object security, identity thật, A/B, bốn race, plan benchmark 4/5 và regression offline của bản package |

Chỉ file thật đã đọc/đối chiếu mới được ghi PASS. Không lấy ảnh tracked cũ làm bằng chứng runtime hiện hành. Rehearsal chung và mở package trên máy khác còn chờ.
