package com.ui.nhanvien;

import com.model.ChucVu;
import com.model.NhanVien;
import com.model.PhongBan;
import com.service.DanhMucService;
import com.service.NhanVienService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
    private List<NhanVien> loadedEmployees = java.util.Collections.emptyList();
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
    private JLabel lblSoLuong;

    public NhanVienPanel() {
        initComponents();
        loadComboboxData();
        loadTableData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        JTabbedPane pages = new JTabbedPane();
        UITheme.styleTabbedPane(pages);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(UITheme.createPageHeader("Hồ sơ nhân sự",
                "Quản lý thông tin, công việc và tài khoản của đội ngũ."), BorderLayout.NORTH);

        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new BorderLayout(12, 0));
        txtTimKiem = new JTextField();
        UITheme.styleTextField(txtTimKiem);
        txtTimKiem.setToolTipText("Nhập họ hoặc tên nhân viên rồi nhấn Enter để tìm kiếm");
        JPanel searchField = new JPanel(new BorderLayout(10, 0));
        searchField.setOpaque(false);
        JLabel searchIcon = new JLabel("Tìm theo tên", UITheme.icon("search", 19, UITheme.TEXT_MUTED), SwingConstants.LEFT);
        searchIcon.setFont(UITheme.FONT_CAPTION_BOLD);
        searchIcon.setForeground(UITheme.TEXT_MUTED);
        searchIcon.setIconTextGap(8);
        searchIcon.setLabelFor(txtTimKiem);
        searchField.add(searchIcon, BorderLayout.WEST);
        searchField.add(txtTimKiem, BorderLayout.CENTER);
        searchCard.add(searchField, BorderLayout.CENTER);

        JButton btnTimKiem = new JButton("Tìm kiếm");
        UITheme.stylePrimaryButton(btnTimKiem);
        JButton btnTaiLai = new JButton("Tải lại", UITheme.icon("refresh", 16, UITheme.TEXT_MAIN));
        UITheme.styleSecondaryButton(btnTaiLai);
        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchActions.setOpaque(false);
        searchActions.add(btnTimKiem);
        searchActions.add(btnTaiLai);
        searchCard.add(searchActions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel rosterCard = UITheme.createCardPanel();
        rosterCard.setLayout(new BorderLayout(0, 8));
        JPanel rosterHeader = new JPanel(new BorderLayout());
        rosterHeader.setOpaque(false);
        rosterHeader.add(UITheme.createSectionHeader("Danh sách nhân viên"), BorderLayout.WEST);
        lblSoLuong = new JLabel("Đang tải dữ liệu");
        lblSoLuong.setFont(UITheme.FONT_CAPTION);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        rosterHeader.add(lblSoLuong, BorderLayout.EAST);
        rosterCard.add(rosterHeader, BorderLayout.NORTH);

        modelNhanVien = new DefaultTableModel(new String[]{
            "Mã NV", "Họ và tên", "Ngày sinh", "Giới tính", "CCCD", "Điện thoại",
            "Email", "Lương cơ bản (đ)", "Phòng ban", "Chức vụ", "Trạng thái", "Tài khoản"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblNhanVien = new JTable(modelNhanVien);
        UITheme.styleTable(tblNhanVien);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {70, 180, 110, 90, 130, 120, 190, 145, 160, 150, 145, 130};
        for (int i = 0; i < widths.length; i++) {
            tblNhanVien.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane scrollTable = new JScrollPane(tblNhanVien);
        UITheme.styleScrollPane(scrollTable);
        rosterCard.add(scrollTable, BorderLayout.CENTER);
        JLabel tableHint = new JLabel("Chọn một nhân viên để xem và cập nhật hồ sơ bên dưới.");
        tableHint.setFont(UITheme.FONT_CAPTION);
        tableHint.setForeground(UITheme.TEXT_MUTED);
        tableHint.setText("Nhấp đúp vào nhân viên để mở hồ sơ.");
        JPanel rosterFooter = new JPanel(new BorderLayout(12, 0));
        rosterFooter.setOpaque(false);
        rosterFooter.add(tableHint, BorderLayout.CENTER);
        JButton btnMoHoSo = new JButton("Mở hồ sơ", UITheme.icon("edit", 16, UITheme.TEXT_MAIN));
        UITheme.styleSecondaryButton(btnMoHoSo);
        JButton btnTaoHoSo = new JButton("Thêm nhân viên", UITheme.icon("plus", 16, Color.WHITE));
        UITheme.stylePrimaryButton(btnTaoHoSo);
        JPanel rosterActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rosterActions.setOpaque(false);
        rosterActions.add(btnMoHoSo);
        rosterActions.add(btnTaoHoSo);
        rosterFooter.add(rosterActions, BorderLayout.EAST);
        rosterCard.add(rosterFooter, BorderLayout.SOUTH);

        JPanel rosterPage = new JPanel(new BorderLayout(0, 14));
        rosterPage.setBackground(UITheme.BG_APP);
        rosterPage.setBorder(new EmptyBorder(14, 0, 0, 0));
        rosterPage.add(searchCard, BorderLayout.NORTH);
        rosterPage.add(rosterCard, BorderLayout.CENTER);

        JPanel editorCard = UITheme.createCardPanel();
        editorCard.setLayout(new BorderLayout(0, 8));
        JTabbedPane editorTabs = new JTabbedPane();
        UITheme.styleTabbedPane(editorTabs);

        txtMaNV = new JTextField();
        txtMaNV.setEditable(false);
        txtMaNV.setToolTipText("Mã nhân viên được tạo tự động");
        txtHoTen = new JTextField();
        txtNgaySinh = new JTextField();
        txtCCCD = new JTextField();
        txtDiaChi = new JTextField();
        txtSoDienThoai = new JTextField();
        txtEmail = new JTextField();
        txtNgayVaoLam = new JTextField();
        txtLuongCoBan = new JTextField();
        for (JTextField field : new JTextField[]{txtMaNV, txtHoTen, txtNgaySinh, txtCCCD,
                txtDiaChi, txtSoDienThoai, txtEmail, txtNgayVaoLam, txtLuongCoBan}) {
            UITheme.styleTextField(field);
        }
        txtNgaySinh.setToolTipText("Định dạng năm-tháng-ngày, ví dụ 2000-01-15");
        txtNgayVaoLam.setToolTipText("Định dạng năm-tháng-ngày; bỏ trống để dùng ngày hôm nay");
        txtLuongCoBan.setToolTipText("Nhập số tiền bằng đồng, không dùng dấu phân cách");
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cboTrangThai = new JComboBox<>(new String[]{"DANG_LAM_VIEC", "NGHI_VIEC"});
        cboPhongBan = new JComboBox<>();
        cboChucVu = new JComboBox<>();
        UITheme.styleComboBox(cboGioiTinh);
        UITheme.styleComboBox(cboTrangThai);
        UITheme.styleComboBox(cboPhongBan);
        UITheme.styleComboBox(cboChucVu);

        JPanel personal = new JPanel(new GridLayout(3, 3, 16, 10));
        personal.setBackground(Color.WHITE);
        personal.setBorder(new EmptyBorder(10, 0, 4, 0));
        personal.add(field("Mã nhân viên", txtMaNV));
        personal.add(field("Họ và tên *", txtHoTen));
        personal.add(field("Ngày sinh · yyyy-mm-dd", txtNgaySinh));
        personal.add(field("Giới tính", cboGioiTinh));
        personal.add(field("CCCD · 12 số", txtCCCD));
        personal.add(field("Điện thoại · 10 số", txtSoDienThoai));
        personal.add(field("Email *", txtEmail));
        personal.add(field("Địa chỉ", txtDiaChi));
        personal.add(field("Ngày vào làm · yyyy-mm-dd", txtNgayVaoLam));
        JPanel personalCanvas = new JPanel(new BorderLayout());
        personalCanvas.setBackground(Color.WHITE);
        personalCanvas.add(personal, BorderLayout.NORTH);
        JScrollPane personalScroll = new JScrollPane(personalCanvas);
        UITheme.styleScrollPane(personalScroll);
        personalScroll.setBorder(null);
        editorTabs.addTab("Thông tin cá nhân", personalScroll);

        JPanel work = new JPanel(new BorderLayout(0, 12));
        work.setBackground(Color.WHITE);
        work.setBorder(new EmptyBorder(10, 0, 4, 0));
        JPanel workFields = new JPanel(new GridLayout(1, 4, 14, 0));
        workFields.setOpaque(false);
        workFields.add(field("Lương cơ bản (đ)", txtLuongCoBan));
        workFields.add(field("Phòng ban", cboPhongBan));
        workFields.add(field("Chức vụ", cboChucVu));
        workFields.add(field("Trạng thái làm việc", cboTrangThai));
        work.add(workFields, BorderLayout.NORTH);

        pnlTaiKhoan = new JPanel(new BorderLayout(0, 8));
        pnlTaiKhoan.setBackground(UITheme.BG_APP);
        pnlTaiKhoan.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER),
                new EmptyBorder(10, 12, 12, 12)));
        chkCapTaiKhoan = new JCheckBox("Tạo tài khoản khi thêm nhân viên");
        chkCapTaiKhoan.setFont(UITheme.FONT_BODY_BOLD);
        chkCapTaiKhoan.setForeground(UITheme.TEXT_MAIN);
        chkCapTaiKhoan.setOpaque(false);
        pnlTaiKhoan.add(chkCapTaiKhoan, BorderLayout.NORTH);
        txtTenDangNhap = new JTextField();
        UITheme.styleTextField(txtTenDangNhap);
        txtTenDangNhap.setEnabled(false);
        txtMatKhau = new JPasswordField();
        UITheme.stylePasswordField(txtMatKhau);
        txtMatKhau.setEnabled(false);
        cboVaiTro = new JComboBox<>(com.session.Session.getInstance().hasRole("DB_Admin")
                ? new String[]{"Employee", "HR_Manager", "Payroll_Officer", "DB_Admin"}
                : new String[]{"Employee"});
        UITheme.styleComboBox(cboVaiTro);
        cboVaiTro.setEnabled(false);
        JPanel accountFields = new JPanel(new GridLayout(1, 3, 14, 0));
        accountFields.setOpaque(false);
        accountFields.add(field("Tên đăng nhập", txtTenDangNhap));
        accountFields.add(field("Mật khẩu", txtMatKhau));
        accountFields.add(field("Vai trò", cboVaiTro));
        pnlTaiKhoan.add(accountFields, BorderLayout.CENTER);
        work.add(pnlTaiKhoan, BorderLayout.CENTER);
        JPanel workCanvas = new JPanel(new BorderLayout());
        workCanvas.setBackground(Color.WHITE);
        workCanvas.add(work, BorderLayout.NORTH);
        JScrollPane workScroll = new JScrollPane(workCanvas);
        UITheme.styleScrollPane(workScroll);
        workScroll.setBorder(null);
        editorTabs.addTab("Công việc & tài khoản", workScroll);
        editorCard.add(editorTabs, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnThem = new JButton("Thêm nhân viên", UITheme.icon("plus", 16, Color.WHITE));
        JButton btnCapNhat = new JButton("Cập nhật", UITheme.icon("edit", 16, UITheme.TEXT_MAIN));
        JButton btnXoa = new JButton("Xóa", UITheme.icon("trash", 16, UITheme.DANGER_TEXT));
        JButton btnLamMoi = new JButton("Làm mới");
        UITheme.stylePrimaryButton(btnThem);
        UITheme.styleSecondaryButton(btnCapNhat);
        UITheme.styleDangerButton(btnXoa);
        UITheme.styleSecondaryButton(btnLamMoi);
        actions.add(btnLamMoi);
        actions.add(btnXoa);
        actions.add(btnCapNhat);
        actions.add(btnThem);
        editorCard.add(actions, BorderLayout.SOUTH);
        JPanel editorPage = new JPanel(new BorderLayout());
        editorPage.setBackground(UITheme.BG_APP);
        editorPage.setBorder(new EmptyBorder(14, 0, 0, 0));
        editorPage.add(editorCard, BorderLayout.CENTER);
        pages.addTab("Danh sách nhân viên", UITheme.icon("users", 16, UITheme.TEXT_MUTED), rosterPage);
        pages.addTab("Hồ sơ & tài khoản", UITheme.icon("briefcase", 16, UITheme.TEXT_MUTED), editorPage);
        add(pages, BorderLayout.CENTER);

        chkCapTaiKhoan.addActionListener(e -> {
            boolean selected = chkCapTaiKhoan.isSelected();
            txtTenDangNhap.setEnabled(selected);
            txtMatKhau.setEnabled(selected);
            cboVaiTro.setEnabled(selected);
        });
        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblNhanVien.getSelectedRow();
                if (row >= 0) fillFormFromTableRow(tblNhanVien.convertRowIndexToModel(row));
            }
        });
        btnThem.addActionListener(e -> xuLyThemNhanVien());
        btnCapNhat.addActionListener(e -> xuLyCapNhatNhanVien());
        btnXoa.addActionListener(e -> xuLyXoaNhanVien());
        btnLamMoi.addActionListener(e -> lamMoiForm());
        btnTimKiem.addActionListener(e -> xuLyTimKiem());
        txtTimKiem.addActionListener(e -> xuLyTimKiem());
        btnTaiLai.addActionListener(e -> loadTableData());
        btnMoHoSo.addActionListener(e -> {
            if (tblNhanVien.getSelectedRow() < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để xem hồ sơ.",
                        "Chưa chọn nhân viên", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            pages.setSelectedIndex(1);
        });
        btnTaoHoSo.addActionListener(e -> {
            lamMoiForm();
            pages.setSelectedIndex(1);
            editorTabs.setSelectedIndex(0);
            txtHoTen.requestFocusInWindow();
        });
        tblNhanVien.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && tblNhanVien.getSelectedRow() >= 0) {
                    pages.setSelectedIndex(1);
                }
            }
        });
    }

    private JPanel field(String text, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_CAPTION_BOLD);
        label.setForeground(UITheme.TEXT_MUTED);
        label.setLabelFor(input);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.setMinimumSize(new Dimension(0, 58));
        return panel;
    }

    private void loadComboboxData() {
        DatabaseTask.run(cboPhongBan, () -> new java.util.AbstractMap.SimpleEntry<>(
                danhMucService.layTatCaPhongBan(), danhMucService.layTatCaChucVu()), data -> {
            cboPhongBan.removeAllItems(); cboChucVu.removeAllItems();
            data.getKey().forEach(cboPhongBan::addItem);
            data.getValue().forEach(cboChucVu::addItem);
            int row = tblNhanVien.getSelectedRow();
            if (row >= 0) fillFormFromTableRow(tblNhanVien.convertRowIndexToModel(row));
        });
    }

    private void loadTableData() {
        modelNhanVien.setRowCount(0);
        DatabaseTask.run(tblNhanVien, () -> nhanVienService.layDanhSachNhanVien(), list -> {
            loadedEmployees = list;
            lblSoLuong.setText(list.size() + " nhân viên");
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
        });
    }

    private void xuLyTimKiem() {
        modelNhanVien.setRowCount(0);
        String search = txtTimKiem.getText();
        DatabaseTask.run(tblNhanVien, () -> nhanVienService.timKiemTheoTen(search), list -> {
            loadedEmployees = list;
            lblSoLuong.setText(list.size() + " kết quả tìm kiếm");
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
        });
    }

    private void fillFormFromTableRow(int row) {
        int id = Integer.parseInt(modelNhanVien.getValueAt(row, 0).toString());
        NhanVien selected = null;
        for (NhanVien nv : loadedEmployees) {
            if (nv.getMaNV() == id) {
                selected = nv;
                txtNgayVaoLam.setText(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().toString() : "");
                txtDiaChi.setText(nv.getDiaChi() != null ? nv.getDiaChi() : "");
                break;
            }
        }
        if (selected == null) return;
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
        for (int i = 0; i < cboPhongBan.getItemCount(); i++) {
            if (cboPhongBan.getItemAt(i).getMaPB() == selected.getMaPB()) {
                cboPhongBan.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cboChucVu.getItemCount(); i++) {
            if (cboChucVu.getItemAt(i).getMaCV() == selected.getMaCV()) {
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

            DatabaseTask.runExclusive(this, () -> nhanVienService.themNhanVien(nv, taoTK, user, pass, role), newId -> {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công! Mã NV: " + newId
                    + (taoTK ? "\nĐã tạo hồ sơ tài khoản. Quản trị viên cần kích hoạt quyền đăng nhập cho tài khoản này." : ""));
            lamMoiForm();
            loadTableData();
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Thao tác thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyCapNhatNhanVien() {
        try {
            if (txtMaNV.getText().isEmpty()) throw new Exception("Vui lòng chọn nhân viên cần cập nhật từ danh sách!");
            NhanVien nv = layThongTinForm();
            nv.setMaNV(Integer.parseInt(txtMaNV.getText()));
            DatabaseTask.runExclusive(this, () -> { nhanVienService.capNhatNhanVien(nv); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin nhân viên thành công!");
            lamMoiForm();
            loadTableData();
                    });
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
                int targetId = Integer.parseInt(txtMaNV.getText());
                DatabaseTask.runExclusive(this, () -> { nhanVienService.xoaNhanVien(targetId); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công!");
                lamMoiForm();
                loadTableData();
                            });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên: " + e.getMessage(), "Không thể xóa nhân viên", JOptionPane.WARNING_MESSAGE);
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
