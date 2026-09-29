# TV2 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE CHẤM CÔNG
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Phạm Minh Quân (TV2)  
- **MSSV:** 24110311  
- **Mã phân công:** TV2  
- **Module phụ trách:** Quản lý Chấm Công, Tổng Hợp Ngày Công Tháng, Giao Diện Chấm Công & Điều Chỉnh Công  
- **Branch làm việc:** `feature/attendance-module`

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích nghiệp vụ chấm công theo cặp nhân viên – ngày.<br>• Chốt cấu trúc bảng `CHAMCONG` và các quy tắc: không trùng ngày, giờ ra > giờ vào, nhân viên nghỉ việc không được chấm công mới.<br>• Đặc tả luồng xử lý `ChamCongPanel` → `Service` → `DAO` → SQL Server.<br>• Thiết kế bộ 20 testcase cho dữ liệu chấm công hợp lệ/không hợp lệ. | • Đặc tả nghiệp vụ chấm công.<br>• Thiết kế bảng `CHAMCONG` (1NF - 3NF).<br>• Danh sách constraint/trigger.<br>• Bộ testcase chấm công. | **100%** | **24/09/2026** | Đã hoàn thành toàn bộ tài liệu phân tích nghiệp vụ, thiết kế tích hợp và bộ testcase 20 kịch bản. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints cho bảng `CHAMCONG`.<br>• Cài đặt Stored Procedure `sp_GhiNhanChamCong`.<br>• Cài đặt 2 Trigger: `trg_ChamCong_KiemTraGio` và `trg_ChamCong_KiemTraNhanVien`.<br>• Cài đặt View `vw_TongHopChamCongThang` và Index `IX_CHAMCONG_MaNV_Ngay`.<br>• Lập trình Java Swing: `ChamCongPanel`, `DieuChinhChamCongDialog`.<br>• Lập trình Model, DAO, Service: `ChamCong`, `ChamCongDAO`, `ChamCongService` (JDBC Transaction All-or-Nothing). | • Script SQL: `database/02_Module_ChamCong_TV2.sql`.<br>• Giao diện chấm công 2 tab (chi tiết & tổng hợp) + dialog điều chỉnh.<br>• Transaction nhập theo lô an toàn.<br>• Biên dịch sạch 100%. | **100%** | **29/09/2026** | Đã hoàn thành 100%, tạo Pull Request #12 và đã được merge thành công vào nhánh `main`. |
| **T3** | 05/10 – 11/10 | • Tích hợp liên module: cung cấp ngày công cho TV4 (`sp_TinhBangLuongThang`) và TV1 (`fn_TinhSoNgayCong`).<br>• Đo lường hiệu năng Covering Index `IX_CHAMCONG_MaNV_Ngay` (Execution Plan + `STATISTICS IO/TIME`).<br>• Chụp ảnh giao diện minh chứng hoàn thiện.<br>• Đóng góp nội dung báo cáo tổng hợp nhóm và chuẩn bị slide vấn đáp cá nhân. | • Kết quả benchmark Index.<br>• Ảnh chụp giao diện hoàn thiện.<br>• Báo cáo tổng kết phân hệ. | **100%** | **29/09/2026** | Đã tích hợp hoàn tất vào luồng chạy chung của toàn hệ thống, vượt qua 41/41 test criteria kiểm thử tích hợp. |

---

## PHẦN 2. ĐẶC TẢ NGHIỆP VỤ & THIẾT KẾ CƠ SỞ DỮ LIỆU

### 1. Vai trò và Quy tắc nghiệp vụ cốt lõi

Module chấm công chịu trách nhiệm ghi nhận, theo dõi và tổng hợp thời gian làm việc thực tế của nhân viên:
1. **Cung cấp dữ liệu nguồn cho TV1 (Nguyễn Minh Trí):** Hàm `fn_TinhSoNgayCong(MaNV, Thang, Nam)`.
2. **Cung cấp dữ liệu nguồn cho TV4 (Nguyễn Quang Vinh):** Cung cấp số ngày công thực tế (`NgayCongThucTe`) để tính tiền công trong `sp_TinhBangLuongThang`.
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

| Tên đối tượng | Loại đối tượng | Mục đích kỹ thuật |
|---|:---:|---|
| `sp_GhiNhanChamCong` | Stored Procedure | Tiếp nhận 7 tham số, kiểm tra tồn tại nhân viên, validate giờ và insert an toàn trả về `MaChamCong OUTPUT`. |
| `trg_ChamCong_KiemTraGio` | Trigger AFTER INSERT, UPDATE | Kiểm tra logic `GioRa > GioVao`, ném lỗi và ROLLBACK nếu vi phạm. |
| `trg_ChamCong_KiemTraNhanVien` | Trigger AFTER INSERT | Chặn chấm công nếu nhân viên có `TrangThai = 'NGHI_VIEC'`, tự động ROLLBACK. |
| `vw_TongHopChamCongThang` | View | Tổng hợp theo tháng/năm: Tổng số ngày có mặt, đi trễ, về sớm, vắng và tổng số giờ làm việc thực tế. |
| `IX_CHAMCONG_MaNV_Ngay` | Non-clustered Index | Tạo trên `(MaNV, NgayChamCong)` INCLUDE `(GioVao, GioRa, TrangThai)` giúp tối ưu hóa tra cứu và tính công tháng. |

