package com.model;

/**
 * Model ánh xạ dữ liệu từ View dbo.vw_TongHopChamCongThang (TV2 sở hữu)
 */
public class TongHopChamCong {
    private int maNV;
    private String hoTen;
    private int thang;
    private int nam;
    private int soNgayDiLam;
    private int soLanDiTre;
    private int soLanVeSom;
    private int soNgayVang;
    private double tongSoGioLam;

    public TongHopChamCong() {
    }

    public TongHopChamCong(int maNV, String hoTen, int thang, int nam,
                          int soNgayDiLam, int soLanDiTre, int soLanVeSom, int soNgayVang, double tongSoGioLam) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.thang = thang;
        this.nam = nam;
        this.soNgayDiLam = soNgayDiLam;
        this.soLanDiTre = soLanDiTre;
        this.soLanVeSom = soLanVeSom;
        this.soNgayVang = soNgayVang;
        this.tongSoGioLam = tongSoGioLam;
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

    public int getSoNgayDiLam() {
        return soNgayDiLam;
    }

    public void setSoNgayDiLam(int soNgayDiLam) {
        this.soNgayDiLam = soNgayDiLam;
    }

    public int getSoLanDiTre() {
        return soLanDiTre;
    }

    public void setSoLanDiTre(int soLanDiTre) {
        this.soLanDiTre = soLanDiTre;
    }

    public int getSoLanVeSom() {
        return soLanVeSom;
    }

    public void setSoLanVeSom(int soLanVeSom) {
        this.soLanVeSom = soLanVeSom;
    }

    public int getSoNgayVang() {
        return soNgayVang;
    }

    public void setSoNgayVang(int soNgayVang) {
        this.soNgayVang = soNgayVang;
    }

    public double getTongSoGioLam() {
        return tongSoGioLam;
    }

    public void setTongSoGioLam(double tongSoGioLam) {
        this.tongSoGioLam = tongSoGioLam;
    }

    @Override
    public String toString() {
        return "TongHopChamCong{" +
                "maNV=" + maNV +
                ", hoTen='" + hoTen + '\'' +
                ", thang=" + thang +
                ", nam=" + nam +
                ", soNgayDiLam=" + soNgayDiLam +
                ", soLanDiTre=" + soLanDiTre +
                ", soLanVeSom=" + soLanVeSom +
                ", soNgayVang=" + soNgayVang +
                ", tongSoGioLam=" + tongSoGioLam +
                '}';
    }
}
