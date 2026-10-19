# TV2 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE CHẤM CÔNG
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Phạm Minh Quân (TV2)  
- **MSSV:** 24110311  
- **Mã phân công:** TV2  
- **Module phụ trách:** Quản lý Chấm Công, Tổng Hợp Ngày Công Tháng, Giao Diện Chấm Công & Điều Chỉnh Công  
- **Branch làm việc:** `fix/tv2-sp-try-catch` (tạo từ live updated `main` @ `e00da71`)
- **Rà soát tài liệu:** 15/10/2026. Đã phê duyệt và thực hiện **Phương án B (Option B)**: bổ sung `BEGIN TRY ... END TRY` và `BEGIN CATCH THROW; END CATCH` trong `dbo.sp_GhiNhanChamCong` nhằm triệt tiêu rủi ro rubric ("Stored Procedure >= 5 + TRY...CATCH"), bảo toàn 7 tham số, các bước validate, output `SCOPE_IDENTITY()`, và giữ nguyên quyền sở hữu transaction của tầng Java Service. Benchmark canonical v3 ngày 13/10/2026 (Messages 574→5 reads / 99.1289%, XML 522→5 reads / 99.0421%, unexplained 52 reads). Bằng chứng lịch sử xem `screenshots/TV2/`; chứng cứ runtime và QA xem [FIX_TASKLIST](FIX_TASKLIST.md).

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích nghiệp vụ chấm công theo cặp nhân viên – ngày.<br>• Chốt cấu trúc bảng `CHAMCONG` và các quy tắc: không trùng ngày, giờ ra > giờ vào, nhân viên nghỉ việc không được chấm công mới.<br>• Đặc tả luồng xử lý `ChamCongPanel` → `Service` → `DAO` → SQL Server.<br>• Thiết kế bộ testcase cho dữ liệu chấm công hợp lệ/không hợp lệ. | • Đặc tả nghiệp vụ chấm công.<br>• Thiết kế bảng `CHAMCONG` (1NF - 3NF).<br>• Danh sách constraint/trigger.<br>• Bộ testcase chấm công. | **100%** | **24/09/2026** | Đã hoàn thành toàn bộ tài liệu phân tích nghiệp vụ, thiết kế tích hợp và bộ testcase kịch bản. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints cho bảng `CHAMCONG`.<br>• Cài đặt Stored Procedure `sp_GhiNhanChamCong`.<br>• Cài đặt 2 Trigger: `trg_ChamCong_KiemTraGio` và `trg_ChamCong_KiemTraNhanVien`.<br>• Cài đặt View `vw_TongHopChamCongThang` và Index `IX_CHAMCONG_MaNV_Ngay`.<br>• Lập trình Java Swing: `ChamCongPanel`, `DieuChinhChamCongDialog`.<br>• Lập trình Model, DAO, Service: `ChamCong`, `ChamCongDAO`, `ChamCongService` (JDBC Transaction All-or-Nothing). | • Script SQL: `database/02_Module_ChamCong_TV2.sql`.<br>• Giao diện chấm công 2 tab (chi tiết & tổng hợp) + dialog điều chỉnh.<br>• Transaction nhập theo lô an toàn.<br>• Mã nguồn Java hoàn thiện. | **100%** | **29/09/2026** | Đã hoàn thành cài đặt CSDL và mã nguồn tầng ứng dụng trong branch làm việc. |
| **T3** | 05/10 – 15/10 | • Hoàn thiện `sp_GhiNhanChamCong` với `TRY...CATCH + THROW` (Option B), đáp ứng rubric SP có TRY...CATCH trong khi bảo toàn transaction của Java Service.<br>• Cung cấp dữ liệu chấm công cho payroll; phối hợp TV3 sở hữu `fn_TongPhuCap` (module 03); `sp_TinhBangLuongThang` đếm trực tiếp khoảng ngày.<br>• Rà soát trigger khóa kỳ, đối chiếu benchmark canonical v3 (13/10/2026), kiểm thử toàn diện live SQL/JDBC trên QA DB dùng một lần và cập nhật 4 tài liệu TV2. | • `database/02_Module_ChamCong_TV2.sql` (Option B) & đồng bộ `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql` + manifest (537 files verified).<br>• `javac 21.0.8` (`--release 11`) compile 55 files Java.<br>• Offline regression PASS (Security 104 assertions log `Java_Verification_20261015_174231_883.log`, GuiQa 88 assertions log `...174248_559.log`).<br>• Live SQL QA PASS 21/21 (log `test_module_chamcong_TV2.log`), Concurrency PASS, Live JDBC Integration PASS 87/87 assertions (log `Tv2JdbcIntegrationTest.log`) trên database QA run-owned `PRJ_Fix_QA_20261015_175527_ea6da333`. | **100% (SQL, JDBC, Code, Package Sync & Docs HOÀN TẤT - Chờ Review)** | **15/10/2026** | Hoàn tất Option B, kiểm thử live SQL & JDBC đạt 100% PASS trên QA DB riêng; các lần thử nghiệm trước (34, 59 assertions và đợt trung gian 174112) đã được lưu lịch sử superseded và thay bằng bộ 87 assertions kiểm chứng typed SQLServerError và negative guards với git hygiene sạch sẽ; hiện đang ở bước Awaiting Independent Review trước khi Git delivery. Benchmark canonical v3 ngày 13/10/2026: 30.000 dòng tạm, 273 chi tiết / 9 tổng hợp; Messages 574→5 (99.1289%), XML 522→5 (99.0421%), chênh lệch 52 reads chưa giải thích. Ảnh cũ trong `screenshots/TV2/` là tư liệu lịch sử. |

