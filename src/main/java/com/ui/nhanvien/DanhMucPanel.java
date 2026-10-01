package com.ui.nhanvien;

import com.model.ChucVu;
import com.model.PhongBan;
import com.service.DanhMucService;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

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
        setLayout(new GridLayout(1, 2, 16, 0));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // ═══════════════════════════════════════════════════════════════
        //  CỘT TRÁI: PHÒNG BAN
        // ═══════════════════════════════════════════════════════════════
        JPanel pnlPB = new JPanel(new BorderLayout(0, 10));
        pnlPB.setBackground(Color.WHITE);
        pnlPB.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        pnlPB.add(UITheme.createSectionHeader("Danh mục Phòng Ban"), BorderLayout.NORTH);

        modelPhongBan = new DefaultTableModel(new String[]{"Mã PB", "Tên Phòng Ban", "Số ĐT", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPhongBan = new JTable(modelPhongBan);
        UITheme.styleTable(tblPhongBan);

        JScrollPane scrollPB = new JScrollPane(tblPhongBan);
        scrollPB.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollPB.getViewport().setBackground(Color.WHITE);
        pnlPB.add(scrollPB, BorderLayout.CENTER);

        // Form Phòng Ban
        JPanel pnlSouthPB = new JPanel(new BorderLayout(0, 8));
        pnlSouthPB.setOpaque(false);

        JPanel pnlFormPB = new JPanel(new GridBagLayout());
        pnlFormPB.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; pnlFormPB.add(new JLabel("Mã PB:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; txtMaPB = new JTextField(); txtMaPB.setEditable(false); UITheme.styleTextField(txtMaPB); pnlFormPB.add(txtMaPB, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; pnlFormPB.add(new JLabel("Tên PB:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; txtTenPB = new JTextField(); UITheme.styleTextField(txtTenPB); pnlFormPB.add(txtTenPB, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; pnlFormPB.add(new JLabel("Số ĐT:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; txtSdtPB = new JTextField(); UITheme.styleTextField(txtSdtPB); pnlFormPB.add(txtSdtPB, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; pnlFormPB.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; cboTrangThaiPB = new JComboBox<>(new String[]{"HOAT_DONG", "NGUNG_HOAT_DONG"}); UITheme.styleComboBox(cboTrangThaiPB); pnlFormPB.add(cboTrangThaiPB, gbc);

        JPanel pnlBtnPB = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtnPB.setOpaque(false);
        JButton btnThemPB = new JButton("Thêm");
        JButton btnSuaPB = new JButton("Sửa");
        JButton btnXoaPB = new JButton("Xóa");
        JButton btnLamMoiPB = new JButton("Làm mới");

        UITheme.stylePrimaryButton(btnThemPB);
        UITheme.styleSecondaryButton(btnSuaPB);
        UITheme.styleDangerButton(btnXoaPB);
        UITheme.styleSecondaryButton(btnLamMoiPB);

        pnlBtnPB.add(btnThemPB);
        pnlBtnPB.add(btnSuaPB);
        pnlBtnPB.add(btnXoaPB);
        pnlBtnPB.add(btnLamMoiPB);

        pnlSouthPB.add(pnlFormPB, BorderLayout.CENTER);
        pnlSouthPB.add(pnlBtnPB, BorderLayout.SOUTH);
        pnlPB.add(pnlSouthPB, BorderLayout.SOUTH);

        // ═══════════════════════════════════════════════════════════════
        //  CỘT PHẢI: CHỨC VỤ
        // ═══════════════════════════════════════════════════════════════
        JPanel pnlCV = new JPanel(new BorderLayout(0, 10));
        pnlCV.setBackground(Color.WHITE);
        pnlCV.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        pnlCV.add(UITheme.createSectionHeader("Danh mục Chức Vụ & Phụ Cấp"), BorderLayout.NORTH);

        modelChucVu = new DefaultTableModel(new String[]{"Mã CV", "Tên Chức Vụ", "Phụ Cấp Chức Vụ (VNĐ)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblChucVu = new JTable(modelChucVu);
        UITheme.styleTable(tblChucVu);

        JScrollPane scrollCV = new JScrollPane(tblChucVu);
        scrollCV.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollCV.getViewport().setBackground(Color.WHITE);
        pnlCV.add(scrollCV, BorderLayout.CENTER);

        // Form Chức Vụ
        JPanel pnlSouthCV = new JPanel(new BorderLayout(0, 8));
        pnlSouthCV.setOpaque(false);

        JPanel pnlFormCV = new JPanel(new GridBagLayout());
        pnlFormCV.setOpaque(false);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; pnlFormCV.add(new JLabel("Mã CV:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; txtMaCV = new JTextField(); txtMaCV.setEditable(false); UITheme.styleTextField(txtMaCV); pnlFormCV.add(txtMaCV, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; pnlFormCV.add(new JLabel("Tên Chức vụ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; txtTenCV = new JTextField(); UITheme.styleTextField(txtTenCV); pnlFormCV.add(txtTenCV, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; pnlFormCV.add(new JLabel("Phụ cấp CV:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; txtPhuCapCV = new JTextField(); UITheme.styleTextField(txtPhuCapCV); pnlFormCV.add(txtPhuCapCV, gbc);

        JPanel pnlBtnCV = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtnCV.setOpaque(false);
        JButton btnThemCV = new JButton("Thêm");
        JButton btnSuaCV = new JButton("Sửa");
        JButton btnXoaCV = new JButton("Xóa");
        JButton btnLamMoiCV = new JButton("Làm mới");

        UITheme.stylePrimaryButton(btnThemCV);
        UITheme.styleSecondaryButton(btnSuaCV);
        UITheme.styleDangerButton(btnXoaCV);
        UITheme.styleSecondaryButton(btnLamMoiCV);

        pnlBtnCV.add(btnThemCV);
        pnlBtnCV.add(btnSuaCV);
        pnlBtnCV.add(btnXoaCV);
        pnlBtnCV.add(btnLamMoiCV);

        pnlSouthCV.add(pnlFormCV, BorderLayout.CENTER);
        pnlSouthCV.add(pnlBtnCV, BorderLayout.SOUTH);
        pnlCV.add(pnlSouthCV, BorderLayout.SOUTH);

        add(pnlPB);
        add(pnlCV);

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

    private void loadDataPhongBan() {
        modelPhongBan.setRowCount(0);
        try {
            List<PhongBan> list = danhMucService.layTatCaPhongBan();
            for (PhongBan pb : list) {
                modelPhongBan.addRow(new Object[]{pb.getMaPB(), pb.getTenPB(), pb.getSoDienThoai(), pb.getTrangThai()});
            }
        } catch (Exception e) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Lỗi tải danh mục Phòng Ban: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Bỏ qua dialog khởi tạo Phòng Ban: " + e.getMessage());
            }
        }
    }

    private void loadDataChucVu() {
        modelChucVu.setRowCount(0);
        try {
            List<ChucVu> list = danhMucService.layTatCaChucVu();
            for (ChucVu cv : list) {
                modelChucVu.addRow(new Object[]{cv.getMaCV(), cv.getTenCV(), cv.getPhuCapChucVu()});
            }
        } catch (Exception e) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Lỗi tải danh mục Chức Vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Bỏ qua dialog khởi tạo Chức Vụ: " + e.getMessage());
            }
        }
    }

    private void xuLyThemPB() {
        try {
            danhMucService.themPhongBan(txtTenPB.getText(), txtSdtPB.getText());
            JOptionPane.showMessageDialog(this, "Thêm phòng ban thành công!");
            lamMoiPB();
            loadDataPhongBan();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLySuaPB() {
        try {
            if (txtMaPB.getText().isEmpty()) throw new Exception("Vui lòng chọn phòng ban cần sửa!");
            danhMucService.capNhatPhongBan(
                Integer.parseInt(txtMaPB.getText()),
                txtTenPB.getText(),
                txtSdtPB.getText(),
                cboTrangThaiPB.getSelectedItem().toString()
            );
            JOptionPane.showMessageDialog(this, "Cập nhật phòng ban thành công!");
            lamMoiPB();
            loadDataPhongBan();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaPB() {
        try {
            if (txtMaPB.getText().isEmpty()) throw new Exception("Vui lòng chọn phòng ban cần xóa!");
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phòng ban này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                danhMucService.xoaPhongBan(Integer.parseInt(txtMaPB.getText()));
                JOptionPane.showMessageDialog(this, "Đã xóa phòng ban thành công!");
                lamMoiPB();
                loadDataPhongBan();
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
            danhMucService.themChucVu(txtTenCV.getText(), phuCap);
            JOptionPane.showMessageDialog(this, "Thêm chức vụ thành công!");
            lamMoiCV();
            loadDataChucVu();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLySuaCV() {
        try {
            if (txtMaCV.getText().isEmpty()) throw new Exception("Vui lòng chọn chức vụ cần sửa!");
            BigDecimal phuCap = new BigDecimal(txtPhuCapCV.getText().trim().isEmpty() ? "0" : txtPhuCapCV.getText().trim());
            danhMucService.capNhatChucVu(Integer.parseInt(txtMaCV.getText()), txtTenCV.getText(), phuCap);
            JOptionPane.showMessageDialog(this, "Cập nhật chức vụ thành công!");
            lamMoiCV();
            loadDataChucVu();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaCV() {
        try {
            if (txtMaCV.getText().isEmpty()) throw new Exception("Vui lòng chọn chức vụ cần xóa!");
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa chức vụ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                danhMucService.xoaChucVu(Integer.parseInt(txtMaCV.getText()));
                JOptionPane.showMessageDialog(this, "Đã xóa chức vụ thành công!");
                lamMoiCV();
                loadDataChucVu();
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
