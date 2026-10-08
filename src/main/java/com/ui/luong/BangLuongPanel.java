package com.ui.luong;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    private int detailPeriod = -1;

    public BangLuongPanel() {
        initComponents();
        loadBangLuong();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        LocalDate today = LocalDate.now();
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        spnThang = new JSpinner(new SpinnerNumberModel(today.getMonthValue(), 1, 12, 1));
        spnNam = new JSpinner(new SpinnerNumberModel(today.getYear(), 2020, 2100, 1));
        spnNgayCongChuan = new JSpinner(new SpinnerNumberModel(26, 1, 31, 1));
        spnThang.setFont(UITheme.FONT_BODY);
        spnNam.setFont(UITheme.FONT_BODY);
        spnNgayCongChuan.setFont(UITheme.FONT_BODY);

        JButton btnTinhLuong = new JButton("Tính / Cập nhật lương");
        UITheme.stylePrimaryButton(btnTinhLuong);

        JButton btnXemKy = new JButton("Xem kỳ");
        UITheme.styleSecondaryButton(btnXemKy);

        JButton btnTaiLai = new JButton("Tải lại");
        UITheme.styleSecondaryButton(btnTaiLai);

        JButton btnXoaKy = new JButton("Xóa kỳ (chưa chốt)");
        UITheme.styleDangerButton(btnXoaKy);

        JLabel lblThang = new JLabel("Tháng:");
        lblThang.setFont(UITheme.FONT_BODY);
        JLabel lblNam = new JLabel("Năm:");
        lblNam.setFont(UITheme.FONT_BODY);
        JLabel lblNCC = new JLabel("Ngày công chuẩn:");
        lblNCC.setFont(UITheme.FONT_BODY);

        pnlTop.add(lblThang);
        pnlTop.add(spnThang);
        pnlTop.add(lblNam);
        pnlTop.add(spnNam);
        pnlTop.add(lblNCC);
        pnlTop.add(spnNgayCongChuan);
        pnlTop.add(btnTinhLuong);
        pnlTop.add(btnXemKy);
        pnlTop.add(btnTaiLai);
        pnlTop.add(Box.createHorizontalStrut(10));
        pnlTop.add(btnXoaKy);
        add(pnlTop, BorderLayout.NORTH);

        modelBangLuong = new DefaultTableModel(new String[]{
            "Mã BL", "Tháng", "Năm", "NCC", "Trạng thái", "Số NV", "Tổng thực nhận (VNĐ)", "Ngày tạo", "Ngày chốt"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBangLuong = new JTable(modelBangLuong);
        UITheme.styleTable(tblBangLuong);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modelChiTiet = new DefaultTableModel(new String[]{
            "Mã CT", "Mã NV", "Họ tên", "Lương CB (VNĐ)", "Ngày công", "Tiền công (VNĐ)", "Phụ cấp (VNĐ)", "Khấu trừ (VNĐ)", "Thực nhận (VNĐ)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        UITheme.styleTable(tblChiTiet);

        JScrollPane scrollBangLuong = new JScrollPane(tblBangLuong);
        scrollBangLuong.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollBangLuong.getViewport().setBackground(Color.WHITE);

        JScrollPane scrollChiTiet = new JScrollPane(tblChiTiet);
        scrollChiTiet.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollChiTiet.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBangLuong, scrollChiTiet);
        splitPane.setResizeWeight(0.42);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        btnTinhLuong.addActionListener(e -> xuLyTinhLuong());
        btnXemKy.addActionListener(e -> xuLyXemKy());
        btnTaiLai.addActionListener(e -> loadBangLuong());
        btnXoaKy.addActionListener(e -> xuLyXoaKyLuong());
        tblBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblBangLuong.getSelectedRow();
                if (row >= 0) {
                    row = tblBangLuong.convertRowIndexToModel(row);
                    int maBangLuong = Integer.parseInt(modelBangLuong.getValueAt(row, 0).toString());
                    loadChiTietBangLuong(maBangLuong);
                } else clearChiTiet();
            }
        });
    }

    private void xuLyTinhLuong() {
        int thang = (Integer) spnThang.getValue();
        int nam = (Integer) spnNam.getValue();
        int ngayCongChuan = (Integer) spnNgayCongChuan.getValue();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tính / Cập nhật bảng lương tháng " + thang + "/" + nam + " với " + ngayCongChuan + " ngày công chuẩn?\n\n"
            + "Ghi chú: Nếu kỳ lương đã tồn tại ở trạng thái CHƯA CHỐT, hệ thống sẽ tính lại toàn bộ theo ngày công mới nhất.",
            "Xác nhận tính lương",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            DatabaseTask.runExclusive(this, () -> payrollService.tinhBangLuongThang(thang, nam, ngayCongChuan), maBangLuong -> {
            JOptionPane.showMessageDialog(this, "Tính / Cập nhật bảng lương tháng " + thang + "/" + nam + " thành công!\nMã bảng lương: " + maBangLuong, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadBangLuong(maBangLuong);
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tính lương thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaKyLuong() {
        int row=tblBangLuong.getSelectedRow();
        if (row >= 0) row = tblBangLuong.convertRowIndexToModel(row);
        int month=row>=0 ? Integer.parseInt(modelBangLuong.getValueAt(row,1).toString()) : (Integer)spnThang.getValue();
        int year=row>=0 ? Integer.parseInt(modelBangLuong.getValueAt(row,2).toString()) : (Integer)spnNam.getValue();
        DatabaseTask.runExclusive(this, () -> payrollService.timBangLuongTheoKy(month,year), period -> {
            if(period==null) { JOptionPane.showMessageDialog(this,"Chưa có bảng lương cho kỳ đã chọn."); return; }
            if(period.isDaChot()) { JOptionPane.showMessageDialog(this,"Kỳ đã chốt. Cần mở lại trước khi xóa."); return; }
            if(JOptionPane.showConfirmDialog(this,"Xóa kỳ lương "+month+"/"+year+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            DatabaseTask.runExclusive(this, () -> { payrollService.xoaBangLuong(period.getMaBangLuong()); return true; }, ok -> {
                JOptionPane.showMessageDialog(this,"Đã xóa kỳ lương."); loadBangLuong();
            });
        });
    }

    private void xuLyXemKy() {
        int month=(Integer)spnThang.getValue(),year=(Integer)spnNam.getValue();
        DatabaseTask.runExclusive(this, () -> payrollService.timBangLuongTheoKy(month,year), period -> {
            if(period==null) { clearChiTiet(); JOptionPane.showMessageDialog(this,"Chưa có bảng lương kỳ này."); return; }
            loadBangLuong(period.getMaBangLuong());
        });
    }

    private void loadBangLuong() { loadBangLuong(-1); }

    private void loadBangLuong(int selectedId) {
        clearChiTiet();
        modelBangLuong.setRowCount(0);
        DatabaseTask.run(tblBangLuong, () -> payrollService.layDanhSachBangLuong(), list -> {
            modelBangLuong.setRowCount(0);
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
            if (selectedId > 0) selectBangLuong(selectedId);
        });
    }

    private void clearChiTiet() {
        detailPeriod = -1;
        DatabaseTask.invalidate(tblChiTiet);
        modelChiTiet.setRowCount(0);
    }

    private void loadChiTietBangLuong(int maBangLuong) {
        detailPeriod = maBangLuong;
        modelChiTiet.setRowCount(0);
        DatabaseTask.run(tblChiTiet, () -> payrollService.layChiTietBangLuong(maBangLuong), list -> {
            if (detailPeriod != maBangLuong) return;
            modelChiTiet.setRowCount(0);
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
        });
    }

    private void selectBangLuong(int maBangLuong) {
        for (int i = 0; i < modelBangLuong.getRowCount(); i++) {
            int value = Integer.parseInt(modelBangLuong.getValueAt(i, 0).toString());
            if (value == maBangLuong) {
                int viewRow = tblBangLuong.convertRowIndexToView(i);
                tblBangLuong.setRowSelectionInterval(viewRow, viewRow);
                tblBangLuong.scrollRectToVisible(tblBangLuong.getCellRect(viewRow, 0, true));
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
