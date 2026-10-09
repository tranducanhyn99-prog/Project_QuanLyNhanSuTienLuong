package com.ui.nhanvien;

import com.model.ChucVu;
import com.model.PhongBan;
import com.service.DanhMucService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;


/**
 * DanhMucPanel – Quản lý Phòng Ban & Chức Vụ chuẩn Enterprise.
 *
 * Giao diện Fluent Design:
 * - Bố cục 2 cột cân đối: Phòng ban (Trái) và Chức vụ (Phải).
 * - Bảng dữ liệu phẳng chuẩn Enterprise với chiều cao hàng 32px.
 * - Form nhập liệu và nút bấm phân cấp rõ ràng.
 *
 * @author Nhóm 06 – DBMS Enterprise
 */
public class DanhMucPanel extends JPanel {

    private final DanhMucService danhMucService = new DanhMucService();

    // Components Phòng Ban
    private JTable tblPhongBan;
    private DefaultTableModel modelPhongBan;
    private JTextField txtMaPB, txtTenPB, txtSdtPB;
    private JComboBox<String> cboTrangThaiPB;

    // Components Chức Vụ
    private JTable tblChucVu;
    private DefaultTableModel modelChucVu;
    private JTextField txtMaCV, txtTenCV, txtPhuCapCV;

