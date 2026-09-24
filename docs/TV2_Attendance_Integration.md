# TV2 – Thiết kế Tích hợp Module Chấm Công
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Phạm Minh Quân (TV2, MSSV 24110311)<br>
**Ngày:** 24/09/2026<br>
**Phiên bản:** 1.0 (Tuần 1)

---

## 1. Tổng quan tích hợp kiến trúc 4 tầng

Tuân thủ kiến trúc phân lớp chuẩn của dự án được quy định tại [TV5_Architecture.md](TV5_Architecture.md), Module Chấm công được cấu trúc xuyên suốt qua 4 tầng:

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
                               │  JDBC Connection (PreparedStatement/CallableStatement)
┌──────────────────────────────▼───────────────────────────────┐
│            DATABASE LAYER (Microsoft SQL Server)             │
│   Bảng: CHAMCONG                                             │
│   Trigger: trg_ChamCong_KiemTraNhanVien                      │
│   View: vw_TongHopChamCongThang                              │
│   Index: IX_CHAMCONG_MaNV_Ngay                               │
│   SP / Trigger liên quan: sp_GhiNhanChamCong, ...            │
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
     * Thêm mới một bản ghi chấm công (dùng Connection đơn lẻ)
     */
    public boolean insertSingle(ChamCong cc) throws SQLException {
        String sql = "INSERT INTO CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cc.getMaNV());
            ps.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            ps.setTime(3, Time.valueOf(cc.getGioVao()));
            if (cc.getGioRa() != null) {
                ps.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                ps.setNull(4, Types.TIME);
            }
            ps.setString(5, cc.getTrangThai());
            ps.setString(6, cc.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Ghi nhận một dòng trong Transaction theo lô (dùng Connection truyền từ Service)
     */
    public void insertInTransaction(Connection conn, ChamCong cc) throws SQLException {
        String sql = "INSERT INTO CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cc.getMaNV());
            ps.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            ps.setTime(3, Time.valueOf(cc.getGioVao()));
            if (cc.getGioRa() != null) {
                ps.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                ps.setNull(4, Types.TIME);
            }
            ps.setString(5, cc.getTrangThai());
            ps.setString(6, cc.getGhiChu());
            ps.executeUpdate();
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
      │                           │                        │                         │── 5. INSERT query ─────►│
      │                           │                        │                         │                         │── 6. Check UQ/FK
      │                           │                        │                         │                         │── 7. Run Triggers
      │                           │                        │                         │◄── 8. Success / Rows ───│
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
      │                           │                        │── 4. insertInTransaction(Dòng 1) ────────────────►│ (Hợp lệ)
      │                           │                        │── 5. insertInTransaction(Dòng 2) ────────────────►│ (Lỗi Trigger/UQ)
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
┌──────────────────┐    ┌─────────────────────────┐    ┌─────────────────────────┐
│  TV5 (Hệ thống)  ├───►│   TV2 (Chấm công)       ├───►│    TV4 (Tính lương)     │
│  - Session       │    │  - Bảng CHAMCONG        │    │  - sp_TinhBangLuong     │
│  - Phân quyền    │    │  - vw_TongHopChamCong   │    │  - Lấy ngày công thực tế│
└──────────────────┘    └───────────┬─────────────┘    └─────────────────────────┘
                                    │
                                    ▼ (điểm giao thoa)
                        ┌─────────────────────────┐
                        │   TV3 (Phụ cấp/Khấu trừ)│
                        │  - sp_GhiNhanChamCong ? │
                        │  - trg_KiemTraGio ?     │
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

---

## 5. Các quyết định giao diện & hợp đồng chưa chốt (Unresolved Contracts)

Để tránh giả định sai lệch kiến trúc hoặc tranh chấp trách nhiệm giữa các thành viên, các vấn đề kỹ thuật sau được ghi nhận rõ là **CHƯA ĐƯỢC CHỐT (UNRESOLVED)**:

### 5.1 Hợp đồng thực thi thủ tục `sp_GhiNhanChamCong` (TV2 vs TV3) — [CHƯA CHỐT / UNRESOLVED]
- Trong tài liệu [TV5_Architecture.md](TV5_Architecture.md), `ChamCongDAO` dự kiến gọi `sp_GhiNhanChamCong`.
- Tuy nhiên, trong [Ke_hoach_phan_cong_Project_DBMS_Nhom06.md](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), đối tượng này lại thuộc sở hữu của TV3.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Chưa có thống nhất về:
  - Tên và danh sách tham số chuẩn: `@MaNV INT, @Ngay DATE, @GioVao TIME, @GioRa TIME, @TrangThai NVARCHAR(20)`.
  - Liệu DAO của TV2 có trực tiếp gọi SP này hay dùng câu lệnh `PreparedStatement` độc lập nếu TV3 chậm tiến độ.

### 5.2 Hợp đồng thông báo ngoại lệ của Trigger `trg_ChamCong_KiemTraGio` (TV3) — [CHƯA CHỐT / UNRESOLVED]
- Trigger kiểm tra giờ ra lớn hơn giờ vào hiện được phân bổ cho TV3 quản lý.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Cần thống nhất mã lỗi (`Error Number`) hoặc chuỗi thông báo từ `RAISERROR` (ví dụ: `N'Lỗi: Giờ ra về phải lớn hơn giờ vào làm.'`) để `ChamCongService` bắt chính xác và không hiển thị chuỗi lỗi hệ thống thô cho người dùng.

### 5.3 Định dạng nguồn dữ liệu đầu vào cho tính năng nhập theo lô — [CHƯA CHỐT / UNRESOLVED]
- Phương án đọc file: File văn bản định dạng `.csv` hay file bảng tính `.xlsx`, hoặc nhập trực tiếp từ giao diện bảng `JTable`.
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Chờ nhóm thống nhất xem có bổ sung thư viện đọc Excel (như Apache POI - vượt quá phạm vi gọn nhẹ) hay chỉ dùng parser CSV tích hợp sẵn bằng thư viện chuẩn Java.

### 5.4 Hợp đồng cấu trúc dữ liệu View `vw_TongHopChamCongThang` đối với TV4 — [CHƯA CHỐT / UNRESOLVED]
- Xác định quy ước: Nếu nhân viên chỉ check-in (`GioRa IS NULL`), bản ghi đó có được tính là 1 ngày công không hay tính là 0.5 ngày công?
- **Tình trạng:** **CHƯA CHỐT (UNRESOLVED)**. Cần TV1 và TV4 phản hồi để hoàn thiện công thức trong View.

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
