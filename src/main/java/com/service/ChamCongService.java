package com.service;

import com.config.DatabaseConnection;
import com.dao.ChamCongDAO;
import com.model.ChamCong;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ChamCongService {

    private final ChamCongDAO chamCongDAO;

    public ChamCongService() {
        this.chamCongDAO = new ChamCongDAO();
    }

    public ChamCongService(ChamCongDAO chamCongDAO) {
        this.chamCongDAO = chamCongDAO;
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu chấm công trước khi lưu.
     */
    public void validateChamCong(ChamCong cc) throws Exception {
        if (cc == null) {
            throw new IllegalArgumentException("Dữ liệu chấm công không được để trống.");
        }
        if (cc.getMaNV() <= 0) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ.");
        }
        if (cc.getNgayChamCong() == null) {
            throw new IllegalArgumentException("Ngày chấm công không được để trống.");
        }
        if (cc.getNgayChamCong().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày chấm công không được vượt quá ngày hiện tại.");
        }
        if (cc.getGioVao() == null) {
            throw new IllegalArgumentException("Giờ vào làm không được để trống.");
        }
        if (cc.getGioRa() != null && !cc.getGioRa().isAfter(cc.getGioVao())) {
            throw new IllegalArgumentException("Giờ ra về phải lớn hơn giờ vào làm.");
        }
        String trangThai = cc.getTrangThai();
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            if (!"CO_MAT".equals(trangThai) && !"DI_TRE".equals(trangThai)
                    && !"VE_SOM".equals(trangThai) && !"VANG".equals(trangThai)) {
                throw new IllegalArgumentException("Trạng thái chấm công không hợp lệ: " + trangThai);
            }
        }
    }

    /**
     * Ghi nhận chấm công cho một nhân viên đơn lẻ.
     */
    public boolean chamCongDonLe(ChamCong cc) throws Exception {
        validateChamCong(cc);
        return chamCongDAO.insertSingle(cc);
    }

    /**
     * Nhập danh sách chấm công theo lô trong một Transaction đảm bảo All-or-Nothing.
     */
    public void nhapChamCongTheoLo(List<ChamCong> danhSach) throws Exception {
        if (danhSach == null || danhSach.isEmpty()) {
            throw new IllegalArgumentException("Danh sách chấm công nhập lô không được rỗng.");
        }

        // Bước 1: Validate toàn bộ danh sách ở tầng Service
        for (ChamCong cc : danhSach) {
            validateChamCong(cc);
        }

        // Bước 2: Thực thi trong một Database Transaction
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (ChamCong cc : danhSach) {
                    chamCongDAO.insertInTransaction(conn, cc);
                }
                conn.commit();
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                throw new Exception("Lỗi khi nhập lô chấm công: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Lấy danh sách chấm công của nhân viên theo tháng và năm.
     */
    public List<ChamCong> layChamCongTheoThang(int maNV, int thang, int nam) throws Exception {
        if (maNV <= 0) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ.");
        }
        if (thang < 1 || thang > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12.");
        }
        if (nam <= 0) {
            throw new IllegalArgumentException("Năm không hợp lệ.");
        }
        return chamCongDAO.findByNhanVienAndMonth(maNV, thang, nam);
    }

    /**
     * Phương thức định danh tương thích DAO để tra cứu theo tháng và năm.
     */
    public List<ChamCong> findByNhanVienAndMonth(int maNV, int thang, int nam) throws Exception {
        return layChamCongTheoThang(maNV, thang, nam);
    }
}