    public DanhMucPanel() {
        initComponents();
        loadDataPhongBan();
        loadDataChucVu();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(UITheme.createPageHeader("Phòng ban & chức vụ",
                "Sắp xếp cơ cấu tổ chức và quản lý phụ cấp cho từng vị trí."), BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 18, 0));
        columns.setOpaque(false);
        JPanel pnlPB = UITheme.createCardPanel();
        pnlPB.setLayout(new BorderLayout(0, 12));
        pnlPB.add(cardHeader("Phòng ban", "Thông tin liên hệ và trạng thái hoạt động", "building"), BorderLayout.NORTH);
        modelPhongBan = new DefaultTableModel(new String[]{"Mã PB", "Tên phòng ban", "Điện thoại", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPhongBan = new JTable(modelPhongBan);
        UITheme.styleTable(tblPhongBan);
        tblPhongBan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhongBan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(tblPhongBan, new int[]{70, 190, 125, 145});
        JScrollPane scrollPB = new JScrollPane(tblPhongBan);
        UITheme.styleScrollPane(scrollPB);
        pnlPB.add(scrollPB, BorderLayout.CENTER);

        txtMaPB = new JTextField();
        txtMaPB.setEditable(false);
        txtMaPB.setToolTipText("Mã phòng ban được tạo tự động");
        txtTenPB = new JTextField();
        txtSdtPB = new JTextField();
        UITheme.styleTextField(txtMaPB);
        UITheme.styleTextField(txtTenPB);
        UITheme.styleTextField(txtSdtPB);
        cboTrangThaiPB = new JComboBox<>(new String[]{"HOAT_DONG", "NGUNG_HOAT_DONG"});
        UITheme.styleComboBox(cboTrangThaiPB);

        JPanel pnlSouthPB = new JPanel(new BorderLayout(0, 12));
        pnlSouthPB.setOpaque(false);
        JPanel pnlFormPB = new JPanel(new GridBagLayout());
        pnlFormPB.setOpaque(false);
        pnlFormPB.setBorder(new EmptyBorder(8, 0, 0, 0));
        addFormRow(pnlFormPB, 0, "Mã phòng ban", txtMaPB);
        addFormRow(pnlFormPB, 1, "Tên phòng ban", txtTenPB);
        addFormRow(pnlFormPB, 2, "Điện thoại", txtSdtPB);
        addFormRow(pnlFormPB, 3, "Trạng thái", cboTrangThaiPB);
        pnlSouthPB.add(pnlFormPB, BorderLayout.CENTER);

        JButton btnThemPB = compactButton("Thêm", 0);
        JButton btnSuaPB = compactButton("Sửa", 1);
        JButton btnXoaPB = compactButton("Xóa", 2);
        JButton btnLamMoiPB = compactButton("Làm mới", 1);
        JPanel pnlBtnPB = buttonRow(btnLamMoiPB, btnXoaPB, btnSuaPB, btnThemPB);
        pnlSouthPB.add(pnlBtnPB, BorderLayout.SOUTH);
        pnlPB.add(pnlSouthPB, BorderLayout.SOUTH);

        JPanel pnlCV = UITheme.createCardPanel();
        pnlCV.setLayout(new BorderLayout(0, 12));
        pnlCV.add(cardHeader("Chức vụ", "Vị trí công việc và mức phụ cấp tương ứng", "briefcase"), BorderLayout.NORTH);
        modelChucVu = new DefaultTableModel(new String[]{"Mã CV", "Tên chức vụ", "Phụ cấp (đ)"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblChucVu = new JTable(modelChucVu);
        UITheme.styleTable(tblChucVu);
        tblChucVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChucVu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(tblChucVu, new int[]{70, 200, 140});
        JScrollPane scrollCV = new JScrollPane(tblChucVu);
        UITheme.styleScrollPane(scrollCV);
        pnlCV.add(scrollCV, BorderLayout.CENTER);

        txtMaCV = new JTextField();
        txtMaCV.setEditable(false);
        txtMaCV.setToolTipText("Mã chức vụ được tạo tự động");
        txtTenCV = new JTextField();
        txtPhuCapCV = new JTextField();
        txtPhuCapCV.setToolTipText("Nhập số tiền bằng đồng; bỏ trống để dùng mức 0");
        UITheme.styleTextField(txtMaCV);
        UITheme.styleTextField(txtTenCV);
        UITheme.styleTextField(txtPhuCapCV);
        JPanel pnlSouthCV = new JPanel(new BorderLayout(0, 12));
        pnlSouthCV.setOpaque(false);
        JPanel pnlFormCV = new JPanel(new GridBagLayout());
        pnlFormCV.setOpaque(false);
        pnlFormCV.setBorder(new EmptyBorder(8, 0, 0, 0));
        addFormRow(pnlFormCV, 0, "Mã chức vụ", txtMaCV);
        addFormRow(pnlFormCV, 1, "Tên chức vụ", txtTenCV);
        addFormRow(pnlFormCV, 2, "Phụ cấp (đ)", txtPhuCapCV);
        JLabel hint = new JLabel("Chọn một dòng để cập nhật thông tin.");
        hint.setFont(UITheme.FONT_CAPTION);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setPreferredSize(new Dimension(0, 36));
        GridBagConstraints hintConstraints = new GridBagConstraints();
        hintConstraints.gridx = 0;
        hintConstraints.gridy = 3;
        hintConstraints.gridwidth = 2;
        hintConstraints.fill = GridBagConstraints.HORIZONTAL;
        hintConstraints.insets = new Insets(5, 0, 5, 0);
        pnlFormCV.add(hint, hintConstraints);
        pnlSouthCV.add(pnlFormCV, BorderLayout.CENTER);

        JButton btnThemCV = compactButton("Thêm", 0);
        JButton btnSuaCV = compactButton("Sửa", 1);
        JButton btnXoaCV = compactButton("Xóa", 2);
        JButton btnLamMoiCV = compactButton("Làm mới", 1);
        JPanel pnlBtnCV = buttonRow(btnLamMoiCV, btnXoaCV, btnSuaCV, btnThemCV);
        pnlSouthCV.add(pnlBtnCV, BorderLayout.SOUTH);
        pnlCV.add(pnlSouthCV, BorderLayout.SOUTH);
        columns.add(pnlPB);
        columns.add(pnlCV);
        add(columns, BorderLayout.CENTER);

        // Events Phòng Ban
        tblPhongBan.getSelectionModel().addListSelectionListener(e -> {
            int row = tblPhongBan.getSelectedRow();
            if (row >= 0) {
                txtMaPB.setText(modelPhongBan.getValueAt(row, 0).toString());
                txtTenPB.setText(modelPhongBan.getValueAt(row, 1).toString());
                Object sdt = modelPhongBan.getValueAt(row, 2);
                txtSdtPB.setText(sdt != null ? sdt.toString() : "");
                cboTrangThaiPB.setSelectedItem(modelPhongBan.getValueAt(row, 3).toString());
            }
        });

        btnThemPB.addActionListener(e -> xuLyThemPB());
        btnSuaPB.addActionListener(e -> xuLySuaPB());
        btnXoaPB.addActionListener(e -> xuLyXoaPB());
        btnLamMoiPB.addActionListener(e -> lamMoiPB());

        // Events Chức Vụ
        tblChucVu.getSelectionModel().addListSelectionListener(e -> {
            int row = tblChucVu.getSelectedRow();
            if (row >= 0) {
                txtMaCV.setText(modelChucVu.getValueAt(row, 0).toString());
                txtTenCV.setText(modelChucVu.getValueAt(row, 1).toString());
                txtPhuCapCV.setText(modelChucVu.getValueAt(row, 2).toString());
            }
        });

        btnThemCV.addActionListener(e -> xuLyThemCV());
        btnSuaCV.addActionListener(e -> xuLySuaCV());
        btnXoaCV.addActionListener(e -> xuLyXoaCV());
        btnLamMoiCV.addActionListener(e -> lamMoiCV());
    }

    private JPanel cardHeader(String title, String subtitle, String iconName) {
        JPanel panel = new JPanel(new BorderLayout(10, 4));
        panel.setOpaque(false);
        JLabel icon = new JLabel(UITheme.icon(iconName, 23, UITheme.PRIMARY));
        panel.add(icon, BorderLayout.WEST);
        JPanel copy = new JPanel(new BorderLayout(0, 4));
        copy.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(UITheme.FONT_SUBTITLE);
        name.setForeground(UITheme.TEXT_MAIN);
        JLabel description = new JLabel("<html>" + subtitle + "</html>");
        description.setFont(UITheme.FONT_CAPTION);
        description.setForeground(UITheme.TEXT_MUTED);
        copy.add(name, BorderLayout.NORTH);
        copy.add(description, BorderLayout.CENTER);
        panel.add(copy, BorderLayout.CENTER);
        return panel;
    }

    private void addFormRow(JPanel panel, int row, String text, JComponent input) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 0, 5, 12);
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_CAPTION_BOLD);
        label.setForeground(UITheme.TEXT_MUTED);
        label.setLabelFor(input);
        panel.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 0, 5, 0);
        panel.add(input, constraints);
    }

    private JButton compactButton(String text, int style) {
        JButton button = new JButton(text);
        if (style == 0) UITheme.stylePrimaryButton(button);
        else if (style == 2) UITheme.styleDangerButton(button);
        else UITheme.styleSecondaryButton(button);
        button.setBorder(new EmptyBorder(8, 8, 8, 8));
        return button;
    }

    private JPanel buttonRow(JButton... buttons) {
        JPanel panel = new JPanel(new GridLayout(1, buttons.length, 7, 0));
        panel.setOpaque(false);
        for (JButton button : buttons) panel.add(button);
        return panel;
    }

    private void setColumnWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void loadDataPhongBan() {
        modelPhongBan.setRowCount(0);
        DatabaseTask.run(tblPhongBan, () -> danhMucService.layTatCaPhongBan(), list -> {
            for (PhongBan pb : list) {
                modelPhongBan.addRow(new Object[]{pb.getMaPB(), pb.getTenPB(), pb.getSoDienThoai(), pb.getTrangThai()});
            }
        });
    }

    private void loadDataChucVu() {
        modelChucVu.setRowCount(0);
        DatabaseTask.run(tblChucVu, () -> danhMucService.layTatCaChucVu(), list -> {
            for (ChucVu cv : list) {
                modelChucVu.addRow(new Object[]{cv.getMaCV(), cv.getTenCV(), cv.getPhuCapChucVu()});
            }
        });
    }

    private void xuLyThemPB() {
        try {
            String name=txtTenPB.getText(),phone=txtSdtPB.getText();
            DatabaseTask.runExclusive(this, () -> { danhMucService.themPhongBan(name, phone); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Thêm phòng ban thành công!");
            lamMoiPB();
            loadDataPhongBan();
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLySuaPB() {
        try {
            if (txtMaPB.getText().isEmpty()) throw new Exception("Vui lòng chọn phòng ban cần sửa!");
            int id=Integer.parseInt(txtMaPB.getText());
            String name=txtTenPB.getText(),phone=txtSdtPB.getText(),status=cboTrangThaiPB.getSelectedItem().toString();
            DatabaseTask.runExclusive(this, () -> { danhMucService.capNhatPhongBan(
                id,
                name,
                phone,
                status
            ); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Cập nhật phòng ban thành công!");
            lamMoiPB();
            loadDataPhongBan();
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaPB() {
        try {
            if (txtMaPB.getText().isEmpty()) throw new Exception("Vui lòng chọn phòng ban cần xóa!");
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phòng ban này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id=Integer.parseInt(txtMaPB.getText());
                DatabaseTask.runExclusive(this, () -> { danhMucService.xoaPhongBan(id); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa phòng ban thành công!");
                lamMoiPB();
                loadDataPhongBan();
                            });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể xóa phòng ban (có thể do đã có nhân viên trực thuộc): " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lamMoiPB() {
        txtMaPB.setText("");
        txtTenPB.setText("");
        txtSdtPB.setText("");
        cboTrangThaiPB.setSelectedIndex(0);
        tblPhongBan.clearSelection();
    }

    private void xuLyThemCV() {
        try {
            BigDecimal phuCap = new BigDecimal(txtPhuCapCV.getText().trim().isEmpty() ? "0" : txtPhuCapCV.getText().trim());
            String name=txtTenCV.getText();
            DatabaseTask.runExclusive(this, () -> { danhMucService.themChucVu(name, phuCap); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Thêm chức vụ thành công!");
            lamMoiCV();
            loadDataChucVu();
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLySuaCV() {
        try {
            if (txtMaCV.getText().isEmpty()) throw new Exception("Vui lòng chọn chức vụ cần sửa!");
            BigDecimal phuCap = new BigDecimal(txtPhuCapCV.getText().trim().isEmpty() ? "0" : txtPhuCapCV.getText().trim());
            int id=Integer.parseInt(txtMaCV.getText());
            String name=txtTenCV.getText();
            DatabaseTask.runExclusive(this, () -> { danhMucService.capNhatChucVu(id, name, phuCap); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Cập nhật chức vụ thành công!");
            lamMoiCV();
            loadDataChucVu();
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaCV() {
        try {
            if (txtMaCV.getText().isEmpty()) throw new Exception("Vui lòng chọn chức vụ cần xóa!");
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa chức vụ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id=Integer.parseInt(txtMaCV.getText());
                DatabaseTask.runExclusive(this, () -> { danhMucService.xoaChucVu(id); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa chức vụ thành công!");
                lamMoiCV();
                loadDataChucVu();
                            });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể xóa chức vụ (có thể do đã có nhân viên đảm nhận): " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lamMoiCV() {
        txtMaCV.setText("");
        txtTenCV.setText("");
        txtPhuCapCV.setText("");
        tblChucVu.clearSelection();
    }
}
