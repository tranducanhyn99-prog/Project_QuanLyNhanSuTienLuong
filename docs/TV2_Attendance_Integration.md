# TV2 – Thiết kế Tích hợp Module Chấm Công
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Phạm Minh Quân (TV2, MSSV 24110311)<br>
**Ngày:** 24/09/2026<br>
**Phiên bản:** 1.0 (Tuần 1)

---

## 1. Tổng quan tích hợp kiến trúc 4 tầng

Tuân thủ kiến trúc phân lớp chuẩn của dự án được quy định tại [TV5_Architecture.md](TV5_Architecture.md), Module Chấm công do **TV2 (Phạm Minh Quân)** chịu trách nhiệm toàn diện từ thiết kế, cài đặt đến vận hành. TV2 sở hữu trọn vẹn:
- **Cơ sở dữ liệu:** Bảng `CHAMCONG`, Index `IX_CHAMCONG_MaNV_Ngay`, Thủ tục `dbo.sp_GhiNhanChamCong`, Trigger `dbo.trg_ChamCong_KiemTraGio`, Trigger `dbo.trg_ChamCong_KiemTraNhanVien`, View `dbo.vw_TongHopChamCongThang`.
- **Ứng dụng Java:** Model `com.model.ChamCong`, DAO `com.dao.ChamCongDAO`, Service `com.service.ChamCongService` (quản lý JDBC Transaction All-or-Nothing).

Hệ thống được cấu trúc xuyên suốt qua 4 tầng:

