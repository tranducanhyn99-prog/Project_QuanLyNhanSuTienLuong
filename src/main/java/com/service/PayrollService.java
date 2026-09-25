package com.service;

import com.dao.BangLuongDAO;
import com.model.BangLuong;
import com.model.ChiTietBangLuong;

import java.sql.SQLException;
import java.util.List;

public class PayrollService {

    private final BangLuongDAO bangLuongDAO = new BangLuongDAO();

    public int tinhBangLuongThang(int thang, int nam, int ngayCongChuan) throws Exception {
        validateKyLuong(thang, nam, ngayCongChuan);
        return bangLuongDAO.tinhBangLuongThang(thang, nam, ngayCongChuan);
    }

    public List<BangLuong> layDanhSachBangLuong() throws SQLException {
        return bangLuongDAO.getAll();
    }

    public BangLuong timBangLuongTheoKy(int thang, int nam) throws Exception {
        validateThangNam(thang, nam);
        return bangLuongDAO.findByThangNam(thang, nam);
    }

    public BangLuong timBangLuongTheoMa(int maBangLuong) throws Exception {
        if (maBangLuong <= 0) {
            throw new Exception("Ma bang luong khong hop le.");
        }
        return bangLuongDAO.findById(maBangLuong);
    }

    public List<ChiTietBangLuong> layChiTietBangLuong(int maBangLuong) throws Exception {
        if (maBangLuong <= 0) {
            throw new Exception("Vui long chon mot ky luong hop le.");
        }
        return bangLuongDAO.getChiTietByBangLuong(maBangLuong);
    }

    private void validateKyLuong(int thang, int nam, int ngayCongChuan) throws Exception {
        validateThangNam(thang, nam);
        if (ngayCongChuan <= 0) {
            throw new Exception("So ngay cong chuan phai lon hon 0.");
        }
    }

    private void validateThangNam(int thang, int nam) throws Exception {
        if (thang < 1 || thang > 12) {
            throw new Exception("Thang phai nam trong khoang 1 den 12.");
        }
        if (nam < 2020 || nam > 2100) {
            throw new Exception("Nam phai nam trong khoang 2020 den 2100.");
        }
    }
}