---

## PHẦN 2. ĐẶC TẢ NGHIỆP VỤ & THIẾT KẾ CƠ SỞ DỮ LIỆU

### 1. Vai trò và Quy tắc nghiệp vụ cốt lõi

Module chấm công chịu trách nhiệm ghi nhận, theo dõi và tổng hợp thời gian làm việc thực tế của nhân viên:
1. `fn_TinhSoNgayCong(MaNV, Thang, Nam)` được cài trong module 02 sau khi tạo `CHAMCONG`; payroll hiện đếm trực tiếp dữ liệu theo khoảng ngày, không gọi function này.
2. Cung cấp số ngày công thực tế cho `sp_TinhBangLuongThang`, dùng truy vấn range theo ngày trong transaction payroll.
3. **Cung cấp dữ liệu nguồn cho TV5 (Trần Đức Anh):** Tổng hợp công, giờ làm việc hiển thị trên báo cáo và xuất phiếu lương.

#### Các quy tắc nghiệp vụ (Business Rules):
- **BR-CC-01 (Duy nhất theo cặp Nhân viên – Ngày):** Mỗi nhân viên trong một ngày chỉ có tối đa một bản ghi chấm công chính thức (`UQ_CHAMCONG_MaNV_Ngay`).
- **BR-CC-02 (Logic thời gian):** Giờ ra về phải lớn hơn giờ vào làm (`GioRa > GioVao`).
- **BR-CC-03 (Trạng thái nhân viên):** Chỉ nhân viên đang làm việc (`TrangThai = 'DANG_LAM_VIEC'`) mới được ghi nhận chấm công. Cấm chấm công cho nhân viên đã nghỉ việc (`TrangThai = 'NGHI_VIEC'`).
- **BR-CC-04 (Thời điểm hợp lệ):** Ngày chấm công không được vượt quá ngày hiện tại (`NgayChamCong <= CAST(GETDATE() AS DATE)`).
- **BR-CC-05 (Giao dịch nhập theo lô All-or-Nothing):** Khi nhập danh sách chấm công hàng loạt, nếu bất kỳ dòng nào vi phạm, toàn bộ lô phải bị ROLLBACK, không lưu dữ liệu dở dang.

### 2. Cấu trúc Bảng `CHAMCONG` (Chuẩn hóa 3NF)

