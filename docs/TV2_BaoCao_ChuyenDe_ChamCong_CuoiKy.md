# BÁO CÁO KỸ THUẬT VÀ THIẾT KẾ CƠ SỞ DỮ LIỆU CUỐI KỲ
## CHUYÊN ĐỀ: PHÂN HỆ QUẢN LÝ CHẤM CÔNG VÀ TỐI ƯU HÓA HIỆU NĂNG TRUY VẤN

- **Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
- **Giảng viên hướng dẫn:** TS. Phan Thị Thể
- **Đề tài:** Hệ Thống Quản Lý Nhân Sự và Tiền Lương
- **Nhóm thực hiện:** Nhóm 06
- **Thành viên chịu trách nhiệm:** TV2 – Phạm Minh Quân (MSSV: 24110311)
- **Branch làm việc:** `feature/tv2-week3-evidence`

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
1. **Liên kết với TV1 (Nhân sự):** Xác thực trạng thái nhân viên (`TrangThai = 'DANG_LAM_VIEC'`) trước khi ghi nhận công; cung cấp dữ liệu cho hàm `fn_TinhSoNgayCong(MaNV, Thang, Nam)`.
2. **Liên kết với TV4 (Tính lương):** Cung cấp số ngày công thực tế (`SoNgayDiLam`) và số giờ làm việc thực tế cho thủ tục `sp_TinhBangLuongThang`, làm cơ sở tính tiền công theo công thức:
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

### 2.3 Stored Procedure `sp_GhiNhanChamCong`
Thủ tục tiếp nhận 7 tham số đầu vào/ra, thực hiện validate toàn diện:
- Kiểm tra mã nhân viên, ngày công, giờ vào không được NULL.
- Kiểm tra ngày không vượt quá ngày hiện tại.
- Kiểm tra miền giá trị trạng thái (`CO_MAT`, `DI_TRE`, `VE_SOM`, `VANG`).
- Kiểm tra logic giờ ra phải lớn hơn giờ vào.
- Kiểm tra trạng thái nhân viên trong `dbo.NHANVIEN` phải là `DANG_LAM_VIEC`.
- Kiểm tra trước trùng lặp cặp `(MaNV, NgayChamCong)`.
- Ghi nhận an toàn và trả về mã chấm công vừa sinh qua tham số `@MaChamCong OUTPUT`.

### 2.4 Các Trigger kiểm soát toàn vẹn
- **`trg_ChamCong_KiemTraGio` (AFTER INSERT, UPDATE):** Kiểm tra set-based trên bảng giả `inserted`. Nếu phát hiện `GioRa IS NOT NULL AND GioRa <= GioVao`, lập tức tung `RAISERROR` và thực thi `ROLLBACK TRANSACTION`.
- **`trg_ChamCong_KiemTraNhanVien` (AFTER INSERT, UPDATE):** Thực hiện JOIN `inserted` với `dbo.NHANVIEN`. Nếu phát hiện bất kỳ nhân viên nào có `TrangThai <> 'DANG_LAM_VIEC'`, lập tức tung `RAISERROR` và `ROLLBACK TRANSACTION`.

### 2.5 View tổng hợp công tháng `vw_TongHopChamCongThang`
View thực hiện tính toán tự động cho từng nhân viên theo từng tháng/năm:
- Số ngày đi làm (`SoNgayDiLam`): Đếm các ngày có trạng thái `CO_MAT`, `DI_TRE`, hoặc `VE_SOM`.
- Số lần đi trễ (`SoLanDiTre`), số lần về sớm (`SoLanVeSom`), số ngày vắng (`SoNgayVang`).
- Tổng số giờ làm việc thực tế (`TongSoGioLam`): Tính toán dựa trên chênh lệch số phút giữa `GioVao` và `GioRa`, quy đổi ra giờ thập phân.

*(Lưu ý về phạm vi sở hữu: TV2 chỉ sở hữu các đối tượng chấm công nêu trên. Các đối tượng phụ cấp và khấu trừ như `sp_XoaKyLuongChuaChot`, `fn_TongKhauTru`, `vw_TongPhuCapThang`, `IX_PHUCAP_MaNV_ThangNam` thuộc quyền sở hữu của TV3 theo đúng kế hoạch phân công).*

---

# CHƯƠNG 3. THỰC NGHIỆM ĐO LƯỜNG VÀ ĐÁNH GIÁ BENCHMARK COVERING INDEX

Kịch bản thực nghiệm độc lập được lưu trữ tại `database/test_benchmark_index_TV2.sql`.

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

### 3.2 Bảng so sánh chỉ số đo lường thực nghiệm

| Chỉ Số Đánh Giá | Giai Đoạn BEFORE (UQ Seek + Key Lookup) | Giai Đoạn AFTER (Covering Index Seek) | Mức Độ Cải Thiện |
|---|:---:|:---:|:---:|
| **Toán tử vật lý (Operator)** | Index Seek trên `UQ_Bench_MaNV_Ngay` + **Key Lookup (Clustered)** | **Index Seek thuần túy** trên `IX_Bench_CHAMCONG_MaNV_Ngay` | Loại bỏ 100% Key Lookup (zero Key Lookup) |
| **Số trang đọc logic (Logical Reads)** | **574 pages** | **5 pages** | **Giảm 99.13% I/O** |
| **Số lần truy cập Clustered Index** | 1 lần cho mỗi dòng tìm thấy (Lookup) | **0 lần** | Triệt tiêu I/O thứ cấp |
| **Scan Count** | 1 | 1 | Quét theo khoảng khóa xác định |
| **Bảo toàn CSDL sản xuất** | Bảng `dbo.CHAMCONG` nguyên vẹn | Bảng `dbo.CHAMCONG` nguyên vẹn | Cách ly hoàn toàn bằng `#CHAMCONG_BENCHMARK` |