```text
┌──────────────────────────────────────────────────────────────┐
│             PRESENTATION LAYER (Java Swing UI)               │
│                  com.ui.chamcong.ChamCongPanel               │
└──────────────────────────────┬───────────────────────────────┘
                               │  gọi phương thức nghiệp vụ
┌──────────────────────────────▼───────────────────────────────┐
│               SERVICE LAYER (Business Logic)                 │
│               com.service.ChamCongService                    │
└──────────────────────────────┬───────────────────────────────┘
                                │  điều phối Transaction / DAO
┌──────────────────────────────▼───────────────────────────────┐
│               DAO LAYER (Data Access Object / JDBC)          │
│                  com.dao.ChamCongDAO                         │
└──────────────────────────────┬───────────────────────────────┘
                               │  JDBC Connection (CallableStatement/PreparedStatement)
┌──────────────────────────────▼───────────────────────────────┐
│       DATABASE LAYER (Microsoft SQL Server - TV2 sở hữu)     │
│   Bảng: CHAMCONG                                             │
│   Thủ tục: dbo.sp_GhiNhanChamCong                            │
│   Trigger: dbo.trg_ChamCong_KiemTraGio                       │
│   Trigger: dbo.trg_ChamCong_KiemTraNhanVien                  │
│   View: dbo.vw_TongHopChamCongThang                          │
│   Index: IX_CHAMCONG_MaNV_Ngay                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Đặc tả chi tiết các thành phần trong module

### 2.1 Lớp Model: `com.model.ChamCong`

Lớp đối tượng thuần túy (Java Bean / POJO) đại diện cho một bản ghi chấm công:

```java
package com.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class ChamCong {
    private int maChamCong;
    private int maNV;
    private LocalDate ngayChamCong;
    private LocalTime gioVao;
    private LocalTime gioRa;
    private String trangThai; // "CO_MAT", "DI_TRE", "VE_SOM", "VANG"
    private String ghiChu;

    public ChamCong() {}

    public ChamCong(int maNV, LocalDate ngayChamCong, LocalTime gioVao, LocalTime gioRa, String trangThai, String ghiChu) {
        this.maNV = maNV;
        this.ngayChamCong = ngayChamCong;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    // Getters và Setters đầy đủ...
    public int getMaChamCong() { return maChamCong; }
    public void setMaChamCong(int maChamCong) { this.maChamCong = maChamCong; }
    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }
    public LocalDate getNgayChamCong() { return ngayChamCong; }
    public void setNgayChamCong(LocalDate ngayChamCong) { this.ngayChamCong = ngayChamCong; }
    public LocalTime getGioVao() { return gioVao; }
    public void setGioVao(LocalTime gioVao) { this.gioVao = gioVao; }
    public LocalTime getGioRa() { return gioRa; }
    public void setGioRa(LocalTime gioRa) { this.gioRa = gioRa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
```

### 2.2 Lớp DAO: `com.dao.ChamCongDAO`

Đảm nhiệm việc kết nối cơ sở dữ liệu qua JDBC, chuyển đổi giữa `ResultSet` và model `ChamCong`:

```java
package com.dao;

import com.config.DatabaseConnection;
import com.model.ChamCong;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChamCongDAO {

    /**
     * Thêm mới một bản ghi chấm công qua Stored Procedure dbo.sp_GhiNhanChamCong (dùng Connection đơn lẻ)
     */
    public boolean insertSingle(ChamCong cc) throws SQLException {
        String sql = "{call dbo.sp_GhiNhanChamCong(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, cc.getMaNV());
            if (cc.getNgayChamCong() != null) {
                cs.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            } else {
                cs.setNull(2, Types.DATE);
            }
            if (cc.getGioVao() != null) {
                cs.setTime(3, Time.valueOf(cc.getGioVao()));
            } else {
                cs.setNull(3, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                cs.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                cs.setNull(4, Types.TIME);
            }
            cs.setString(5, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            cs.setString(6, cc.getGhiChu());
            cs.registerOutParameter(7, Types.INTEGER);

            cs.execute();
            int generatedId = cs.getInt(7);
            cc.setMaChamCong(generatedId);
            return generatedId > 0;
        }
    }

    /**
     * Ghi nhận một dòng trong Transaction theo lô qua dbo.sp_GhiNhanChamCong (dùng Connection truyền từ Service)
     */
    public void insertInTransaction(Connection conn, ChamCong cc) throws SQLException {
        String sql = "{call dbo.sp_GhiNhanChamCong(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, cc.getMaNV());
            if (cc.getNgayChamCong() != null) {
                cs.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            } else {
                cs.setNull(2, Types.DATE);
            }
            if (cc.getGioVao() != null) {
                cs.setTime(3, Time.valueOf(cc.getGioVao()));
            } else {
                cs.setNull(3, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                cs.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                cs.setNull(4, Types.TIME);
            }
            cs.setString(5, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            cs.setString(6, cc.getGhiChu());
            cs.registerOutParameter(7, Types.INTEGER);

            cs.execute();
            int generatedId = cs.getInt(7);
            cc.setMaChamCong(generatedId);
        }
    }

    /**
     * Lấy danh sách chấm công theo tháng/năm của nhân viên
     */
    public List<ChamCong> findByNhanVienAndMonth(int maNV, int thang, int nam) throws SQLException {
        List<ChamCong> list = new ArrayList<>();
        String sql = "SELECT MaChamCong, MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu "
                   + "FROM CHAMCONG WHERE MaNV = ? AND MONTH(NgayChamCong) = ? AND YEAR(NgayChamCong) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChamCong cc = new ChamCong();
                    cc.setMaChamCong(rs.getInt("MaChamCong"));
                    cc.setMaNV(rs.getInt("MaNV"));
                    cc.setNgayChamCong(rs.getDate("NgayChamCong").toLocalDate());
                    cc.setGioVao(rs.getTime("GioVao").toLocalTime());
                    Time gioRa = rs.getTime("GioRa");
                    if (gioRa != null) cc.setGioRa(gioRa.toLocalTime());
                    cc.setTrangThai(rs.getString("TrangThai"));
                    cc.setGhiChu(rs.getString("GhiChu"));
                    list.add(cc);
                }
            }
        }
        return list;
    }
}
```

### 2.3 Lớp Service: `com.service.ChamCongService`

Đảm nhiệm kiểm tra nghiệp vụ tầng ứng dụng và điều phối Transaction nhập chấm công theo lô:

```java
package com.service;

import com.config.DatabaseConnection;
import com.dao.ChamCongDAO;
import com.model.ChamCong;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ChamCongService {
    private final ChamCongDAO chamCongDAO = new ChamCongDAO();

    /**
     * Validate nghiệp vụ cơ bản trước khi lưu
     */
    public void validateChamCong(ChamCong cc) throws Exception {
        if (cc == null) throw new IllegalArgumentException("Dữ liệu chấm công không được rỗng.");
        if (cc.getMaNV() <= 0) throw new IllegalArgumentException("Mã nhân viên không hợp lệ.");
        if (cc.getNgayChamCong() == null) throw new IllegalArgumentException("Ngày chấm công không được để trống.");
        if (cc.getNgayChamCong().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày chấm công không được vượt quá ngày hiện tại.");
        }
        if (cc.getGioVao() == null) throw new IllegalArgumentException("Giờ vào làm không được để trống.");
        if (cc.getGioRa() != null && !cc.getGioRa().isAfter(cc.getGioVao())) {
            throw new IllegalArgumentException("Giờ ra về phải lớn hơn giờ vào làm.");
        }
    }

    /**
     * Ghi nhận chấm công đơn lẻ
     */
    public boolean chamCongDonLe(ChamCong cc) throws Exception {
        validateChamCong(cc);
        return chamCongDAO.insertSingle(cc);
    }

    /**
     * Transaction: Nhập chấm công theo lô (Batch Processing)
     * Đảm bảo nguyên tắc Atomicity (All-or-Nothing)
     */
    public void nhapChamCongTheoLo(List<ChamCong> danhSach) throws Exception {
        if (danhSach == null || danhSach.isEmpty()) {
            throw new IllegalArgumentException("Danh sách chấm công nhập lô không được rỗng.");
        }

        // Bước 1: Validate nhanh toàn bộ danh sách ở tầng Java
        for (ChamCong cc : danhSach) {
            validateChamCong(cc);
        }

        // Bước 2: Thực thi trong một Database Transaction
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction
            try {
                for (ChamCong cc : danhSach) {
                    chamCongDAO.insertInTransaction(conn, cc);
                }
                conn.commit(); // Thành công toàn bộ
            } catch (SQLException ex) {
                conn.rollback(); // Lỗi bất kỳ dòng nào -> rollback toàn bộ
                throw new Exception("Lỗi khi nhập lô: " + ex.getMessage(), ex);
            }
        }
    }
}
```

### 2.4 Lớp Giao diện: `com.ui.chamcong.ChamCongPanel`

Giao diện đồ họa Swing hiển thị bảng dữ liệu, form nhập và phân quyền:
- **Thành phần chính:**
  - Bảng danh sách chấm công (`JTable`) kèm phân trang/cuộn (`JScrollPane`).
  - Bộ lọc: Chọn Tháng, Năm, Phòng ban hoặc tra cứu theo Mã nhân viên.
  - Form nhập thông tin: Mã NV, Ngày, Giờ vào, Giờ ra, Trạng thái, Ghi chú.
  - Các nút tác vụ: "Chấm công", "Nhập danh sách theo lô", "Làm mới".
- **Kiểm soát phân quyền theo `Session`:**
  - Kiểm tra `Session.getInstance().getVaiTro()`:
    - Nếu là `HR_Manager` hoặc `DB_Admin`: Kích hoạt đầy đủ các nút nhập dữ liệu và chấm công theo lô.
    - Nếu là `Payroll_Officer`: Vô hiệu hóa (`setEnabled(false)`) các nút thêm/sửa; chỉ cho phép xem và lọc số liệu.
    - Nếu là `Employee`: Menu chấm công bị ẩn hoặc chuyển sang chế độ cá nhân (chỉ xem bản thân).

---

## 3. Luồng dữ liệu và Sequence Diagrams

### 3.1 Luồng ghi nhận chấm công đơn lẻ

```text
User (HR_Manager)           ChamCongPanel           ChamCongService             ChamCongDAO               SQL Server
      │                           │                        │                         │                         │
      │── 1. Nhập form & nhấn ───►│                        │                         │                         │
      │      "Lưu chấm công"      │── 2. chamCongDonLe() ─►│                         │                         │
      │                           │                        │── 3. validate()         │                         │
      │                           │                        │── 4. insertSingle() ───►│                         │
      │                           │                        │                         │── 5. CallableStatement ─►│
      │                           │                        │                         │   (sp_GhiNhanChamCong)  │── 6. Check UQ/FK
      │                           │                        │                         │                         │── 7. Run Triggers
      │                           │                        │                         │◄── 8. Success / OutID ──│
      │                           │                        │◄── 9. Return true ──────│                         │
      │                           │◄── 10. Hoàn thành ─────│                         │                         │
      │◄── 11. JOptionPane ───────│                        │                         │                         │
      │       "Lưu thành công!"   │                        │                         │                         │
