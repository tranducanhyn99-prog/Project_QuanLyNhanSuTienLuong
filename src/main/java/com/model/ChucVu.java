package com.model;

import java.math.BigDecimal;

public class ChucVu {
    private int maCV;
    private String tenCV;
    private BigDecimal phuCapChucVu;

    public ChucVu() {
        this.phuCapChucVu = BigDecimal.ZERO;
    }

    public ChucVu(int maCV, String tenCV, BigDecimal phuCapChucVu) {
        this.maCV = maCV;
        this.tenCV = tenCV;
        this.phuCapChucVu = phuCapChucVu;
    }

    public int getMaCV() {
        return maCV;
    }

    public void setMaCV(int maCV) {
        this.maCV = maCV;
    }

    public String getTenCV() {
        return tenCV;
    }

    public void setTenCV(String tenCV) {
        this.tenCV = tenCV;
    }

    public BigDecimal getPhuCapChucVu() {
        return phuCapChucVu;
    }

    public void setPhuCapChucVu(BigDecimal phuCapChucVu) {
        this.phuCapChucVu = phuCapChucVu;
    }

    @Override
    public String toString() {
        return tenCV;
    }
}
