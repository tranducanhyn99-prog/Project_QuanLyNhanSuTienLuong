package com.ui.luong;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BangLuongPanel extends JPanel {

    private final PayrollService payrollService = new PayrollService();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JSpinner spnThang;
    private JSpinner spnNam;
    private JSpinner spnNgayCongChuan;
    private JTable tblBangLuong;
    private JTable tblChiTiet;
    private DefaultTableModel modelBangLuong;
    private DefaultTableModel modelChiTiet;
    private int detailPeriod = -1;
    private JLabel lblPeriodCount;
    private JLabel lblDetailInfo;

    public BangLuongPanel() {
        initComponents();
        loadBangLuong();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        LocalDate today = LocalDate.now();
        JPanel pnlTop = UITheme.createCardPanel();
        pnlTop.setLayout(new BorderLayout(0, 10));
        pnlTop.setBorder(new EmptyBorder(12, 14, 12, 14));

        spnThang = new JSpinner(new SpinnerNumberModel(today.getMonthValue(), 1, 12, 1));
        spnNam = new JSpinner(new SpinnerNumberModel(today.getYear(), 2020, 2100, 1));
        spnNgayCongChuan = new JSpinner(new SpinnerNumberModel(26, 1, 31, 1));
        UITheme.styleSpinner(spnThang);
        UITheme.styleSpinner(spnNam);
        UITheme.styleSpinner(spnNgayCongChuan);
        spnThang.setPreferredSize(new Dimension(68, 36));
        spnNam.setPreferredSize(new Dimension(92, 36));
        spnNgayCongChuan.setPreferredSize(new Dimension(68, 36));
        spnNam.setEditor(new JSpinner.NumberEditor(spnNam, "#"));
        UITheme.styleSpinner(spnNam);

        JButton btnTinhLuong = new JButton("Tính / cập nhật lương");
        UITheme.stylePrimaryButton(btnTinhLuong);
        btnTinhLuong.setIcon(UITheme.icon("wallet", 16, Color.WHITE));

        JButton btnXemKy = new JButton("Xem kỳ");
        UITheme.styleSecondaryButton(btnXemKy);
        btnXemKy.setIcon(UITheme.icon("search", 16, UITheme.TEXT_MAIN));

        JButton btnTaiLai = new JButton("Làm mới");
        UITheme.styleSecondaryButton(btnTaiLai);
        btnTaiLai.setIcon(UITheme.icon("refresh", 16, UITheme.TEXT_MAIN));

        JButton btnXoaKy = new JButton("Xóa kỳ nháp");
        UITheme.styleDangerButton(btnXoaKy);
        btnXoaKy.setIcon(UITheme.icon("trash", 16, UITheme.DANGER_TEXT));
        btnXoaKy.setToolTipText("Chỉ xóa kỳ lương chưa chốt");

        JLabel lblThang = new JLabel("Tháng:");
        lblThang.setFont(UITheme.FONT_BODY);
        JLabel lblNam = new JLabel("Năm:");
        lblNam.setFont(UITheme.FONT_BODY);
        JLabel lblNCC = new JLabel("Ngày công chuẩn:");
        lblNCC.setFont(UITheme.FONT_BODY);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(lblThang);
        filters.add(spnThang);
        filters.add(lblNam);
        filters.add(spnNam);
        filters.add(lblNCC);
        filters.add(spnNgayCongChuan);
        pnlTop.add(filters, BorderLayout.NORTH);
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel primaryActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        primaryActions.setOpaque(false);
        primaryActions.add(btnTinhLuong);
        primaryActions.add(btnXemKy);
        primaryActions.add(btnTaiLai);
        actions.add(primaryActions, BorderLayout.WEST);
        actions.add(btnXoaKy, BorderLayout.EAST);
        pnlTop.add(actions, BorderLayout.SOUTH);
        JPanel heading = new JPanel(new BorderLayout(0, 16));
        heading.setOpaque(false);
        heading.add(UITheme.createPageHeader("Bảng lương", "Tính lương theo ngày công và quản lý các kỳ thanh toán."), BorderLayout.NORTH);
        heading.add(pnlTop, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);

        modelBangLuong = new DefaultTableModel(new String[]{
            "Mã kỳ", "Tháng", "Năm", "Công chuẩn", "Trạng thái", "Nhân viên", "Tổng thực nhận", "Ngày tạo", "Ngày chốt"
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
            "Mã chi tiết", "Mã NV", "Họ tên", "Lương cơ bản", "Ngày công", "Tiền công", "Phụ cấp", "Khấu trừ", "Thực nhận"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        UITheme.styleTable(tblChiTiet);
        configureColumns(tblBangLuong, new int[]{80, 68, 80, 100, 140, 100, 180, 160, 160});
        configureColumns(tblChiTiet, new int[]{100, 80, 190, 155, 100, 155, 145, 145, 160});

        JScrollPane scrollBangLuong = new JScrollPane(tblBangLuong);
        UITheme.styleScrollPane(scrollBangLuong);

        JScrollPane scrollChiTiet = new JScrollPane(tblChiTiet);
        UITheme.styleScrollPane(scrollChiTiet);

        lblPeriodCount = new JLabel("Đang tải danh sách...");
        lblDetailInfo = new JLabel("Chọn một kỳ lương để xem chi tiết");
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            tableSection("Các kỳ lương", lblPeriodCount, scrollBangLuong),
            tableSection("Chi tiết nhân viên", lblDetailInfo, scrollChiTiet));
        splitPane.setResizeWeight(0.42);
        splitPane.setDividerSize(12);
        splitPane.setBackground(UITheme.BG_APP);
        splitPane.setBorder(null);
        splitPane.getTopComponent().setMinimumSize(new Dimension(0, 135));
        splitPane.getBottomComponent().setMinimumSize(new Dimension(0, 155));
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

    private JPanel tableSection(String title, JLabel description, JScrollPane scroll) {
        JPanel section = UITheme.createCardPanel();
        section.setLayout(new BorderLayout());
        section.setBorder(new EmptyBorder(1, 1, 1, 1));
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel label = new JLabel(title);
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_MAIN);
        description.setFont(UITheme.FONT_CAPTION);
        description.setForeground(UITheme.TEXT_MUTED);
        header.add(label, BorderLayout.WEST);
        header.add(description, BorderLayout.EAST);
        section.add(header, BorderLayout.NORTH);
        scroll.setBorder(null);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private void configureColumns(JTable table, int[] widths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
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
            lblPeriodCount.setText(list.size() + " kỳ lương");
            if (selectedId > 0) selectBangLuong(selectedId);
        });
    }

    private void clearChiTiet() {
        detailPeriod = -1;
        DatabaseTask.invalidate(tblChiTiet);
        modelChiTiet.setRowCount(0);
        lblDetailInfo.setText("Chọn một kỳ lương để xem chi tiết");
    }

    private void loadChiTietBangLuong(int maBangLuong) {
        detailPeriod = maBangLuong;
        modelChiTiet.setRowCount(0);
        lblDetailInfo.setText("Đang tải kỳ #" + maBangLuong + "...");
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
            lblDetailInfo.setText(list.size() + " nhân viên  ·  Kỳ #" + maBangLuong);
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
