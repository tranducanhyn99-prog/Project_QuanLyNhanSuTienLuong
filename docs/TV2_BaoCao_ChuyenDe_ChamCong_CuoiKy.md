# BÁO CÁO KỸ THUẬT VÀ THIẾT KẾ CƠ SỞ DỮ LIỆU CUỐI KỲ
## CHUYÊN ĐỀ: PHÂN HỆ QUẢN LÝ CHẤM CÔNG VÀ TỐI ƯU HÓA HIỆU NĂNG TRUY VẤN

- **Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
- **Giảng viên hướng dẫn:** TS. Phan Thị Thể
- **Đề tài:** Hệ Thống Quản Lý Nhân Sự và Tiền Lương
- **Nhóm thực hiện:** Nhóm 06
- **Thành viên chịu trách nhiệm:** TV2 – Phạm Minh Quân (MSSV: 24110311)
- **Branch làm việc:** `fix/tv2-sp-try-catch` (tạo từ live updated `main` @ `e00da71`)
- **Rà soát tài liệu:** 15/10/2026. Đã phê duyệt và áp dụng **Phương án B (Option B)**: bao bọc validation + INSERT + SCOPE_IDENTITY trong `TRY...CATCH` với bare `THROW;` trong `sp_GhiNhanChamCong`. Benchmark canonical v3 ngày 13/10/2026 (Messages 574→5 / 99.1289%, XML 522→5 / 99.0421%, unexplained 52 reads). Bằng chứng lịch sử xem `screenshots/TV2/`; evidence runtime hiện hành xem [FIX_TASKLIST](FIX_TASKLIST.md).

---

# MỤC LỤC

