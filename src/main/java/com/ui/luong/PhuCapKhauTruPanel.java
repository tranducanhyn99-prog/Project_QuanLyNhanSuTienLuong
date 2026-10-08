package com.ui.luong;

import com.dao.NhanVienDAO;
import com.model.KhauTruNhanVien;
import com.model.NhanVien;
import com.model.PhuCapNhanVien;
import com.service.PhuCapKhauTruService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class PhuCapKhauTruPanel extends JPanel {
    private final PhuCapKhauTruService service;
    private final NhanVienDAO nhanVienDAO;

    private JComboBox<Integer> cbThang, cbNam;
    private JButton btnLoc, btnXoaKyLuong;

    // Tab Phụ Cấp
    private JTable tblPhuCap;
    private DefaultTableModel modelPhuCap;
    private JComboBox<String> cbNhanVienPC;
    private JTextField txtTenPhuCap, txtSoTienPC, txtGhiChuPC;
    private JButton btnThemPC, btnXoaPC;

    // Tab Khấu Trừ
    private JTable tblKhauTru;
    private DefaultTableModel modelKhauTru;
    private JComboBox<String> cbNhanVienKT;
    private JTextField txtTenKhauTru, txtSoTienKT, txtLyDoKT;
    private JButton btnThemKT, btnXoaKT;

    // Tab Tổng hợp View
    private JTable tblTongHop;
    private DefaultTableModel modelTongHop;

    private List<NhanVien> listNhanVien;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0 VNĐ");

    public PhuCapKhauTruPanel() {
        this.service = new PhuCapKhauTruService();
        this.nhanVienDAO = new NhanVienDAO();
        initComponents();
        loadNhanVienCombobox();
        loadAllData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(22, 24, 22, 24));
        cbThang = new JComboBox<>();
        for (int i = 1; i <= 12; i++) cbThang.addItem(i);
        cbNam = new JComboBox<>();
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2020; i <= curYear + 2; i++) cbNam.addItem(i);
        cbThang.setSelectedItem(Calendar.getInstance().get(Calendar.MONTH) + 1);
        cbNam.setSelectedItem(curYear);
        UITheme.styleComboBox(cbThang);
        UITheme.styleComboBox(cbNam);
        cbThang.setPreferredSize(new Dimension(80, 38));
        cbNam.setPreferredSize(new Dimension(100, 38));
        btnLoc = new JButton("Xem dữ liệu");
        UITheme.stylePrimaryButton(btnLoc);
        btnLoc.setIcon(UITheme.icon("search", 16, Color.WHITE));
        btnXoaKyLuong = new JButton("Xóa kỳ lương nháp");
        UITheme.styleDangerButton(btnXoaKyLuong);
        btnXoaKyLuong.setIcon(UITheme.icon("trash", 16, UITheme.DANGER_TEXT));
        btnXoaKyLuong.setVisible(com.session.Session.getInstance().hasRole("DB_Admin", "Payroll_Officer"));
        JPanel toolbar = card(new BorderLayout(12, 0));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.add(fieldLabel("Tháng"));
        filters.add(cbThang);
        filters.add(fieldLabel("Năm"));
        filters.add(cbNam);
        filters.add(btnLoc);
        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(btnXoaKyLuong, BorderLayout.EAST);
        JPanel heading = new JPanel(new BorderLayout(0, 16));
        heading.setOpaque(false);
        heading.add(UITheme.createPageHeader("Phụ cấp & khấu trừ",
                "Quản lý các khoản bổ sung và điều chỉnh thu nhập theo kỳ lương."), BorderLayout.NORTH);
        heading.add(toolbar, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        UITheme.styleTabbedPane(tabs);
        tabs.addTab("Phụ cấp", UITheme.icon("plus", 16, UITheme.PRIMARY), createPhuCapPanel());
        tabs.addTab("Khấu trừ", UITheme.icon("wallet", 16, UITheme.PRIMARY), createKhauTruPanel());
        tabs.addTab("Tổng hợp theo kỳ", UITheme.icon("chart", 16, UITheme.PRIMARY), createTongHopPanel());
        add(tabs, BorderLayout.CENTER);
        btnLoc.addActionListener(e -> loadAllData());
        cbThang.addActionListener(e -> loadAllData());
        cbNam.addActionListener(e -> loadAllData());
        btnXoaKyLuong.addActionListener(e -> handleXoaKyLuong());
    }

    private JPanel createPhuCapPanel() {
        JPanel panel = tabContent();
        modelPhuCap = readOnlyModel(new String[]{
            "Mã khoản", "Mã NV", "Họ tên", "Khoản phụ cấp", "Số tiền (VNĐ)", "Ngày ghi nhận", "Ghi chú"
        });
        tblPhuCap = new JTable(modelPhuCap);
        panel.add(tableSurface(tblPhuCap, new int[]{90, 80, 180, 180, 155, 130, 200}), BorderLayout.CENTER);
        cbNhanVienPC = new JComboBox<>();
        UITheme.styleComboBox(cbNhanVienPC);
        txtTenPhuCap = new JTextField("Phụ cấp ăn trưa");
        txtSoTienPC = new JTextField("730000");
        txtGhiChuPC = new JTextField("Phụ cấp định kỳ");
        UITheme.styleTextField(txtTenPhuCap);
        UITheme.styleTextField(txtSoTienPC);
        UITheme.styleTextField(txtGhiChuPC);
        txtSoTienPC.setHorizontalAlignment(JTextField.RIGHT);
        btnThemPC = new JButton("Thêm phụ cấp");
        UITheme.stylePrimaryButton(btnThemPC);
        btnThemPC.setIcon(UITheme.icon("plus", 16, Color.WHITE));
        btnXoaPC = new JButton("Xóa khoản đã chọn");
        UITheme.styleDangerButton(btnXoaPC);
        btnXoaPC.setIcon(UITheme.icon("trash", 16, UITheme.DANGER_TEXT));
        panel.add(entryForm("Thêm khoản phụ cấp", new String[]{
                "Nhân viên", "Tên khoản phụ cấp", "Số tiền (VNĐ)", "Ghi chú"
            }, new JComponent[]{cbNhanVienPC, txtTenPhuCap, txtSoTienPC, txtGhiChuPC},
            btnThemPC, btnXoaPC), BorderLayout.SOUTH);
        btnThemPC.addActionListener(e -> handleThemPhuCap());
        btnXoaPC.addActionListener(e -> handleXoaPhuCap());
        return panel;
    }

    private JPanel createKhauTruPanel() {
        JPanel panel = tabContent();
        modelKhauTru = readOnlyModel(new String[]{
            "Mã khoản", "Mã NV", "Họ tên", "Khoản khấu trừ", "Số tiền (VNĐ)", "Ngày ghi nhận", "Lý do"
        });
        tblKhauTru = new JTable(modelKhauTru);
        panel.add(tableSurface(tblKhauTru, new int[]{90, 80, 180, 180, 155, 130, 200}), BorderLayout.CENTER);
        cbNhanVienKT = new JComboBox<>();
        UITheme.styleComboBox(cbNhanVienKT);
        txtTenKhauTru = new JTextField("Tạm ứng lương");
        txtSoTienKT = new JTextField("1000000");
        txtLyDoKT = new JTextField("Ứng lương cá nhân");
        UITheme.styleTextField(txtTenKhauTru);
        UITheme.styleTextField(txtSoTienKT);
        UITheme.styleTextField(txtLyDoKT);
        txtSoTienKT.setHorizontalAlignment(JTextField.RIGHT);
        btnThemKT = new JButton("Thêm khấu trừ");
        UITheme.stylePrimaryButton(btnThemKT);
        btnThemKT.setIcon(UITheme.icon("plus", 16, Color.WHITE));
        btnXoaKT = new JButton("Xóa khoản đã chọn");
        UITheme.styleDangerButton(btnXoaKT);
        btnXoaKT.setIcon(UITheme.icon("trash", 16, UITheme.DANGER_TEXT));
        panel.add(entryForm("Thêm khoản khấu trừ", new String[]{
                "Nhân viên", "Tên khoản khấu trừ", "Số tiền (VNĐ)", "Lý do"
            }, new JComponent[]{cbNhanVienKT, txtTenKhauTru, txtSoTienKT, txtLyDoKT},
            btnThemKT, btnXoaKT), BorderLayout.SOUTH);
        btnThemKT.addActionListener(e -> handleThemKhauTru());
        btnXoaKT.addActionListener(e -> handleXoaKhauTru());
        return panel;
    }

    private JPanel createTongHopPanel() {
        JPanel panel = tabContent();
        modelTongHop = readOnlyModel(new String[]{
            "Mã NV", "Họ tên", "Kỳ lương", "Số khoản phụ cấp", "Tổng phụ cấp (VNĐ)", "Tổng khấu trừ (VNĐ)"
        });
        tblTongHop = new JTable(modelTongHop);
        panel.add(tableSurface(tblTongHop, new int[]{90, 220, 120, 150, 195, 195}), BorderLayout.CENTER);
        JLabel note = new JLabel("Các khoản phụ cấp và khấu trừ được tổng hợp theo kỳ đang chọn.");
        note.setFont(UITheme.FONT_CAPTION);
        note.setForeground(UITheme.TEXT_MUTED);
        note.setBorder(new EmptyBorder(3, 2, 3, 2));
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel tabContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));
        return panel;
    }

    private DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JScrollPane tableSurface(JTable table, int[] widths) {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane scroll = new JScrollPane(table);
        UITheme.styleScrollPane(scroll);
        return scroll;
    }

    private JPanel card(LayoutManager layout) {
        JPanel panel = UITheme.createCardPanel();
        panel.setLayout(layout);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        return panel;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_BODY);
        label.setForeground(UITheme.TEXT_MAIN);
        return label;
    }

    private JPanel entryForm(String title, String[] labels, JComponent[] inputs,
                             JButton addButton, JButton deleteButton) {
        JPanel form = card(new BorderLayout(0, 12));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_BODY_BOLD);
        titleLabel.setForeground(UITheme.TEXT_MAIN);
        form.add(titleLabel, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        int[] preferredWidths = {210, 160, 120, 160};
        double[] weights = {1.4, 1.05, 0.8, 1.05};
        for (int i = 0; i < labels.length; i++) {
            JPanel field = new JPanel(new BorderLayout(0, 7));
            field.setOpaque(false);
            JLabel label = fieldLabel(labels[i]);
            label.setLabelFor(inputs[i]);
            field.add(label, BorderLayout.NORTH);
            inputs[i].setPreferredSize(new Dimension(120, 38));
            field.add(inputs[i], BorderLayout.CENTER);
            field.setPreferredSize(new Dimension(preferredWidths[i], field.getPreferredSize().height));
            field.setMinimumSize(new Dimension(0, field.getPreferredSize().height));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = i;
            constraints.weightx = weights[i];
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 0, 0, i < labels.length - 1 ? 12 : 0);
            fields.add(field, constraints);
        }
        form.add(fields, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(deleteButton);
        actions.add(addButton);
        form.add(actions, BorderLayout.SOUTH);
        return form;
    }

    private void loadNhanVienCombobox() {
        DatabaseTask.run(cbNhanVienPC, () -> nhanVienDAO.getAll(), employees -> {
            listNhanVien=employees; cbNhanVienPC.removeAllItems(); cbNhanVienKT.removeAllItems();
            for(NhanVien nv:employees) {
                String label=nv.getMaNV()+" - "+nv.getHoTen();
                cbNhanVienPC.addItem(label); cbNhanVienKT.addItem(label);
            }
        });
    }

    private void loadAllData() {
        int month=(Integer)cbThang.getSelectedItem(),year=(Integer)cbNam.getSelectedItem();
        modelPhuCap.setRowCount(0);
        modelKhauTru.setRowCount(0);
        modelTongHop.setRowCount(0);
        DatabaseTask.run(tblPhuCap, () -> service.getListPhuCap(month,year), list -> {
            modelPhuCap.setRowCount(0);
            for(PhuCapNhanVien p:list) modelPhuCap.addRow(new Object[]{p.getMaPCNV(),p.getMaNV(),p.getHoTen(),p.getTenPhuCap(),moneyFormat.format(p.getSoTien()),p.getNgayGhiNhan(),p.getGhiChu()});
        });
        DatabaseTask.run(tblKhauTru, () -> service.getListKhauTru(month,year), list -> {
            modelKhauTru.setRowCount(0);
            for(KhauTruNhanVien k:list) modelKhauTru.addRow(new Object[]{k.getMaKTNV(),k.getMaNV(),k.getHoTen(),k.getTenKhauTru(),moneyFormat.format(k.getSoTien()),k.getNgayGhiNhan(),k.getLyDo()});
        });
        DatabaseTask.run(tblTongHop, () -> {
            List<Map<String,Object>> rows=service.getTongHopPhuCap(month,year);
            for(Map<String,Object> row:rows) row.put("TongKhauTru",service.getTongKhauTruNV((Integer)row.get("MaNV"),month,year));
            return rows;
        }, rows -> {
            modelTongHop.setRowCount(0);
            for(Map<String,Object> row:rows) modelTongHop.addRow(new Object[]{row.get("MaNV"),row.get("HoTen"),month+"/"+year,row.get("SoKhoanPhuCap"),moneyFormat.format(row.get("TongTienPhuCap")),moneyFormat.format(row.get("TongKhauTru"))});
        });
    }

    private void handleThemPhuCap() {
        try {
            int selectedIdx = cbNhanVienPC.getSelectedIndex();
            if (selectedIdx < 0 || listNhanVien == null || listNhanVien.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int maNV = listNhanVien.get(selectedIdx).getMaNV();
            int thang = (int) cbThang.getSelectedItem();
            int nam = (int) cbNam.getSelectedItem();
            String ten = txtTenPhuCap.getText().trim();
            BigDecimal tien = new BigDecimal(txtSoTienPC.getText().trim());
            String ghiChu = txtGhiChuPC.getText().trim();

            PhuCapNhanVien pc = new PhuCapNhanVien(maNV, thang, nam, ten, tien, null, ghiChu);
            DatabaseTask.runExclusive(this, () -> { service.themPhuCap(pc); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Thêm phụ cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadAllData();
                    });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm phụ cấp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleXoaPhuCap() {
        int row = tblPhuCap.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng phụ cấp cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        row = tblPhuCap.convertRowIndexToModel(row);
        int maPCNV = (int) modelPhuCap.getValueAt(row, 0);
        String tenNV = modelPhuCap.getValueAt(row, 2).toString();
        String khoanPC = modelPhuCap.getValueAt(row, 3).toString();

        int opt = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa khoản phụ cấp [" + khoanPC + "] của nhân viên [" + tenNV + "] (Mã PC: " + maPCNV + ")?",
                "Xác nhận xóa phụ cấp",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (opt == JOptionPane.YES_OPTION) {
            try {
                DatabaseTask.runExclusive(this, () -> { service.xoaPhuCap(maPCNV); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa phụ cấp thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
                            });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa phụ cấp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleThemKhauTru() {
        try {
            int selectedIdx = cbNhanVienKT.getSelectedIndex();
            if (selectedIdx < 0 || listNhanVien == null || listNhanVien.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int maNV = listNhanVien.get(selectedIdx).getMaNV();
            int thang = (int) cbThang.getSelectedItem();
            int nam = (int) cbNam.getSelectedItem();
            String ten = txtTenKhauTru.getText().trim();
            BigDecimal tien = new BigDecimal(txtSoTienKT.getText().trim());
            String lyDo = txtLyDoKT.getText().trim();

            KhauTruNhanVien kt = new KhauTruNhanVien(maNV, thang, nam, ten, tien, null, lyDo);
            DatabaseTask.runExclusive(this, () -> { service.themKhauTru(kt); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Thêm khoản khấu trừ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadAllData();
                    });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm khấu trừ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleXoaKhauTru() {
        int row = tblKhauTru.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng khấu trừ cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        row = tblKhauTru.convertRowIndexToModel(row);
        int maKTNV = (int) modelKhauTru.getValueAt(row, 0);
        String tenNV = modelKhauTru.getValueAt(row, 2).toString();
        String khoanKT = modelKhauTru.getValueAt(row, 3).toString();

        int opt = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa khoản khấu trừ [" + khoanKT + "] của nhân viên [" + tenNV + "] (Mã KT: " + maKTNV + ")?",
                "Xác nhận xóa khấu trừ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (opt == JOptionPane.YES_OPTION) {
            try {
                DatabaseTask.runExclusive(this, () -> { service.xoaKhauTru(maKTNV); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa khoản khấu trừ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
                            });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa khấu trừ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleXoaKyLuong() {
        int thang = (int) cbThang.getSelectedItem();
        int nam = (int) cbNam.getSelectedItem();
        int opt = JOptionPane.showConfirmDialog(this,
                "Xóa bảng lương nháp cùng toàn bộ chi tiết của kỳ " + thang + "/" + nam + "?\nKỳ lương đã chốt sẽ được giữ nguyên.\nBạn có muốn tiếp tục?",
                "Xác nhận xóa kỳ lương",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (opt == JOptionPane.YES_OPTION) {
            try {
                DatabaseTask.runExclusive(this, () -> { service.xoaKyLuongChuaChot(thang, nam); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa kỳ lương nháp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
                            });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa kỳ lương: " + ex.getMessage(), "Xóa kỳ lương", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
