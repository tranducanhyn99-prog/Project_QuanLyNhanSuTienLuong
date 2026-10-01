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

/**
 * UITheme – Design System trung tâm cho ứng dụng Quản lý Nhân sự & Tiền lương.
 *
 * Phong cách tham chiếu: Microsoft Fluent Design / Modern Enterprise ERP
 * - Gam màu Light theme với điểm nhấn Blue Accent (#0066CC).
 * - Typography Segoe UI nhất quán.
 * - Flat design: Tuyệt đối không dùng gradient lòe loẹt hay animation rườm rà.
 * - Bảng dữ liệu chuẩn Enterprise với chiều cao hàng 32px, padding thoáng đãng.
 *
 * @author Nhóm 06 – DBMS Enterprise
 */
public final class UITheme {

    private UITheme() {}

    // ─── BẢNG MÀU CHUẨN (PALETTE) ────────────────────────────────────
    public static final Color PRIMARY          = new Color(0, 102, 204);     // #0066CC (Fluent Blue)
    public static final Color PRIMARY_HOVER    = new Color(0, 82, 163);      // #0052A3
    public static final Color PRIMARY_ACTIVE   = new Color(0, 61, 122);      // #003D7A
    public static final Color PRIMARY_LIGHT    = new Color(237, 245, 253);   // #EDF5FD (Active/Select tint)
    public static final Color PRIMARY_BORDER   = new Color(204, 226, 247);   // #CCE2F7

    public static final Color BG_APP           = new Color(248, 250, 252);   // #F8FAFC (Nền ứng dụng)
    public static final Color BG_SIDEBAR       = new Color(241, 245, 249);   // #F1F5F9 (Nền sidebar)
    public static final Color BG_CARD          = Color.WHITE;                // #FFFFFF (Nền thẻ/bảng)
    public static final Color BG_ROW_ALT       = new Color(248, 250, 252);   // #F8FAFC (Zebra stripe)

    public static final Color BORDER           = new Color(226, 232, 240);   // #E2E8F0 (Viền mỏng chuẩn)
    public static final Color BORDER_INPUT     = new Color(203, 213, 225);   // #CBD5E1 (Viền ô nhập liệu)
    public static final Color BORDER_FOCUS     = PRIMARY;

    public static final Color TEXT_MAIN        = new Color(30, 41, 59);      // #1E293B (Chữ chính)
    public static final Color TEXT_MUTED       = new Color(100, 116, 139);   // #64748B (Chữ phụ)
    public static final Color TEXT_SUBTLE     = new Color(148, 163, 184);   // #94A3B8 (Chữ mờ/placeholder)

    // Trạng thái (Status tags)
    public static final Color SUCCESS_TEXT     = new Color(21, 128, 61);     // #15803D
    public static final Color SUCCESS_BG       = new Color(220, 252, 231);   // #DCFCE7
    public static final Color SUCCESS_BORDER   = new Color(134, 239, 172);   // #86EFAC

    public static final Color WARNING_TEXT     = new Color(180, 83, 9);      // #B45309
    public static final Color WARNING_BG       = new Color(254, 243, 199);   // #FEF3C7
    public static final Color WARNING_BORDER   = new Color(253, 230, 138);   // #FDE68A

    public static final Color DANGER_TEXT      = new Color(185, 28, 28);     // #B91C1C
    public static final Color DANGER_BG        = new Color(254, 226, 226);   // #FEE2E2
    public static final Color DANGER_BORDER    = new Color(252, 165, 165);   // #FCA5A5

    public static final Color INFO_TEXT        = new Color(3, 105, 161);     // #0369A1
    public static final Color INFO_BG          = new Color(224, 242, 254);   // #E0F2FE
    public static final Color INFO_BORDER      = new Color(186, 230, 253);   // #BAE6FD

    // ─── TYPOGRAPHY (SEGOE UI) ───────────────────────────────────────
    public static final String FONT_FAMILY     = "Segoe UI";

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

    // ─── ĐỊNH DẠNG BẢNG DỮ LIỆU CHUẨN ENTERPRISE ─────────────────────
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_MAIN);
        table.setBackground(Color.WHITE);
        table.setRowHeight(32);
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
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_INPUT));

        // Renderer cho từng ô: có padding 8px và xen kẽ màu dòng (Zebra)
        TableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setFont(FONT_BODY);
                c.setBorder(new EmptyBorder(0, 10, 0, 10));

                if (isSelected) {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(PRIMARY_ACTIVE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : BG_ROW_ALT);
                    c.setForeground(TEXT_MAIN);
                }

                // Căn lề thông minh dựa vào loại dữ liệu
                if (value != null) {
                    String str = value.toString().trim();
                    if (str.matches("^\\d+(\\.\\d+)?$") || str.endsWith("VNĐ") || str.endsWith("đ") || str.contains(",")) {
                        c.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else if (str.equals("HOAT_DONG") || str.equals("DANG_LAM_VIEC") || str.equals("DA_CHOT") || str.equals("CO_MAT")
                            || str.equals("DI_TRE") || str.equals("NGHI_VIEC") || str.equals("CHUA_CHOT")) {
                        c.setHorizontalAlignment(SwingConstants.CENTER);
                        renderStatusTag(c, str);
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

            private void renderStatusTag(JLabel c, String status) {
                switch (status) {
                    case "HOAT_DONG":
                    case "DANG_LAM_VIEC":
                    case "DA_CHOT":
                    case "CO_MAT":
                        c.setForeground(SUCCESS_TEXT);
                        break;
                    case "DI_TRE":
                    case "CHUA_CHOT":
                        c.setForeground(WARNING_TEXT);
                        break;
                    case "NGHI_VIEC":
                    case "NGUNG_HOAT_DONG":
                    case "VANG_MAT":
                        c.setForeground(DANGER_TEXT);
                        break;
                    default:
                        c.setForeground(TEXT_MAIN);
                        break;
                }
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
        }
    }

    // ─── ĐỊNH DẠNG NÚT BẤM (BUTTON STYLES) ───────────────────────────

    public static void stylePrimaryButton(JButton btn) {
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY_HOVER, 1, true),
            new EmptyBorder(6, 16, 6, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_ACTIVE);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_HOVER);
            }
        });
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(BORDER_INPUT, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(Color.WHITE);
            }
        });
    }

    public static void styleDangerButton(JButton btn) {
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(DANGER_TEXT);
        btn.setBackground(DANGER_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(DANGER_BORDER, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(254, 202, 202));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(DANGER_BG);
            }
        });
    }

    // ─── ĐỊNH DẠNG INPUT FIELDS & COMBOBOX ────────────────────────────

    public static void styleTextField(JTextField tf) {
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_INPUT, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
    }

    public static void stylePasswordField(JPasswordField pf) {
        pf.setFont(FONT_BODY);
        pf.setForeground(TEXT_MAIN);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(PRIMARY);
        pf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_INPUT, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 32));
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setFont(FONT_BODY);
        cb.setForeground(TEXT_MAIN);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 32));
    }

    // ─── KHUNG CHỨA (CARD & SECTIONS) ────────────────────────────────

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }

    public static JPanel createSectionHeader(String title) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SUBTITLE);
        lbl.setForeground(new Color(51, 65, 85));

        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    public static JLabel createBadge(String text, Color fg, Color bg, Color border) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_CAPTION_BOLD);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new CompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));
        return badge;
    }
}
