package com.model;

import java.math.BigDecimal;

/**
 * ChiTietBangLuong – Model ánh xạ bảng CHITIETBANGLUONG + dữ liệu mở rộng từ View.
 *
 * Hợp nhất (merged) giữa thiết kế của TV4 (Nguyễn Quang Vinh) và TV5 (Trần Đức Anh):
 * - Đầy đủ các trường tính toán từ bảng CHITIETBANGLUONG
 * - Bổ sung các trường hiển thị khi JOIN với NHANVIEN, PHONGBAN, CHUCVU và BANGLUONG
 */
public class ChiTietBangLuong {

    // Thuộc tính cơ bản từ bảng CHITIETBANGLUONG
    private int        maChiTiet;
    private int        maBangLuong;
    private int        maNV;
    private String     hoTen;
    private BigDecimal luongCoBan     = BigDecimal.ZERO;
    private int        ngayCongThucTe;
    private BigDecimal tienCong       = BigDecimal.ZERO;
    private BigDecimal tongPhuCap     = BigDecimal.ZERO;
    private BigDecimal tongKhauTru    = BigDecimal.ZERO;
    private BigDecimal thucNhan       = BigDecimal.ZERO;

    // Thuộc tính mở rộng từ View (JOIN)
    private int    thang;
    private int    nam;
    private int    ngayCongChuan;
    private String trangThaiBangLuong;
    private String tenPB;
    private String tenCV;

    public ChiTietBangLuong() {
    }

    // ─── Getters & Setters ───────────────────────────────────────────

    public int getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(int maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public int getMaBangLuong() {
        return maBangLuong;
    }

    public void setMaBangLuong(int maBangLuong) {
        this.maBangLuong = maBangLuong;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public BigDecimal getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(BigDecimal luongCoBan) {
        this.luongCoBan = luongCoBan != null ? luongCoBan : BigDecimal.ZERO;
    }

    public int getNgayCongThucTe() {
        return ngayCongThucTe;
    }

    public void setNgayCongThucTe(int ngayCongThucTe) {
        this.ngayCongThucTe = ngayCongThucTe;
    }

    public BigDecimal getTienCong() {
        return tienCong;
    }

    public void setTienCong(BigDecimal tienCong) {
        this.tienCong = tienCong != null ? tienCong : BigDecimal.ZERO;
    }

    public BigDecimal getTongPhuCap() {
        return tongPhuCap;
    }

    public void setTongPhuCap(BigDecimal tongPhuCap) {
        this.tongPhuCap = tongPhuCap != null ? tongPhuCap : BigDecimal.ZERO;
    }

    public BigDecimal getTongKhauTru() {
        return tongKhauTru;
    }

    public void setTongKhauTru(BigDecimal tongKhauTru) {
        this.tongKhauTru = tongKhauTru != null ? tongKhauTru : BigDecimal.ZERO;
    }

    public BigDecimal getThucNhan() {
        return thucNhan;
    }

    public void setThucNhan(BigDecimal thucNhan) {
        this.thucNhan = thucNhan != null ? thucNhan : BigDecimal.ZERO;
    }

    public int getThang() {
        return thang;
    }

    public void setThang(int thang) {
        this.thang = thang;
    }

    public int getNam() {
        return nam;
    }

    public void setNam(int nam) {
        this.nam = nam;
    }

    public int getNgayCongChuan() {
        return ngayCongChuan;
    }

    public void setNgayCongChuan(int ngayCongChuan) {
        this.ngayCongChuan = ngayCongChuan;
    }

    public String getTrangThaiBangLuong() {
        return trangThaiBangLuong;
    }

    public void setTrangThaiBangLuong(String trangThaiBangLuong) {
        this.trangThaiBangLuong = trangThaiBangLuong;
    }

    public String getTenPB() {
        return tenPB;
    }

    public void setTenPB(String tenPB) {
        this.tenPB = tenPB;
    }

    public String getTenCV() {
        return tenCV;
    }

    public void setTenCV(String tenCV) {
        this.tenCV = tenCV;
    }

    @Override
    public String toString() {
        return (hoTen != null ? hoTen : "NV " + maNV) + " – Thực nhận: " + thucNhan;
    }
}
