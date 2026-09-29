package com.model;

import java.math.BigDecimal;
import java.sql.Date;

public class PhuCapNhanVien {
    private int maPCNV;
    private int maNV;
    private String hoTen;
    private int thang;
    private int nam;
    private String tenPhuCap;
    private BigDecimal soTien;
    private Date ngayGhiNhan;
    private String ghiChu;

    public PhuCapNhanVien() {}

    public PhuCapNhanVien(int maNV, int thang, int nam, String tenPhuCap, BigDecimal soTien, Date ngayGhiNhan, String ghiChu) {
        this.maNV = maNV;
        this.thang = thang;
        this.nam = nam;
        this.tenPhuCap = tenPhuCap;
        this.soTien = soTien;
        this.ngayGhiNhan = ngayGhiNhan;
        this.ghiChu = ghiChu;
    }

    public int getMaPCNV() { return maPCNV; }
    public void setMaPCNV(int maPCNV) { this.maPCNV = maPCNV; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public String getTenPhuCap() { return tenPhuCap; }
    public void setTenPhuCap(String tenPhuCap) { this.tenPhuCap = tenPhuCap; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public Date getNgayGhiNhan() { return ngayGhiNhan; }
    public void setNgayGhiNhan(Date ngayGhiNhan) { this.ngayGhiNhan = ngayGhiNhan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}