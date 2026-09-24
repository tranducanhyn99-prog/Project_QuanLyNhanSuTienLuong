# TV2 – Phân tích & Thiết kế Module Chấm Công
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Phạm Minh Quân (TV2, MSSV 24110311)<br>
**Ngày:** 24/09/2026<br>
**Phiên bản:** 1.0 (Tuần 1)

---

## 1. Đặc tả nghiệp vụ chấm công

### 1.1 Mô tả tổng quan

Module chấm công chịu trách nhiệm **ghi nhận, theo dõi và tổng hợp thời gian làm việc thực tế** của nhân viên theo từng ngày. Trong hệ thống Quản lý Nhân sự và Tiền lương, dữ liệu chấm công giữ vai trò bản lề:
- Cung cấp dữ liệu nguồn trực tiếp cho **TV1 (Nguyễn Minh Trí)** thông qua hàm `fn_TinhSoNgayCong(MaNV, Thang, Nam)`.
- Cung cấp số ngày công thực tế (`NgayCongThucTe`) cho **TV4 (Nguyễn Quang Vinh)** phục vụ tính tiền công trong thủ tục `sp_TinhBangLuongThang`.
- Cung cấp số liệu tổng hợp giờ làm việc, ngày đi làm phục vụ báo cáo và tra cứu cho **TV5 (Trần Đức Anh)**.

Mỗi bản ghi chấm công phản ánh một lần làm việc của một nhân viên cụ thể trong một ngày xác định, bao gồm giờ vào làm (`GioVao`) và giờ ra về (`GioRa`).

### 1.2 Tác nhân và phân quyền liên quan

Căn cứ theo ma trận bảo mật tại [TV5_Security_Design.md](TV5_Security_Design.md) và thiết kế kiến trúc [TV5_Architecture.md](TV5_Architecture.md):

| Tác nhân | Vai trò SQL Server | Quyền hạn trên Module Chấm công |
|---|---|---|
| **HR_Manager** | `role_HRManager` | Thực hiện ghi nhận chấm công hằng ngày, chỉnh sửa thông tin chấm công hợp lệ, nhập chấm công theo lô, xem tổng hợp chấm công toàn công ty. |
| **DB_Admin** | `role_DBAdmin` | Toàn quyền quản trị, bảo trì dữ liệu, kiểm tra tính toàn vẹn hệ thống và khắc phục sự cố. |
| **Payroll_Officer** | `role_PayrollOfficer` | Chỉ có quyền đọc (`SELECT`) dữ liệu bảng `CHAMCONG` và view `vw_TongHopChamCongThang` để đối soát và tính lương; bị `DENY` thao tác INSERT/UPDATE/DELETE. |
| **Employee** | `role_Employee` | Bị `DENY` thao tác trực tiếp trên bảng `CHAMCONG`; chỉ được tra cứu lịch sử chấm công của chính mình thông qua giao diện hoặc View được cấp phép. |

### 1.3 Quy tắc nghiệp vụ cốt lõi

1. **Tính duy nhất theo cặp Nhân viên – Ngày:** Mỗi nhân viên trong một ngày chỉ có tối đa một bản ghi chấm công chính thức. Hệ thống không cho phép trùng lặp dữ liệu chấm công cùng ngày (`UQ_CHAMCONG_MaNV_Ngay`).
2. **Tính hợp lý về thời gian làm việc:** Giờ ra về (`GioRa`) phải lớn hơn giờ vào làm (`GioVao`) trong cùng ca làm việc của ngày chấm công (`GioRa > GioVao`).
3. **Tính hợp lệ của nhân viên:** Chỉ nhân viên đang làm việc (`TrangThai = 'DANG_LAM'`) mới được ghi nhận chấm công. Nghiêm cấm chấm công cho nhân viên đã nghỉ việc (`TrangThai = 'DA_NGHI'`).
4. **Tính hợp lệ về ngày chấm công:** Ngày chấm công (`NgayChamCong`) không được vượt quá ngày hiện tại (`NgayChamCong <= CAST(GETDATE() AS DATE)`).
5. **Nguyên tắc nhập chấm công theo lô (Batch Processing):** Khi nhập danh sách chấm công hàng loạt (ví dụ: import dữ liệu máy chấm công vân tay/thẻ từ hằng ngày), toàn bộ lô dữ liệu phải tuân thủ nguyên tắc toàn vẹn **All-or-Nothing**. Nếu bất kỳ dòng nào vi phạm quy tắc (nhân viên đã nghỉ việc, trùng ngày, giờ không hợp lệ), toàn bộ giao dịch (Transaction) phải được `ROLLBACK`, không ghi nhận dữ liệu dở dang.

