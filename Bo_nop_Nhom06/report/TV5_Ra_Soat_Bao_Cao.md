# TV5 — Rà soát và bàn giao báo cáo Word

**Trần Đức Anh — 24110155 · Cập nhật 16/10/2026**

[Word trong bộ nộp](Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) đã chuẩn hóa trình bày theo cẩm nang ngày 15/10 và cập nhật hai đoạn về Option B TV2: TRY…CATCH + bare THROW, transaction batch do Java Service quản lý, SQL 21/21, JDBC 105, controlled suite 179/179 và concurrency 4/4. Bản package và bản trong `docs/` hiện đồng nhất byte (SHA-256 giống nhau). Render bằng Microsoft Word vẫn có 69 trang tổng, 56 trang nội dung; chỉ trang vật lý 11 và 33 đổi so với bản đã rà trước. Lượt tích hợp 15/10 cập nhật nguồn và phạm vi TV3 theo PR #43/#48. Không thêm năm trang Word TV3 vào độ dài báo cáo nhóm.

SHA-256 của bản Word trong bộ nộp:

```text
c107a3d4c634da5a6ca42574d56a3b7d1b8ed6dc5cc38768c9f050127b3ad877
```

SHA-256 của bản Word trong `docs/`:

```text
c107a3d4c634da5a6ca42574d56a3b7d1b8ed6dc5cc38768c9f050127b3ad877
```

**Đối chiếu hai bản:** package Word và bản trong `docs/` có cùng nội dung, bytes và SHA-256. Cả hai đã được xuất QA bằng Microsoft Word; tổng số trang là 69, trong đó 56 trang nội dung.

## Chuẩn hóa theo cẩm nang ngày 15 tháng 10

Áp dụng `QUY_CHUAN_BAO_CAO_DO_AN_MON_HOC_VIET_NAM.md` (bản 1.0 ngày 15/10/2026) như hướng dẫn tham khảo. Không coi đây là mẫu chính thức của trường hoặc quy chuẩn bắt buộc toàn quốc. Kế hoạch học phần vẫn được ưu tiên: 50–100 trang nội dung, không cộng bìa, mục lục và phụ lục. Chưa có mẫu trình bày riêng của giảng viên trong lượt chỉnh này.

- A4 dọc; lề trên/dưới 2,5 cm, trái 3,5 cm, phải 2 cm. Chính văn Times New Roman 13 pt, giãn dòng 1,5; trước đoạn 0 pt, sau đoạn 6 pt. Bảng/chú thích 11,5 pt; mã Consolas 10 pt.
- Giữ chín chương; sửa số mục dạng `1.1.`, bổ sung tóm tắt 220 từ và thuật ngữ đã dùng. Chương bắt đầu trang mới; bỏ các đoạn trống dùng để ép trang.
- Có 53 chú thích bảng (52 trong chính văn và Bảng A.1), bốn chú thích hình, nguồn và dẫn chiếu trong bài; lặp hàng tiêu đề khi bảng tiếp trang. Mục lục, danh mục bảng/hình dùng trường Word và đã cập nhật.
- Bìa không hiện số; phần đầu dùng i–viii; chính văn bắt đầu từ 1, tài liệu tham khảo và phụ lục tiếp tục số Ả Rập. Bản Word trong bộ nộp có **69 trang vật lý**; chính văn ở **trang vật lý 10–65 = 56 trang**. Nguồn tham khảo bắt đầu trang vật lý 66.
- Tài liệu tham khảo chuyển từ bảng đường dẫn sang 15 mục có chủ thể, nguồn nội bộ, phiên bản/ngày và giới hạn. Phân biệt 224 assertions ngày 09/10 với 257 assertions ngày 15/10; cập nhật câu về ngắt relay ngày 13/10. Không tạo số đo hoặc kết quả kiểm thử mới.
- Giữ nguyên số liệu và nội dung bảng nghiệp vụ, bốn ảnh nhúng, mã Java/SQL và minh chứng bàn giao; chỉ sửa nhãn cột `Số assertion`, bổ sung bảng viết tắt và chuyển danh mục nguồn sang đoạn văn. Số liệu TV2 Messages/XML và giới hạn TV3 được bảo toàn.

Bản cuối đã xuất bằng Microsoft Word và render đủ 69 trang PNG ở QA. So với PDF QA trước khi cập nhật Option B, chỉ trang vật lý 11 và 33 đổi; 67 trang còn lại trùng pixel. Đã xem trực quan hai trang đổi; không thấy tràn lề, hàng bảng bị cắt hoặc vỡ bố cục.

`render_docx.py` không chạy được vì máy thiếu LibreOffice trên PATH; bản QA được xuất bằng Microsoft Word cài trên máy và chuyển PDF thành PNG bằng Poppler. PDF/PNG chỉ dùng kiểm bố cục, lưu ở `build/report-vn-guidelines/`, không được đóng gói thay PDF nộp cuối. Không chạy lại SQL/Java/live GUI trong lượt chỉnh báo cáo.

## Nội dung đã cập nhật

