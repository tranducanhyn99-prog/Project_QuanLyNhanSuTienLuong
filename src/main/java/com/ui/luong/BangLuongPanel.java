package com.ui.luong;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BangLuongPanel extends JPanel {

    private final PayrollService payrollService = new PayrollService();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JSpinner spnThang;
    private JSpinner spnNam;
    private JSpinner spnNgayCongChuan;
    private JTable tblBangLuong;
    private JTable tblChiTiet;
    private DefaultTableModel modelBangLuong;
    private DefaultTableModel modelChiTiet;

    public BangLuongPanel() {
        initComponents();
        loadBangLuong();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate today = LocalDate.now();
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTop.setBorder(BorderFactory.createTitledBorder("Tính bảng lương tháng"));

        spnThang = new JSpinner(new SpinnerNumberModel(today.getMonthValue(), 1, 12, 1));
        spnNam = new JSpinner(new SpinnerNumberModel(today.getYear(), 2020, 2100, 1));
        spnNgayCongChuan = new JSpinner(new SpinnerNumberModel(26, 1, 31, 1));

        JButton btnTinhLuong = new JButton("Tính lương");
        JButton btnXemKy = new JButton("Xem kỳ");
        JButton btnTaiLai = new JButton("Tải lại");

        pnlTop.add(new JLabel("Tháng:"));
        pnlTop.add(spnThang);
        pnlTop.add(new JLabel("Năm:"));
        pnlTop.add(spnNam);
        pnlTop.add(new JLabel("Ngày công chuẩn:"));
        pnlTop.add(spnNgayCongChuan);
        pnlTop.add(btnTinhLuong);
        pnlTop.add(btnXemKy);
        pnlTop.add(btnTaiLai);
        add(pnlTop, BorderLayout.NORTH);

        modelBangLuong = new DefaultTableModel(new String[]{
            "Mã BL", "Tháng", "Năm", "NCC", "Trạng thái", "Số NV", "Tổng thực nhận", "Ngày tạo", "Ngày chốt"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBangLuong = new JTable(modelBangLuong);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modelChiTiet = new DefaultTableModel(new String[]{
            "Mã CT", "Mã NV", "Họ tên", "Lương CB", "Ngày công", "Tiền công", "Phụ cấp", "Khấu trừ", "Thực nhận"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);

        JScrollPane scrollBangLuong = new JScrollPane(tblBangLuong);
        scrollBangLuong.setBorder(BorderFactory.createTitledBorder("Danh sách kỳ lương"));

        JScrollPane scrollChiTiet = new JScrollPane(tblChiTiet);
        scrollChiTiet.setBorder(BorderFactory.createTitledBorder("Chi tiết bảng lương"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBangLuong, scrollChiTiet);
        splitPane.setResizeWeight(0.45);
        add(splitPane, BorderLayout.CENTER);

        btnTinhLuong.addActionListener(e -> xuLyTinhLuong());
        btnXemKy.addActionListener(e -> xuLyXemKy());
        btnTaiLai.addActionListener(e -> loadBangLuong());
        tblBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblBangLuong.getSelectedRow();
                if (row >= 0) {
                    int maBangLuong = Integer.parseInt(modelBangLuong.getValueAt(row, 0).toString());
                    loadChiTietBangLuong(maBangLuong);
                }
            }
        });
    }

    private void xuLyTinhLuong() {
        int thang = (Integer) spnThang.getValue();
        int nam = (Integer) spnNam.getValue();
        int ngayCongChuan = (Integer) spnNgayCongChuan.getValue();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tính bảng lương tháng " + thang + "/" + nam + " với " + ngayCongChuan + " ngày công chuẩn?",
            "Xác nhận tính lương",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int maBangLuong = payrollService.tinhBangLuongThang(thang, nam, ngayCongChuan);
            JOptionPane.showMessageDialog(this, "Tính lương thành công. Mã bảng lương: " + maBangLuong);
            loadBangLuong();
            selectBangLuong(maBangLuong);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tính lương thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXemKy() {
        int thang = (Integer) spnThang.getValue();
        int nam = (Integer) spnNam.getValue();

        try {
            BangLuong bangLuong = payrollService.timBangLuongTheoKy(thang, nam);
            if (bangLuong == null) {
                modelChiTiet.setRowCount(0);
                JOptionPane.showMessageDialog(this, "Chưa có bảng lương tháng " + thang + "/" + nam + ".");
                return;
            }
            selectBangLuong(bangLuong.getMaBangLuong());
            loadChiTietBangLuong(bangLuong.getMaBangLuong());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải kỳ lương: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBangLuong() {
        modelBangLuong.setRowCount(0);
        try {
            List<BangLuong> list = payrollService.layDanhSachBangLuong();
            for (BangLuong bl : list) {
                modelBangLuong.addRow(new Object[]{
                    bl.getMaBangLuong(),
                    bl.getThang(),
                    bl.getNam(),
                    bl.getNgayCongChuan(),
                    bl.getTrangThai(),
                    bl.getSoNhanVien(),
                    formatMoney(bl.getTongThucNhan()),
                    formatDateTime(bl.getNgayTao()),
                    formatDateTime(bl.getNgayChot())
                });
            }
            modelChiTiet.setRowCount(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bảng lương: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChiTietBangLuong(int maBangLuong) {
        modelChiTiet.setRowCount(0);
        try {
            List<ChiTietBangLuong> list = payrollService.layChiTietBangLuong(maBangLuong);
            for (ChiTietBangLuong ct : list) {
                modelChiTiet.addRow(new Object[]{
                    ct.getMaChiTiet(),
                    ct.getMaNV(),
                    ct.getHoTen(),
                    formatMoney(ct.getLuongCoBan()),
                    ct.getNgayCongThucTe(),
                    formatMoney(ct.getTienCong()),
                    formatMoney(ct.getTongPhuCap()),
                    formatMoney(ct.getTongKhauTru()),
                    formatMoney(ct.getThucNhan())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết bảng lương: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectBangLuong(int maBangLuong) {
        for (int i = 0; i < modelBangLuong.getRowCount(); i++) {
            int value = Integer.parseInt(modelBangLuong.getValueAt(i, 0).toString());
            if (value == maBangLuong) {
                tblBangLuong.setRowSelectionInterval(i, i);
                tblBangLuong.scrollRectToVisible(tblBangLuong.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value != null ? value : BigDecimal.ZERO);
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? dateTimeFormatter.format(value) : "";
    }
}
