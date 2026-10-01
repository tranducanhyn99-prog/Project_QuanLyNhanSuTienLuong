package com.ui.luong;

import com.dao.NhanVienDAO;
import com.model.KhauTruNhanVien;
import com.model.NhanVien;
import com.model.PhuCapNhanVien;
import com.service.PhuCapKhauTruService;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. Toolbar Kỳ Làm Việc (Tháng / Năm)
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        cbThang = new JComboBox<>();
        for (int i = 1; i <= 12; i++) cbThang.addItem(i);
        cbNam = new JComboBox<>();
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2020; i <= curYear + 2; i++) cbNam.addItem(i);

        cbThang.setSelectedItem(9);
        cbNam.setSelectedItem(2026);
        UITheme.styleComboBox(cbThang);
        UITheme.styleComboBox(cbNam);

        btnLoc = new JButton("Lọc dữ liệu kỳ");
        UITheme.stylePrimaryButton(btnLoc);

        btnXoaKyLuong = new JButton("Xóa kỳ lương chưa chốt (Transaction)");
        UITheme.styleDangerButton(btnXoaKyLuong);

        JLabel lblThang = new JLabel("Tháng:");
        lblThang.setFont(UITheme.FONT_BODY);
        JLabel lblNam = new JLabel("Năm:");
        lblNam.setFont(UITheme.FONT_BODY);

        pnlTop.add(lblThang);
        pnlTop.add(cbThang);
        pnlTop.add(lblNam);
        pnlTop.add(cbNam);
        pnlTop.add(btnLoc);
        pnlTop.add(btnXoaKyLuong);

        add(pnlTop, BorderLayout.NORTH);

        // 2. TabbedPane trung tâm
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);
        tabbedPane.addTab("1. Phụ Cấp Nhân Viên", createPhuCapPanel());
        tabbedPane.addTab("2. Khấu Trừ Nhân Viên", createKhauTruPanel());
        tabbedPane.addTab("3. Tổng Hợp Kỳ (View & Function)", createTongHopPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // Listeners
        btnLoc.addActionListener(e -> loadAllData());
        btnXoaKyLuong.addActionListener(e -> handleXoaKyLuong());
    }

    private JPanel createPhuCapPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.BG_APP);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Bảng danh sách
        String[] cols = {"Mã PC", "Mã NV", "Họ Tên", "Khoản Phụ Cấp", "Số Tiền (VNĐ)", "Ngày Ghi", "Ghi Chú"};
        modelPhuCap = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhuCap = new JTable(modelPhuCap);
        UITheme.styleTable(tblPhuCap);

        JScrollPane scroll = new JScrollPane(tblPhuCap);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        // Form nhập liệu
        JPanel pnlForm = new JPanel(new GridLayout(2, 5, 10, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        cbNhanVienPC = new JComboBox<>();
        UITheme.styleComboBox(cbNhanVienPC);
        txtTenPhuCap = new JTextField("Phụ cấp ăn trưa");
        UITheme.styleTextField(txtTenPhuCap);
        txtSoTienPC = new JTextField("730000");
        UITheme.styleTextField(txtSoTienPC);
        txtGhiChuPC = new JTextField("Phụ cấp định kỳ");
        UITheme.styleTextField(txtGhiChuPC);

        btnThemPC = new JButton("Thêm phụ cấp");
        UITheme.stylePrimaryButton(btnThemPC);
        btnXoaPC = new JButton("Xóa chọn");
        UITheme.styleDangerButton(btnXoaPC);

        pnlForm.add(new JLabel("Nhân viên:"));
        pnlForm.add(new JLabel("Tên khoản phụ cấp:"));
        pnlForm.add(new JLabel("Số tiền (VNĐ):"));
        pnlForm.add(new JLabel("Ghi chú:"));
        pnlForm.add(new JLabel("Thao tác:"));

        pnlForm.add(cbNhanVienPC);
        pnlForm.add(txtTenPhuCap);
        pnlForm.add(txtSoTienPC);
        pnlForm.add(txtGhiChuPC);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.add(btnThemPC);
        pnlBtn.add(btnXoaPC);
        pnlForm.add(pnlBtn);

        pnl.add(pnlForm, BorderLayout.SOUTH);

        btnThemPC.addActionListener(e -> handleThemPhuCap());
        btnXoaPC.addActionListener(e -> handleXoaPhuCap());

        return pnl;
    }

    private JPanel createKhauTruPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.BG_APP);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã KT", "Mã NV", "Họ Tên", "Khoản Khấu Trừ", "Số Tiền (VNĐ)", "Ngày Ghi", "Lý Do"};
        modelKhauTru = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKhauTru = new JTable(modelKhauTru);
        UITheme.styleTable(tblKhauTru);

        JScrollPane scroll = new JScrollPane(tblKhauTru);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        JPanel pnlForm = new JPanel(new GridLayout(2, 5, 10, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        cbNhanVienKT = new JComboBox<>();
        UITheme.styleComboBox(cbNhanVienKT);
        txtTenKhauTru = new JTextField("Tạm ứng lương");
        UITheme.styleTextField(txtTenKhauTru);
        txtSoTienKT = new JTextField("1000000");
        UITheme.styleTextField(txtSoTienKT);
        txtLyDoKT = new JTextField("Ứng lương cá nhân");
        UITheme.styleTextField(txtLyDoKT);

        btnThemKT = new JButton("Thêm khấu trừ");
        UITheme.stylePrimaryButton(btnThemKT);
        btnXoaKT = new JButton("Xóa chọn");
        UITheme.styleDangerButton(btnXoaKT);

        pnlForm.add(new JLabel("Nhân viên:"));
        pnlForm.add(new JLabel("Tên khoản khấu trừ:"));
        pnlForm.add(new JLabel("Số tiền (VNĐ):"));
        pnlForm.add(new JLabel("Lý do:"));
        pnlForm.add(new JLabel("Thao tác:"));

        pnlForm.add(cbNhanVienKT);
        pnlForm.add(txtTenKhauTru);
        pnlForm.add(txtSoTienKT);
        pnlForm.add(txtLyDoKT);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.add(btnThemKT);
        pnlBtn.add(btnXoaKT);
        pnlForm.add(pnlBtn);

        pnl.add(pnlForm, BorderLayout.SOUTH);

        btnThemKT.addActionListener(e -> handleThemKhauTru());
        btnXoaKT.addActionListener(e -> handleXoaKhauTru());

        return pnl;
    }

    private JPanel createTongHopPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.BG_APP);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã NV", "Họ Tên", "Kỳ (Tháng/Năm)", "Số Khoản PC", "Tổng Tiền Phụ Cấp (View)", "Tổng Khấu Trừ (Function)"};
        modelTongHop = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTongHop = new JTable(modelTongHop);
        UITheme.styleTable(tblTongHop);

        JScrollPane scroll = new JScrollPane(tblTongHop);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private void loadNhanVienCombobox() {
        try {
            listNhanVien = nhanVienDAO.getAll();
            cbNhanVienPC.removeAllItems();
            cbNhanVienKT.removeAllItems();
            if (listNhanVien != null) {
                for (NhanVien nv : listNhanVien) {
                    String item = nv.getMaNV() + " - " + nv.getHoTen();
                    cbNhanVienPC.addItem(item);
                    cbNhanVienKT.addItem(item);
                }
            }
        } catch (Exception ex) {
            // Trường hợp DB chưa sẵn sàng nhân viên
        }
    }

    private void loadAllData() {
        int thang = (int) cbThang.getSelectedItem();
        int nam = (int) cbNam.getSelectedItem();

        // 1. Load Phụ Cấp
        try {
            modelPhuCap.setRowCount(0);
            List<PhuCapNhanVien> listPC = service.getListPhuCap(thang, nam);
            for (PhuCapNhanVien p : listPC) {
                modelPhuCap.addRow(new Object[]{
                        p.getMaPCNV(), p.getMaNV(), p.getHoTen(), p.getTenPhuCap(),
                        moneyFormat.format(p.getSoTien()), p.getNgayGhiNhan(), p.getGhiChu()
                });
            }
        } catch (Exception ex) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Lỗi tải Phụ cấp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Bỏ qua dialog tải Phụ cấp: " + ex.getMessage());
            }
        }

        // 2. Load Khấu Trừ
        try {
            modelKhauTru.setRowCount(0);
            List<KhauTruNhanVien> listKT = service.getListKhauTru(thang, nam);
            for (KhauTruNhanVien k : listKT) {
                modelKhauTru.addRow(new Object[]{
                        k.getMaKTNV(), k.getMaNV(), k.getHoTen(), k.getTenKhauTru(),
                        moneyFormat.format(k.getSoTien()), k.getNgayGhiNhan(), k.getLyDo()
                });
            }
        } catch (Exception ex) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Lỗi tải Khấu trừ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Bỏ qua dialog tải Khấu trừ: " + ex.getMessage());
            }
        }

        // 3. Load Tổng Hợp từ View & Function
        try {
            modelTongHop.setRowCount(0);
            List<Map<String, Object>> listTH = service.getTongHopPhuCap(thang, nam);
            for (Map<String, Object> map : listTH) {
                int maNV = (int) map.get("MaNV");
                BigDecimal tongPC = (BigDecimal) map.get("TongTienPhuCap");
                BigDecimal tongKT = service.getTongKhauTruNV(maNV, thang, nam);

                modelTongHop.addRow(new Object[]{
                        maNV,
                        map.get("HoTen"),
                        map.get("Thang") + "/" + map.get("Nam"),
                        map.get("SoKhoanPhuCap"),
                        moneyFormat.format(tongPC),
                        moneyFormat.format(tongKT)
                });
            }
        } catch (Exception ex) {
            // Không ngắt app nếu bảng view rỗng
        }
    }

    private void handleThemPhuCap() {
        try {
            int selectedIdx = cbNhanVienPC.getSelectedIndex();
            if (selectedIdx < 0 || listNhanVien == null || listNhanVien.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
                return;
            }
            int maNV = listNhanVien.get(selectedIdx).getMaNV();
            int thang = (int) cbThang.getSelectedItem();
            int nam = (int) cbNam.getSelectedItem();
            String ten = txtTenPhuCap.getText().trim();
            BigDecimal tien = new BigDecimal(txtSoTienPC.getText().trim());
            String ghiChu = txtGhiChuPC.getText().trim();

            PhuCapNhanVien pc = new PhuCapNhanVien(maNV, thang, nam, ten, tien, null, ghiChu);
            service.themPhuCap(pc);
            JOptionPane.showMessageDialog(this, "Thêm phụ cấp thành công!");
            loadAllData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm phụ cấp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleXoaPhuCap() {
        int row = tblPhuCap.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng phụ cấp cần xóa!");
            return;
        }
        int maPCNV = (int) modelPhuCap.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "Xác nhận xóa khoản phụ cấp mã " + maPCNV + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                service.xoaPhuCap(maPCNV);
                loadAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa phụ cấp: " + ex.getMessage());
            }
        }
    }

    private void handleThemKhauTru() {
        try {
            int selectedIdx = cbNhanVienKT.getSelectedIndex();
            if (selectedIdx < 0 || listNhanVien == null || listNhanVien.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
                return;
            }
            int maNV = listNhanVien.get(selectedIdx).getMaNV();
            int thang = (int) cbThang.getSelectedItem();
            int nam = (int) cbNam.getSelectedItem();
            String ten = txtTenKhauTru.getText().trim();
            BigDecimal tien = new BigDecimal(txtSoTienKT.getText().trim());
            String lyDo = txtLyDoKT.getText().trim();

            KhauTruNhanVien kt = new KhauTruNhanVien(maNV, thang, nam, ten, tien, null, lyDo);
            service.themKhauTru(kt);
            JOptionPane.showMessageDialog(this, "Thêm khoản khấu trừ thành công!");
            loadAllData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm khấu trừ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleXoaKhauTru() {
        int row = tblKhauTru.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng khấu trừ cần xóa!");
            return;
        }
        int maKTNV = (int) modelKhauTru.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "Xác nhận xóa khoản khấu trừ mã " + maKTNV + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                service.xoaKhauTru(maKTNV);
                loadAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa khấu trừ: " + ex.getMessage());
            }
        }
    }

    private void handleXoaKyLuong() {
        int thang = (int) cbThang.getSelectedItem();
        int nam = (int) cbNam.getSelectedItem();
        int opt = JOptionPane.showConfirmDialog(this,
                "CẢNH BÁO TRANSACTION: Hệ thống sẽ gọi sp_XoaKyLuongChuaChot để xóa toàn bộ chi tiết và bảng lương tháng " + thang + "/" + nam + ".\nNếu kỳ lương đã chốt, Transaction sẽ tự động ROLLBACK toàn bộ.\nBạn có muốn tiếp tục?",
                "Xác nhận Transaction xóa kỳ lương", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                service.xoaKyLuongChuaChot(thang, nam);
                JOptionPane.showMessageDialog(this, "Đã thực hiện xong Transaction xóa kỳ lương thành công!");
                loadAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Transaction Rollback do lỗi: " + ex.getMessage(), "Kết quả Transaction", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}