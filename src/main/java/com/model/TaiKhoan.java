package com.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TaiKhoan – Model ánh xạ bảng TAIKHOAN.
 *
 * Lưu trữ thông tin đăng nhập, vai trò và trạng thái tài khoản.
 * MatKhau luôn là chuỗi SHA-256 hex (64 ký tự), không bao giờ lưu plaintext.
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class TaiKhoan {

    private int           maTK;
    private int           maNV;           // -1 hoặc 0 nếu là DB_Admin hệ thống
    private String        tenDangNhap;
    private String        matKhau;        // SHA-256 hex string (64 ký tự)
    private String        vaiTro;         // "DB_Admin" | "HR_Manager" | "Payroll_Officer" | "Employee"
    private String        trangThai;      // "HOAT_DONG" | "KHOA"
    private LocalDate     ngayTao;
    private LocalDateTime ngaySuaCuoi;

    // Thuộc tính hiển thị (join từ NHANVIEN)
    private String hoTenNV;

    // ─── Constructors ────────────────────────────────────────────────

    public TaiKhoan() {
        this.trangThai = "HOAT_DONG";
    }

    public TaiKhoan(int maTK, int maNV, String tenDangNhap, String vaiTro, String trangThai) {
        this.maTK        = maTK;
        this.maNV        = maNV;
        this.tenDangNhap = tenDangNhap;
        this.vaiTro      = vaiTro;
        this.trangThai   = trangThai;
    }

    // ─── Getters & Setters ───────────────────────────────────────────

    public int getMaTK() {
        return maTK;
    }

    public void setMaTK(int maTK) {
        this.maTK = maTK;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgaySuaCuoi() {
        return ngaySuaCuoi;
    }

    public void setNgaySuaCuoi(LocalDateTime ngaySuaCuoi) {
        this.ngaySuaCuoi = ngaySuaCuoi;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String hoTenNV) {
        this.hoTenNV = hoTenNV;
    }

    // ─── Utility ─────────────────────────────────────────────────────

    public boolean isActive() {
        return "HOAT_DONG".equals(trangThai);
    }

    public boolean isLocked() {
        return "KHOA".equals(trangThai);
    }

    @Override
    public String toString() {
        return tenDangNhap + " (" + vaiTro + ")";
    }
}
