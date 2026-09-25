package com.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BangLuong {
    private int maBangLuong;
    private int thang;
    private int nam;
    private int ngayCongChuan;
    private String trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayChot;

    private int soNhanVien;
    private BigDecimal tongTienCong = BigDecimal.ZERO;
    private BigDecimal tongPhuCap = BigDecimal.ZERO;
    private BigDecimal tongKhauTru = BigDecimal.ZERO;
    private BigDecimal tongThucNhan = BigDecimal.ZERO;

    public int getMaBangLuong() {
        return maBangLuong;
    }

    public void setMaBangLuong(int maBangLuong) {
        this.maBangLuong = maBangLuong;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayChot() {
        return ngayChot;
    }

    public void setNgayChot(LocalDateTime ngayChot) {
        this.ngayChot = ngayChot;
    }

    public int getSoNhanVien() {
        return soNhanVien;
    }

    public void setSoNhanVien(int soNhanVien) {
        this.soNhanVien = soNhanVien;
    }

    public BigDecimal getTongTienCong() {
        return tongTienCong;
    }

    public void setTongTienCong(BigDecimal tongTienCong) {
        this.tongTienCong = tongTienCong;
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

    public BigDecimal getTongThucNhan() {
        return tongThucNhan;
    }

    public void setTongThucNhan(BigDecimal tongThucNhan) {
        this.tongThucNhan = tongThucNhan;
    }
}
