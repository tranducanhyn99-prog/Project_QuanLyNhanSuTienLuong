package com.ui.nhanvien;

import com.model.ChucVu;
import com.model.NhanVien;
import com.model.PhongBan;
import com.service.DanhMucService;
import com.service.NhanVienService;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * NhanVienPanel – Quản lý Hồ sơ nhân sự chuẩn Enterprise.
 *
 * Giao diện Fluent Design:
 * - Bảng dữ liệu phẳng chuẩn Enterprise với chiều cao hàng 32px.
 * - Form nhập liệu chia 3 cột thẳng hàng, thoáng đãng.
 * - Nút bấm phân cấp màu sắc chuẩn: Thêm (Primary), Cập nhật (Secondary), Xóa (Danger).
 * - Giữ nguyên 100% logic Stored Procedure, Transaction và Soft Delete trigger.
 *
 * @author Nhóm 06 – DBMS Enterprise
 */
public class NhanVienPanel extends JPanel {

    private final NhanVienService nhanVienService = new NhanVienService();
    private final DanhMucService danhMucService = new DanhMucService();

    // Table & Model
    private JTable tblNhanVien;
    private DefaultTableModel modelNhanVien;

    // Form fields
    private JTextField txtMaNV, txtHoTen, txtNgaySinh, txtCCCD, txtDiaChi, txtSoDienThoai, txtEmail, txtNgayVaoLam, txtLuongCoBan;
    private JComboBox<String> cboGioiTinh, cboTrangThai;
    private JComboBox<PhongBan> cboPhongBan;
    private JComboBox<ChucVu> cboChucVu;

    // Tài khoản kèm theo (Transaction tích hợp)
    private JCheckBox chkCapTaiKhoan;
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cboVaiTro;
    private JPanel pnlTaiKhoan;

    // Search
    private JTextField txtTimKiem;

