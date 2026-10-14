# TV2 — AUDIT NHẤT QUÁN TÀI LIỆU vs SOURCE (BẢN HANDOFF CẬP NHẬT)

- **Phạm vi đối chiếu:** Schema `CHAMCONG`, view tổng hợp, constraints, ownership SQL so với các tài liệu báo cáo chuyên đề, tiến độ và hướng dẫn thuyết trình của TV2.
- **Source căn cứ:** `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`.
- **Chính sách:** File này ghi nhận đầy đủ kết quả rà soát, đề xuất sửa chữa cho TV5/TV2 và KHÔNG trực tiếp chỉnh sửa các file tracked.

---

## 1. Trạng thái Các Finding Nghiệp Vụ & Tài Liệu (A1 – A8)

### Finding A1 — (Đã cập nhật vào Slide/Script) Mã trạng thái công trên UI
- **Thực trạng source:** `ChamCongPanel.java:109` và `DieuChinhChamCongDialog.java:138` nạp trực tiếp 4 mã `CO_MAT, DI_TRE, VE_SOM, VANG`.
- **Xử lý:** Trong nội dung Slide 4 và Speaking Script mới, người trình bày đọc mã kỹ thuật và diễn giải nghĩa tiếng Việt bằng lời, không nói "combo hiển thị tiếng Việt".
- **Khuyến nghị TV5:** Cập nhật hướng dẫn thuyết trình và báo cáo Word thống nhất cách gọi mã nghiệp vụ kèm chú thích nghĩa tiếng Việt.

### Finding A2 — (Đã giải quyết hoàn toàn) Số liệu Benchmark Covering Index
- **Thực trạng trước đó:** Báo cáo ghi nhận 574 ➔ 5 gắn nhãn lịch sử; tồn tại sự chênh lệch chưa giải thích giữa 574 (Messages) và 522 (XML Plan).
- **Kết quả giải quyết (Run ID `run_20261013_140800`):**
  - Đã thực thi 1 lượt đo đồng nhất duy nhất trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81`.
  - Kết quả: Logical Reads giảm từ **574 xuống 5** (Giảm **99.13%** theo Messages); từ **522 xuống 5** (Giảm **99.04%** theo XML `RunTimeCountersPerThread`).
  - Độ chênh 574 vs 522 đã được giải trình rõ: 574 bao gồm trang dẫn đường (b-tree navigation) qua 273 lần Key Lookup ở Storage Engine; 522 là số trang lá dữ liệu được đọc tại toán tử. Khi có covering index, cả hai phương thức đo đều khớp chính xác ở con số 5 trang đọc logic.
- **Khuyến nghị TV5:** Đưa bảng số liệu chính thức này vào Báo cáo cuối kỳ (§3.2) và Slide 6 của nhóm.

### Finding A3 — (Đã cập nhật vào Slide/Script) Phân định quyền sở hữu `fn_TongPhuCap`
- **Thực trạng:** Ma trận phân công ghi nhận `fn_TongPhuCap` do TV2 phụ trách, cài trong module 03 (`03_phucap_khautru_TV3.sql:214–224`). Báo cáo chuyên đề TV2 cũ chưa liệt kê hàm này vào bảng ownership.
- **Xử lý:** Đã bổ sung vào Slide 6 và kịch bản vấn đáp (Câu 8).
- **Khuyến nghị TV5:** Thêm một mục mô tả hàm `fn_TongPhuCap` trong Chương 2 báo cáo tổng thể nhóm, ghi nhận đúng TV2 phụ trách để thỏa mãn tiêu chí rubric "≥5 hàm chia đều cho các thành viên".

### Finding A4 — (Đã chuẩn hóa) DDL Bảng `dbo.CHAMCONG`
- **Thực trạng:** Một số trích đoạn văn bản trong báo cáo tiến độ cũ ghi thiếu `NOT NULL` ở `MaChamCong` và thiếu từ khóa `CLUSTERED` ở `PK_CHAMCONG`.
- **Xử lý:** Đã đối chiếu schema thật từ database live QA:
  - `MaChamCong INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CHAMCONG PRIMARY KEY CLUSTERED`
  - `TrangThai NVARCHAR(20) NOT NULL CONSTRAINT DF_CHAMCONG_TrangThai DEFAULT N'CO_MAT'`
- **Khuyến nghị TV5:** Đồng bộ khối DDL chuẩn xác vào Báo cáo Word cuối kỳ.

### Finding A5 — Hàm `fn_TinhSoNgayCong` (Thuộc sở hữu TV1)
- Hàm do TV1 phụ trách (dù cài ở module 02). Phân hệ Lương đếm trực tiếp bảng CHAMCONG trong giao dịch có khóa HOLDLOCK mà không gọi hàm này. TV2 không nhận hàm này là của mình.

### Finding A6 — Ghi chú comment trong ChamCongDAO
- Comment cũ trong DAO gợi ý DAO tự kiểm kỳ chốt; thực tế việc kiểm tra do trigger module 05 của TV5 đảm nhiệm. Đây là nợ kỹ thuật ở comment mã nguồn, không ảnh hưởng tới hoạt động runtime.

### Finding A7 — Nghiệp vụ nhập lô trên GUI
- Nhập lô bằng giao dịch All-or-Nothing đã được kiểm thử tự động 21/21 PASS trong test suite SQL (TC-CC-08, TC-CC-09). Chưa thực hiện thao tác tay nhập lô trên GUI, người trình bày không nhận demo tay phần này.

### Finding A8 — Bằng chứng hình ảnh lịch sử
- 8 ảnh chụp màn hình trong `screenshots/TV2/` là tư liệu kiểm thử lịch sử từ các giai đoạn trước (kỳ 09/2026). Toàn bộ minh chứng hiện hành được chuyển giao bằng file log, file plan `.sqlplan` và file CSV dữ liệu thô.

---

## 2. Bảng Đối chiếu Khớp 100%

- **Từ điển dữ liệu CHAMCONG:** Tên cột, kiểu dữ liệu, ràng buộc NOT NULL, DEFAULT, CHECK, FOREIGN KEY, UNIQUE khớp 100% giữa code SQL, CSDL live QA và tài liệu thiết kế.
- **View `vw_TongHopChamCongThang`:** Đủ 9 cột đầu ra, khớp giữa định nghĩa SQL view và phương thức `getTongHopTheoThang` trong Java DAO.
- **Stored Procedure `sp_GhiNhanChamCong`:** Đủ 6 tham số IN + 1 tham số OUT, đúng trình tự 7 bước tiền kiểm tra.
- **Trigger:** Đủ 2 trigger module 02 và 1 trigger module 05 bảo vệ kỳ chốt lương.
