package com.model;

public class PhongBan {
    private int maPB;
    private String tenPB;
    private String soDienThoai;
    private String trangThai; // "HOAT_DONG", "NGUNG_HOAT_DONG"

    public PhongBan() {}

    public PhongBan(int maPB, String tenPB, String soDienThoai, String trangThai) {
        this.maPB = maPB;
        this.tenPB = tenPB;
        this.soDienThoai = soDienThoai;
        this.trangThai = trangThai;
    }

    public int getMaPB() {
        return maPB;
    }

    public void setMaPB(int maPB) {
        this.maPB = maPB;
    }

    public String getTenPB() {
        return tenPB;
    }

    public void setTenPB(String tenPB) {
        this.tenPB = tenPB;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return tenPB;
    }
}
