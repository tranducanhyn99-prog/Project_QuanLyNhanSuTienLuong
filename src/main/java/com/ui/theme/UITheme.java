package com.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UITheme – Design System trung tâm cho ứng dụng Quản lý Nhân sự & Tiền lương.
 *
 * Phong cách thiết kế: Modern SaaS / Enterprise ERP (chuẩn Gusto, Rippling, BambooHR)
 * - Gam màu hiện đại: Nền Slate-50 (#F8FAFC), Accent Indigo-600 (#4F46E5), Sidebar Dark Slate (#0F172A).
 * - Typography Segoe UI sắc nét, độ tương phản cao đạt chuẩn WCAG AA.
 * - Nút bấm bo tròn 8px với công nghệ tự vẽ (Custom Antialiased Painting) triệt tiêu lỗi trắng-trên-trắng.
 * - Bảng dữ liệu thoáng đãng (Row height 38px), tag trạng thái dạng Rounded Pill Badge.
 *
 * @author Nhóm 06 – DBMS Enterprise
 */
public final class UITheme {

    private UITheme() {}

    // ─── BẢNG MÀU CHUẨN (ENTERPRISE PALETTE) ────────────────────────────
    public static final Color PRIMARY          = new Color(79, 70, 229);     // #4F46E5 (Indigo 600)
    public static final Color PRIMARY_HOVER    = new Color(67, 56, 202);     // #4338CA (Indigo 700)
    public static final Color PRIMARY_ACTIVE   = new Color(55, 48, 163);     // #3730A3 (Indigo 800)
    public static final Color PRIMARY_LIGHT    = new Color(238, 242, 255);   // #EEF2FF (Indigo 50)
    public static final Color PRIMARY_BORDER   = new Color(199, 210, 254);   // #C7D2FE (Indigo 200)

    public static final Color BG_APP           = new Color(248, 250, 252);   // #F8FAFC (Slate 50)
    public static final Color BG_SIDEBAR       = new Color(15, 23, 42);      // #0F172A (Slate 900 - Dark Navy)
    public static final Color BG_SIDEBAR_HOVER = new Color(30, 41, 59);      // #1E293B (Slate 800)
    public static final Color BG_SIDEBAR_ACTIVE= new Color(51, 65, 85);      // #334155 (Slate 700)
    public static final Color BG_CARD          = Color.WHITE;                // #FFFFFF (Nền thẻ)
    public static final Color BG_ROW_ALT       = new Color(248, 250, 252);   // #F8FAFC (Zebra stripe)

    public static final Color BORDER           = new Color(226, 232, 240);   // #E2E8F0 (Slate 200)
    public static final Color BORDER_INPUT     = new Color(203, 213, 225);   // #CBD5E1 (Slate 300)
    public static final Color BORDER_FOCUS     = PRIMARY;

    public static final Color TEXT_MAIN        = new Color(15, 23, 42);      // #0F172A (Slate 900)
    public static final Color TEXT_MUTED       = new Color(100, 116, 139);   // #64748B (Slate 500)
    public static final Color TEXT_SUBTLE      = new Color(148, 163, 184);   // #94A3B8 (Slate 400)
    public static final Color TEXT_SIDEBAR     = new Color(203, 213, 225);   // #CBD5E1 (Slate 300)
    public static final Color TEXT_SIDEBAR_MUTED= new Color(100, 116, 139);  // #64748B

    // Trạng thái (Status tags)
    public static final Color SUCCESS_TEXT     = new Color(4, 120, 87);      // #047857 (Emerald 700)
    public static final Color SUCCESS_BG       = new Color(236, 253, 245);   // #ECFDF5 (Emerald 50)
    public static final Color SUCCESS_BORDER   = new Color(167, 243, 208);   // #A7F3D0 (Emerald 200)

    public static final Color WARNING_TEXT     = new Color(180, 83, 9);      // #B45309 (Amber 700)
    public static final Color WARNING_BG       = new Color(254, 243, 199);   // #FEF3C7 (Amber 50)
    public static final Color WARNING_BORDER   = new Color(253, 230, 138);   // #FDE68A (Amber 200)

    public static final Color DANGER_TEXT      = new Color(190, 18, 60);     // #BE123C (Rose 700)
    public static final Color DANGER_BG        = new Color(255, 241, 242);   // #FFF1F2 (Rose 50)
    public static final Color DANGER_BORDER    = new Color(254, 205, 211);   // #FECDD3 (Rose 200)

    public static final Color INFO_TEXT        = new Color(3, 105, 161);     // #0369A1 (Sky 700)
    public static final Color INFO_BG          = new Color(240, 249, 255);   // #F0F9FF (Sky 50)
    public static final Color INFO_BORDER      = new Color(186, 230, 253);   // #BAE6FD (Sky 200)

    // ─── TYPOGRAPHY (SEGOE UI) ───────────────────────────────────────
    public static final String FONT_FAMILY     = "Segoe UI";

    public static final Font FONT_HERO         = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_TITLE_LARGE  = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_TITLE        = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_SUBTITLE     = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BODY         = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD    = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_CAPTION      = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_CAPTION_BOLD = new Font(FONT_FAMILY, Font.BOLD, 11);

    // ─── THIẾT LẬP GLOBAL UI DEFAULTS ────────────────────────────────
    public static void setupGlobalUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Label.foreground", TEXT_MAIN);
        UIManager.put("Button.font", FONT_BODY_BOLD);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BODY_BOLD);
        UIManager.put("Spinner.font", FONT_BODY);
        UIManager.put("CheckBox.font", FONT_BODY);
        UIManager.put("TabbedPane.font", FONT_BODY);
        UIManager.put("Panel.background", BG_APP);
    }

    // ─── CUSTOM MODERN BUTTON (TRIỆT TIÊU LỖI WHITE-ON-WHITE) ─────────
    public static class ModernButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private Color normalFg;
        private Color borderColor;
        private int cornerRadius = 8;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, Color bg, Color hover, Color fg, Color border) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = hover;
            this.pressedBg = hover.darker();
            this.normalFg = fg;
            this.borderColor = border;

            setFont(FONT_BODY_BOLD);
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) { isHovered = true; repaint(); }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) { isPressed = true; repaint(); }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width + 24, 120), Math.max(d.height, 40));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            Color curBg = normalBg;
            if (!isEnabled()) {
                curBg = new Color(241, 245, 249);
            } else if (isPressed) {
                curBg = pressedBg;
            } else if (isHovered) {
                curBg = hoverBg;
            }

            // Fill rounded background
            g2.setColor(curBg);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

            // Draw border if present
            if (borderColor != null && isEnabled()) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, cornerRadius, cornerRadius));
            }
            g2.dispose();

            // Set foreground and let Swing paint text & icon
            setForeground(isEnabled() ? normalFg : TEXT_SUBTLE);
            super.paintComponent(g);
        }
    }

    // ─── ROUNDED BUTTON UI CHO MỌI JBUTTON ─────────────────────────────
    public static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final int radius;
        private final Color borderCol;

        public RoundedButtonUI(int radius, Color borderCol) {
            this.radius = radius;
            this.borderCol = borderCol;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            c.setOpaque(false);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = b.getWidth();
            int h = b.getHeight();

            Color bg = b.getBackground();
            if (!b.isEnabled()) {
                bg = new Color(241, 245, 249);
            } else if (b.getModel().isPressed()) {
                bg = bg.darker();
            }

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            if (borderCol != null && b.isEnabled()) {
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, radius, radius));
            }
            g2.dispose();

            super.paint(g, c);
        }
    }

    // ─── ĐỊNH DẠNG NÚT BẤM (BUTTON STYLES CHO JBUTTON CÓ SẴN) ───────────

    public static void stylePrimaryButton(JButton btn) {
        btn.setUI(new RoundedButtonUI(8, PRIMARY_HOVER));
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(PRIMARY_HOVER); btn.setForeground(Color.WHITE); }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(PRIMARY_ACTIVE); btn.setForeground(Color.WHITE); }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(PRIMARY_HOVER); btn.setForeground(Color.WHITE); }
            }
        });
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setUI(new RoundedButtonUI(8, BORDER_INPUT));
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(new Color(241, 245, 249)); }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(Color.WHITE); }
            }
        });
    }

    public static void styleDangerButton(JButton btn) {
        btn.setUI(new RoundedButtonUI(8, DANGER_BORDER));
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(DANGER_TEXT);
        btn.setBackground(DANGER_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(new Color(254, 202, 202)); }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(DANGER_BG); }
            }
        });
    }

    public static void styleSuccessButton(JButton btn) {
        btn.setUI(new RoundedButtonUI(8, SUCCESS_BORDER));
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(SUCCESS_TEXT);
        btn.setBackground(SUCCESS_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(new Color(167, 243, 208)); }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(SUCCESS_BG); }
            }
        });
    }

    public static void styleWarningButton(JButton btn) {
        btn.setUI(new RoundedButtonUI(8, WARNING_BORDER));
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(WARNING_TEXT);
        btn.setBackground(WARNING_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(new Color(253, 230, 138)); }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) { btn.setBackground(WARNING_BG); }
            }
        });
    }


    // ─── ĐỊNH DẠNG BẢNG DỮ LIỆU CHUẨN ENTERPRISE (ROW 38PX, PILL TAGS) ─
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_MAIN);
        table.setBackground(Color.WHITE);
        table.setRowHeight(38);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_ACTIVE);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Pill Renderer for Table Cells
        TableCellRenderer pillRenderer = new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) defaultRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setFont(FONT_BODY);
                c.setBorder(new EmptyBorder(0, 12, 0, 12));

                if (isSelected) {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(PRIMARY_ACTIVE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : BG_ROW_ALT);
                    c.setForeground(TEXT_MAIN);
                }

                if (value != null) {
                    String str = value.toString().trim();
                    if (str.matches("^\\d+(\\.\\d+)?$") || str.endsWith("VNĐ") || str.endsWith("đ") || str.contains(",") || str.contains(".")) {
                        c.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else if (isStatusString(str)) {
                        c.setHorizontalAlignment(SwingConstants.CENTER);
                        return createStatusPill(str, isSelected);
                    } else if (str.matches("^\\d{4}-\\d{2}-\\d{2}$") || str.matches("^\\d{2}:\\d{2}$")) {
                        c.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        c.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                } else {
                    c.setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(pillRenderer);
        }
    }

    private static boolean isStatusString(String str) {
        return str.equals("HOAT_DONG") || str.equals("DANG_LAM_VIEC") || str.equals("DA_CHOT") || str.equals("CO_MAT")
                || str.equals("DI_TRE") || str.equals("NGHI_VIEC") || str.equals("CHUA_CHOT") || str.equals("KHOA")
                || str.equals("Đang làm việc") || str.equals("Đã chốt") || str.equals("Chưa chốt") || str.equals("Hoạt động")
                || str.equals("Có mặt") || str.equals("Đi trễ") || str.equals("Nghỉ việc");
    }

    public static Component createStatusPill(String status, boolean isSelected) {
        String labelText = status;
        Color fg = SUCCESS_TEXT;
        Color bg = SUCCESS_BG;
        Color border = SUCCESS_BORDER;

        switch (status) {
            case "HOAT_DONG":
            case "Hoạt động":
                labelText = "Hoạt động";
                fg = SUCCESS_TEXT; bg = SUCCESS_BG; border = SUCCESS_BORDER;
                break;
            case "DANG_LAM_VIEC":
            case "Đang làm việc":
                labelText = "Đang làm việc";
                fg = SUCCESS_TEXT; bg = SUCCESS_BG; border = SUCCESS_BORDER;
                break;
            case "DA_CHOT":
            case "Đã chốt":
                labelText = "Đã chốt sổ";
                fg = SUCCESS_TEXT; bg = SUCCESS_BG; border = SUCCESS_BORDER;
                break;
            case "CO_MAT":
            case "Có mặt":
                labelText = "Có mặt";
                fg = SUCCESS_TEXT; bg = SUCCESS_BG; border = SUCCESS_BORDER;
                break;
            case "DI_TRE":
            case "Đi trễ":
                labelText = "Đi trễ";
                fg = WARNING_TEXT; bg = WARNING_BG; border = WARNING_BORDER;
                break;
            case "CHUA_CHOT":
            case "Chưa chốt":
                labelText = "Chưa chốt";
                fg = WARNING_TEXT; bg = WARNING_BG; border = WARNING_BORDER;
                break;
            case "NGHI_VIEC":
            case "Nghỉ việc":
                labelText = "Nghỉ việc";
                fg = DANGER_TEXT; bg = DANGER_BG; border = DANGER_BORDER;
                break;
            case "KHOA":
            case "Khóa":
                labelText = "Đã khóa";
                fg = DANGER_TEXT; bg = DANGER_BG; border = DANGER_BORDER;
                break;
        }

        JPanel pillPanel = new JPanel(new GridBagLayout());
        pillPanel.setOpaque(true);
        pillPanel.setBackground(isSelected ? PRIMARY_LIGHT : Color.WHITE);

        JLabel pill = new JLabel(labelText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(167, 243, 208));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        pill.setFont(FONT_CAPTION_BOLD);
        pill.setForeground(fg);
        pill.setBackground(bg);
        pill.setOpaque(false);
        pill.setBorder(new CompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(3, 10, 3, 10)
        ));

        pillPanel.add(pill);
        return pillPanel;
    }

    // ─── ĐỊNH DẠNG Ô NHẬP LIỆU (INPUTS) ─────────────────────────────────

    public static void styleTextField(JTextField tf) {
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_INPUT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
    }

    public static void stylePasswordField(JPasswordField pf) {
        pf.setFont(FONT_BODY);
        pf.setForeground(TEXT_MAIN);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(PRIMARY);
        pf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_INPUT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 36));
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setFont(FONT_BODY);
        cb.setForeground(TEXT_MAIN);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 36));
    }

    // ─── KHUNG THẺ VÀ THỐNG KÊ (CARDS & KPI WIDGETS) ────────────────────

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JPanel createSectionHeader(String title) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SUBTITLE);
        lbl.setForeground(new Color(51, 65, 85));

        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    public static JPanel createStatCard(String title, String value, String subtitle, Color themeColor, String iconSymbol) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        // Top Row: Title + Icon Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font(FONT_FAMILY, Font.BOLD, 11));
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblIcon = new JLabel(iconSymbol, SwingConstants.CENTER);
        lblIcon.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 25));
        lblIcon.setForeground(themeColor);
        lblIcon.setPreferredSize(new Dimension(28, 28));
        lblIcon.setBorder(new LineBorder(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 60), 1, true));

        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(lblIcon, BorderLayout.EAST);

        // Center: Large Value
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font(FONT_FAMILY, Font.BOLD, 22));
        lblValue.setForeground(TEXT_MAIN);
        lblValue.setBorder(new EmptyBorder(8, 0, 4, 0));

        // Bottom: Subtitle / trend
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_CAPTION);
        lblSub.setForeground(themeColor);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(lblValue);
        centerPanel.add(lblSub);

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    public static JLabel createBadge(String text, Color fg, Color bg, Color border) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_CAPTION_BOLD);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new CompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(3, 10, 3, 10)
        ));
        return badge;
    }
}