```sql
CREATE TABLE dbo.CHAMCONG (
    MaChamCong   INT           IDENTITY(1,1),
    MaNV         INT           NOT NULL,
    NgayChamCong DATE          NOT NULL,
    GioVao       TIME(0)       NOT NULL,
    GioRa        TIME(0)       NULL,
    TrangThai    NVARCHAR(20)  NOT NULL CONSTRAINT DF_CHAMCONG_TrangThai DEFAULT N'CO_MAT',
    GhiChu       NVARCHAR(255) NULL,

    CONSTRAINT PK_CHAMCONG PRIMARY KEY (MaChamCong),
    CONSTRAINT FK_CHAMCONG_NHANVIEN FOREIGN KEY (MaNV) REFERENCES dbo.NHANVIEN(MaNV),
    CONSTRAINT UQ_CHAMCONG_MaNV_Ngay UNIQUE (MaNV, NgayChamCong),
    CONSTRAINT CHK_CHAMCONG_Ngay CHECK (NgayChamCong <= CAST(GETDATE() AS DATE)),
    CONSTRAINT CHK_CHAMCONG_TrangThai CHECK (TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM', N'VANG'))
);
```

### 3. Các đối tượng CSDL do TV2 sở hữu

Theo đúng ma trận phân công đối tượng SQL của Nhóm 06 trong repository:

| Tên đối tượng | Loại đối tượng | Mục đích kỹ thuật |
|---|:---:|---|
| `sp_GhiNhanChamCong` | Stored Procedure | Tiếp nhận 7 tham số, kiểm tra tồn tại nhân viên, validate giờ và insert an toàn trả về `MaChamCong OUTPUT`. Đã áp dụng Option B: bao bọc TRY...CATCH với bare `THROW;` để chuyển lỗi nguyên trạng về Java caller mà không tự mở hay can thiệp transaction của Service. |
| `trg_ChamCong_KiemTraGio` | Trigger AFTER INSERT, UPDATE | Kiểm tra logic `GioRa > GioVao`, ném lỗi và ROLLBACK nếu vi phạm. |
| `trg_ChamCong_KiemTraNhanVien` | Trigger AFTER INSERT, UPDATE | Chặn chấm công nếu nhân viên có `TrangThai <> 'DANG_LAM_VIEC'`, tự động ROLLBACK. |
| `vw_TongHopChamCongThang` | View | Tổng hợp theo tháng/năm: Tổng số ngày có mặt, đi trễ, về sớm, vắng và tổng số giờ làm việc thực tế. |
| `IX_CHAMCONG_MaNV_Ngay` | Non-clustered Index | Tạo trên `(MaNV, NgayChamCong)` INCLUDE `(GioVao, GioRa, TrangThai)` giúp tối ưu hóa tra cứu và tính công tháng. |
| `fn_TongPhuCap` (module 03) | Scalar Function | Phân công TV2 phối hợp TV3 (cài tại module 03): tính tổng tiền phụ cấp của nhân viên theo tháng/năm, phục vụ rubric ≥5 Function. |

*(Lưu ý quyền sở hữu: TV2 không sở hữu các đối tượng phụ cấp/khấu trừ thuộc TV3 như `sp_XoaKyLuongChuaChot`, `fn_TongKhauTru`, `vw_TongPhuCapThang`, `trg_PhuCap_KhongSuaKhiDaChotLuong`).*

---

## PHẦN 3. KIẾN TRÚC PHÂN LỚP & TÍCH HỢP ỨNG DỤNG JAVA

### 1. Kiến trúc phân tầng chuẩn
- **Presentation Layer:**
  - `ChamCongPanel.java`: Giao diện chính gồm 2 tab: Tab 1 (Ghi nhận chấm công chi tiết theo ngày, lọc theo kỳ, bảng nhật ký) và Tab 2 (Tổng hợp công theo tháng từ View).
  - `DieuChinhChamCongDialog.java`: Hộp thoại mở dạng drill-down cho phép quản lý nhân sự điều chỉnh giờ vào, giờ ra, trạng thái công của một nhân viên trong tháng.
- **Service Layer (`ChamCongService.java`):**
  - Kiểm tra định dạng thời gian và ràng buộc nghiệp vụ trước khi chuyển xuống CSDL.
  - Điều phối Transaction khi nhập chấm công theo lô (`nhapChamCongTheoLo`): Quản lý kết nối JDBC bằng `setAutoCommit(false)`, `commit()`, và `rollback()` khi gặp lỗi bất kỳ.
