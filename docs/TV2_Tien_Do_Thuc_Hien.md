# TV2 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE CHẤM CÔNG
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Phạm Minh Quân (TV2)  
- **MSSV:** 24110311  
- **Mã phân công:** TV2  
- **Module phụ trách:** Quản lý Chấm Công, Tổng Hợp Ngày Công Tháng, Giao Diện Chấm Công & Điều Chỉnh Công  
- **Branch làm việc:** `feature/tv2-week3-evidence`
- **Rà soát tài liệu:** 08/10/2026. Chứng cứ runtime hiện hành xem [FIX_TASKLIST](FIX_TASKLIST.md); benchmark và ảnh cũ bên dưới chỉ là tư liệu lịch sử.

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích nghiệp vụ chấm công theo cặp nhân viên – ngày.<br>• Chốt cấu trúc bảng `CHAMCONG` và các quy tắc: không trùng ngày, giờ ra > giờ vào, nhân viên nghỉ việc không được chấm công mới.<br>• Đặc tả luồng xử lý `ChamCongPanel` → `Service` → `DAO` → SQL Server.<br>• Thiết kế bộ testcase cho dữ liệu chấm công hợp lệ/không hợp lệ. | • Đặc tả nghiệp vụ chấm công.<br>• Thiết kế bảng `CHAMCONG` (1NF - 3NF).<br>• Danh sách constraint/trigger.<br>• Bộ testcase chấm công. | **100%** | **24/09/2026** | Đã hoàn thành toàn bộ tài liệu phân tích nghiệp vụ, thiết kế tích hợp và bộ testcase kịch bản. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints cho bảng `CHAMCONG`.<br>• Cài đặt Stored Procedure `sp_GhiNhanChamCong`.<br>• Cài đặt 2 Trigger: `trg_ChamCong_KiemTraGio` và `trg_ChamCong_KiemTraNhanVien`.<br>• Cài đặt View `vw_TongHopChamCongThang` và Index `IX_CHAMCONG_MaNV_Ngay`.<br>• Lập trình Java Swing: `ChamCongPanel`, `DieuChinhChamCongDialog`.<br>• Lập trình Model, DAO, Service: `ChamCong`, `ChamCongDAO`, `ChamCongService` (JDBC Transaction All-or-Nothing). | • Script SQL: `database/02_Module_ChamCong_TV2.sql`.<br>• Giao diện chấm công 2 tab (chi tiết & tổng hợp) + dialog điều chỉnh.<br>• Transaction nhập theo lô an toàn.<br>• Mã nguồn Java hoàn thiện. | **100%** | **29/09/2026** | Đã hoàn thành cài đặt CSDL và mã nguồn tầng ứng dụng trong branch làm việc. |
| **T3** | 05/10 – 11/10 | • Cung cấp dữ liệu chấm công cho payroll; function `fn_TinhSoNgayCong` có trong module 02, nhưng `sp_TinhBangLuongThang` hiện đếm trực tiếp bằng truy vấn khoảng ngày.<br>• Rà soát trigger khóa kỳ, ngày được lưu lại và thao tác DB chạy nền.<br>• Hoàn thiện báo cáo và đối chiếu evidence. | • Mã nguồn TV2 đã tích hợp.<br>• Regression SQL/UI và tài liệu cập nhật. | **100%** | **08/10/2026** | Các suite và race hiện hành được ghi tại [FIX_TASKLIST](FIX_TASKLIST.md). Kết quả benchmark 574→5 và ảnh chụp cũ là tư liệu lịch sử, không phải benchmark hay bằng chứng UI của lần xác minh hiện hành. |

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
| `sp_GhiNhanChamCong` | Stored Procedure | Tiếp nhận 7 tham số, kiểm tra tồn tại nhân viên, validate giờ và insert an toàn trả về `MaChamCong OUTPUT`. |
| `trg_ChamCong_KiemTraGio` | Trigger AFTER INSERT, UPDATE | Kiểm tra logic `GioRa > GioVao`, ném lỗi và ROLLBACK nếu vi phạm. |
| `trg_ChamCong_KiemTraNhanVien` | Trigger AFTER INSERT, UPDATE | Chặn chấm công nếu nhân viên có `TrangThai <> 'DANG_LAM_VIEC'`, tự động ROLLBACK. |
| `vw_TongHopChamCongThang` | View | Tổng hợp theo tháng/năm: Tổng số ngày có mặt, đi trễ, về sớm, vắng và tổng số giờ làm việc thực tế. |
| `IX_CHAMCONG_MaNV_Ngay` | Non-clustered Index | Tạo trên `(MaNV, NgayChamCong)` INCLUDE `(GioVao, GioRa, TrangThai)` giúp tối ưu hóa tra cứu và tính công tháng. |

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

