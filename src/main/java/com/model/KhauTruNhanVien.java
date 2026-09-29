package com.model;

import java.math.BigDecimal;
import java.sql.Date;

public class KhauTruNhanVien {
    private int maKTNV;
    private int maNV;
    private String hoTen;
    private int thang;
    private int nam;
    private String tenKhauTru;
    private BigDecimal soTien;
    private Date ngayGhiNhan;
    private String lyDo;

    public KhauTruNhanVien() {}

    public KhauTruNhanVien(int maNV, int thang, int nam, String tenKhauTru, BigDecimal soTien, Date ngayGhiNhan, String lyDo) {
        this.maNV = maNV;
        this.thang = thang;
        this.nam = nam;
        this.tenKhauTru = tenKhauTru;
        this.soTien = soTien;
        this.ngayGhiNhan = ngayGhiNhan;
        this.lyDo = lyDo;
    }

    public int getMaKTNV() { return maKTNV; }
    public void setMaKTNV(int maKTNV) { this.maKTNV = maKTNV; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public String getTenKhauTru() { return tenKhauTru; }
    public void setTenKhauTru(String tenKhauTru) { this.tenKhauTru = tenKhauTru; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public Date getNgayGhiNhan() { return ngayGhiNhan; }
    public void setNgayGhiNhan(Date ngayGhiNhan) { this.ngayGhiNhan = ngayGhiNhan; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
}