```

### 3.2 Luồng Transaction nhập chấm công theo lô (All-or-Nothing)

```text
User (HR_Manager)           ChamCongPanel           ChamCongService             ChamCongDAO               SQL Server
      │                           │                        │                         │                         │
      │── 1. Chọn file / lô ─────►│                        │                         │                         │
      │      nhấn "Nhập theo lô"  │── 2. nhapTheoLo(list) ─►                         │                         │
      │                           │                        │── 3. setAutoCommit(false)                         │
      │                           │                        │                                                   │
      │                           │                        │── 4. insertInTransaction (sp dòng 1) ────────────►│ (Hợp lệ)
      │                           │                        │── 5. insertInTransaction (sp dòng 2) ────────────►│ (Lỗi Trigger/UQ)
      │                           │                        │                                                   │   RAISERROR!
      │                           │                        │◄── 6. Catch SQLException ─────────────────────────│
      │                           │                        │── 7. conn.rollback() ────────────────────────────►│ (Hủy toàn bộ)
      │                           │◄── 8. Ném Exception ───│                                                   │
      │◄── 9. JOptionPane Lỗi ────│                        │                                                   │
      │      "Đã rollback lô!"    │                        │                                                   │
```

---

## 4. Ma trận tích hợp & Giao diện liên module (Dependency Map)

```text
                    ┌─────────────────────────┐
                    │    TV1 (Nhân sự)        │
                    │  - Bảng NHANVIEN        │
                    │  - fn_TinhSoNgayCong    │
                    └───────────┬─────────────┘
                                │ cung cấp mã & trạng thái NV
                                ▼