## PHẦN 5. BENCHMARK LỊCH SỬ — NON-CLUSTERED COVERING INDEX

`database/test_benchmark_index_TV2.sql` và kết quả 574→5 bên dưới mô tả benchmark lịch sử; không đại diện cho runtime hiện tại. Log và plan hiện hành xem [FIX_TASKLIST](FIX_TASKLIST.md).

### 1. Bối cảnh kỹ thuật và phương pháp luận
- **Hiện trạng bảng sản xuất:** Bảng `dbo.CHAMCONG` có sẵn ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay` trên `(MaNV, NgayChamCong)`. Ràng buộc này tạo ra một Non-clustered Index tìm kiếm theo khóa. Do đó, việc so sánh với Table Scan (bỏ toàn bộ index) là phi thực tế trong môi trường sản xuất.
- **Giá trị cốt lõi của Covering Index:** Bổ sung mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)` giúp toàn bộ thông tin truy vấn chi tiết và tổng hợp công đều nằm trên tầng lá của chỉ mục Non-clustered, loại bỏ hoàn toàn toán tử tốn kém **Key Lookup (Clustered Lookup)**.
- **Tính an toàn và cách ly (Isolation):** Benchmark sử dụng bảng tạm cục bộ phiên làm việc `#CHAMCONG_BENCHMARK` chứa 30.000 bản ghi thử nghiệm. Toàn bộ kịch bản tự dọn dẹp sau khi chạy, **bảo toàn 100% cấu trúc và dữ liệu bảng sản xuất `dbo.CHAMCONG`**.
- **Tiêu chí đánh giá chuẩn xác:** Sử dụng `SET STATISTICS IO ON` và Actual Execution Plan để đo lường số lượt đọc trang logic (Logical Reads). Tuyệt đối không dựa vào thời gian đồng hồ (wall-clock time) do chịu ảnh hưởng bởi tài nguyên nền của hệ điều hành.

### 2. Bảng đối chiếu chỉ số hiệu năng thực tế

| Chỉ Số Đánh Giá | Trước Khi Có Covering Index (BEFORE) | Sau Khi Có Covering Index (AFTER) | Mức Độ Cải Thiện |
|---|:---:|:---:|:---:|
| **Physical Operator** | Index Seek trên `UQ_Bench_MaNV_Ngay` + **Key Lookup (Clustered)** | **Index Seek thuần túy** trên `IX_Bench_CHAMCONG_MaNV_Ngay` | Loại bỏ hoàn toàn Key Lookup (zero Key Lookup) |
| **Logical Reads (I/O)** | **574 pages** | **5 pages** | **Giảm 99.13% số lượt đọc trang logic** |
| **Số lần truy cập bảng chính** | 1 lần qua Clustered B-tree cho mỗi dòng tìm thấy | **0 lần** (truy cập 100% trên index lá) | Triệt tiêu I/O thứ cấp |
| **Scan Count** | 1 | 1 | Quét theo phạm vi khóa |
| **Tác động CSDL sản xuất** | Không ảnh hưởng (`#CHAMCONG_BENCHMARK`) | Không ảnh hưởng (`#CHAMCONG_BENCHMARK`) | Bảng `dbo.CHAMCONG` được bảo toàn nguyên vẹn |

### 3. Ảnh benchmark lịch sử (không phải evidence hiện tại)

Năm ảnh chụp màn hình chứng minh kết quả thực nghiệm được lưu trữ nguyên vẹn tại thư mục `screenshots/TV2/`:
1. `screenshots/TV2/TV2_Benchmark_ExecutionPlan_BEFORE.png`: Thể hiện Execution Plan giai đoạn BEFORE gồm toán tử Index Seek trên `UQ_Bench_MaNV_Ngay` kết hợp với Key Lookup (Clustered).
2. `screenshots/TV2/TV2_Benchmark_ExecutionPlan_AFTER.png`: Thể hiện Execution Plan giai đoạn AFTER chuyển thành Index Seek thuần túy trên `IX_Bench_CHAMCONG_MaNV_Ngay` không còn Key Lookup.
3. `screenshots/TV2/TV2_Benchmark_StatisticsIO_BEFORE.png`: Thống kê I/O giai đoạn BEFORE ghi nhận **574 logical reads**.
4. `screenshots/TV2/TV2_Benchmark_StatisticsIO_AFTER.png`: Thống kê I/O giai đoạn AFTER ghi nhận giảm xuống chỉ còn **5 logical reads**.
5. `screenshots/TV2/TV2_Benchmark_Results_Metadata.png`: Bảng kết quả truy vấn tổng hợp và metadata chỉ mục xác nhận cấu trúc `INCLUDE (GioVao, GioRa, TrangThai)`.

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
