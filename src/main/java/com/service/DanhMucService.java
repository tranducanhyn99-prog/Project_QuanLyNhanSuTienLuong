package com.service;

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
        return phongBanDAO.getAll();
    }

    public List<PhongBan> layPhongBanDangHoatDong() throws SQLException {
        return phongBanDAO.getActive();
    }

    public void themPhongBan(String tenPB, String soDienThoai) throws Exception {
        if (tenPB == null || tenPB.trim().isEmpty()) {
            throw new Exception("Tên phòng ban không được để trống!");
        }
        PhongBan pb = new PhongBan(0, tenPB.trim(), soDienThoai != null ? soDienThoai.trim() : null, "HOAT_DONG");
        phongBanDAO.insert(pb);
    }

    public void capNhatPhongBan(int maPB, String tenPB, String soDienThoai, String trangThai) throws Exception {
        if (tenPB == null || tenPB.trim().isEmpty()) {
            throw new Exception("Tên phòng ban không được để trống!");
        }
        PhongBan pb = new PhongBan(maPB, tenPB.trim(), soDienThoai != null ? soDienThoai.trim() : null, trangThai);
        phongBanDAO.update(pb);
    }

    public void xoaPhongBan(int maPB) throws SQLException {
        phongBanDAO.delete(maPB);
    }

    // --- CHỨC VỤ ---
    public List<ChucVu> layTatCaChucVu() throws SQLException {
        return chucVuDAO.getAll();
    }

    public void themChucVu(String tenCV, BigDecimal phuCap) throws Exception {
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
        chucVuDAO.delete(maCV);
    }
}
