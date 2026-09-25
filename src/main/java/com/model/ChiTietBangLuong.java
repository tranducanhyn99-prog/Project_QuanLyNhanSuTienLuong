package com.model;

import java.math.BigDecimal;

public class ChiTietBangLuong {
    private int maChiTiet;
    private int maBangLuong;
    private int maNV;
    private String hoTen;
    private BigDecimal luongCoBan = BigDecimal.ZERO;
    private int ngayCongThucTe;
    private BigDecimal tienCong = BigDecimal.ZERO;
    private BigDecimal tongPhuCap = BigDecimal.ZERO;
    private BigDecimal tongKhauTru = BigDecimal.ZERO;
    private BigDecimal thucNhan = BigDecimal.ZERO;

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
        this.luongCoBan = luongCoBan;
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
        this.tienCong = tienCong;
    }

    public BigDecimal getTongPhuCap() {
        return tongPhuCap;
    }

    public void setTongPhuCap(BigDecimal tongPhuCap) {
        this.tongPhuCap = tongPhuCap;
    }

    public BigDecimal getTongKhauTru() {
        return tongKhauTru;
    }

    public void setTongKhauTru(BigDecimal tongKhauTru) {
        this.tongKhauTru = tongKhauTru;
    }

    public BigDecimal getThucNhan() {
        return thucNhan;
    }

    public void setThucNhan(BigDecimal thucNhan) {
        this.thucNhan = thucNhan;
    }
}