    public NhanVienPanel() {
        initComponents();
        loadComboboxData();
        loadTableData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. TOP: THANH TÌM KIẾM PHẲNG
        JPanel pnlTop = new JPanel(new BorderLayout(14, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchLeft.setOpaque(false);

        JLabel lblTimKiem = new JLabel("Tìm kiếm nhân sự:");
        lblTimKiem.setFont(UITheme.FONT_BODY_BOLD);
        lblTimKiem.setForeground(UITheme.TEXT_MAIN);

        txtTimKiem = new JTextField(24);
        UITheme.styleTextField(txtTimKiem);

        JButton btnTimKiem = new JButton("Tìm kiếm (Index)");
        UITheme.stylePrimaryButton(btnTimKiem);

        JButton btnTaiLai = new JButton("Tải lại danh sách");
        UITheme.styleSecondaryButton(btnTaiLai);

        searchLeft.add(lblTimKiem);
        searchLeft.add(txtTimKiem);
        searchLeft.add(btnTimKiem);
        searchLeft.add(btnTaiLai);

        pnlTop.add(searchLeft, BorderLayout.WEST);
        add(pnlTop, BorderLayout.NORTH);

        // 2. CENTER: BẢNG DỮ LIỆU CHUẨN ENTERPRISE
        modelNhanVien = new DefaultTableModel(new String[]{
            "Mã NV", "Họ Tên", "Ngày Sinh", "Phái", "CCCD", "SĐT", "Email", "Lương CB (VNĐ)", "Phòng Ban", "Chức Vụ", "Trạng Thái", "Tài Khoản"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblNhanVien = new JTable(modelNhanVien);
        UITheme.styleTable(tblNhanVien);

        JScrollPane scrollTable = new JScrollPane(tblNhanVien);
        scrollTable.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollTable.getViewport().setBackground(Color.WHITE);
        add(scrollTable, BorderLayout.CENTER);

        // 3. SOUTH: FORM THÔNG TIN & NÚT BẤM (CARD PHẲNG)
        JPanel pnlSouth = new JPanel(new BorderLayout(0, 10));
        pnlSouth.setOpaque(false);

        JPanel pnlFormCard = new JPanel(new BorderLayout(0, 10));
        pnlFormCard.setBackground(Color.WHITE);
        pnlFormCard.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        pnlFormCard.add(UITheme.createSectionHeader("Thông tin hồ sơ nhân sự"), BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Mã NV, Họ Tên, Ngày Sinh
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0; pnlForm.add(createFieldLabel("Mã Nhân Viên:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtMaNV = new JTextField(); txtMaNV.setEditable(false); UITheme.styleTextField(txtMaNV); pnlForm.add(txtMaNV, gbc);

        gbc.gridx = 2; gbc.weightx = 0; pnlForm.add(createFieldLabel("Họ và Tên (*):"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; txtHoTen = new JTextField(); UITheme.styleTextField(txtHoTen); pnlForm.add(txtHoTen, gbc);

        gbc.gridx = 4; gbc.weightx = 0; pnlForm.add(createFieldLabel("Ngày Sinh (YYYY-MM-DD):"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0; txtNgaySinh = new JTextField(); UITheme.styleTextField(txtNgaySinh); pnlForm.add(txtNgaySinh, gbc);

        // Row 1: Giới Tính, CCCD, Số Điện Thoại
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0; pnlForm.add(createFieldLabel("Giới Tính:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"}); UITheme.styleComboBox(cboGioiTinh); pnlForm.add(cboGioiTinh, gbc);

        gbc.gridx = 2; gbc.weightx = 0; pnlForm.add(createFieldLabel("CCCD (12 số):"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; txtCCCD = new JTextField(); UITheme.styleTextField(txtCCCD); pnlForm.add(txtCCCD, gbc);

        gbc.gridx = 4; gbc.weightx = 0; pnlForm.add(createFieldLabel("Số Điện Thoại (10 số):"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0; txtSoDienThoai = new JTextField(); UITheme.styleTextField(txtSoDienThoai); pnlForm.add(txtSoDienThoai, gbc);

        // Row 2: Email, Địa Chỉ, Ngày Vào Làm
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0; pnlForm.add(createFieldLabel("Email (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtEmail = new JTextField(); UITheme.styleTextField(txtEmail); pnlForm.add(txtEmail, gbc);

        gbc.gridx = 2; gbc.weightx = 0; pnlForm.add(createFieldLabel("Địa Chỉ:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; txtDiaChi = new JTextField(); UITheme.styleTextField(txtDiaChi); pnlForm.add(txtDiaChi, gbc);

        gbc.gridx = 4; gbc.weightx = 0; pnlForm.add(createFieldLabel("Ngày Vào Làm:"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0; txtNgayVaoLam = new JTextField(); UITheme.styleTextField(txtNgayVaoLam); pnlForm.add(txtNgayVaoLam, gbc);

        // Row 3: Lương Cơ Bản, Phòng Ban, Chức Vụ
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.weightx = 0; pnlForm.add(createFieldLabel("Lương Cơ Bản:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtLuongCoBan = new JTextField(); UITheme.styleTextField(txtLuongCoBan); pnlForm.add(txtLuongCoBan, gbc);

        gbc.gridx = 2; gbc.weightx = 0; pnlForm.add(createFieldLabel("Phòng Ban:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; cboPhongBan = new JComboBox<>(); UITheme.styleComboBox(cboPhongBan); pnlForm.add(cboPhongBan, gbc);

        gbc.gridx = 4; gbc.weightx = 0; pnlForm.add(createFieldLabel("Chức Vụ:"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0; cboChucVu = new JComboBox<>(); UITheme.styleComboBox(cboChucVu); pnlForm.add(cboChucVu, gbc);

        // Row 4: Trạng Thái
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.weightx = 0; pnlForm.add(createFieldLabel("Trạng Thái:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; cboTrangThai = new JComboBox<>(new String[]{"DANG_LAM_VIEC", "NGHI_VIEC"}); UITheme.styleComboBox(cboTrangThai); pnlForm.add(cboTrangThai, gbc);

        // Subpanel: Cấp tài khoản đồng thời
        pnlTaiKhoan = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        pnlTaiKhoan.setBackground(new Color(248, 250, 252));
        pnlTaiKhoan.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        chkCapTaiKhoan = new JCheckBox("Cấp tài khoản đăng nhập (Transaction Atomicity)");
        chkCapTaiKhoan.setFont(UITheme.FONT_BODY_BOLD);
        chkCapTaiKhoan.setOpaque(false);

        txtTenDangNhap = new JTextField(10);
        UITheme.styleTextField(txtTenDangNhap);
        txtTenDangNhap.setEnabled(false);

        txtMatKhau = new JPasswordField(10);
        UITheme.stylePasswordField(txtMatKhau);
        txtMatKhau.setEnabled(false);

        cboVaiTro = new JComboBox<>(new String[]{"Employee", "HR_Manager", "Payroll_Officer", "DB_Admin"});
        UITheme.styleComboBox(cboVaiTro);
        cboVaiTro.setEnabled(false);

        pnlTaiKhoan.add(chkCapTaiKhoan);
        pnlTaiKhoan.add(new JLabel("Username:"));
        pnlTaiKhoan.add(txtTenDangNhap);
        pnlTaiKhoan.add(new JLabel("Mật khẩu:"));
        pnlTaiKhoan.add(txtMatKhau);
        pnlTaiKhoan.add(new JLabel("Vai trò:"));
        pnlTaiKhoan.add(cboVaiTro);

        gbc.gridx = 2; gbc.gridy = 4; gbc.gridwidth = 4;
        pnlForm.add(pnlTaiKhoan, gbc);

        pnlFormCard.add(pnlForm, BorderLayout.CENTER);

        // Buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnThem = new JButton("Thêm Nhân Viên");
        UITheme.stylePrimaryButton(btnThem);

        JButton btnCapNhat = new JButton("Cập Nhật");
        UITheme.styleSecondaryButton(btnCapNhat);

        JButton btnXoa = new JButton("Xóa (Soft Delete)");
        UITheme.styleDangerButton(btnXoa);

        JButton btnLamMoi = new JButton("Làm Mới Form");
        UITheme.styleSecondaryButton(btnLamMoi);

        pnlButtons.add(btnThem);
        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);

        pnlSouth.add(pnlFormCard, BorderLayout.CENTER);
        pnlSouth.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlSouth, BorderLayout.SOUTH);

        // Event listeners
        chkCapTaiKhoan.addActionListener(e -> {
            boolean sel = chkCapTaiKhoan.isSelected();
            txtTenDangNhap.setEnabled(sel);
            txtMatKhau.setEnabled(sel);
            cboVaiTro.setEnabled(sel);
        });

        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            int row = tblNhanVien.getSelectedRow();
            if (row >= 0) fillFormFromTableRow(row);
        });

        btnThem.addActionListener(e -> xuLyThemNhanVien());
        btnCapNhat.addActionListener(e -> xuLyCapNhatNhanVien());
        btnXoa.addActionListener(e -> xuLyXoaNhanVien());
        btnLamMoi.addActionListener(e -> lamMoiForm());
        btnTimKiem.addActionListener(e -> xuLyTimKiem());
        btnTaiLai.addActionListener(e -> loadTableData());
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MAIN);
        return lbl;
    }

    private void loadComboboxData() {
        try {
            cboPhongBan.removeAllItems();
            List<PhongBan> listPB = danhMucService.layPhongBanDangHoatDong();
            for (PhongBan pb : listPB) cboPhongBan.addItem(pb);

            cboChucVu.removeAllItems();
            List<ChucVu> listCV = danhMucService.layTatCaChucVu();
            for (ChucVu cv : listCV) cboChucVu.addItem(cv);
        } catch (Exception e) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu danh mục: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Bỏ qua dialog khởi tạo danh mục: " + e.getMessage());
            }
        }
    }

    private void loadTableData() {
        modelNhanVien.setRowCount(0);
        try {
            List<NhanVien> list = nhanVienService.layDanhSachNhanVien();
            for (NhanVien nv : list) {
                modelNhanVien.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getHoTen(),
                    nv.getNgaySinh(),
                    nv.getGioiTinh(),
                    nv.getCccd(),
                    nv.getSoDienThoai(),
                    nv.getEmail(),
                    nv.getLuongCoBan(),
                    nv.getTenPB(),
                    nv.getTenCV(),
                    nv.getTrangThai(),
                    nv.getTenDangNhap() != null ? nv.getTenDangNhap() : "[Chưa cấp]"
                });
            }
        } catch (Exception e) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Lỗi tải danh sách nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Bỏ qua dialog khởi tạo nhân viên: " + e.getMessage());
            }
        }
    }

    private void xuLyTimKiem() {
        modelNhanVien.setRowCount(0);
        try {
            List<NhanVien> list = nhanVienService.timKiemTheoTen(txtTimKiem.getText());
            for (NhanVien nv : list) {
                modelNhanVien.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getHoTen(),
                    nv.getNgaySinh(),
                    nv.getGioiTinh(),
                    nv.getCccd(),
                    nv.getSoDienThoai(),
                    nv.getEmail(),
                    nv.getLuongCoBan(),
                    nv.getTenPB(),
                    nv.getTenCV(),
                    nv.getTrangThai(),
                    nv.getTenDangNhap() != null ? nv.getTenDangNhap() : "[Chưa cấp]"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromTableRow(int row) {
        txtMaNV.setText(modelNhanVien.getValueAt(row, 0).toString());
        txtHoTen.setText(modelNhanVien.getValueAt(row, 1).toString());
        txtNgaySinh.setText(modelNhanVien.getValueAt(row, 2).toString());
        cboGioiTinh.setSelectedItem(modelNhanVien.getValueAt(row, 3).toString());
        txtCCCD.setText(modelNhanVien.getValueAt(row, 4).toString());
        txtSoDienThoai.setText(modelNhanVien.getValueAt(row, 5).toString());
        txtEmail.setText(modelNhanVien.getValueAt(row, 6).toString());
        txtLuongCoBan.setText(modelNhanVien.getValueAt(row, 7).toString());
        cboTrangThai.setSelectedItem(modelNhanVien.getValueAt(row, 10).toString());

        // Select PhongBan & ChucVu
        String tenPB = modelNhanVien.getValueAt(row, 8).toString();
        for (int i = 0; i < cboPhongBan.getItemCount(); i++) {
            if (cboPhongBan.getItemAt(i).getTenPB().equalsIgnoreCase(tenPB)) {
                cboPhongBan.setSelectedIndex(i);
                break;
            }
        }
        String tenCV = modelNhanVien.getValueAt(row, 9).toString();
        for (int i = 0; i < cboChucVu.getItemCount(); i++) {
            if (cboChucVu.getItemAt(i).getTenCV().equalsIgnoreCase(tenCV)) {
                cboChucVu.setSelectedIndex(i);
                break;
            }
        }
    }

    private void xuLyThemNhanVien() {
        try {
            NhanVien nv = layThongTinForm();
            boolean taoTK = chkCapTaiKhoan.isSelected();
            String user = txtTenDangNhap.getText();
            String pass = new String(txtMatKhau.getPassword());
            String role = cboVaiTro.getSelectedItem().toString();

            int newId = nhanVienService.themNhanVien(nv, taoTK, user, pass, role);
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công! Mã NV: " + newId + (taoTK ? " (Đã cấp tài khoản thành công)" : ""));
            lamMoiForm();
            loadTableData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Thao tác thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyCapNhatNhanVien() {
        try {
            if (txtMaNV.getText().isEmpty()) throw new Exception("Vui lòng chọn nhân viên cần cập nhật từ danh sách!");
            NhanVien nv = layThongTinForm();
            nv.setMaNV(Integer.parseInt(txtMaNV.getText()));
            nhanVienService.capNhatNhanVien(nv);
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin nhân viên thành công!");
            lamMoiForm();
            loadTableData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaNhanVien() {
        try {
            if (txtMaNV.getText().isEmpty()) throw new Exception("Vui lòng chọn nhân viên cần xóa!");
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Hệ thống sẽ kiểm tra ràng buộc lương và chấm công trước khi xóa.\nBạn có chắc chắn muốn xóa nhân viên này?", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                nhanVienService.xoaNhanVien(Integer.parseInt(txtMaNV.getText()));
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công!");
                lamMoiForm();
                loadTableData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên: " + e.getMessage(), "Thông báo ràng buộc CSDL", JOptionPane.WARNING_MESSAGE);
        }
    }

    private NhanVien layThongTinForm() throws Exception {
        NhanVien nv = new NhanVien();
        nv.setHoTen(txtHoTen.getText().trim());
        try {
            nv.setNgaySinh(LocalDate.parse(txtNgaySinh.getText().trim()));
        } catch (Exception e) {
            throw new Exception("Ngày sinh không đúng định dạng YYYY-MM-DD (Ví dụ: 2000-01-15)!");
        }
        nv.setGioiTinh(cboGioiTinh.getSelectedItem().toString());
        nv.setCccd(txtCCCD.getText().trim());
        nv.setDiaChi(txtDiaChi.getText().trim());
        nv.setSoDienThoai(txtSoDienThoai.getText().trim());
        nv.setEmail(txtEmail.getText().trim());
        if (!txtNgayVaoLam.getText().trim().isEmpty()) {
            try {
                nv.setNgayVaoLam(LocalDate.parse(txtNgayVaoLam.getText().trim()));
            } catch (Exception e) {
                throw new Exception("Ngày vào làm không đúng định dạng YYYY-MM-DD!");
            }
        } else {
            nv.setNgayVaoLam(LocalDate.now());
        }
        try {
            nv.setLuongCoBan(new BigDecimal(txtLuongCoBan.getText().trim()));
        } catch (Exception e) {
            throw new Exception("Lương cơ bản phải là số nguyên hoặc số thập phân hợp lệ!");
        }

        PhongBan pb = (PhongBan) cboPhongBan.getSelectedItem();
        if (pb == null) throw new Exception("Chưa chọn phòng ban!");
        nv.setMaPB(pb.getMaPB());

        ChucVu cv = (ChucVu) cboChucVu.getSelectedItem();
        if (cv == null) throw new Exception("Chưa chọn chức vụ!");
        nv.setMaCV(cv.getMaCV());

        nv.setTrangThai(cboTrangThai.getSelectedItem().toString());
        return nv;
    }

    private void lamMoiForm() {
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtNgaySinh.setText("");
        txtCCCD.setText("");
        txtDiaChi.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtNgayVaoLam.setText("");
        txtLuongCoBan.setText("");
        cboGioiTinh.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        if (cboPhongBan.getItemCount() > 0) cboPhongBan.setSelectedIndex(0);
        if (cboChucVu.getItemCount() > 0) cboChucVu.setSelectedIndex(0);

        chkCapTaiKhoan.setSelected(false);
        txtTenDangNhap.setText("");
        txtTenDangNhap.setEnabled(false);
        txtMatKhau.setText("");
        txtMatKhau.setEnabled(false);
        cboVaiTro.setEnabled(false);

        tblNhanVien.clearSelection();
    }
}
