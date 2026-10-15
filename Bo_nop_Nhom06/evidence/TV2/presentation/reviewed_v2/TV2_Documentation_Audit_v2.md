# TV2 — AUDIT NHẤT QUÁN TÀI LIỆU vs SOURCE (BẢN REVIEWED V2 ĐÃ KIỂM ĐỊNH)

- **Phạm vi đối chiếu:** Schema `CHAMCONG`, view tổng hợp, constraints, ownership SQL so với các tài liệu báo cáo chuyên đề, tiến độ và hướng dẫn thuyết trình của TV2.
- **Source căn cứ:** `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`.
- **Chính sách:** File này ghi nhận đầy đủ kết quả rà soát, đề xuất sửa chữa cho TV5/TV2 và KHÔNG trực tiếp chỉnh sửa các file tracked.

---

## 1. Trạng thái Các Finding Nghiệp Vụ & Tài Liệu (A1 – A8)

### Finding A1 — Mã trạng thái công trên UI
- **Thực trạng source:** `ChamCongPanel.java:109` và `DieuChinhChamCongDialog.java:138` nạp trực tiếp 4 mã kỹ thuật: `CO_MAT, DI_TRE, VE_SOM, VANG`.
- **Xử lý:** Trong Slide 4 và Speaking Script mới (bản v2), người trình bày đọc mã kỹ thuật và giải thích nghĩa tiếng Việt bằng lời (`Có mặt / Đi trễ / Về sớm / Vắng`), không tuyên bố giao diện hiển thị tiếng Việt.
- **Khuyến nghị TV5:** Cập nhật hướng dẫn thuyết trình và báo cáo Word thống nhất cách gọi mã nghiệp vụ kèm chú thích nghĩa tiếng Việt.

### Finding A2 — Số liệu Benchmark Covering Index & Giải trình Kỹ thuật
- **Thực trạng trước đó:** Báo cáo cũ ghi nhận 574 ➔ 5 gắn nhãn lịch sử; tồn tại sự chênh lệch chưa giải thích giữa 574 (Messages) và 522 (XML Plan).
- **Kết quả giải quyết (Run ID `run_20261013_140800`):**
  - Đã thực thi 1 lượt đo đồng nhất duy nhất trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81` trên 30.000 dòng bảng tạm `#CHAMCONG_BENCHMARK`.
  - Kết quả: Logical Reads giảm từ **574 xuống 5** (Giảm **99.13%** theo Messages `SET STATISTICS IO`); từ **522 xuống 5** (Giảm **99.04%** theo XML `RunTimeCountersPerThread`).
  - **Giải trình kỹ thuật khách quan:** Con số 574 đo từ `SET STATISTICS IO` trong Messages phản ánh tổng số trang đọc ở tầng Storage Engine buffer pool; con số 522 là số trang ghi nhận từ per-operator counters (`RunTimeCountersPerThread`) trong XML Execution Plan. Nguyên nhân cụ thể của 52 trang chênh lệch chưa có căn cứ kỹ thuật nội bộ của SQL Server để khẳng định (không đưa ra giả định chưa chứng minh về số trang dẫn đường b-tree). Điểm cốt lõi: Khi có covering index, **cả hai phương thức đo đều ghi nhận chính xác 5 trang đọc logic và 0 Key Lookup**.
  - **Thời gian thực thi:** Truy vấn chi tiết giảm từ 31ms xuống 2ms (Messages); truy vấn tổng hợp không có dòng thời gian kết thúc trong Messages, `QueryTimeStats` trong XML Plan ghi nhận Before 1ms/1ms (CPU/Elapsed), After 0ms/0ms, minh bạch không đưa ra tuyên bố về độ tăng tốc ở cấp độ thời gian dưới 1ms.
- **Khuyến nghị TV5:** Đưa bảng số liệu chính thức này vào Báo cáo cuối kỳ (§3.2) và Slide 6 của nhóm.

### Finding A3 — Phân định quyền sở hữu `fn_TongPhuCap`
- **Thực trạng:** Ma trận phân công ghi nhận `fn_TongPhuCap` do TV2 phụ trách, cài trong module 03 (`03_phucap_khautru_TV3.sql:214–224`). Báo cáo chuyên đề TV2 cũ chưa liệt kê hàm này vào bảng ownership.
- **Xử lý:** Đã bổ sung vào Slide 6 và kịch bản vấn đáp (Câu 8).
- **Khuyến nghị TV5:** Thêm một mục mô tả hàm `fn_TongPhuCap` trong Chương 2 báo cáo tổng thể nhóm, ghi nhận đúng TV2 phụ trách để thỏa mãn tiêu chí rubric "≥5 hàm chia đều cho các thành viên".

