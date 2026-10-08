package com.ui.luong;

import com.dao.NhanVienDAO;
import com.model.KhauTruNhanVien;
import com.model.NhanVien;
import com.model.PhuCapNhanVien;
import com.service.PhuCapKhauTruService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

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

        cbThang.setSelectedItem(Calendar.getInstance().get(Calendar.MONTH) + 1);
        cbNam.setSelectedItem(curYear);
        UITheme.styleComboBox(cbThang);
        UITheme.styleComboBox(cbNam);

        // Khắc phục nút Lọc dữ liệu kỳ không bị chữ trắng trên nền trắng
        btnLoc = createPrimaryButton("Lọc dữ liệu kỳ");

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
        btnXoaKyLuong.setVisible(com.session.Session.getInstance().hasRole("DB_Admin", "Payroll_Officer"));
        pnlTop.add(btnXoaKyLuong);

        add(pnlTop, BorderLayout.NORTH);

        // 2. TabbedPane trung tâm
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);
        tabbedPane.addTab("1. Phụ Cấp Nhân Viên", createPhuCapPanel());
        tabbedPane.addTab("2. Khấu Trừ Nhân Viên", createKhauTruPanel());
        tabbedPane.addTab("3. Tổng Hợp Kỳ (View & Function)", createTongHopPanel());
        add(tabbedPane, BorderLayout.CENTER);

        btnLoc.addActionListener(e -> loadAllData());
        cbThang.addActionListener(e -> loadAllData());
        cbNam.addActionListener(e -> loadAllData());
        btnXoaKyLuong.addActionListener(e -> handleXoaKyLuong());
    }

    private JPanel createPhuCapPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.BG_APP);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Bảng danh sách phụ cấp
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
        JPanel pnlForm = new JPanel(new BorderLayout(14, 0));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Cột nhập thông tin (4 trường dữ liệu)
        JPanel pnlInputs = new JPanel(new GridLayout(2, 4, 12, 6));
        pnlInputs.setOpaque(false);

        cbNhanVienPC = new JComboBox<>();
        UITheme.styleComboBox(cbNhanVienPC);
        txtTenPhuCap = new JTextField("Phụ cấp ăn trưa");
        UITheme.styleTextField(txtTenPhuCap);
        txtSoTienPC = new JTextField("730000");
        UITheme.styleTextField(txtSoTienPC);
        txtGhiChuPC = new JTextField("Phụ cấp định kỳ");
        UITheme.styleTextField(txtGhiChuPC);

        JLabel lblNv = new JLabel("Nhân viên:");
        lblNv.setFont(UITheme.FONT_BODY);
        JLabel lblTen = new JLabel("Tên khoản phụ cấp:");
        lblTen.setFont(UITheme.FONT_BODY);
        JLabel lblTien = new JLabel("Số tiền (VNĐ):");
        lblTien.setFont(UITheme.FONT_BODY);
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(UITheme.FONT_BODY);

        pnlInputs.add(lblNv);
        pnlInputs.add(lblTen);
        pnlInputs.add(lblTien);
        pnlInputs.add(lblGhiChu);

        pnlInputs.add(cbNhanVienPC);
        pnlInputs.add(txtTenPhuCap);
        pnlInputs.add(txtSoTienPC);
        pnlInputs.add(txtGhiChuPC);

        // Cột nút thao tác bên phải
        JPanel pnlActions = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlActions.setOpaque(false);

        JLabel lblActions = new JLabel("Thao tác:");
        lblActions.setFont(UITheme.FONT_BODY);
        pnlActions.add(lblActions);

        // Tạo nút Thêm phụ cấp nền xanh ngọc lục bảo chữ trắng (Solid Button)
        btnThemPC = createSuccessButton("Thêm phụ cấp");
        btnThemPC.setToolTipText("Thêm mới một khoản phụ cấp cho nhân viên đã chọn");

        btnXoaPC = new JButton("Xóa phụ cấp");
        UITheme.styleDangerButton(btnXoaPC);
        btnXoaPC.setToolTipText("Xóa khoản phụ cấp đang được chọn trên bảng");

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.add(btnThemPC);
        pnlBtn.add(btnXoaPC);
        pnlActions.add(pnlBtn);

        pnlForm.add(pnlInputs, BorderLayout.CENTER);
        pnlForm.add(pnlActions, BorderLayout.EAST);
        pnl.add(pnlForm, BorderLayout.SOUTH);

        btnThemPC.addActionListener(e -> handleThemPhuCap());
        btnXoaPC.addActionListener(e -> handleXoaPhuCap());

        return pnl;
    }

    private JPanel createKhauTruPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.BG_APP);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Bảng danh sách khấu trừ
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

        JPanel pnlForm = new JPanel(new BorderLayout(14, 0));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Cột nhập thông tin
        JPanel pnlInputs = new JPanel(new GridLayout(2, 4, 12, 6));
        pnlInputs.setOpaque(false);

        cbNhanVienKT = new JComboBox<>();
        UITheme.styleComboBox(cbNhanVienKT);
        txtTenKhauTru = new JTextField("Tạm ứng lương");
        UITheme.styleTextField(txtTenKhauTru);
        txtSoTienKT = new JTextField("1000000");
        UITheme.styleTextField(txtSoTienKT);
        txtLyDoKT = new JTextField("Ứng lương cá nhân");
        UITheme.styleTextField(txtLyDoKT);

        JLabel lblNv = new JLabel("Nhân viên:");
        lblNv.setFont(UITheme.FONT_BODY);
        JLabel lblTen = new JLabel("Tên khoản khấu trừ:");
        lblTen.setFont(UITheme.FONT_BODY);
        JLabel lblTien = new JLabel("Số tiền (VNĐ):");
        lblTien.setFont(UITheme.FONT_BODY);
        JLabel lblLyDo = new JLabel("Lý do:");
        lblLyDo.setFont(UITheme.FONT_BODY);

        pnlInputs.add(lblNv);
        pnlInputs.add(lblTen);
        pnlInputs.add(lblTien);
        pnlInputs.add(lblLyDo);

        pnlInputs.add(cbNhanVienKT);
        pnlInputs.add(txtTenKhauTru);
        pnlInputs.add(txtSoTienKT);
        pnlInputs.add(txtLyDoKT);

        // Cột nút thao tác bên phải
        JPanel pnlActions = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlActions.setOpaque(false);

        JLabel lblActions = new JLabel("Thao tác:");
        lblActions.setFont(UITheme.FONT_BODY);
        pnlActions.add(lblActions);

        // Tạo nút Thêm khấu trừ nền xanh ngọc lục bảo chữ trắng (Solid Button)
        btnThemKT = createSuccessButton("Thêm khấu trừ");
        btnThemKT.setToolTipText("Thêm mới một khoản khấu trừ cho nhân viên đã chọn");

        btnXoaKT = new JButton("Xóa khấu trừ");
        UITheme.styleDangerButton(btnXoaKT);
        btnXoaKT.setToolTipText("Xóa khoản khấu trừ đang được chọn trên bảng");

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.add(btnThemKT);
        pnlBtn.add(btnXoaKT);
        pnlActions.add(pnlBtn);

        pnlForm.add(pnlInputs, BorderLayout.CENTER);
        pnlForm.add(pnlActions, BorderLayout.EAST);
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

    // =========================================================================
    // HÀM TẠO NÚT BẤM TỰ VẼ NỀN (KHẮC PHỤC 100% LỖI TÀNG HÌNH TRÊN WINDOWS L&F)
    // =========================================================================
    private JButton createSuccessButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(13, 120, 82)); // Nhấn chuột: xanh đậm
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(22, 175, 120)); // Rê chuột: xanh sáng
                } else {
                    g2.setColor(new Color(16, 149, 102)); // Trạng thái thường: Emerald Green
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 75, 150));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(25, 125, 225));
                } else {
                    g2.setColor(new Color(0, 102, 204)); // Màu Primary của dự án
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
                "CẢNH BÁO TRANSACTION: Hệ thống sẽ gọi sp_XoaKyLuongChuaChot để xóa toàn bộ chi tiết và bảng lương tháng " + thang + "/" + nam + ".\nNếu kỳ lương đã chốt, Transaction sẽ tự động ROLLBACK toàn bộ.\nBạn có muốn tiếp tục?",
                "Xác nhận Transaction xóa kỳ lương",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (opt == JOptionPane.YES_OPTION) {
            try {
                DatabaseTask.runExclusive(this, () -> { service.xoaKyLuongChuaChot(thang, nam); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã thực hiện xong Transaction xóa kỳ lương thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
                            });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Transaction Rollback do lỗi: " + ex.getMessage(), "Kết quả Transaction", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