1. [CHƯƠNG 1. TỔNG QUAN PHÂN HỆ VÀ ĐẶC TẢ NGHIỆP VỤ](#chương-1-tổng-quan-phân-hệ-và-đặc-tả-nghiệp-vụ)
2. [CHƯƠNG 2. CÁC ĐỐI TƯỢNG CƠ SỞ DỮ LIỆU THIẾT KẾ & CÀI ĐẶT (OWNERSHIP TV2)](#chương-2-các-đối-tượng-cơ-sở-dữ-liệu-thiết-kế--cài-đặt-ownership-tv2)
3. [CHƯƠNG 3. THỰC NGHIỆM ĐO LƯỜNG VÀ ĐÁNH GIÁ BENCHMARK COVERING INDEX](#chương-3-thực-nghiệm-đo-lường-và-đánh-giá-benchmark-covering-index)
4. [CHƯƠNG 4. BỘ KỊCH BẢN KIỂM THỬ THIẾT KẾ CHO PHÂN HỆ CHẤM CÔNG](#chương-4-bộ-kịch-bản-kiểm-thử-thiết-kế-cho-phân-hệ-chấm-công)
5. [CHƯƠNG 5. KIẾN TRÚC ỨNG DỤNG JAVA VÀ TÌNH TRẠNG MINH CHỨNG GIAO DIỆN](#chương-5-kiến-trúc-ứng-dụng-java-và-tình-trạng-minh-chứng-giao-diện)
6. [CHƯƠNG 6. TỒN TẠI KỸ THUẬT VÀ ĐỀ XUẤT NÂNG CẤP (REMAINING GAPS)](#chương-6-tồn-tại-kỹ-thuật-và-đề-xuất-nâng-cấp-remaining-gaps)

---

# CHƯƠNG 1. TỔNG QUAN PHÂN HỆ VÀ ĐẶC TẢ NGHIỆP VỤ

### 1.1 Vai trò trong hệ thống liên thông
Phân hệ Quản lý Chấm công giữ vai trò thu thập và xác thực dữ liệu thực tế về sự hiện diện và số giờ lao động của nhân viên trong doanh nghiệp:
1. Ghi nhận trạng thái nhân viên và ngày công; `fn_TinhSoNgayCong(MaNV, Thang, Nam)` được cài trong module 02 sau bảng `CHAMCONG`.
2. **Liên kết với TV4 (Tính lương):** Cung cấp số ngày công cho `sp_TinhBangLuongThang`, hiện đếm trực tiếp bằng truy vấn range theo ngày trong transaction (không gọi `fn_TinhSoNgayCong`), làm cơ sở tính tiền công theo công thức:
   $$\text{TienCong} = \frac{\text{LuongCoBan}}{26} \times \text{SoNgayCongThucTe}$$
3. **Liên kết với TV5 (Bảo mật & Báo cáo):** Cung cấp số liệu tổng hợp công phục vụ kết xuất phiếu lương và báo cáo chi phí nhân sự toàn doanh nghiệp.

### 1.2 Các quy tắc nghiệp vụ cốt lõi (Business Rules)
- **BR-CC-01 (Duy nhất theo cặp Nhân viên – Ngày):** Trong cùng một ngày, mỗi nhân viên chỉ tồn tại duy nhất một bản ghi chấm công chính thức (`UQ_CHAMCONG_MaNV_Ngay`).
- **BR-CC-02 (Logic thời gian vào/ra):** Giờ ra về phải lớn hơn giờ vào làm (`GioRa > GioVao`).
- **BR-CC-03 (Trạng thái nhân viên):** Cấm tuyệt đối việc chấm công cho nhân viên đã nghỉ việc (`TrangThai <> 'DANG_LAM_VIEC'`). Được kiểm soát chặt chẽ bởi trigger `trg_ChamCong_KiemTraNhanVien`.
- **BR-CC-04 (Giới hạn thời gian hợp lệ):** Ngày chấm công không được vượt quá ngày hiện tại (`NgayChamCong <= CAST(GETDATE() AS DATE)`). Ràng buộc `CHK_CHAMCONG_Ngay`.
- **BR-CC-05 (Giao dịch nhập theo lô All-or-Nothing):** Khi điểm danh cả công ty, nếu bất kỳ nhân viên nào vi phạm quy tắc nghiệp vụ, toàn bộ giao dịch phải tự động ROLLBACK, không lưu dữ liệu dở dang.

---

# CHƯƠNG 2. CÁC ĐỐI TƯỢNG CƠ SỞ DỮ LIỆU THIẾT KẾ & CÀI ĐẶT (OWNERSHIP TV2)

Toàn bộ mã nguồn cài đặt các đối tượng CSDL của TV2 được lưu trữ tại `database/02_Module_ChamCong_TV2.sql`:

### 2.1 Bảng dữ liệu `CHAMCONG` (Chuẩn hóa 3NF)
```sql
CREATE TABLE dbo.CHAMCONG (
    MaChamCong    INT           IDENTITY(1,1) NOT NULL,
    MaNV          INT           NOT NULL,
    NgayChamCong  DATE          NOT NULL,
    GioVao        TIME(0)       NOT NULL,
    GioRa         TIME(0)       NULL,
    TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_CHAMCONG_TrangThai DEFAULT N'CO_MAT',
    GhiChu        NVARCHAR(255) NULL,

    CONSTRAINT PK_CHAMCONG PRIMARY KEY CLUSTERED (MaChamCong),
    CONSTRAINT FK_CHAMCONG_NHANVIEN FOREIGN KEY (MaNV) REFERENCES dbo.NHANVIEN(MaNV),
    CONSTRAINT UQ_CHAMCONG_MaNV_Ngay UNIQUE (MaNV, NgayChamCong),
    CONSTRAINT CHK_CHAMCONG_TrangThai CHECK (TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM', N'VANG')),
    CONSTRAINT CHK_CHAMCONG_Ngay CHECK (NgayChamCong <= CAST(GETDATE() AS DATE))
);
```

### 2.2 Non-clustered Covering Index `IX_CHAMCONG_MaNV_Ngay`
```sql
CREATE NONCLUSTERED INDEX IX_CHAMCONG_MaNV_Ngay
ON dbo.CHAMCONG (MaNV, NgayChamCong)
INCLUDE (GioVao, GioRa, TrangThai);
```

### 2.3 Stored Procedure `sp_GhiNhanChamCong` (Cập nhật Option B)
Thủ tục tiếp nhận 7 tham số đầu vào/ra, thực hiện validate toàn diện:
- Tiếp nhận: `@MaNV`, `@NgayChamCong`, `@GioVao`, `@GioRa = NULL`, `@TrangThai`, `@GhiChu = NULL`, `@MaChamCong INT OUTPUT`.
- Kiểm tra mã nhân viên, ngày công, giờ vào không được NULL.
- Kiểm tra ngày không vượt quá ngày hiện tại.
- Kiểm tra miền giá trị trạng thái (`CO_MAT`, `DI_TRE`, `VE_SOM`, `VANG`).
- Kiểm tra độ dài ghi chú ≤ 255 ký tự.
- Kiểm tra logic giờ ra phải lớn hơn giờ vào.
- Kiểm tra trạng thái nhân viên trong `dbo.NHANVIEN` phải là `DANG_LAM_VIEC`.
- Kiểm tra trước trùng lặp cặp `(MaNV, NgayChamCong)`.
- Ghi nhận an toàn và gán `@MaChamCong = SCOPE_IDENTITY()`.
- **Cấu trúc TRY...CATCH (Option B):** Toàn bộ validation, lệnh INSERT và gán OUTPUT được bao bọc trong khối `BEGIN TRY ... END TRY`. Khi phát sinh lỗi (do `RAISERROR 16` ở bước tiền kiểm tra hoặc do vi phạm ràng buộc/trigger hệ thống), luồng điều khiển chuyển sang `BEGIN CATCH`, tại đây lệnh bare `THROW;` tái ném ngoại lệ nguyên trạng về phía caller.
- **Bảo toàn Transaction Ownership:** Thủ tục cố ý không chứa `BEGIN TRANSACTION`, `COMMIT` hay `ROLLBACK` nội bộ. Toàn quyền quản trị tính nguyên tử (atomicity) của giao dịch điểm danh theo lô thuộc về tầng Java Service (`ChamCongService.nhapChamCongTheoLo`), bảo đảm luồng rollback sạch sẽ của caller.

### 2.4 Các Trigger kiểm soát toàn vẹn
- **`trg_ChamCong_KiemTraGio` (AFTER INSERT, UPDATE):** Kiểm tra set-based trên bảng giả `inserted`. Nếu phát hiện `GioRa IS NOT NULL AND GioRa <= GioVao`, lập tức tung `RAISERROR` và thực thi `ROLLBACK TRANSACTION`.
- **`trg_ChamCong_KiemTraNhanVien` (AFTER INSERT, UPDATE):** Thực hiện JOIN `inserted` với `dbo.NHANVIEN`. Nếu phát hiện bất kỳ nhân viên nào có `TrangThai <> 'DANG_LAM_VIEC'`, lập tức tung `RAISERROR` và `ROLLBACK TRANSACTION`.
- **`trg_ChamCong_KhongSuaKhiDaChotLuong` (module 05):** AFTER INSERT/UPDATE/DELETE. Khóa kỳ payroll và chặn mutation nếu kỳ của ngày cũ hoặc mới đã `DA_CHOT`, kể cả lệnh phát ra ngoài Java.

### 2.5 View tổng hợp công tháng `vw_TongHopChamCongThang`
View thực hiện tính toán tự động cho từng nhân viên theo từng tháng/năm:
- Số ngày đi làm (`SoNgayDiLam`): Đếm các ngày có trạng thái `CO_MAT`, `DI_TRE`, hoặc `VE_SOM`.
- Số lần đi trễ (`SoLanDiTre`), số lần về sớm (`SoLanVeSom`), số ngày vắng (`SoNgayVang`).
- Tổng số giờ làm việc thực tế (`TongSoGioLam`): Tính toán dựa trên chênh lệch số phút giữa `GioVao` và `GioRa`, quy đổi ra giờ thập phân.

### 2.6 Hàm vô hướng `fn_TongPhuCap` (Phân công TV2 phối hợp TV3)
Theo kế hoạch phân công và ma trận rubric (≥5 Function), TV2 phụ trách hàm `dbo.fn_TongPhuCap(@MaNV INT, @Thang INT, @Nam INT)` được cài đặt tại `database/03_phucap_khautru_TV3.sql`:
- Tính tổng tiền phụ cấp của nhân viên theo tháng và năm từ bảng `PHUCAPNHANVIEN`.
- Trả về `ISNULL(SUM(SoTien), 0)` dưới dạng `DECIMAL(18,2)`.
- Được thủ tục tính lương `sp_TinhBangLuongThang` (TV4) liên kết sử dụng khi tổng hợp lương.

*(Lưu ý về phạm vi sở hữu: Ngoài `fn_TongPhuCap` được giao, các đối tượng phụ cấp/khấu trừ khác như `sp_XoaKyLuongChuaChot`, `fn_TongKhauTru`, `vw_TongPhuCapThang`, `IX_PHUCAP_MaNV_ThangNam` thuộc quyền sở hữu của TV3).*

---

# CHƯƠNG 3. THỰC NGHIỆM ĐO LƯỜNG VÀ ĐÁNH GIÁ BENCHMARK COVERING INDEX

Kịch bản benchmark cũ được lưu tại `database/test_benchmark_index_TV2.sql`. Kết quả 574→5 và ảnh đi kèm là tư liệu lịch sử, không phải số đo của lần xác minh hiện hành. Runtime evidence hiện hành xem [FIX_TASKLIST](FIX_TASKLIST.md).

### 3.1 Thiết kế môi trường và nguyên lý cách ly (Isolation)
1. **Lý do kỹ thuật cốt lõi:**
   - Bảng sản xuất `dbo.CHAMCONG` có sẵn ràng buộc `UQ_CHAMCONG_MaNV_Ngay`, vốn đã tự động tạo một Non-clustered Index tìm kiếm theo khóa.
   - Do đó, so sánh với Table Scan (bỏ toàn bộ index) là phi thực tế trong môi trường sản xuất.
   - Vấn đề nghẽn cổ chai thực tế là: khi truy vấn chi tiết hoặc tổng hợp công tháng, chỉ mục `UQ_CHAMCONG_MaNV_Ngay` chỉ chứa khóa `(MaNV, NgayChamCong)`, buộc SQL Server phải thực hiện toán tử **Key Lookup (Clustered)** về bảng chính cho từng dòng tìm được để lấy các cột `GioVao, GioRa, TrangThai`.
2. **Giải pháp Covering Index:**
   - Chỉ mục `IX_CHAMCONG_MaNV_Ngay` bổ sung mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`, đưa toàn bộ dữ liệu cần thiết lên các trang lá của chỉ mục Non-clustered.
   - Kết quả: Loại bỏ hoàn toàn 100% toán tử Key Lookup.
3. **Bảo toàn CSDL sản xuất:**
   - Thực nghiệm được tiến hành hoàn toàn trên bảng tạm cục bộ phiên làm việc `#CHAMCONG_BENCHMARK` trong `tempdb` với 30.000 bản ghi mô phỏng định chế (100 nhân viên $\times$ 300 ngày).
   - Tuyệt đối không can thiệp, xóa sửa bảng sản xuất `dbo.CHAMCONG`.
   - Kết thúc script, bảng tạm được tự động giải phóng sạch sẽ.

### 3.2 Bảng số đo benchmark canonical v3 (Run ID: run_20261013_140800)

Thực nghiệm chính thức ngày 13/10/2026 trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81` với fixture chuẩn 30.000 bản ghi trên bảng tạm `#CHAMCONG_BENCHMARK`:

| STT | Kịch bản truy vấn | Baseline (`#CHAMCONG_BENCHMARK`) | Đo lường Covering Index | Logical Reads Before (Messages / XML) | Logical Reads After (Messages / XML) | Tỷ lệ giảm Logical Reads (Messages / XML) | Thời gian CPU / Elapsed (Messages `SET STATISTICS TIME`) | Thời gian CPU / Elapsed (XML Plan `QueryTimeStats`) |
|:---:|---|---|---|:---:|:---:|:---:|:---:|:---:|
| 1 | **Truy vấn chi tiết (1.1 ➔ 3.1):** Tra cứu công 9 tháng nhân viên 25 (273 bản ghi) | `UQ_Bench_MaNV_Ngay` (Index Seek + Key Lookup) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Pure Index Seek, 0 Key Lookup) | **574 / 522** | **5 / 5** | **99.1289% / 99.0421%** | **15ms / 31ms ➔ 0ms / 2ms** | **0ms / 0ms ➔ 0ms / 0ms** |
| 2 | **Truy vấn tổng hợp (1.2 ➔ 3.2):** Tổng hợp công & giờ làm theo tháng (9 bản ghi) | `UQ_Bench_MaNV_Ngay` (Seek + Lookup + Stream Aggregate) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Seek + Stream Aggregate, 0 Lookup) | **574 / 522** | **5 / 5** | **99.1289% / 99.0421%** | *Không xuất dòng kết thúc trong Messages* | **1ms / 1ms ➔ 0ms / 0ms** *(không tuyên bố tốc độ <1ms)* |

**Phân tích kỹ thuật chuyên sâu:**
1. Khi chưa có covering index, truy vấn chi tiết 273 ngày công buộc SQL Server phải thực hiện 273 lần Key Lookup về Clustered Index, tiêu tốn **574 logical reads** theo `SET STATISTICS IO` (tầng buffer pool) và **522 logical reads** theo bộ đếm per-operator runtime trong XML Execution Plan. Các bộ đếm quan sát được có sự chênh lệch (574 trang theo Messages `SET STATISTICS IO` so với 522 trang theo bộ đếm per-operator runtime trong XML Execution Plan), và nguyên nhân chi tiết của 52 trang chênh lệch này là UNKNOWN (không đưa ra giả định chưa kiểm chứng).
2. Khi tạo covering index `IX_Bench_CHAMCONG_MaNV_Ngay` với mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`, toàn bộ dữ liệu cần thiết nằm trọn vẹn trên trang lá của index phụ. Kế hoạch loại bỏ hoàn toàn 273 lần Key Lookup. Số lượt đọc logic giảm mạnh còn đúng **5 trang** ở cả hai công cụ đo — đạt hiệu suất cắt giảm **99.1289% logical reads** (theo Messages `SET STATISTICS IO`) và **99.0421%** (theo XML Execution Plan). Đây là số liệu đo lường logical reads / giảm truy xuất bộ đệm buffer pool trên warm cache, không đưa ra tuyên bố về cắt giảm đọc đĩa vật lý.
3. Về thời gian thực thi: Truy vấn chi tiết ghi nhận thời gian chạy theo Messages giảm từ 31ms xuống 2ms (CPU 15ms xuống 0ms); trong khi bộ đếm `QueryTimeStats` của XML Plan ghi nhận 0ms CPU / 0ms Elapsed cho cả trước và sau do độ phân giải lấy mẫu. Truy vấn tổng hợp ghi nhận `QueryTimeStats` giảm từ 1ms/1ms xuống 0ms/0ms (Messages không xuất dòng kết thúc cho câu aggregate). Nhóm minh bạch không tuyên bố kết luận về độ tăng tốc ở cấp độ thời gian dưới 1ms.
4. Cảnh báo *'Null value is eliminated by an aggregate or other SET operation'* phát sinh từ phép toán `COUNT(CASE WHEN TrangThai IN (...) THEN 1 END)` không có mệnh đề `ELSE`, theo đúng chuẩn xử lý giá trị NULL của ANSI SQL.

### 3.3 Ảnh benchmark lịch sử (tư liệu tham khảo)
Năm ảnh trong `screenshots/TV2/` là artefact lịch sử từ giai đoạn thiết kế ban đầu; chúng phản ánh trực quan sự chuyển đổi plan nhưng tách biệt với số liệu đo canonical v3 ngày 13/10/2026:
- `screenshots/TV2/TV2_Benchmark_ExecutionPlan_BEFORE.png`: Kế hoạch thực thi BEFORE xuất hiện toán tử Index Seek trên `UQ_Bench_MaNV_Ngay` kèm theo Key Lookup (Clustered).
- `screenshots/TV2/TV2_Benchmark_ExecutionPlan_AFTER.png`: Kế hoạch thực thi AFTER chuyển sang Index Seek thuần túy trên `IX_Bench_CHAMCONG_MaNV_Ngay`, 0 Key Lookup.
- `screenshots/TV2/TV2_Benchmark_StatisticsIO_BEFORE.png`: Thống kê I/O giai đoạn BEFORE ghi nhận **574 logical reads**.
- `screenshots/TV2/TV2_Benchmark_StatisticsIO_AFTER.png`: Thống kê I/O giai đoạn AFTER ghi nhận giảm xuống chỉ còn **5 logical reads**.
- `screenshots/TV2/TV2_Benchmark_Results_Metadata.png`: Bảng kết quả tổng hợp công và metadata xác thực cấu trúc `INCLUDE (GioVao, GioRa, TrangThai)`.

---

# CHƯƠNG 4. BỘ KỊCH BẢN KIỂM THỬ THIẾT KẾ CHO PHÂN HỆ CHẤM CÔNG

Bộ kịch bản kiểm thử tự động của TV2 được thiết kế tại `database/tests_TV2/test_module_chamcong_TV2.sql` với cơ chế self-seeding token per-run và dọn dẹp cách ly:

Danh sách dưới đây mô tả thiết kế test. Log và kết quả runtime hiện hành xem [FIX_TASKLIST](FIX_TASKLIST.md); không suy luận kết quả từ tài liệu thiết kế này.

1. **TC-CC-01:** Chấm công đơn lẻ hợp lệ qua `sp_GhiNhanChamCong`.
2. **TC-CC-02:** Chặn trùng lặp cặp `(MaNV, NgayChamCong)` qua SP và qua ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay`.
3. **TC-CC-03:** Chặn nhân viên không tồn tại qua SP và qua khóa ngoại `FK_CHAMCONG_NHANVIEN`.
4. **TC-CC-04:** Chặn ngày trong tương lai qua SP và qua Check Constraint `CHK_CHAMCONG_Ngay`.
5. **TC-CC-05:** Chặn giá trị trạng thái không hợp lệ qua SP và qua Check Constraint `CHK_CHAMCONG_TrangThai`.
6. **TC-CC-06:** Chặn nhân viên đã nghỉ việc (`NGHI_VIEC`) qua SP và qua Trigger `trg_ChamCong_KiemTraNhanVien`.
7. **TC-CC-07:** Chặn giờ ra nhỏ hơn hoặc bằng giờ vào làm qua SP và qua Trigger `trg_ChamCong_KiemTraGio`.
8. **TC-CC-08:** Giao dịch nhập lô 5 bản ghi hợp lệ trong một Database Transaction (COMMIT).
9. **TC-CC-09:** Giao dịch nhập lô có dòng vi phạm $\rightarrow$ ROLLBACK toàn bộ, không lưu dữ liệu dở dang.
10. **TC-CC-10:** Kiểm tra tính toán dữ liệu tổng hợp từ View `vw_TongHopChamCongThang`.
11. **TC-CC-11:** Kiểm tra tìm kiếm và tra cứu qua Non-clustered Index `IX_CHAMCONG_MaNV_Ngay`.
12. **TC-CC-12:** Trigger `trg_ChamCong_KiemTraGio` chặn lệnh UPDATE sửa giờ vào/ra vi phạm.
13. **TC-CC-13:** Kiểm tra Stored Procedure chặn tham số bắt buộc NULL.
14. **TC-CC-14:** Dọn dẹp sạch sẽ toàn bộ dữ liệu kiểm thử, bảo toàn CSDL.

---

# CHƯƠNG 5. KIẾN TRÚC ỨNG DỤNG JAVA VÀ TÌNH TRẠNG MINH CHỨNG GIAO DIỆN

### 5.1 Kiến trúc các tầng Java
- **Presentation (`com.ui.chamcong`):**
  - `ChamCongPanel`: Tích hợp 2 tab chức năng (Chi tiết chấm công & Tổng hợp tháng).
  - `DieuChinhChamCongDialog`: Hộp thoại drill-down cho phép hiệu chỉnh giờ, trạng thái hoặc xóa công.
- **Service (`com.service.ChamCongService`):**
  - Quản lý Transaction nhập lô All-or-Nothing thông qua JDBC:
    ```java
    conn.setAutoCommit(false);
    try {
        for (ChamCong cc : danhSach) {
            chamCongDAO.insertInTransaction(conn, cc);
        }
        conn.commit();
    } catch (Exception ex) {
        conn.rollback();
        throw ex;
    }
    ```
- **DAO (`com.dao.ChamCongDAO`):**
  - Gọi thủ tục `sp_GhiNhanChamCong` bằng `CallableStatement`.
  - Đọc View `vw_TongHopChamCongThang` bằng `PreparedStatement`.
  - DAO ghi cả `NgayChamCong` khi sửa. Trigger SQL module 05 kiểm tra kỳ cũ/đích và chặn INSERT/UPDATE/DELETE khi kỳ đã chốt.
  - UI chuyển JDBC task khỏi EDT bằng `DatabaseTask`/`SwingWorker`; cập nhật component trên EDT.

### 5.2 Minh chứng hình ảnh giao diện thực tế (UI Evidence)
- **Tình trạng:** Ảnh dưới đây là bản chụp lịch sử; không dùng làm bằng chứng UI/runtime hiện tại.
- **Danh mục ảnh lưu tại `screenshots/TV2/`:**
  - `screenshots/TV2/TV2_ChamCong_ChiTiet.png`: Tab 1 chấm công chi tiết — form ghi nhận chấm công, nút điểm danh hàng loạt (Transaction) và nhật ký chấm công với dữ liệu thật.
  - `screenshots/TV2/TV2_ChamCong_TongHopThang.png`: Tab 2 tổng hợp ngày công tháng — dữ liệu tổng hợp đọc từ View `vw_TongHopChamCongThang`.
  - `screenshots/TV2/TV2_DieuChinhChamCongDialog.png`: Hộp thoại điều chỉnh ngày công dạng drill-down với bản ghi công thật và các thao tác thêm/cập nhật/xóa.

---

# CHƯƠNG 6. TỒN TẠI KỸ THUẬT VÀ ĐỀ XUẤT NÂNG CẤP (REMAINING GAPS)

1. **Ảnh UI:** Ảnh lưu trữ là tư liệu lịch sử, không được xem là minh chứng runtime hiện tại. Trạng thái kiểm thử UI hiện hành xem [FIX_TASKLIST](FIX_TASKLIST.md).
2. **Dữ liệu `dbo.CHAMCONG`:** Không suy luận số lượng/baseline từ tài liệu cũ. QA fixtures được tạo riêng và dọn bởi runner; database dự án gốc không bị mutate trong xác minh nêu tại [FIX_TASKLIST](FIX_TASKLIST.md).
3. **Mở rộng nguồn thu thập dữ liệu chấm công:** Hệ thống hiện hoạt động trên cơ chế nhập liệu từ giao diện và điểm danh hàng loạt (Batch Transaction). Hướng mở rộng tiếp theo là xây dựng Service lắng nghe sự kiện từ máy chấm công vân tay / khuôn mặt (ZKTeco/Hikvision API) để tự động đẩy dữ liệu vào CSDL.
4. **Tình trạng kiểm thử và xác minh hiện hành (15/10/2026):**
   - Trình biên dịch: quan sát được `javac 21.0.8` (với cờ `--release 11`), biên dịch thành công 55 mã nguồn Java (bao gồm `src/test/java/com/test/Tv2JdbcIntegrationTest.java`; không phải môi trường native JDK 11).
   - Kiểm thử offline Java: Lệnh thực thi `.\run_verification.ps1 -TestClass SecurityRegressionTest` ghi nhận **104 assertions PASS** tại log [build/test-results/Java_Verification_20261015_174231_883.log](../build/test-results/Java_Verification_20261015_174231_883.log); lệnh `.\run_verification.ps1 -TestClass GuiQaRegressionTest` ghi nhận **88 assertions PASS** tại log [build/test-results/Java_Verification_20261015_174248_559.log](../build/test-results/Java_Verification_20261015_174248_559.log).
   - Kiểm thử runtime live SQL: Thực thi trên Database QA dùng một lần có ownership marker `PRJ_Fix_QA_20261015_175527_ea6da333`, cài đặt đầy đủ schema 5 modules (gồm TV2 Option B hash `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`), kiểm tra định nghĩa thủ tục có TRY/CATCH + bare THROW và OUTPUT. Bộ test suite `database/tests_TV2/test_module_chamcong_TV2.sql` đạt **21 / 21 PASS (100%)** (log [docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/test_module_chamcong_TV2.log](evidence/TV2/optionb-20261015/20261015_175527_ea6da333/test_module_chamcong_TV2.log)), test tương tranh `database/tests/Closed_Source_Concurrency.ps1` **PASS** (log [docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Closed_Source_Concurrency.log](evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Closed_Source_Concurrency.log)).
   - Kiểm thử live JDBC Transaction Integration Test (`src/test/java/com/test/Tv2JdbcIntegrationTest.java` qua `database/tests_TV2/run_tv2_jdbc_optionb.ps1`): Các minh chứng trước đây (34, 59 assertions và đợt trung gian 174112) đã được lưu lịch sử superseded. Thực hiện kiểm thử đầu cuối qua `ChamCongService.nhapChamCongTheoLo` -> `ChamCongDAO.insertInTransaction` -> `dbo.sp_GhiNhanChamCong` -> `SQLException` -> `rollback`. Đạt **87 / 87 assertions PASS (100%)** qua 8 negative guards (từ chối null/prod/protected DB/missing credentials trước khi mutate), 6 kịch bản bắt buộc + chẩn đoán transaction cùng connection, chứng minh 2 INSERTs có ID > 0 trước rollback, bắt đúng causal `SQLServerException` với `SQLServerError` (Number=50000, Severity=16, State=1) tiếng Việt chuẩn, kiểm tra @@TRANCOUNT/XACT_STATE. Log: [docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Tv2JdbcIntegrationTest.log](evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Tv2JdbcIntegrationTest.log). QA database có marker đã được DROP sạch sẽ sau kiểm thử.
   - Đồng bộ package và manifest: Đã đồng bộ `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql` (10,064 bytes, SHA-256 `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`) và cập nhật dòng 3 của `Bo_nop_Nhom06/SHA256_MANIFEST.csv`, xác thực thành công 537/537 file trong manifest. Word nhóm (đoạn 193, 824) và Slide 5 notes đang chờ TV5 cập nhật theo đề xuất của TV2.
   - *Hiện trạng bàn giao:* Đã hoàn tất kiểm thử nội bộ đạt 100% PASS, hiện ở trạng thái **Awaiting Independent Review** (chờ reviewer độc lập nghiệm thu trước khi Git delivery; chưa tuyên bố PR được merge hay giảng viên đã nghiệm thu).