- **DAO Layer (`ChamCongDAO.java`):**
  - Gọi `sp_GhiNhanChamCong` thông qua `CallableStatement`.
  - Truy vấn dữ liệu tổng hợp tháng từ View `vw_TongHopChamCongThang` thông qua `PreparedStatement`.
  - UPDATE/DELETE qua DAO không tự kiểm tra kỳ; trigger SQL `trg_ChamCong_KhongSuaKhiDaChotLuong` tại module 05 bảo vệ INSERT/UPDATE/DELETE và ngày nguồn/đích khi kỳ đã chốt. Race đóng kỳ được kiểm chứng bằng hai connection trong QA.

### 1.1 Ngày công và thao tác nền
`ChamCongDAO.updateChamCong` ghi cả `NgayChamCong` cùng giờ/trạng thái/ghi chú. Bảo vệ kỳ cũ và kỳ mới do trigger SQL thực thi, kể cả khi gọi trực tiếp SQL. UI dùng `DatabaseTask`/`SwingWorker` để đưa truy vấn và mutation khỏi Swing EDT; callback UI chạy lại trên EDT. Bằng chứng regression hiện hành và giới hạn test xem [FIX_TASKLIST](FIX_TASKLIST.md).
- **Model Layer (`ChamCong.java`, `TongHopChamCong.java`):**
  - Thực thể POJO đại diện cho bản ghi chấm công và đối tượng tổng hợp công theo tháng.

---

## PHẦN 4. KỊCH BẢN KIỂM THỬ PHÂN HỆ CHẤM CÔNG (TEST DESIGN)

Bộ kịch bản kiểm thử tự động của TV2 được thiết kế tại `database/tests_TV2/test_module_chamcong_TV2.sql` với cơ chế self-seeding token per-run và dọn dẹp cách ly:

Danh sách dưới đây là phạm vi thiết kế test. Kết quả thực thi mới nhất và log xem [FIX_TASKLIST](FIX_TASKLIST.md).

1. **TC-CC-01:** Chấm công đơn lẻ hợp lệ qua `sp_GhiNhanChamCong`.
2. **TC-CC-02:** Chặn trùng lặp cặp `(MaNV, NgayChamCong)` qua SP và qua ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay`.
3. **TC-CC-03:** Chặn nhân viên không tồn tại qua SP và qua khóa ngoại `FK_CHAMCONG_NHANVIEN`.
4. **TC-CC-04:** Chặn ngày trong tương lai qua SP và qua Check Constraint `CHK_CHAMCONG_Ngay`.
5. **TC-CC-05:** Chặn giá trị trạng thái không hợp lệ qua SP và qua Check Constraint `CHK_CHAMCONG_TrangThai`.
6. **TC-CC-06:** Chặn nhân viên đã nghỉ việc (`NGHI_VIEC`) qua SP và qua Trigger `trg_ChamCong_KiemTraNhanVien`.
7. **TC-CC-07:** Chặn giờ ra nhỏ hơn hoặc bằng giờ vào làm qua SP và qua Trigger `trg_ChamCong_KiemTraGio`.
8. **TC-CC-08:** Giao dịch nhập lô 5 bản ghi hợp lệ trong một Database Transaction (COMMIT).
9. **TC-CC-09:** Giao dịch nhập lô có dòng vi phạm $\rightarrow$ ROLLBACK toàn bộ, không lưu dữ liệu dở dang.
10. **TC-CC-10:** Kiểm tra dữ liệu tổng hợp từ View `vw_TongHopChamCongThang`.
11. **TC-CC-11:** Kiểm tra tìm kiếm và tra cứu qua Non-clustered Index `IX_CHAMCONG_MaNV_Ngay`.
12. **TC-CC-12:** Trigger `trg_ChamCong_KiemTraGio` chặn lệnh UPDATE sửa giờ vào/ra vi phạm.
13. **TC-CC-13:** Kiểm tra Stored Procedure chặn tham số bắt buộc NULL.
14. **TC-CC-14:** Dọn dẹp sạch sẽ toàn bộ dữ liệu kiểm thử, bảo toàn CSDL.

---

## PHẦN 5. BENCHMARK CANONICAL V3 (13/10/2026) VÀ MINH CHỨNG LỊCH SỬ

Kịch bản benchmark canonical v3 được lưu tại `Bo_nop_Nhom06/evidence/TV2/reproduction/test_benchmark_index_TV2_guarded.sql` (Run ID: `run_20261013_140800` trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81`).
Lưu ý: Không chạy lại benchmark trong đợt sửa Option B vì thay đổi TRY...CATCH không tác động đến chỉ mục hay truy vấn benchmark.