┌──────────────────┐    ┌───────────────────────────────────────────┐    ┌─────────────────────────┐
│  TV5 (Hệ thống)  ├───►│              TV2 (Chấm công)              ├───►│    TV4 (Tính lương)     │
│  - Session       │    │  - Bảng CHAMCONG & Index MaNV_Ngay        │    │  - sp_TinhBangLuong     │
│  - Phân quyền    │    │  - sp_GhiNhanChamCong                     │    │  - Lấy ngày công thực tế│
└──────────────────┘    │  - trg_ChamCong_KiemTraNhanVien           │    └─────────────────────────┘
                        │  - trg_ChamCong_KiemTraGio                │                 ▲
                        │  - vw_TongHopChamCongThang                │                 │
                        │  - Model, DAO, Service, Panel             │                 │
                        └─────────────────────┬─────────────────────┘                 │
                                              │ cung cấp số liệu công                 │
                                              ▼                                       │
                                  ┌─────────────────────────┐                         │
                                  │   TV3 (Phụ cấp/Khấu trừ)│─────────────────────────┘
                                  │  - Bảng PHUCAP, KHAUTRU │
                                  └─────────────────────────┘
```

### 4.1 Tích hợp với TV1 (Nhân sự Core)
- **Đầu vào:** TV2 phụ thuộc vào bảng `NHANVIEN` của TV1 (`MaNV`, `TrangThai`). Trigger `trg_ChamCong_KiemTraNhanVien` truy vấn trực tiếp `NHANVIEN.TrangThai`.
- **Đầu ra:** Bảng `CHAMCONG` là nguồn dữ liệu để hàm `fn_TinhSoNgayCong(MaNV, Thang, Nam)` của TV1 tính toán số ngày công.

### 4.2 Tích hợp với TV4 (Tính bảng lương)
- **Đầu ra:** Thủ tục tính lương `sp_TinhBangLuongThang` của TV4 cần số ngày công thực tế của từng nhân viên. Dữ liệu này được lấy qua hàm `fn_TinhSoNgayCong` hoặc truy vấn trực tiếp view `vw_TongHopChamCongThang` của TV2.

### 4.3 Tích hợp với TV5 (Kiến trúc & Bảo mật)
- Tuân thủ thiết kế Singleton của `com.session.Session` để kiểm soát ẩn/hiện menu trên `ChamCongPanel`.
- Kết nối CSDL thông qua lớp dùng chung `com.config.DatabaseConnection`.
- Phù hợp với ma trận GRANT/DENY của 4 Login SQL Server trong [TV5_Security_Design.md](TV5_Security_Design.md).

### 4.4 Tích hợp với TV3 (Phụ cấp & Khấu trừ)
- Toàn bộ các đối tượng chấm công (`CHAMCONG`, `sp_GhiNhanChamCong`, `trg_ChamCong_KiemTraGio`, `trg_ChamCong_KiemTraNhanVien`, `vw_TongHopChamCongThang`) đều thuộc quyền sở hữu của TV2.
- TV2 cung cấp dữ liệu số ngày công và giờ làm việc (qua bảng `CHAMCONG` và view `vw_TongHopChamCongThang`) để TV3 tham chiếu tính toán phụ cấp chuyên cần hoặc các khoản khấu trừ liên quan nếu nghiệp vụ yêu cầu.

---

## 5. Tình trạng các hợp đồng giao diện & Quyết định kiến trúc (Interface Contracts & Status)

Sau khi rà soát và hoàn thiện triển khai ở Tuần 2, tình trạng các giao diện tích hợp như sau:

### 5.1 Thủ tục `dbo.sp_GhiNhanChamCong` (Sở hữu TV2) — [ĐÃ HOÀN TẤT / RESOLVED]
- **Quyền sở hữu:** TV2 (Phạm Minh Quân) trực tiếp sở hữu, cài đặt và chịu trách nhiệm trong module Chấm công.
- Đã được cài đặt chính thức trong file kịch bản CSDL module Chấm công (`database/02_Module_ChamCong_TV2.sql`).
- Danh sách tham số chuẩn (7 tham số):
  - `@MaNV INT`
  - `@NgayChamCong DATE`
  - `@GioVao TIME(0)`
  - `@GioRa TIME(0) = NULL`
  - `@TrangThai NVARCHAR(20)`
  - `@GhiChu NVARCHAR(255) = NULL`
  - `@MaChamCong INT OUTPUT`
- Lớp `ChamCongDAO` gọi trực tiếp qua `CallableStatement` (`{call dbo.sp_GhiNhanChamCong(?, ?, ?, ?, ?, ?, ?)}`), nhận giá trị `MaChamCong` tự tăng qua tham số OUTPUT thứ 7.

### 5.2 Trigger `dbo.trg_ChamCong_KiemTraGio` (Sở hữu TV2) — [ĐÃ HOÀN TẤT / RESOLVED]
- **Quyền sở hữu:** TV2 (Phạm Minh Quân) trực tiếp sở hữu và quản lý trên bảng `CHAMCONG`.
- Đã được cài đặt chính thức trong `database/02_Module_ChamCong_TV2.sql` bằng cú pháp `CREATE OR ALTER TRIGGER dbo.trg_ChamCong_KiemTraGio ON dbo.CHAMCONG AFTER INSERT, UPDATE`.
- Khi vi phạm (`GioRa IS NOT NULL AND GioRa <= GioVao`), trigger thực hiện:
  - Thông báo lỗi tiếng Việt: `N'Lỗi: Giờ ra về phải lớn hơn giờ vào làm.'` (severity 16, state 1).
  - Tự động hủy giao dịch: `ROLLBACK TRANSACTION; RETURN;`.
- Tầng `ChamCongService` bắt ngoại lệ `SQLException` và ném thông điệp tường minh cho tầng UI hiển thị.

### 5.3 Cấu trúc dữ liệu View `dbo.vw_TongHopChamCongThang` (Sở hữu TV2) — [ĐÃ HOÀN TẤT / RESOLVED]
- **Quyền sở hữu:** TV2 (Phạm Minh Quân) trực tiếp sở hữu và quản lý.
- Đã được cài đặt chính thức trong `database/02_Module_ChamCong_TV2.sql`.
- Cung cấp 9 cột chuẩn hóa: `MaNV`, `HoTen`, `Thang`, `Nam`, `SoNgayDiLam`, `SoLanDiTre`, `SoLanVeSom`, `SoNgayVang`, `TongSoGioLam`.
- Đáp ứng đầy đủ yêu cầu tính lương của TV4 và báo cáo của TV5.

### 5.4 Định dạng nguồn dữ liệu đầu vào cho tính năng nhập theo lô — [CHƯA CHỐT / UNRESOLVED]
- Phương án đọc file: File văn bản định dạng `.csv` hay file bảng tính `.xlsx`, hoặc nhập trực tiếp từ giao diện bảng `JTable`.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Chờ nhóm thống nhất xem có bổ sung thư viện đọc Excel (như Apache POI - vượt quá phạm vi gọn nhẹ) hay chỉ dùng parser CSV tích hợp sẵn bằng thư viện chuẩn Java SE.

---

## 6. Checklist tích hợp Tuần 2 theo chuẩn dự án

Căn cứ theo [TV5_Integration_Checklist.md](TV5_Integration_Checklist.md), TV2 sẽ rà soát các hạng mục sau khi mở Pull Request ở Tuần 2:

- [ ] Lớp `ChamCong` tuân thủ đầy đủ chuẩn đóng gói (Encapsulation), không expose dữ liệu nhạy cảm.
- [ ] Lớp `ChamCongDAO` không sử dụng `Statement` ghép chuỗi; sử dụng `PreparedStatement` hoặc `CallableStatement`.
- [ ] Mọi tài nguyên `Connection`, `PreparedStatement`, `ResultSet` được đóng an toàn bằng khối `try-with-resources`.
- [ ] Lớp `ChamCongService` xử lý đầy đủ các trường hợp ngoại lệ từ CSDL và ném thông báo có ý nghĩa.
- [ ] Transaction nhập chấm công theo lô đảm bảo `setAutoCommit(false)` và `rollback()` khi gặp ngoại lệ.
- [ ] Giao diện `ChamCongPanel` ẩn/khóa các chức năng tương ứng với quyền hạn của từng Role (`HR_Manager`, `Payroll_Officer`, `Employee`).
- [ ] Kiểm thử độc lập trên SSMS và trên ứng dụng Java đạt kết quả đồng nhất.