### 1.4 Luồng nghiệp vụ tổng quan

```text
┌──────────────────────────────────────────────────────────┐
│                   HR_Manager thao tác                    │
│   (Ghi nhận đơn lẻ trên ChamCongPanel hoặc Nhập theo lô) │
└────────────────────────────┬─────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────┐
│              Tầng Service (ChamCongService)              │
│  - Kiểm tra định dạng giờ vào, giờ ra, ngày chấm công    │
│  - Kiểm tra logic GioRa > GioVao                         │
│  - Khởi tạo Transaction (nếu nhập theo lô)               │
└────────────────────────────┬─────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────┐
│                Tầng DAO (ChamCongDAO)                    │
│  - Thực thi CallableStatement / PreparedStatement        │
│  - Truyền tham số đến SQL Server                         │
└────────────────────────────┬─────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────┐
│               Tầng CSDL (SQL Server Engine)              │
│  1. Kiểm tra FK_CHAMCONG_NHANVIEN (Mã NV tồn tại)        │
│  2. Kiểm tra UQ_CHAMCONG_MaNV_Ngay (Không trùng ngày)    │
│  3. Kích hoạt trg_ChamCong_KiemTraNhanVien:              │
│     → Nếu NHANVIEN.TrangThai = 'DA_NGHI' → RAISERROR     │
│  4. Kích hoạt kiểm tra giờ làm việc                      │
│     → Nếu GioRa <= GioVao → Báo lỗi                      │
│  5. Lưu dữ liệu hoặc ROLLBACK nếu có lỗi                 │
└──────────────────────────────────────────────────────────┘
```

---

## 2. Thiết kế bảng CHAMCONG

### 2.1 Cấu trúc DDL đề xuất

```sql
CREATE TABLE CHAMCONG (
    MaChamCong    INT           IDENTITY(1,1) PRIMARY KEY,
    MaNV          INT           NOT NULL,
    NgayChamCong  DATE          NOT NULL,
    GioVao        TIME(0)       NOT NULL,
    GioRa         TIME(0)       NULL,
    TrangThai     NVARCHAR(20)  NOT NULL DEFAULT N'CO_MAT'
        CONSTRAINT CHK_CHAMCONG_TrangThai
            CHECK (TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM', N'VANG')),
    GhiChu        NVARCHAR(255) NULL,

    -- Ràng buộc khóa ngoại tham chiếu nhân viên
    CONSTRAINT FK_CHAMCONG_NHANVIEN
        FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV),

    -- Ràng buộc duy nhất: Mỗi nhân viên chỉ có 1 bản ghi mỗi ngày
    CONSTRAINT UQ_CHAMCONG_MaNV_Ngay
        UNIQUE (MaNV, NgayChamCong),

    -- Ràng buộc ngày chấm công không vượt quá ngày hiện tại
    CONSTRAINT CHK_CHAMCONG_Ngay
        CHECK (NgayChamCong <= CAST(GETDATE() AS DATE))
);
```

### 2.2 Mô tả chi tiết từng cột

| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc | Diễn giải nghiệp vụ |
|---|---|---|---|---|
| `MaChamCong` | `INT` | No | `IDENTITY(1,1)`, `PK` | Khóa chính tự tăng, định danh duy nhất cho bản ghi chấm công. |
| `MaNV` | `INT` | No | `FK` → `NHANVIEN(MaNV)` | Mã nhân viên được chấm công. |
| `NgayChamCong` | `DATE` | No | `CHECK <= GETDATE()` | Ngày diễn ra ca làm việc (định dạng `YYYY-MM-DD`). |
| `GioVao` | `TIME(0)` | No | — | Thời điểm nhân viên bắt đầu ca làm việc (độ chính xác đến giây). |
| `GioRa` | `TIME(0)` | Yes | — | Thời điểm nhân viên kết thúc ca làm việc (có thể NULL khi vừa check-in). |
| `TrangThai` | `NVARCHAR(20)` | No | `DEFAULT N'CO_MAT'`, `CHECK` | Trạng thái ghi nhận (`CO_MAT`, `DI_TRE`, `VE_SOM`, `VANG`). |
| `GhiChu` | `NVARCHAR(255)` | Yes | — | Ghi chú bổ sung (lý do đi trễ, giải trình ca làm, v.v.). |

### 2.3 Ràng buộc toàn vẹn & Constraint đặc biệt

| Tên ràng buộc | Loại ràng buộc | Mục đích nghiệp vụ |
|---|---|---|
| `PK_CHAMCONG` | PRIMARY KEY | Định danh duy nhất cho từng lượt chấm công. |
| `FK_CHAMCONG_NHANVIEN` | FOREIGN KEY | Đảm bảo tính toàn vẹn tham chiếu đến bảng `NHANVIEN`. Không thể chấm công cho mã nhân viên không tồn tại. |
| `UQ_CHAMCONG_MaNV_Ngay` | UNIQUE (`MaNV`, `NgayChamCong`) | Ngăn chặn việc nhân viên bị chấm công 2 lần trong cùng một ngày làm việc. |
| `CHK_CHAMCONG_Ngay` | CHECK | Ngăn chặn việc nhập trước ngày công trong tương lai. |
| `CHK_CHAMCONG_TrangThai` | CHECK | Đảm bảo giá trị cột `TrangThai` nằm trong danh mục chuẩn hóa. |

### 2.4 Đánh giá chuẩn hóa dữ liệu (1NF – 3NF)

- **1NF (First Normal Form):** Mọi thuộc tính trong bảng `CHAMCONG` đều mang giá trị nguyên tố (atomic values). Không có thuộc tính đa trị hoặc nhóm lặp. Mỗi cột có kiểu dữ liệu xác định rõ ràng.
- **2NF (Second Normal Form):** Đạt 1NF và toàn bộ các thuộc tính không khóa (`NgayChamCong`, `GioVao`, `GioRa`, `TrangThai`, `GhiChu`) đều phụ thuộc hàm đầy đủ vào khóa chính `MaChamCong` cũng như khóa ứng viên (`MaNV`, `NgayChamCong`). Không có sự phụ thuộc từng phần.
- **3NF (Third Normal Form):** Đạt 2NF và không tồn tại phụ thuộc bắc cầu giữa các thuộc tính không khóa. Thông tin phòng ban, họ tên, chức vụ của nhân viên không lưu tại bảng `CHAMCONG` mà được liên kết qua `MaNV` tới bảng `NHANVIEN`.

---

## 3. Các đối tượng cơ sở dữ liệu liên quan (SQL Objects)

Căn cứ ma trận phân công tại [Ke_hoach_phan_cong_Project_DBMS_Nhom06.md](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md):

### 3.1 Trigger sở hữu chính của TV2: `trg_ChamCong_KiemTraNhanVien`

```text
Tên Trigger:   trg_ChamCong_KiemTraNhanVien
Bảng áp dụng:  CHAMCONG
Sự kiện:       AFTER INSERT, UPDATE
Mục đích:      Ngăn chặn ghi nhận hoặc cập nhật chấm công cho nhân viên đã nghỉ việc (TrangThai = 'DA_NGHI').

Đặc tả logic:
  1. Kiểm tra trong bảng 'inserted' xem có MaNV nào liên kết với NHANVIEN có TrangThai = 'DA_NGHI' không.
  2. NẾU tồn tại:
     - Gửi thông báo lỗi bằng RAISERROR: N'Lỗi: Không được phép ghi nhận chấm công cho nhân viên đã nghỉ việc.', 16, 1
     - Thực thi ROLLBACK TRANSACTION
  3. NGƯỢC LẠI: Cho phép thao tác tiếp tục.
```