### 1. Bối cảnh kỹ thuật và phương pháp luận
- **Hiện trạng bảng sản xuất:** Bảng `dbo.CHAMCONG` có sẵn ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay` trên `(MaNV, NgayChamCong)`. Ràng buộc này tạo ra một Non-clustered Index tìm kiếm theo khóa. Do đó, việc so sánh với Table Scan (bỏ toàn bộ index) là phi thực tế trong môi trường sản xuất.
- **Giá trị cốt lõi của Covering Index:** Bổ sung mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)` giúp toàn bộ thông tin truy vấn chi tiết và tổng hợp công đều nằm trên tầng lá của chỉ mục Non-clustered, loại bỏ hoàn toàn toán tử tốn kém **Key Lookup (Clustered Lookup)**.
- **Tính an toàn và cách ly (Isolation):** Benchmark sử dụng bảng tạm cục bộ phiên làm việc `#CHAMCONG_BENCHMARK` chứa 30.000 bản ghi thử nghiệm (100 nhân viên × 300 ngày). Toàn bộ kịch bản tự dọn dẹp sau khi chạy, **bảo toàn 100% cấu trúc và dữ liệu bảng sản xuất `dbo.CHAMCONG`**.
- **Tiêu chí đánh giá chuẩn xác:** Đo lường trên warm cache, có index hint, tập dữ liệu 30.000 dòng tạm, 273 dòng chi tiết và 9 dòng tổng hợp. Đo bằng cả hai công cụ: Messages (`SET STATISTICS IO ON`) và XML Execution Plan. Tuyệt đối không dựa vào thời gian đồng hồ (wall-clock time) dưới 1ms.

### 2. Bảng đối chiếu chỉ số hiệu năng canonical v3

| Chỉ Số Đánh Giá | Trước Khi Có Covering Index (BEFORE) | Sau Khi Có Covering Index (AFTER) | Mức Độ Cải Thiện |
|---|:---:|:---:|:---:|
| **Physical Operator (Truy vấn chi tiết 273 dòng)** | Index Seek trên `UQ_Bench_MaNV_Ngay` + **Key Lookup (Clustered)** | **Index Seek thuần túy** trên `IX_Bench_CHAMCONG_MaNV_Ngay` | Loại bỏ hoàn toàn 273 lần Key Lookup |
| **Logical Reads - Messages (`SET STATISTICS IO`)** | **574 pages** | **5 pages** | **Giảm 99.1289%** |
| **Logical Reads - XML Plan (`RunTimeCountersPerThread`)** | **522 pages** | **5 pages** | **Giảm 99.0421%** |
| **Độ lệch đo lường giữa hai nguồn** | 52 trang (574 vs 522) | 0 trang (5 vs 5) | Nguyên nhân chênh lệch 52 reads ở baseline là chưa xác định (unexplained discrepancy) |
| **Thời gian Messages (`STATISTICS TIME`)** | 15ms CPU / 31ms Elapsed | 0ms CPU / 2ms Elapsed | Cải thiện I/O buffer pool |
| **Thời gian XML Plan (`QueryTimeStats`)** | 0ms CPU / 0ms Elapsed | 0ms CPU / 0ms Elapsed | Không tuyên bố tăng tốc dưới 1ms |
| **Physical Operator (Truy vấn tổng hợp 9 dòng)** | Seek + Lookup + Stream Aggregate | Seek + Stream Aggregate (0 Lookup) | Loại bỏ Key Lookup |
| **Logical Reads (Tổng hợp - Messages / XML)** | **574 / 522 pages** | **5 / 5 pages** | **Giảm 99.1289% / 99.0421%** |
| **Tác động CSDL sản xuất** | Không ảnh hưởng (`#CHAMCONG_BENCHMARK`) | Không ảnh hưởng (`#CHAMCONG_BENCHMARK`) | Bảng `dbo.CHAMCONG` được bảo toàn nguyên vẹn |

