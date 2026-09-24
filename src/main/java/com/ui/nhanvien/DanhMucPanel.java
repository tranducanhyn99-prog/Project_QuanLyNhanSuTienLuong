package com.ui.nhanvien;

import com.model.ChucVu;
import com.model.PhongBan;
import com.service.DanhMucService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

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
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel Trái: Phòng Ban ---
        JPanel pnlPB = new JPanel(new BorderLayout(5, 5));
        pnlPB.setBorder(BorderFactory.createTitledBorder("Quản lý Phòng Ban"));

        modelPhongBan = new DefaultTableModel(new String[]{"Mã PB", "Tên Phòng Ban", "Số ĐT", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPhongBan = new JTable(modelPhongBan);
        pnlPB.add(new JScrollPane(tblPhongBan), BorderLayout.CENTER);

        JPanel pnlFormPB = new JPanel(new GridLayout(5, 2, 5, 5));
        pnlFormPB.add(new JLabel("Mã PB:"));
        txtMaPB = new JTextField();
        txtMaPB.setEditable(false);
        pnlFormPB.add(txtMaPB);

        pnlFormPB.add(new JLabel("Tên Phòng Ban:"));
        txtTenPB = new JTextField();
        pnlFormPB.add(txtTenPB);

        pnlFormPB.add(new JLabel("Số Điện Thoại:"));
        txtSdtPB = new JTextField();
        pnlFormPB.add(txtSdtPB);

        pnlFormPB.add(new JLabel("Trạng Thái:"));
        cboTrangThaiPB = new JComboBox<>(new String[]{"HOAT_DONG", "NGUNG_HOAT_DONG"});
        pnlFormPB.add(cboTrangThaiPB);

        JPanel pnlBtnPB = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThemPB = new JButton("Thêm");
        JButton btnSuaPB = new JButton("Sửa");
        JButton btnXoaPB = new JButton("Xóa");
        JButton btnLamMoiPB = new JButton("Làm mới");
        pnlBtnPB.add(btnThemPB);
        pnlBtnPB.add(btnSuaPB);
        pnlBtnPB.add(btnXoaPB);
        pnlBtnPB.add(btnLamMoiPB);

        JPanel pnlSouthPB = new JPanel(new BorderLayout());
        pnlSouthPB.add(pnlFormPB, BorderLayout.CENTER);
        pnlSouthPB.add(pnlBtnPB, BorderLayout.SOUTH);
        pnlPB.add(pnlSouthPB, BorderLayout.SOUTH);

        // --- Panel Phải: Chức Vụ ---
        JPanel pnlCV = new JPanel(new BorderLayout(5, 5));
        pnlCV.setBorder(BorderFactory.createTitledBorder("Quản lý Chức Vụ"));

        modelChucVu = new DefaultTableModel(new String[]{"Mã CV", "Tên Chức Vụ", "Phụ Cấp Chức Vụ"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblChucVu = new JTable(modelChucVu);
        pnlCV.add(new JScrollPane(tblChucVu), BorderLayout.CENTER);

        JPanel pnlFormCV = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlFormCV.add(new JLabel("Mã CV:"));
        txtMaCV = new JTextField();
        txtMaCV.setEditable(false);
        pnlFormCV.add(txtMaCV);

        pnlFormCV.add(new JLabel("Tên Chức Vụ:"));
        txtTenCV = new JTextField();
        pnlFormCV.add(txtTenCV);

        pnlFormCV.add(new JLabel("Phụ Cấp (VNĐ):"));
        txtPhuCapCV = new JTextField();
        pnlFormCV.add(txtPhuCapCV);

        JPanel pnlBtnCV = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThemCV = new JButton("Thêm");
        JButton btnSuaCV = new JButton("Sửa");
        JButton btnXoaCV = new JButton("Xóa");
        JButton btnLamMoiCV = new JButton("Làm mới");
        pnlBtnCV.add(btnThemCV);
        pnlBtnCV.add(btnSuaCV);
        pnlBtnCV.add(btnXoaCV);
        pnlBtnCV.add(btnLamMoiCV);

        JPanel pnlSouthCV = new JPanel(new BorderLayout());
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
            JOptionPane.showMessageDialog(this, "Lỗi tải danh mục Phòng Ban: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Lỗi tải danh mục Chức Vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