- TV5: `fn_TinhThucNhan` và fallback DDL tiền thống nhất `DECIMAL(18,2)`. [Proof](../evidence/TV5/verification-20261013/summary.txt) có chín ca tiền, ba overflow 8115 và payroll lưu/rollback tiền lớn. Không thay công thức TV4.
- Benchmark TV5 ngày 13/10: 10.000 dòng tạm, 833 kết quả đầy đủ khớp, **126 xuống 13 logical reads**, hai actual plans, Messages và cleanup. Số **142 xuống 15** cũ giữ ngày/phạm vi lịch sử. Hint, warm cache và MAXDOP 1 được nêu rõ.
- TV2: dùng [biên nhận](../evidence/TV2/README_TV5_RECEIPT.md) và bảng v3. Fixture 30.000 dòng, 273 chi tiết/9 tổng hợp. **Messages 574 xuống 5** và **XML 522 xuống 5** tách nguồn. XML detail CPU/elapsed 0/0 ở cả hai phía; không dùng thời gian sai trong bảng v2. Chênh 52 reads chưa xác định nguyên nhân.
- TV1: CHECK tuổi schema mới dùng `NgaySinh <= DATEADD(YEAR,-18,NgayVaoLam)`. [Chín ca/18 thao tác](../evidence/TV5/age-boundaries-20261013/TV1_Age_Boundaries.sql.messages.log) kiểm sinh nhật, 29/2 và rollback profile. Không nhận database đang tồn tại đã được migration.
- Kiểm thử TV5 trước đó: Java offline **257 assertions** (GuiQa 88, Security 104, LightTheme 45, LoginChip 20), tách **87 assertions GUI live**, bảy kiểm lại điện thoại và 21 layout bounds. TV2 Option B sau PR #52: 55 nguồn compile, controlled suite **179/179**, SQL **21/21**, concurrency **4/4**, JDBC **105 assertions**; raw logs nằm trong [evidence Option B](../evidence/TV2/optionb-20261016/). Handler Swing/dialog tự động không thay tập dượt bằng tay. [GUI bổ sung](../evidence/TV5/gui-followup-20261013/) ghi phép đo, cleanup và giới hạn ảnh render.
- Metadata source, danh mục minh chứng, số trang và trạng thái [deck chung 15 slide](../slides/Nhom06_ThuyetTrinh_15_Slides.pptx) đã cập nhật. Fixture demo 09/10 cho A 730.000đ/B 0đ giữ riêng với số QA bổ sung 1.230.000đ.

## Kiểm bố cục ngày 15 tháng 10 trước chuẩn hóa

Đã xuất trước/sau bằng Microsoft Word, đều 61 trang vật lý và 55 trang nội dung (trang 5–59). Chỉ sáu trang 2, 53, 55, 58, 60, 61 đổi; đã xem từng trang, bảng/text không tràn hoặc mất nội dung. 55 trang còn lại trùng pixel. [QA package](../evidence/TV5/package-integration-20261015/README.md) có phạm vi/log/hash; PDF QA ở `build/tv5-integrate-tv3/`, chưa dùng làm PDF nộp cuối.

## Kiểm bố cục ngày 14 tháng 10

Bản 14/10 đã xuất lại bằng Microsoft Word, vẫn có **61 trang vật lý**; nội dung nằm trang 5–59, tương đương **55 trang**, đáp ứng ngưỡng 50–100 của kế hoạch. So với PDF 13/10, text chỉ đổi ở trang vật lý 2, 58 và 60; đã xem ba trang này, không thấy tràn lề hay vỡ bảng. PDF QA nằm riêng trong `build/tv2-final-status-render/`, không đóng gói thay Word. Không cộng Word TV4 vào số trang nhóm.

Ở lượt QA 13/10, so với baseline sau sửa tiền hash `414f257829fc46729232512e3aa142fbb32be12979b555e6df8b67daf553d8a2`, chín trang vật lý 2/19/26/54/55/58/59/60/61 đổi; **52 trang còn lại trùng pixel**. Đã xem đủ trang đổi và rà toàn bộ, không có text ngoài lề, bảng tràn hoặc thay pagination tại thời điểm đó. Bản sửa tiền trước đó chỉ đổi trang 33 so với baseline cũ; đó là lượt QA khác.

PDF/ảnh và biên nhận QA nằm riêng trong `build/tv5-final-docs-20261013/`; không nhận PDF cũ là bản nộp mới. Hash bản Word 11/10 trước sửa tiền: `21c1b7025453804a1bfe873627b986b0cdc94310710fdb5ebaf944f2d3c26251`.

## Phần cần xác nhận thêm

TV1 handoff PR #51 đã được nhận: ERD 10 bảng, log test 7/7, benchmark 75→10 và deck thành viên; nội dung slide 1–3 nhóm đã được đối chiếu, nhưng vẫn giữ dạng tóm tắt trong giới hạn 15 slide. Biên bản v2 TV2 được giữ nguyên; biên nhận TV5 ghi v3 và evidence Option B cuối. Mọi thành viên vẫn cần tập dượt lời nói/demo, không suy ra đã rehearsal từ bộ bàn giao.

Cần tập dượt chung và kiểm cài/chạy trên máy nhận. Kiểm Java scale không thay Windows DPI thật; cài QA sạch cùng máy không thay thử máy khác. Nếu yêu cầu nộp PDF, xuất từ Word cuối trên máy nộp và kiểm lại trang. Theo dõi [README bộ nộp](../README_BO_NOP.md) và checklist TV5.

## Cập nhật sau PR #52 — 16/10/2026

Hai đoạn Word được cập nhật theo kết quả TV2 cuối và đã áp dụng đồng nhất cho bản `docs/`. PDF QA 69 trang dùng để so bố cục; PDF này không phải file nộp cuối. Các trang 11 và 33 đã được xem lại trực quan, 67 trang khác trùng pixel với bản QA trước. Slide 5 notes cũng được đồng bộ, không đổi phần hình ảnh deck.
