package com.service;

import com.dao.NhanVienDAO;
import com.model.NhanVien;
import com.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class NhanVienService {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public List<NhanVien> layDanhSachNhanVien() throws SQLException {
        return nhanVienDAO.getAll();
    }

    public List<NhanVien> timKiemTheoTen(String hoTen) throws SQLException {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return nhanVienDAO.getAll();
        }
        return nhanVienDAO.searchByHoTen(hoTen.trim());
    }

    /**
     * Thêm nhân viên có kiểm tra nghiệp vụ và bọc Transaction tạo tài khoản
     */
    public int themNhanVien(NhanVien nv, boolean taoTaiKhoan, String tenDangNhap, String matKhau, String vaiTro) throws Exception {
        validateNhanVien(nv);

        String matKhauHash = null;
        if (taoTaiKhoan) {
            if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
                throw new Exception("Tên đăng nhập không được để trống khi chọn cấp tài khoản!");
            }
            if (matKhau == null || matKhau.trim().isEmpty()) {
                throw new Exception("Mật khẩu không được để trống khi chọn cấp tài khoản!");
            }
            matKhauHash = PasswordUtil.hashSHA256(matKhau.trim());
        }

        return nhanVienDAO.themNhanVien(nv, taoTaiKhoan, tenDangNhap != null ? tenDangNhap.trim() : null, matKhauHash, vaiTro);
    }

    public void capNhatNhanVien(NhanVien nv) throws Exception {
        validateNhanVien(nv);
        if (nv.getMaNV() <= 0) {
            throw new Exception("Mã nhân viên không hợp lệ!");
        }
        nhanVienDAO.update(nv);
    }

    public void xoaNhanVien(int maNV) throws SQLException {
        nhanVienDAO.delete(maNV);
    }

    private void validateNhanVien(NhanVien nv) throws Exception {
        if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            throw new Exception("Họ và tên không được để trống!");
        }
        if (nv.getNgaySinh() == null) {
            throw new Exception("Ngày sinh không được để trống!");
        }
        LocalDate ngayVao = nv.getNgayVaoLam() != null ? nv.getNgayVaoLam() : LocalDate.now();
        if (Period.between(nv.getNgaySinh(), ngayVao).getYears() < 18) {
            throw new Exception("Nhân viên phải từ 18 tuổi trở lên khi vào làm việc!");
        }
        if (nv.getCccd() == null || !nv.getCccd().matches("\\d{12}")) {
            throw new Exception("Căn cước công dân (CCCD) phải bao gồm đúng 12 chữ số!");
        }
        if (nv.getSoDienThoai() == null || !nv.getSoDienThoai().matches("0\\d{9}")) {
            throw new Exception("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        if (nv.getEmail() == null || !nv.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new Exception("Định dạng email không hợp lệ!");
        }
        if (nv.getLuongCoBan() == null || nv.getLuongCoBan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Lương cơ bản phải lớn hơn 0!");
        }
        if (nv.getMaPB() <= 0) {
            throw new Exception("Vui lòng chọn phòng ban hợp lệ!");
        }
        if (nv.getMaCV() <= 0) {
            throw new Exception("Vui lòng chọn chức vụ hợp lệ!");
        }
    }
}
