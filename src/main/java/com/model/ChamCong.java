package com.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class ChamCong {
    private int maChamCong;
    private int maNV;
    private LocalDate ngayChamCong;
    private LocalTime gioVao;
    private LocalTime gioRa;
    private String trangThai = "CO_MAT";
    private String ghiChu;

    public ChamCong() {
        this.trangThai = "CO_MAT";
    }

    public ChamCong(int maNV, LocalDate ngayChamCong, LocalTime gioVao, LocalTime gioRa, String trangThai, String ghiChu) {
        this.maNV = maNV;
        this.ngayChamCong = ngayChamCong;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
        this.trangThai = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : "CO_MAT";
        this.ghiChu = ghiChu;
    }

    public ChamCong(int maChamCong, int maNV, LocalDate ngayChamCong, LocalTime gioVao, LocalTime gioRa, String trangThai, String ghiChu) {
        this.maChamCong = maChamCong;
        this.maNV = maNV;
        this.ngayChamCong = ngayChamCong;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
        this.trangThai = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : "CO_MAT";
        this.ghiChu = ghiChu;
    }

    public int getMaChamCong() {
        return maChamCong;
    }

    public void setMaChamCong(int maChamCong) {
        this.maChamCong = maChamCong;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public LocalDate getNgayChamCong() {
        return ngayChamCong;
    }

    public void setNgayChamCong(LocalDate ngayChamCong) {
        this.ngayChamCong = ngayChamCong;
    }

    public LocalTime getGioVao() {
        return gioVao;
    }

    public void setGioVao(LocalTime gioVao) {
        this.gioVao = gioVao;
    }

    public LocalTime getGioRa() {
        return gioRa;
    }

    public void setGioRa(LocalTime gioRa) {
        this.gioRa = gioRa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : "CO_MAT";
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "ChamCong{" +
                "maChamCong=" + maChamCong +
                ", maNV=" + maNV +
                ", ngayChamCong=" + ngayChamCong +
                ", gioVao=" + gioVao +
                ", gioRa=" + gioRa +
                ", trangThai='" + trangThai + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