### 3.2 View sở hữu chính của TV2: `vw_TongHopChamCongThang`

```text
Tên View:      vw_TongHopChamCongThang
Mục đích:      Tổng hợp số liệu chấm công theo từng nhân viên trong từng tháng và năm.
               Phục vụ cho TV4 lấy số ngày công thực tế tính lương và hiển thị báo cáo.

Các cột trả về:
  - MaNV             INT
  - HoTen            NVARCHAR(100) (kết nối từ NHANVIEN)
  - Thang            INT
  - Nam              INT
  - SoNgayDiLam      INT (COUNT số ngày có chấm công hợp lệ)
  - TongSoGioLam     DECIMAL(10,2) (tổng thời gian làm việc thực tế)
  - SoLanDiTre       INT (số lần TrangThai = 'DI_TRE')

Logic truy vấn:
  SELECT
      cc.MaNV,
      nv.HoTen,
      MONTH(cc.NgayChamCong) AS Thang,
      YEAR(cc.NgayChamCong)  AS Nam,
      COUNT(cc.MaChamCong)   AS SoNgayDiLam,
      SUM(CASE
            WHEN cc.GioRa IS NOT NULL AND cc.GioRa > cc.GioVao
            THEN DATEDIFF(MINUTE, cc.GioVao, cc.GioRa) / 60.0
            ELSE 0
          END)               AS TongSoGioLam,
      SUM(CASE WHEN cc.TrangThai = N'DI_TRE' THEN 1 ELSE 0 END) AS SoLanDiTre
  FROM CHAMCONG cc
  JOIN NHANVIEN nv ON cc.MaNV = nv.MaNV
  GROUP BY cc.MaNV, nv.HoTen, MONTH(cc.NgayChamCong), YEAR(cc.NgayChamCong);
```

### 3.3 Index sở hữu chính của TV2: `IX_CHAMCONG_MaNV_Ngay`

```text
Tên Index:     IX_CHAMCONG_MaNV_Ngay
Bảng áp dụng:  CHAMCONG
Loại Index:    NONCLUSTERED INDEX
Cột Index:     (MaNV, NgayChamCong)
Cột INCLUDE:   (GioVao, GioRa, TrangThai)

Mục đích và lợi ích hiệu năng:
  - Tối ưu hóa các câu truy vấn lọc theo MaNV và khoảng ngày trong tháng.
  - Bao phủ toàn bộ các cột cần thiết cho việc tính ngày công (Covering Index),
    giúp SQL Server thực hiện Index Seek thay vì Table Scan / Clustered Index Scan.
  - Phục vụ trực tiếp cho View vw_TongHopChamCongThang và hàm fn_TinhSoNgayCong.
  - Tuần 3 sẽ thực hiện đo kiểm Execution Plan và SET STATISTICS IO, TIME để đối chiếu.
```

### 3.4 Transaction nghiệp vụ của TV2: Nhập chấm công theo lô (Batch Processing)

```text
Tên nghiệp vụ: Nhập chấm công theo lô (Import danh sách chấm công hằng ngày)
Phạm vi:       Tầng Service (ChamCongService) điều phối thông qua JDBC Transaction hoặc Stored Procedure
Nguyên tắc:    Tính nguyên tố (Atomicity) - Toàn bộ thành công hoặc hủy bỏ toàn bộ.

Kịch bản lỗi phải Rollback:
  - Một dòng trong danh sách có MaNV thuộc về nhân viên đã nghỉ việc (kích hoạt trg_ChamCong_KiemTraNhanVien).
  - Một dòng trong danh sách có NgayChamCong trùng với dữ liệu đã tồn tại (vi phạm UQ_CHAMCONG_MaNV_Ngay).
  - Một dòng có giờ ra nhỏ hơn hoặc bằng giờ vào (vi phạm logic kiểm tra giờ).
  - Lỗi kết nối CSDL hoặc ngoại lệ runtime giữa quá trình ghi lô.
```

