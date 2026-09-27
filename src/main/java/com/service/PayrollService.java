package com.service;

import com.dao.BangLuongDAO;
import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.session.Session;

import java.sql.SQLException;
import java.util.List;

/**
 * PayrollService – Tầng nghiệp vụ xử lý Tính lương và Chốt bảng lương.
 *
 * Hợp nhất (merged) giữa mã nguồn của TV4 (Nguyễn Quang Vinh) và TV5 (Trần Đức Anh):
 * - TV4: Nghiệp vụ tính lương, kiểm tra tháng/năm/ngày công chuẩn, tra cứu kỳ lương
 * - TV5: Nghiệp vụ chốt bảng lương (phân quyền vai trò), tra cứu phiếu lương cá nhân và báo cáo
 */
public class PayrollService {

    private final BangLuongDAO bangLuongDAO = new BangLuongDAO();

    // ═══════════════════════════════════════════════════════════════════
    //  NGHIỆP VỤ TÍNH LƯƠNG (TV4)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tính bảng lương cho một tháng/năm cụ thể (TV4)
     */
    public int tinhBangLuongThang(int thang, int nam, int ngayCongChuan) throws Exception {
        validateKyLuong(thang, nam, ngayCongChuan);

        // Kiểm tra quyền nếu đã đăng nhập
        Session session = Session.getInstance();
        if (session.isLoggedIn() && !session.hasRole("DB_Admin", "Payroll_Officer")) {
            throw new Exception("Bạn không có quyền tính bảng lương! Chỉ DB_Admin hoặc Payroll_Officer mới được thực hiện.");
        }

        try {
            return bangLuongDAO.tinhBangLuongThang(thang, nam, ngayCongChuan);
        } catch (SQLException ex) {
            throw new Exception("Lỗi khi tính bảng lương: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lấy toàn bộ danh sách kỳ lương
     */
    public List<BangLuong> layDanhSachBangLuong() throws SQLException {
        return bangLuongDAO.getAll();
    }

    /**
     * Alias method cho TV5 UI
     */
    public List<BangLuong> getDanhSachBangLuong() throws SQLException {
        return layDanhSachBangLuong();
    }

    /**
     * Tìm kỳ lương theo tháng và năm (TV4)
     */
    public BangLuong timBangLuongTheoKy(int thang, int nam) throws Exception {
        validateThangNam(thang, nam);
        return bangLuongDAO.findByThangNam(thang, nam);
    }

    /**
     * Alias method cho TV5 UI
     */
    public BangLuong timBangLuong(int thang, int nam) throws SQLException {
        return bangLuongDAO.findByThangNam(thang, nam);
    }

    /**
     * Tìm kỳ lương theo ID
     */
    public BangLuong timBangLuongTheoMa(int maBangLuong) throws Exception {
        if (maBangLuong <= 0) {
            throw new Exception("Mã bảng lương không hợp lệ.");
        }
        return bangLuongDAO.findById(maBangLuong);
    }

    /**
     * Lấy danh sách chi tiết bảng lương theo kỳ (TV4)
     */
    public List<ChiTietBangLuong> layChiTietBangLuong(int maBangLuong) throws Exception {
        if (maBangLuong <= 0) {
            throw new Exception("Vui lòng chọn một kỳ lương hợp lệ.");
        }
        return bangLuongDAO.getChiTietByBangLuong(maBangLuong);
    }

    /**
     * Alias method cho TV5 UI
     */
    public List<ChiTietBangLuong> getChiTietByBangLuong(int maBangLuong) throws SQLException {
        return bangLuongDAO.getChiTietByBangLuong(maBangLuong);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NGHIỆP VỤ CHỐT LƯƠNG & BÁO CÁO (TV5)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Chốt bảng lương theo MaBangLuong (TV5)
     * Kiểm tra quyền: Chỉ DB_Admin hoặc Payroll_Officer mới được chốt.
     */
    public void chotBangLuong(int maBangLuong) throws Exception {
        Session session = Session.getInstance();
        if (session.isLoggedIn() && !session.hasRole("DB_Admin", "Payroll_Officer")) {
            throw new Exception("Bạn không có quyền chốt bảng lương! Chỉ DB_Admin hoặc Payroll_Officer mới được thực hiện.");
        }

        if (maBangLuong <= 0) {
            throw new Exception("Mã bảng lương không hợp lệ!");
        }

        try {
            bangLuongDAO.chotBangLuong(maBangLuong);
        } catch (SQLException ex) {
            throw new Exception("Lỗi khi chốt bảng lương: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lấy chi tiết bảng lương theo tháng/năm (TV5)
     */
    public List<ChiTietBangLuong> getChiTietByThangNam(int thang, int nam) throws SQLException {
        return bangLuongDAO.getChiTietByThangNam(thang, nam);
    }

    /**
     * Lấy phiếu lương cá nhân của nhân viên đang đăng nhập (TV5)
     */
    public List<ChiTietBangLuong> getPhieuLuongCaNhan() throws Exception {
        Session session = Session.getInstance();
        int maNV = session.getMaNV();

        if (maNV <= 0) {
            throw new Exception("Tài khoản không gắn với nhân viên nào, không thể xem phiếu lương cá nhân!");
        }

        try {
            return bangLuongDAO.getPhieuLuongByMaNV(maNV);
        } catch (SQLException ex) {
            throw new Exception("Lỗi khi truy vấn phiếu lương: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lấy toàn bộ chi tiết lương cho báo cáo tổng hợp (TV5)
     */
    public List<ChiTietBangLuong> getBaoCaoTongHop() throws Exception {
        Session session = Session.getInstance();
        if (session.isLoggedIn() && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer")) {
            throw new Exception("Bạn không có quyền xem báo cáo tổng hợp!");
        }

        try {
            return bangLuongDAO.getAllChiTietView();
        } catch (SQLException ex) {
            throw new Exception("Lỗi khi truy vấn báo cáo: " + ex.getMessage(), ex);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  VALIDATION HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private void validateKyLuong(int thang, int nam, int ngayCongChuan) throws Exception {
        validateThangNam(thang, nam);
        if (ngayCongChuan <= 0 || ngayCongChuan > 31) {
            throw new Exception("Số ngày công chuẩn phải từ 1 đến 31 ngày.");
        }
    }

    private void validateThangNam(int thang, int nam) throws Exception {
        if (thang < 1 || thang > 12) {
            throw new Exception("Tháng phải nằm trong khoảng 1 đến 12.");
        }
        if (nam < 2020 || nam > 2100) {
            throw new Exception("Năm phải nằm trong khoảng 2020 đến 2100.");
        }
    }
}