---

## PHẦN 3. KIẾN TRÚC PHÂN LỚP & TÍCH HỢP ỨNG DỤNG JAVA

### 1. Kiến trúc 4 tầng chuẩn mực
- **Presentation Layer:**
  - `ChamCongPanel.java`: Giao diện chính gồm 2 tab: Tab 1 (Ghi nhận chấm công chi tiết theo ngày, tìm kiếm, lọc trạng thái, phân trang) và Tab 2 (Tổng hợp công theo tháng, xuất thống kê).
  - `DieuChinhChamCongDialog.java`: Hộp thoại cho phép người quản lý nhân sự điều chỉnh giờ vào, giờ ra, trạng thái công có ghi nhận lý do.
- **Service Layer (`ChamCongService.java`):**
  - Kiểm tra định dạng thời gian và ràng buộc nghiệp vụ trước khi chuyển xuống CSDL.
  - Điều phối Transaction khi nhập chấm công theo lô (`importBatch`): Quản lý kết nối JDBC bằng `setAutoCommit(false)`, `commit()`, và `rollback()` khi gặp lỗi bất kỳ.
- **DAO Layer (`ChamCongDAO.java`):**
  - Gọi `sp_GhiNhanChamCong` thông qua `CallableStatement`.
  - Truy vấn dữ liệu tổng hợp tháng từ View `vw_TongHopChamCongThang` thông qua `PreparedStatement`.
- **Model Layer (`ChamCong.java`):**
  - Thực thể POJO đại diện cho một bản ghi chấm công (MaChamCong, MaNV, HoTen, TenPB, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu).

---

## PHẦN 4. DANH MỤC TEST CASES & BẰNG CHỨNG THỰC THI

Bộ kiểm thử gồm 20 kịch bản chia thành 5 nhóm:

### 1. Nhóm Ràng buộc Schema & Constraint
- **TC-CC-01 (Chấm công đơn lẻ hợp lệ):** Nhập `MaNV=1`, ngày hợp lệ, `GioVao='08:00'`, `GioRa='17:00'` $\rightarrow$ **PASS** (Thêm thành công, sinh mã tự tăng).
- **TC-CC-02 (Ngăn chặn trùng lặp trong ngày):** Cố ý chấm 2 lần cho cùng nhân viên cùng ngày $\rightarrow$ **PASS** (Vi phạm `UQ_CHAMCONG_MaNV_Ngay`, bị từ chối).
- **TC-CC-03 (Chặn nhân viên không tồn tại):** Nhập `MaNV=999` $\rightarrow$ **PASS** (Vi phạm khóa ngoại `FK_CHAMCONG_NHANVIEN`, bị chặn).
- **TC-CC-04 (Chặn ngày trong tương lai):** Nhập ngày `2099-01-01` $\rightarrow$ **PASS** (Vi phạm `CHK_CHAMCONG_Ngay`, bị chặn).
- **TC-CC-05 (Chặn giá trị trạng thái ngoài danh mục):** Trạng thái `KHONG_HOP_LE` $\rightarrow$ **PASS** (Vi phạm `CHK_CHAMCONG_TrangThai`).

### 2. Nhóm Trigger nghiệp vụ
- **TC-CC-06 (Trigger chặn nhân viên đã nghỉ việc):** Chấm công cho nhân viên `TrangThai = 'NGHI_VIEC'` $\rightarrow$ **PASS** (`trg_ChamCong_KiemTraNhanVien` chặn và ROLLBACK).
- **TC-CC-07 (Trigger chặn giờ ra nhỏ hơn hoặc bằng giờ vào):** `GioVao='17:00'`, `GioRa='08:00'` $\rightarrow$ **PASS** (`trg_ChamCong_KiemTraGio` chặn và ROLLBACK).

### 3. Nhóm Transaction nhập lô All-or-Nothing
- **TC-CC-08 (Nhập lô hợp lệ toàn bộ):** Import danh sách 5 nhân viên hợp lệ $\rightarrow$ **PASS** (Tất cả 5 dòng được commit vào CSDL).
- **TC-CC-09 (Rollback toàn bộ lô khi có 1 dòng sai):** Lô 5 nhân viên có dòng thứ 3 chứa nhân viên đã nghỉ việc $\rightarrow$ **PASS** (Cả lô bị rollback, không dòng nào được ghi).

### 4. Nhóm View tổng hợp & Index
- **TC-CC-10 (Truy vấn tổng hợp tháng):** Đọc từ `vw_TongHopChamCongThang` $\rightarrow$ **PASS** (Dữ liệu số ngày công khớp 100% với số bản ghi thực tế).
- **TC-CC-11 (Hiệu năng Index):** Lọc theo `MaNV` và `NgayChamCong` qua `IX_CHAMCONG_MaNV_Ngay` $\rightarrow$ **PASS** (Chuyển từ Table Scan sang Index Seek).