### 3.5 Các đối tượng SQL khác theo ma trận phân công chung

Theo bảng phân công tổng thể của nhóm:
- `sp_CapNhatNhanVien`: TV2 chịu trách nhiệm cài đặt theo phân công SQL dùng chung, phối hợp cùng TV1 để kiểm thử tính toàn vẹn liên kết.
- `fn_TongPhuCap`: TV2 chịu trách nhiệm cài đặt để TV4 gọi trong quá trình tính lương.

---

## 4. Các điểm giao thoa & Quyết định kiến trúc chưa chốt (Unresolved Decisions)

Để đảm bảo tính độc lập và tuân thủ chặt chẽ ranh giới trách nhiệm trong dự án, TV2 ghi nhận rõ các vấn đề kiến trúc và giao diện hiện **CHƯA ĐƯỢC CHỐT (UNRESOLVED)** giữa các thành viên:

### 4.1 Quyền sở hữu và phân công thủ tục `sp_GhiNhanChamCong` (TV2 vs TV3) — [CHƯA CHỐT / UNRESOLVED]

- **Hiện trạng phân công:** Trong tài liệu [Ke_hoach_phan_cong_Project_DBMS_Nhom06.md](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), thủ tục `sp_GhiNhanChamCong` được xếp vào danh mục sở hữu của **TV3 (Trần Tiến Đạt)** kèm ghi chú *"phối hợp TV2 tích hợp vào ứng dụng"*.
- **Vấn đề giao thoa:** Tuy nhiên, trong [TV5_Architecture.md](TV5_Architecture.md) Mục 2.3, lớp `ChamCongDAO` (do TV2 chịu trách nhiệm cài đặt) lại gọi trực tiếp `sp_GhiNhanChamCong`.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Chưa có quyết định cuối cùng về việc:
  - Liệu TV3 sẽ là người viết và kiểm thử độc lập thủ tục này rồi bàn giao cho TV2 gọi qua JDBC;
  - Hay quyền sở hữu thủ tục này sẽ được điều chuyển về cho TV2 quản lý trực tiếp trong module Chấm công.
- **Hành động:** Trong tài liệu Tuần 1, TV2 định nghĩa giao diện mong đợi (contract) của thủ tục này nhưng **không tự ý gán quyền sở hữu hay tự ý giải quyết phân công**.

### 4.2 Quyền sở hữu và kiểm soát Trigger `trg_ChamCong_KiemTraGio` (TV2 vs TV3) — [CHƯA CHỐT / UNRESOLVED]

- **Hiện trạng phân công:** Trong ma trận phân công CSDL, trigger `trg_ChamCong_KiemTraGio` (kiểm tra `GioRa > GioVao` trên bảng `CHAMCONG`) được phân cho **TV3 (Trần Tiến Đạt)**, trong khi trigger `trg_ChamCong_KiemTraNhanVien` (kiểm tra nhân viên nghỉ việc) được phân cho **TV2**.
- **Vấn đề giao thoa:** Cả hai trigger này đều gắn trên cùng một bảng dữ liệu là `CHAMCONG`. Việc hai thành viên cùng quản lý trigger trên cùng một bảng có thể dẫn đến xung đột thứ tự thực thi (`sp_settriggerorder`) hoặc trùng lặp xử lý lỗi.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Cần cuộc họp kỹ thuật toàn nhóm để thống nhất gộp trigger hay giữ tách biệt theo đúng phân công rubric.

### 4.3 Kiểu dữ liệu và trạng thái nghiệp vụ bảng `CHAMCONG` — [CHƯA CHỐT / UNRESOLVED]

- **Hiện trạng:**
  - Kiểu dữ liệu cho giờ: Đang đề xuất dùng `TIME(0)` kết hợp cột `NgayChamCong` kiểu `DATE`. Tuy nhiên, nếu nghiệp vụ mở rộng sang ca đêm (vắt qua ngày hôm sau), cần xem xét kiểu `DATETIME`.
  - Cột `TrangThai`: Đang đề xuất thêm cột trạng thái (`CO_MAT`, `DI_TRE`, `VE_SOM`, `VANG`) để hỗ trợ báo cáo. Cần TV1 và TV4 rà soát xem có ảnh hưởng đến hàm đếm ngày công `fn_TinhSoNgayCong` hay không.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Chờ thống nhất chính thức từ toàn nhóm trước khi tạo script DDL tại Tuần 2.