### 3. Ảnh benchmark lịch sử (tư liệu tham khảo, tách biệt với số liệu v3)

Năm ảnh chụp màn hình trong `screenshots/TV2/` là tư liệu lịch sử được ghi lại từ giai đoạn đầu, phản ánh trực quan sự thay đổi plan và I/O 574→5 nhưng tách bạch với số liệu canonical v3 ngày 13/10/2026:
1. `screenshots/TV2/TV2_Benchmark_ExecutionPlan_BEFORE.png`: Kế hoạch thực thi BEFORE gồm Index Seek kèm Key Lookup (Clustered).
2. `screenshots/TV2/TV2_Benchmark_ExecutionPlan_AFTER.png`: Kế hoạch thực thi AFTER là Index Seek thuần túy trên `IX_Bench_CHAMCONG_MaNV_Ngay`, 0 Key Lookup.
3. `screenshots/TV2/TV2_Benchmark_StatisticsIO_BEFORE.png`: Thống kê I/O BEFORE ghi nhận 574 logical reads.
4. `screenshots/TV2/TV2_Benchmark_StatisticsIO_AFTER.png`: Thống kê I/O AFTER giảm xuống còn 5 logical reads.
5. `screenshots/TV2/TV2_Benchmark_Results_Metadata.png`: Bảng kết quả tổng hợp công và metadata xác thực cấu trúc `INCLUDE (GioVao, GioRa, TrangThai)`.

---

## PHẦN 6. TÌNH TRẠNG MINH CHỨNG GIAO DIỆN ỨNG DỤNG JAVA SWING (UI EVIDENCE)

- **Trạng thái:** Ảnh bên dưới là tư liệu lịch sử, không chứng minh UI/runtime hiện tại.
- **Danh mục minh chứng UI đã lưu trữ tại `screenshots/TV2/`:**
  - `screenshots/TV2/TV2_ChamCong_ChiTiet.png`: Tab 1 chấm công chi tiết — form ghi nhận chấm công, nút điểm danh hàng loạt (Transaction) và nhật ký chấm công với dữ liệu thật.
  - `screenshots/TV2/TV2_ChamCong_TongHopThang.png`: Tab 2 tổng hợp ngày công tháng — dữ liệu tổng hợp đọc từ View `vw_TongHopChamCongThang`.
  - `screenshots/TV2/TV2_DieuChinhChamCongDialog.png`: Hộp thoại điều chỉnh ngày công dạng drill-down với bản ghi công thật và các thao tác thêm/cập nhật/xóa.

---

## PHẦN 7. BẢO TOÀN TRÁCH NHIỆM & CÁC TỒN TẠI KỸ THUẬT (REMAINING GAPS)

### 1. Phân định quyền sở hữu (Ownership Boundary)
- Toàn bộ nội dung tuân thủ nghiêm ngặt phạm vi phân công của TV2 (Chấm công).
- Không can thiệp, chỉnh sửa cấu trúc hay logic nghiệp vụ của TV1 (Nhân sự), TV3 (Phụ cấp/Khấu trừ), TV4 (Tính lương), TV5 (Bảo mật/Chốt lương).
- Schema sản xuất `dbo.CHAMCONG` và các đối tượng CSDL liên quan được bảo toàn nguyên vẹn.

