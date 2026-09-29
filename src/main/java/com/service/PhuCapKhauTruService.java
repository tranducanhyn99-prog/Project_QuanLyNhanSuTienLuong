package com.service;

import com.dao.KhauTruDAO;
import com.dao.PhuCapDAO;
import com.model.KhauTruNhanVien;
import com.model.PhuCapNhanVien;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PhuCapKhauTruService {
    private final PhuCapDAO phuCapDAO;
    private final KhauTruDAO khauTruDAO;

    public PhuCapKhauTruService() {
        this.phuCapDAO = new PhuCapDAO();
        this.khauTruDAO = new KhauTruDAO();
    }

    public List<PhuCapNhanVien> getListPhuCap(int thang, int nam) throws SQLException {
        return phuCapDAO.getListByKy(thang, nam);
    }

    public void themPhuCap(PhuCapNhanVien pc) throws SQLException {
        if (pc.getSoTien() == null || pc.getSoTien().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền phụ cấp không được âm!");
        }
        if (pc.getThang() < 1 || pc.getThang() > 12 || pc.getNam() < 2020) {
            throw new IllegalArgumentException("Kỳ tháng/năm không hợp lệ!");
        }
        phuCapDAO.insert(pc);
    }

    public void xoaPhuCap(int maPCNV) throws SQLException {
        phuCapDAO.delete(maPCNV);
    }

    public List<KhauTruNhanVien> getListKhauTru(int thang, int nam) throws SQLException {
        return khauTruDAO.getListByKy(thang, nam);
    }

    public void themKhauTru(KhauTruNhanVien kt) throws SQLException {
        if (kt.getSoTien() == null || kt.getSoTien().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền khấu trừ không được âm!");
        }
        if (kt.getThang() < 1 || kt.getThang() > 12 || kt.getNam() < 2020) {
            throw new IllegalArgumentException("Kỳ tháng/năm không hợp lệ!");
        }
        khauTruDAO.insert(kt);
    }

    public void xoaKhauTru(int maKTNV) throws SQLException {
        khauTruDAO.delete(maKTNV);
    }

    public List<Map<String, Object>> getTongHopPhuCap(int thang, int nam) throws SQLException {
        return phuCapDAO.getTongHopPhuCap(thang, nam);
    }

    public BigDecimal getTongKhauTruNV(int maNV, int thang, int nam) throws SQLException {
        return khauTruDAO.getTongKhauTruNV(maNV, thang, nam);
    }

    // Nghiệp vụ transaction xóa kỳ lương chưa chốt
    public void xoaKyLuongChuaChot(int thang, int nam) throws SQLException {
        khauTruDAO.xoaKyLuongChuaChot(thang, nam);
    }
}