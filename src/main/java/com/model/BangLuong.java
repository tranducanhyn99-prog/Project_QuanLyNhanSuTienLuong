package com.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BangLuong – Model ánh xạ bảng BANGLUONG.
 *
 * Hợp nhất (merged) giữa thiết kế của TV4 (Nguyễn Quang Vinh) và TV5 (Trần Đức Anh):
 * - Lưu trữ thông tin kỳ lương: tháng, năm, ngày công chuẩn, trạng thái, ngày tạo, ngày chốt
 * - Thống kê tổng hợp: số nhân viên, tổng tiền công, tổng phụ cấp, tổng khấu trừ, tổng thực nhận
 * - Các phương thức tiện ích: isDaChot(), isChuaChot(), getDisplayLabel()
 */
public class BangLuong {

    private int           maBangLuong;
    private int           thang;
    private int           nam;
    private int           ngayCongChuan = 26;
    private String        trangThai     = "CHUA_CHOT";
    private LocalDateTime ngayTao;
    private LocalDateTime ngayChot;

    // Các trường thống kê tổng hợp (tính toán từ CHITIETBANGLUONG)
    private int        soNhanVien;
    private BigDecimal tongTienCong = BigDecimal.ZERO;
    private BigDecimal tongPhuCap   = BigDecimal.ZERO;
    private BigDecimal tongKhauTru  = BigDecimal.ZERO;
    private BigDecimal tongThucNhan = BigDecimal.ZERO;

    public BangLuong() {
    }

    public BangLuong(int thang, int nam, int ngayCongChuan) {
        this.thang = thang;
        this.nam = nam;
        this.ngayCongChuan = ngayCongChuan;
        this.trangThai = "CHUA_CHOT";
    }

    // ─── Getters & Setters ───────────────────────────────────────────

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
        this.tongTienCong = tongTienCong != null ? tongTienCong : BigDecimal.ZERO;
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

    public BigDecimal getTongThucNhan() {
        return tongThucNhan;
    }

    public void setTongThucNhan(BigDecimal tongThucNhan) {
        this.tongThucNhan = tongThucNhan != null ? tongThucNhan : BigDecimal.ZERO;
    }

    // ─── Utility ─────────────────────────────────────────────────────

    public boolean isDaChot() {
        return "DA_CHOT".equals(trangThai);
    }

    public boolean isChuaChot() {
        return "CHUA_CHOT".equals(trangThai);
    }

    public String getDisplayLabel() {
        String tt = isDaChot() ? "Đã chốt" : "Chưa chốt";
        return "Tháng " + thang + "/" + nam + " – " + tt;
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