### 2. Các tồn tại kỹ thuật được ghi nhận rõ ràng (Remaining Gaps)
1. **Ảnh UI:** Ảnh lưu trữ là tư liệu cũ, không phải minh chứng runtime hiện tại. Evidence UI mới nhất xem [FIX_TASKLIST](FIX_TASKLIST.md).
2. **Dữ liệu `dbo.CHAMCONG`:** Không suy luận số lượng/baseline từ tài liệu cũ. QA fixtures được tạo riêng và dọn bởi runner; database dự án gốc không bị mutate trong xác minh nêu tại [FIX_TASKLIST](FIX_TASKLIST.md).
3. **Mở rộng cơ chế chấm công:** Hiện tại hỗ trợ chấm công thủ công và điểm danh theo lô qua giao diện; đề xuất mở rộng kết nối API thiết bị chấm công phần cứng (ZKTeco/Hikvision) trong các pha tiếp theo.
4. **Tình trạng xác minh và môi trường (15/10/2026):**
   - Biên dịch bằng `javac 21.0.8 (với cờ --release 11)`, hoàn tất 55 mã nguồn Java (bao gồm `src/test/java/com/test/Tv2JdbcIntegrationTest.java`; không phải native JDK 11).
   - Đã chạy kiểm thử offline bằng runner: `.\run_verification.ps1 -TestClass SecurityRegressionTest` (**104 assertions PASS**, log `build/test-results/Java_Verification_20261015_174231_883.log`) và `.\run_verification.ps1 -TestClass GuiQaRegressionTest` (**88 assertions PASS**, log `build/test-results/Java_Verification_20261015_174248_559.log`).
   - Kiểm thử runtime SQL/JDBC: Đã tạo cơ sở dữ liệu QA dùng một lần có ownership marker `PRJ_Fix_QA_20261015_175527_ea6da333`, cài đặt đầy đủ schema Option B (hash `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`), kiểm tra định nghĩa thủ tục có TRY/CATCH + bare THROW và OUTPUT. Chạy suite SQL TV2 đạt **21 / 21 PASS (100%)** (log `docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/test_module_chamcong_TV2.log`), test tương tranh `Closed_Source_Concurrency.ps1` **PASS** (log `docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Closed_Source_Concurrency.log`).
   - Kiểm thử live JDBC Transaction Integration Test (`src/test/java/com/test/Tv2JdbcIntegrationTest.java` qua `database/tests_TV2/run_tv2_jdbc_optionb.ps1`): Các lần thử nghiệm trước đây (34, 59 assertions và đợt trung gian 174112) đã được lưu lịch sử superseded. Đạt **87 / 87 assertions PASS (100%)** qua 8 negative guards (từ chối null/prod/protected DB/missing credentials trước khi mutate), 6 kịch bản bắt buộc + chẩn đoán transaction, chứng minh 2 INSERTs có ID > 0 trước rollback, bắt đúng causal `SQLServerException` với `SQLServerError` (Number=50000, Severity=16, State=1) tiếng Việt chuẩn, kiểm tra @@TRANCOUNT/XACT_STATE. Log: `docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Tv2JdbcIntegrationTest.log`. Database QA có marker đã được DROP sạch sẽ sau test.
   - Đồng bộ tài liệu và package: Đã đồng bộ `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql` (10,064 bytes, SHA-256 `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`) và cập nhật dòng 3 của `Bo_nop_Nhom06/SHA256_MANIFEST.csv`, xác thực thành công 537/537 file trong manifest. Word nhóm (đoạn 193, 824) và Slide 5 notes đang chờ TV5 cập nhật theo đề xuất của TV2. Rehearsal đang chờ thực hiện cùng nhóm.
   - *Hiện trạng bàn giao:* Toàn bộ kiểm thử nội bộ TV2 Option B đã hoàn tất và đạt 100% PASS, hiện đang ở trạng thái **Awaiting Independent Review** (chờ reviewer độc lập nghiệm thu trước khi Git delivery; chưa tuyên bố PR được merge hay giảng viên đã nghiệm thu).

## Cập nhật sau PR #52 — 16/10/2026

PR #52 đã merge. Bản QA cuối ghi 55 nguồn biên dịch, controlled suite 179/179, SQL 21/21, concurrency 4/4 và JDBC 105 assertions; evidence mới được giữ riêng tại `Bo_nop_Nhom06/evidence/TV2/optionb-20261016/`. TV5 đã cập nhật hai đoạn Word và notes slide 5, đồng thời đưa evidence vào package. Các con số 87 assertions ở phần lịch sử ngày 15/10 vẫn là kết quả của lượt cũ. Rehearsal nhóm vẫn chưa thực hiện.