### Finding A4 — DDL Bảng `dbo.CHAMCONG` và Minh chứng Schema Hiện hành
- **Thực trạng:** Một số trích đoạn văn bản trong báo cáo tiến độ cũ ghi thiếu `NOT NULL` ở `MaChamCong` và thiếu từ khóa `CLUSTERED` ở `PK_CHAMCONG`.
- **Xử lý:** Đã xuất file đối chiếu schema thực tế từ database live QA tại `schema-inspection/TV2_live_schema_inspection.log`:
  - `MaChamCong INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CHAMCONG PRIMARY KEY CLUSTERED`
  - `TrangThai NVARCHAR(20) NOT NULL CONSTRAINT DF_CHAMCONG_TrangThai DEFAULT N'CO_MAT'`
  - Đầy đủ các ràng buộc `FK_CHAMCONG_NHANVIEN`, `CHK_CHAMCONG_Ngay`, `CHK_CHAMCONG_TrangThai`, `UQ_CHAMCONG_MaNV_Ngay`, 2 Trigger module 02 và 1 Trigger module 05.
- **Khuyến nghị TV5:** Đồng bộ khối DDL chuẩn xác vào Báo cáo Word cuối kỳ.

### Finding A5 — Hàm `fn_TinhSoNgayCong` (Thuộc sở hữu TV1)
- Hàm do TV1 phụ trách (dù cài ở module 02). Phân hệ Lương đếm trực tiếp bảng CHAMCONG trong giao dịch có khóa HOLDLOCK mà không gọi hàm này. TV2 không nhận hàm này là của mình.

### Finding A6 — Ghi chú comment trong ChamCongDAO
- Comment cũ trong DAO gợi ý DAO tự kiểm kỳ chốt; thực tế việc kiểm tra do trigger module 05 của TV5 đảm nhiệm. Đây là nợ kỹ thuật ở comment mã nguồn, không ảnh hưởng tới hoạt động runtime.

### Finding A7 — Nghiệp vụ nhập lô trên GUI
- Nhập lô bằng giao dịch All-or-Nothing đã được kiểm thử tự động 21/21 PASS trong test suite SQL (`TC-CC-08`, `TC-CC-09`). Chưa thực hiện thao tác tay nhập lô trên GUI, người trình bày không nhận demo tay phần này.

### Finding A8 — Bằng chứng hình ảnh lịch sử
- 8 ảnh chụp màn hình trong `screenshots/TV2/` là tư liệu kiểm thử lịch sử từ các giai đoạn trước (kỳ 09/2026). Toàn bộ minh chứng hiện hành được chuyển giao bằng file log, file plan `.sqlplan`, file CSV dữ liệu thô và log schema live.

---

## 2. Bảng Đối chiếu Khớp 100%

- **Từ điển dữ liệu CHAMCONG:** Tên cột, kiểu dữ liệu, ràng buộc NOT NULL, DEFAULT, CHECK, FOREIGN KEY, UNIQUE khớp 100% giữa code SQL, CSDL live QA và tài liệu thiết kế (chứng minh qua `schema-inspection/TV2_live_schema_inspection.log`).
- **View `vw_TongHopChamCongThang`:** Đủ 9 cột đầu ra, khớp giữa định nghĩa SQL view và phương thức `getTongHopTheoThang` trong Java DAO. Cảnh báo NULL elimination đến từ `COUNT(CASE WHEN ... THEN 1 END)` không có `ELSE` theo chuẩn ANSI SQL.
- **Stored Procedure `sp_GhiNhanChamCong`:** Đủ 6 tham số IN + 1 tham số OUT, đúng trình tự 7 bước tiền kiểm tra.
- **Trigger:** Đủ 2 trigger module 02 (`trg_ChamCong_KiemTraGio` - `TC-CC-07`, `trg_ChamCong_KiemTraNhanVien` - `TC-CC-06`) và 1 trigger module 05 bảo vệ kỳ chốt lương.
- **Ranh giới an toàn:** Kiểm tra số lượng bảng và 0 bản ghi trong `dbo.CHAMCONG` bảo đảm tính toàn vẹn của bảng mục tiêu trước và sau benchmark.
