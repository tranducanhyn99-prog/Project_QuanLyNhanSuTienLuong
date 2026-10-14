package com.service;

import com.session.Session;
import com.dao.ChucVuDAO;
import com.dao.PhongBanDAO;
import com.model.ChucVu;
import com.model.PhongBan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class DanhMucService {

    private final PhongBanDAO phongBanDAO = new PhongBanDAO();
    private final ChucVuDAO chucVuDAO = new ChucVuDAO();

    // --- PHÒNG BAN ---
    public List<PhongBan> layTatCaPhongBan() throws SQLException {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager", "Payroll_Officer");
        return phongBanDAO.getAll();
    }

    public List<PhongBan> layPhongBanDangHoatDong() throws SQLException {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager", "Payroll_Officer");
        return phongBanDAO.getActive();
    }

    public void themPhongBan(String tenPB, String soDienThoai) throws Exception {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager");
        if (tenPB == null || tenPB.trim().isEmpty()) {
            throw new Exception("Tên phòng ban không được để trống!");
        }
        PhongBan pb = new PhongBan(0, tenPB.trim(), normalizePhone(soDienThoai), "HOAT_DONG");
        phongBanDAO.insert(pb);
    }

    public void capNhatPhongBan(int maPB, String tenPB, String soDienThoai, String trangThai) throws Exception {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager");
        if (tenPB == null || tenPB.trim().isEmpty()) {
            throw new Exception("Tên phòng ban không được để trống!");
        }
        PhongBan pb = new PhongBan(maPB, tenPB.trim(), normalizePhone(soDienThoai), trangThai);
        phongBanDAO.update(pb);
    }

    public void xoaPhongBan(int maPB) throws SQLException {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager");
        phongBanDAO.delete(maPB);
    }

    private static String normalizePhone(String phone) throws Exception {
        if (phone == null || phone.trim().isEmpty()) return null;
        String value = phone.trim();
        if (!value.matches("[0-9]{10,15}")) {
            throw new Exception("SĐT phòng ban: 10–15 chữ số hoặc để trống.");
        }
        return value;
    }

    // --- CHỨC VỤ ---
    public List<ChucVu> layTatCaChucVu() throws SQLException {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager", "Payroll_Officer");
        return chucVuDAO.getAll();
    }

    public void themChucVu(String tenCV, BigDecimal phuCap) throws Exception {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager");
        if (tenCV == null || tenCV.trim().isEmpty()) {
            throw new Exception("Tên chức vụ không được để trống!");
        }
        if (phuCap == null || phuCap.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Phụ cấp chức vụ không được âm!");
        }
        ChucVu cv = new ChucVu(0, tenCV.trim(), phuCap);
        chucVuDAO.insert(cv);
    }

    public void capNhatChucVu(int maCV, String tenCV, BigDecimal phuCap) throws Exception {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager");
        if (tenCV == null || tenCV.trim().isEmpty()) {
            throw new Exception("Tên chức vụ không được để trống!");
        }
        if (phuCap == null || phuCap.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Phụ cấp chức vụ không được âm!");
        }
        ChucVu cv = new ChucVu(maCV, tenCV.trim(), phuCap);
        chucVuDAO.update(cv);
    }

    public void xoaChucVu(int maCV) throws SQLException {
        Session.getInstance().requireRoles("DB_Admin", "HR_Manager");
        chucVuDAO.delete(maCV);
    }
}