### 4.4 Cơ chế thực thi Transaction nhập chấm công theo lô — [CHƯA CHỐT / UNRESOLVED]

- **Lựa chọn 1:** Thực thi hoàn toàn tại tầng CSDL thông qua một Stored Procedure nhận dữ liệu dạng Table-Valued Parameter (TVP) hoặc JSON/XML.
- **Lựa chọn 2:** Thực thi tại tầng Java JDBC (Service quản lý `Connection.setAutoCommit(false)`, lặp batch insert qua DAO, và gọi `commit()` / `rollback()`).
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Cả hai phương án đều đáp ứng tiêu chí Transaction của môn học; sẽ chốt giải pháp dựa trên độ tương thích với driver JDBC và thời gian triển khai ở Tuần 2.

### 4.5 Hợp đồng giao tiếp dữ liệu với Module Tính Lương (TV4) — [CHƯA CHỐT / UNRESOLVED]

- Quy tắc xác định "1 ngày công hợp lệ": Nhân viên phải có đủ cả `GioVao` và `GioRa`, hay chỉ cần có mặt? Đi trễ có bị trừ công hay không?
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Đang chờ phản hồi từ TV1 (tác giả `fn_TinhSoNgayCong`) và TV4 (tác giả `sp_TinhBangLuongThang`).

---

## 5. Kế hoạch triển khai Tuần 2 & Tuần 3

| Tuần | Mục tiêu công việc chính | Sản phẩm dự kiến |
|---|---|---|
| **Tuần 2** | - Cài đặt script DDL tạo bảng `CHAMCONG`, các Constraint và Index `IX_CHAMCONG_MaNV_Ngay`.<br>- Cài đặt Trigger `trg_ChamCong_KiemTraNhanVien`.<br>- Cài đặt View `vw_TongHopChamCongThang`.<br>- Phối hợp thống nhất và cài đặt các SP/Trigger còn chưa chốt.<br>- Xây dựng mã nguồn Java: `ChamCong.java`, `ChamCongDAO.java`, `ChamCongService.java`, `ChamCongPanel.java`.<br>- Cài đặt Transaction nhập chấm công theo lô. | - File script SQL module chấm công.<br>- Source code Java hoạt động.<br>- Giao diện chấm công kết nối CSDL.<br>- Bộ test kiểm thử giao dịch. |
| **Tuần 3** | - Thực hiện kiểm thử toàn diện với bộ test case đã thiết kế.<br>- Đo hiệu năng Index `IX_CHAMCONG_MaNV_Ngay` (STATISTICS IO/TIME, Execution Plan).<br>- Thu thập ảnh chụp màn hình và log minh chứng phục vụ báo cáo.<br>- Tham gia integration test toàn hệ thống cùng TV5. | - Báo cáo kiểm thử chấm công.<br>- Minh chứng hiệu năng index và rollback trigger.<br>- Đóng góp nội dung vào báo cáo tổng kết 50–100 trang. |

---

## Phụ lục – Tham chiếu tài liệu liên quan

1. [Ke_hoach_phan_cong_Project_DBMS_Nhom06.md](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md) – Ma trận phân công và trách nhiệm thành viên.
2. [TV5_Architecture.md](TV5_Architecture.md) – Kiến trúc phân lớp 4 tầng và nguyên tắc DAO/Service.
3. [TV5_Security_Design.md](TV5_Security_Design.md) – Ma trận phân quyền CSDL và ứng dụng.
4. [TV5_Integration_Checklist.md](TV5_Integration_Checklist.md) – Tiêu chuẩn kỹ thuật khi mở Pull Request.
5. [TV4_Payroll_Analysis.md](TV4_Payroll_Analysis.md) – Yêu cầu dữ liệu nguồn cho module tính lương.