*(Ghi chú kỹ thuật: Báo cáo tuân thủ nguyên tắc không sử dụng thời gian đồng hồ wall-clock time để so sánh vì biến thiên theo tài nguyên nền của máy chủ).*

### 3.3 Danh mục minh chứng hình ảnh Benchmark đã lưu trữ
Năm ảnh chụp màn hình chứng minh kết quả thực nghiệm được lưu trữ nguyên vẹn tại `screenshots/TV2/`:
- `screenshots/TV2/TV2_Benchmark_ExecutionPlan_BEFORE.png`: Kế hoạch thực thi BEFORE xuất hiện toán tử Index Seek trên `UQ_Bench_MaNV_Ngay` kèm theo Key Lookup (Clustered).
- `screenshots/TV2/TV2_Benchmark_ExecutionPlan_AFTER.png`: Kế hoạch thực thi AFTER chuyển sang Index Seek thuần túy trên `IX_Bench_CHAMCONG_MaNV_Ngay`, 0 Key Lookup.
- `screenshots/TV2/TV2_Benchmark_StatisticsIO_BEFORE.png`: Thống kê I/O giai đoạn BEFORE ghi nhận **574 logical reads**.
- `screenshots/TV2/TV2_Benchmark_StatisticsIO_AFTER.png`: Thống kê I/O giai đoạn AFTER ghi nhận giảm xuống chỉ còn **5 logical reads**.
- `screenshots/TV2/TV2_Benchmark_Results_Metadata.png`: Bảng kết quả tổng hợp công và metadata xác thực cấu trúc `INCLUDE (GioVao, GioRa, TrangThai)`.

---

# CHƯƠNG 4. BỘ KỊCH BẢN KIỂM THỬ THIẾT KẾ CHO PHÂN HỆ CHẤM CÔNG

Bộ kịch bản kiểm thử tự động của TV2 được thiết kế tại `database/tests_TV2/test_module_chamcong_TV2.sql` với cơ chế self-seeding token per-run và dọn dẹp cách ly:

**Trạng thái thực thi hiện tại:** Chưa thực thi / chưa có log trong worktree này; chương này chỉ mô tả thiết kế kiểm thử, không tuyên bố kết quả PASS.

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
  - Ràng buộc an toàn: Không cho phép sửa/xóa chấm công của kỳ lương đã chốt (`BANGLUONG.TrangThai = 'DA_CHOT'`).

### 5.2 Minh chứng hình ảnh giao diện thực tế (UI Evidence)
- **Tình trạng:** **ĐÃ THU THẬP & ĐÃ XÁC THỰC (COLLECTED & VERIFIED)**.
- **Phương thức thu thập:** Cả 3 ảnh chụp giao diện được chụp trực tiếp từ một phiên desktop tương tác thật (interactive desktop session): ứng dụng Java Swing được mở trực tiếp trên môi trường desktop đồ họa, kết nối tới CSDL SQL Server thật (`QuanLyNhanSuTienLuong`), và chụp màn hình thực tế; nội dung ảnh đã được rà soát và xác thực trước khi lưu vào repository.
- **Danh mục minh chứng UI đã lưu trữ tại `screenshots/TV2/`:**
  - `screenshots/TV2/TV2_ChamCong_ChiTiet.png`: Tab 1 chấm công chi tiết — form ghi nhận chấm công, nút điểm danh hàng loạt (Transaction) và nhật ký chấm công với dữ liệu thật.
  - `screenshots/TV2/TV2_ChamCong_TongHopThang.png`: Tab 2 tổng hợp ngày công tháng — dữ liệu tổng hợp đọc từ View `vw_TongHopChamCongThang`.
  - `screenshots/TV2/TV2_DieuChinhChamCongDialog.png`: Hộp thoại điều chỉnh ngày công dạng drill-down với bản ghi công thật và các thao tác thêm/cập nhật/xóa.

---

# CHƯƠNG 6. TỒN TẠI KỸ THUẬT VÀ ĐỀ XUẤT NÂNG CẤP (REMAINING GAPS)

1. **Minh chứng giao diện Java Swing (Đã hoàn tất):** Cả 3 ảnh chụp giao diện thật có nội dung trực quan cho TV2 đã được thu thập từ phiên desktop tương tác thật và đã được xác thực, lưu trữ tại `screenshots/TV2/` (chi tiết tại mục 5.2).
2. **Dữ liệu bảng sản xuất `dbo.CHAMCONG`:** Hiện tại đang duy trì bộ dữ liệu baseline mẫu (1 bản ghi) để đảm bảo an toàn cho các kịch bản kiểm thử E2E liên phân hệ. Dữ liệu thực tế toàn bộ các tháng của nhân viên sẽ được nạp trong pha chuyển giao dữ liệu thực tế.
3. **Mở rộng nguồn thu thập dữ liệu chấm công:** Hệ thống hiện hoạt động trên cơ chế nhập liệu từ giao diện và điểm danh hàng loạt (Batch Transaction). Hướng mở rộng tiếp theo là xây dựng Service lắng nghe sự kiện từ máy chấm công vân tay / khuôn mặt (ZKTeco/Hikvision API) để tự động đẩy dữ liệu vào CSDL.
