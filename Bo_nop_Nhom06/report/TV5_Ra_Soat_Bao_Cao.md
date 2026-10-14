# TV5 — Rà soát và bàn giao báo cáo Word

**Trần Đức Anh — 24110155 · Cập nhật 14/10/2026**

[Word nhóm](Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) được đồng bộ nguyên byte với `docs/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx`. Ba chỗ ghi trạng thái source/PR cũ ở lời mở đầu, bảng bàn giao và cuối báo cáo đã cập nhật sau khi PR #44–#46 merge; §8.5/§9.3 vốn đã có benchmark TV2 ngày 13/10 nên không sửa số liệu. Bản Word trước lượt này đối chiếu checkout `a7e1096` và các sửa local tại ngày 13/10.

SHA-256 của cả hai bản Word:

```text
173F4181F7925FB8792AC9FAD42BE51725AF438361B883CDF9EFD709E88AFF11
```

## Nội dung đã cập nhật

- TV5: `fn_TinhThucNhan` và fallback DDL tiền thống nhất `DECIMAL(18,2)`. [Proof](../evidence/TV5/verification-20261013/summary.txt) có chín ca tiền, ba overflow 8115 và payroll lưu/rollback tiền lớn. Không thay công thức TV4.
- Benchmark TV5 ngày 13/10: 10.000 dòng tạm, 833 kết quả đầy đủ khớp, **126 xuống 13 logical reads**, hai actual plans, Messages và cleanup. Số **142 xuống 15** cũ giữ ngày/phạm vi lịch sử. Hint, warm cache và MAXDOP 1 được nêu rõ.
- TV2: dùng [biên nhận](../evidence/TV2/README_TV5_RECEIPT.md) và bảng v3. Fixture 30.000 dòng, 273 chi tiết/9 tổng hợp. **Messages 574 xuống 5** và **XML 522 xuống 5** tách nguồn. XML detail CPU/elapsed 0/0 ở cả hai phía; không dùng thời gian sai trong bảng v2. Chênh 52 reads chưa xác định nguyên nhân.
- TV1: CHECK tuổi schema mới dùng `NgaySinh <= DATEADD(YEAR,-18,NgayVaoLam)`. [Chín ca/18 thao tác](../evidence/TV5/age-boundaries-20261013/TV1_Age_Boundaries.sql.messages.log) kiểm sinh nhật, 29/2 và rollback profile. Không nhận database đang tồn tại đã được migration.
- Kiểm thử: Java offline hiện **257 assertions** (GuiQa 88, Security 104, LightTheme 45, LoginChip 20), tách **87 assertions GUI live**, bảy kiểm lại điện thoại và 21 layout bounds. Handler Swing/dialog tự động không thay tập dượt bằng tay. [GUI bổ sung](../evidence/TV5/gui-followup-20261013/) ghi phép đo, cleanup và giới hạn ảnh render.
- Metadata source, danh mục minh chứng, số trang và trạng thái [deck chung 15 slide](../slides/Nhom06_ThuyetTrinh_15_Slides.pptx) đã cập nhật. Fixture demo 09/10 cho A 730.000đ/B 0đ giữ riêng với số QA bổ sung 1.230.000đ.

## Kiểm bố cục cuối

Bản 14/10 đã xuất lại bằng Microsoft Word, vẫn có **61 trang vật lý**; nội dung nằm trang 5–59, tương đương **55 trang**, đáp ứng ngưỡng 50–100 của kế hoạch. So với PDF 13/10, text chỉ đổi ở trang vật lý 2, 58 và 60; đã xem ba trang này, không thấy tràn lề hay vỡ bảng. PDF QA nằm riêng trong `build/tv2-final-status-render/`, không đóng gói thay Word. Không cộng Word TV4 vào số trang nhóm.

Ở lượt QA 13/10, so với baseline sau sửa tiền hash `414f257829fc46729232512e3aa142fbb32be12979b555e6df8b67daf553d8a2`, chín trang vật lý 2/19/26/54/55/58/59/60/61 đổi; **52 trang còn lại trùng pixel**. Đã xem đủ trang đổi và rà toàn bộ, không có text ngoài lề, bảng tràn hoặc thay pagination tại thời điểm đó. Bản sửa tiền trước đó chỉ đổi trang 33 so với baseline cũ; đó là lượt QA khác.

PDF/ảnh và biên nhận QA nằm riêng trong `build/tv5-final-docs-20261013/`; không nhận PDF cũ là bản nộp mới. Hash bản Word 11/10 trước sửa tiền: `21c1b7025453804a1bfe873627b986b0cdc94310710fdb5ebaf944f2d3c26251`.

## Phần cần xác nhận thêm

TV1/TV3 vẫn cần xác nhận bàn giao nội dung của mình. Biên bản v2 TV2 được giữ nguyên làm bản gốc; [biên nhận TV5](../evidence/TV2/README_TV5_RECEIPT.md) đã hiệu đính tham chiếu theo v3 mà không thay evidence của TV2. Deck tổng hợp do TV5 dựng từ guide/source đã rà, giữ ownership, không nhận đã nghiệm thu lời nói của các thành viên.

Cần tập dượt chung và kiểm cài/chạy trên máy nhận. Kiểm Java scale không thay Windows DPI thật; cài QA sạch cùng máy không thay thử máy khác. Nếu yêu cầu nộp PDF, xuất từ Word cuối trên máy nộp và kiểm lại trang. Theo dõi [README bộ nộp](../README_BO_NOP.md) và checklist TV5.
