package com.ui.nhanvien;

import com.model.ChucVu;
import com.model.NhanVien;
import com.model.PhongBan;
import com.service.DanhMucService;
import com.service.NhanVienService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. TOP: THANH TÌM KIẾM
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTop.setBorder(BorderFactory.createTitledBorder("Tìm kiếm nhân viên (Theo Index Họ Tên)"));
        pnlTop.add(new JLabel("Họ tên nhân viên:"));
        txtTimKiem = new JTextField(20);
        pnlTop.add(txtTimKiem);
        JButton btnTimKiem = new JButton("Tìm kiếm");
        JButton btnTaiLai = new JButton("Tải lại tất cả");
        pnlTop.add(btnTimKiem);
        pnlTop.add(btnTaiLai);

        add(pnlTop, BorderLayout.NORTH);

        // 2. CENTER: BẢNG DỮ LIỆU
        modelNhanVien = new DefaultTableModel(new String[]{
            "Mã NV", "Họ Tên", "Ngày Sinh", "Phái", "CCCD", "SĐT", "Email", "Lương CB", "Phòng Ban", "Chức Vụ", "Trạng Thái", "Tài Khoản"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblNhanVien = new JTable(modelNhanVien);
        add(new JScrollPane(tblNhanVien), BorderLayout.CENTER);

        // 3. SOUTH: FORM THÔNG TIN & NÚT BẤM
        JPanel pnlSouth = new JPanel(new BorderLayout(5, 5));
        
        JPanel pnlForm = new JPanel(new GridLayout(6, 4, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin hồ sơ nhân sự"));

        pnlForm.add(new JLabel("Mã Nhân Viên:"));
        txtMaNV = new JTextField();
        txtMaNV.setEditable(false);
        pnlForm.add(txtMaNV);

        pnlForm.add(new JLabel("Họ và Tên (*):"));
        txtHoTen = new JTextField();
        pnlForm.add(txtHoTen);

        pnlForm.add(new JLabel("Ngày Sinh (YYYY-MM-DD):"));
        txtNgaySinh = new JTextField();
        pnlForm.add(txtNgaySinh);

        pnlForm.add(new JLabel("Giới Tính:"));
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        pnlForm.add(cboGioiTinh);

        pnlForm.add(new JLabel("CCCD (12 số):"));
        txtCCCD = new JTextField();
        pnlForm.add(txtCCCD);

        pnlForm.add(new JLabel("Số Điện Thoại (10 số):"));
        txtSoDienThoai = new JTextField();
        pnlForm.add(txtSoDienThoai);

        pnlForm.add(new JLabel("Email (*):"));
        txtEmail = new JTextField();
        pnlForm.add(txtEmail);

        pnlForm.add(new JLabel("Địa Chỉ:"));
        txtDiaChi = new JTextField();
        pnlForm.add(txtDiaChi);

        pnlForm.add(new JLabel("Ngày Vào Làm (YYYY-MM-DD):"));
        txtNgayVaoLam = new JTextField();
        pnlForm.add(txtNgayVaoLam);

        pnlForm.add(new JLabel("Lương Cơ Bản (VNĐ):"));
        txtLuongCoBan = new JTextField();
        pnlForm.add(txtLuongCoBan);

        pnlForm.add(new JLabel("Phòng Ban:"));
        cboPhongBan = new JComboBox<>();
        pnlForm.add(cboPhongBan);

        pnlForm.add(new JLabel("Chức Vụ:"));
        cboChucVu = new JComboBox<>();
        pnlForm.add(cboChucVu);

        pnlForm.add(new JLabel("Trạng Thái:"));
        cboTrangThai = new JComboBox<>(new String[]{"DANG_LAM_VIEC", "NGHI_VIEC"});
        pnlForm.add(cboTrangThai);

        // Subpanel: Cấp tài khoản đồng thời
        pnlTaiKhoan = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        pnlTaiKhoan.setBorder(BorderFactory.createTitledBorder("Cấp phát tài khoản đăng nhập (Transaction)"));
        chkCapTaiKhoan = new JCheckBox("Cấp tài khoản");
        txtTenDangNhap = new JTextField(10);
        txtTenDangNhap.setEnabled(false);
        txtMatKhau = new JPasswordField(10);
        txtMatKhau.setEnabled(false);
        cboVaiTro = new JComboBox<>(new String[]{"Employee", "HR_Manager", "Payroll_Officer", "DB_Admin"});
        cboVaiTro.setEnabled(false);

        pnlTaiKhoan.add(chkCapTaiKhoan);
        pnlTaiKhoan.add(new JLabel("Username:"));
        pnlTaiKhoan.add(txtTenDangNhap);
        pnlTaiKhoan.add(new JLabel("Mật khẩu:"));
        pnlTaiKhoan.add(txtMatKhau);
        pnlTaiKhoan.add(new JLabel("Vai trò:"));
        pnlTaiKhoan.add(cboVaiTro);

        // Buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnThem = new JButton("Thêm Nhân Viên");
        JButton btnCapNhat = new JButton("Cập Nhật");
        JButton btnXoa = new JButton("Xóa (Soft Delete Check)");
        JButton btnLamMoi = new JButton("Làm Mới Form");

        pnlButtons.add(btnThem);
        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);

        JPanel pnlFormAndAccount = new JPanel(new BorderLayout(5, 5));
        pnlFormAndAccount.add(pnlForm, BorderLayout.CENTER);
        pnlFormAndAccount.add(pnlTaiKhoan, BorderLayout.SOUTH);

        pnlSouth.add(pnlFormAndAccount, BorderLayout.CENTER);
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

    private void loadComboboxData() {
        try {
            cboPhongBan.removeAllItems();
            List<PhongBan> listPB = danhMucService.layPhongBanDangHoatDong();
            for (PhongBan pb : listPB) cboPhongBan.addItem(pb);

            cboChucVu.removeAllItems();
            List<ChucVu> listCV = danhMucService.layTatCaChucVu();
            for (ChucVu cv : listCV) cboChucVu.addItem(cv);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu danh mục: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
