package com.ui.theme;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;

/** Shared, dependency-free light design system for every PeopleOS screen. */
public final class UITheme {
    private UITheme() { }
    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    public static final Color PRIMARY_ACTIVE = new Color(55, 48, 163);
    public static final Color PRIMARY_LIGHT = new Color(238, 240, 255);
    public static final Color PRIMARY_BORDER = new Color(207, 211, 253);
    public static final Color BG_APP = new Color(245, 247, 251);
    public static final Color BG_SIDEBAR = Color.WHITE;
    public static final Color BG_SIDEBAR_HOVER = new Color(246, 247, 253);
    public static final Color BG_SIDEBAR_ACTIVE = PRIMARY_LIGHT;
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_ROW_ALT = new Color(250, 251, 254);
    public static final Color BORDER = new Color(225, 230, 240);
    public static final Color BORDER_INPUT = new Color(185, 196, 214);
    public static final Color BORDER_FOCUS = PRIMARY;
    public static final Color TEXT_MAIN = new Color(23, 36, 59);
    public static final Color TEXT_MUTED = new Color(82, 98, 122);
    public static final Color TEXT_SUBTLE = new Color(94, 106, 128);
    public static final Color TEXT_SIDEBAR = new Color(69, 83, 108);
    public static final Color TEXT_SIDEBAR_MUTED = TEXT_MUTED;
    public static final Color SUCCESS_TEXT = new Color(4, 110, 79);
    public static final Color SUCCESS_BG = new Color(232, 249, 241);
    public static final Color SUCCESS_BORDER = new Color(172, 226, 202);
    public static final Color WARNING_TEXT = new Color(145, 74, 10);
    public static final Color WARNING_BG = new Color(255, 246, 220);
    public static final Color WARNING_BORDER = new Color(241, 217, 161);
    public static final Color DANGER_TEXT = new Color(180, 28, 64);
    public static final Color DANGER_BG = new Color(255, 239, 242);
    public static final Color DANGER_BORDER = new Color(243, 193, 205);
    public static final Color INFO_TEXT = new Color(3, 101, 148);
    public static final Color INFO_BG = new Color(234, 247, 255);
    public static final Color INFO_BORDER = new Color(180, 219, 242);
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_HERO = new Font(FONT_FAMILY, Font.BOLD, 27);
    public static final Font FONT_TITLE_LARGE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_CAPTION_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    private static boolean initialized;

    /** Keep controls and dialogs light even when the operating system uses dark mode. */
    public static synchronized void setupGlobalUI() {
        if (initialized) return;
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }
        for (String name : new String[]{"Label", "Button", "ToggleButton", "TextField", "PasswordField",
                "FormattedTextField", "TextArea", "TextPane", "EditorPane", "ComboBox", "List", "Table",
                "TableHeader", "Spinner", "CheckBox", "RadioButton", "TabbedPane", "Menu", "MenuItem",
                "OptionPane", "ToolTip", "TitledBorder"}) {
            UIManager.put(name + ".font", new FontUIResource(FONT_BODY));
            UIManager.put(name + ".foreground", new ColorUIResource(TEXT_MAIN));
            UIManager.put(name + ".background", new ColorUIResource(BG_CARD));
        }
        UIManager.put("Panel.background", new ColorUIResource(BG_APP));
        UIManager.put("Viewport.background", new ColorUIResource(BG_CARD));
        UIManager.put("OptionPane.background", new ColorUIResource(BG_APP));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT_MAIN));
        UIManager.put("OptionPane.messageFont", new FontUIResource(FONT_BODY));
        UIManager.put("Button.font", new FontUIResource(FONT_BODY_BOLD));
        UIManager.put("Button.select", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("Button.disabledText", new ColorUIResource(TEXT_MUTED));
        UIManager.put("Button.focus", new ColorUIResource(PRIMARY));
        UIManager.put("TextField.inactiveForeground", new ColorUIResource(TEXT_MUTED));
        UIManager.put("TextField.inactiveBackground", new ColorUIResource(BG_APP));
        UIManager.put("TextField.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("TextField.selectionForeground", new ColorUIResource(PRIMARY_ACTIVE));
        UIManager.put("PasswordField.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("PasswordField.selectionForeground", new ColorUIResource(PRIMARY_ACTIVE));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(PRIMARY_ACTIVE));
        UIManager.put("ComboBox.disabledForeground", new ColorUIResource(TEXT_MUTED));
        UIManager.put("ComboBox.disabledBackground", new ColorUIResource(BG_APP));
        UIManager.put("List.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("List.selectionForeground", new ColorUIResource(PRIMARY_ACTIVE));
        UIManager.put("CheckBox.background", new ColorUIResource(BG_CARD));
        UIManager.put("CheckBox.disabledText", new ColorUIResource(TEXT_MUTED));
        UIManager.put("CheckBox.icon", new CheckIcon());
        UIManager.put("TabbedPane.selected", new ColorUIResource(BG_CARD));
        UIManager.put("TabbedPane.focus", new ColorUIResource(PRIMARY));
        UIManager.put("SplitPane.background", new ColorUIResource(BG_APP));
        UIManager.put("SplitPaneDivider.draggingColor", new ColorUIResource(BORDER));
        UIManager.put("ToolTip.background", new ColorUIResource(TEXT_MAIN));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.WHITE));
        UIManager.put("ToolTip.border", new EmptyBorder(8, 10, 8, 10));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("OptionPane.informationIcon", icon("info", 32, INFO_TEXT));
        UIManager.put("OptionPane.warningIcon", icon("info", 32, WARNING_TEXT));
        UIManager.put("OptionPane.errorIcon", icon("close", 32, DANGER_TEXT));
        UIManager.put("OptionPane.questionIcon", icon("info", 32, PRIMARY));
        initialized = true;
    }
    private static Graphics2D smooth(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    public static final class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final Insets insets;
        public RoundedBorder(Color color, int radius, Insets insets) { this.color=color; this.radius=radius; this.insets=insets; }
        @Override public Insets getBorderInsets(Component c) { return (Insets)insets.clone(); }
        @Override public Insets getBorderInsets(Component c, Insets target) {
            target.set(insets.top,insets.left,insets.bottom,insets.right); return target;
        }
        @Override public void paintBorder(Component c, Graphics graphics, int x, int y, int w, int h) {
            Graphics2D g=smooth(graphics);
            boolean focused=c.isFocusOwner() || (c instanceof JComponent && Boolean.TRUE.equals(((JComponent)c).getClientProperty("theme.focus")));
            g.setColor(focused ? BORDER_FOCUS : color); g.setStroke(new BasicStroke(focused ? 1.8f : 1f));
            g.draw(new RoundRectangle2D.Float(x+1f,y+1f,w-2f,h-2f,radius,radius)); g.dispose();
        }
    }

    /** Model-based states also work when a button is activated from the keyboard. */
    public static class RoundedButtonUI extends BasicButtonUI {
        private final int radius;
        private final Color borderCol;
        public RoundedButtonUI(int radius, Color borderCol) { this.radius=radius; this.borderCol=borderCol; }
        @Override public void installUI(JComponent c) {
            super.installUI(c); AbstractButton b=(AbstractButton)c;
            b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setRolloverEnabled(true);
        }
        @Override public void paint(Graphics graphics, JComponent c) {
            AbstractButton b=(AbstractButton)c; Graphics2D g=smooth(graphics); Color bg=b.getBackground();
            if (!b.isEnabled()) bg=new Color(235,239,246);
            else if (b.getModel().isPressed() && b.getModel().isArmed()) bg=mix(bg,TEXT_MAIN,.12f);
            else if (b.getModel().isRollover()) {
                Object hover=b.getClientProperty("theme.hover"); bg=hover instanceof Color ? (Color)hover : mix(bg,PRIMARY,.05f);
            }
            g.setColor(bg); g.fillRoundRect(1,1,c.getWidth()-2,c.getHeight()-2,radius,radius);
            if (borderCol!=null) {
                g.setColor(b.isEnabled() ? borderCol : BORDER); g.drawRoundRect(1,1,c.getWidth()-3,c.getHeight()-3,radius,radius);
            }
            if (b.isFocusOwner()) {
                g.setColor(b.getBackground().equals(PRIMARY) ? Color.WHITE : PRIMARY); g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(4,4,c.getWidth()-9,c.getHeight()-9,Math.max(4,radius-3),Math.max(4,radius-3));
            }
            g.dispose(); super.paint(graphics,c);
        }
        @Override protected void paintText(Graphics g, AbstractButton b, Rectangle rect, String text) {
            if (b.isEnabled()) { super.paintText(g,b,rect,text); return; }
            g.setColor(TEXT_MUTED);
            javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(g,text,b.getDisplayedMnemonicIndex(),rect.x,rect.y+g.getFontMetrics().getAscent());
        }
    }
    public static class ModernButton extends JButton {
        public ModernButton(String text, Color bg, Color hover, Color fg, Color border) {
            super(text); configureButton(this,bg,hover,fg,border);
        }
        @Override public Dimension getPreferredSize() {
            Dimension d=super.getPreferredSize(); return new Dimension(Math.max(d.width,110),Math.max(d.height,38));
        }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE,getPreferredSize().height); }
    }
    private static void configureButton(JButton b, Color bg, Color hover, Color fg, Color border) {
        b.setUI(new RoundedButtonUI(10,border)); b.setFont(FONT_BODY_BOLD); b.setBackground(bg); b.setForeground(fg);
        b.putClientProperty("theme.hover",hover); b.setBorder(new EmptyBorder(9,14,9,14));
        b.setFocusPainted(false); b.setIconTextGap(8); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMinimumSize(new Dimension(36,38));
    }
    public static void stylePrimaryButton(JButton b) { configureButton(b,PRIMARY,PRIMARY_HOVER,Color.WHITE,PRIMARY); }
    public static void styleSecondaryButton(JButton b) { configureButton(b,BG_CARD,BG_SIDEBAR_HOVER,TEXT_MAIN,BORDER_INPUT); }
    public static void styleDangerButton(JButton b) { configureButton(b,DANGER_BG,new Color(255,225,232),DANGER_TEXT,DANGER_BORDER); }
    public static void styleSuccessButton(JButton b) { configureButton(b,SUCCESS_BG,new Color(215,243,229),SUCCESS_TEXT,SUCCESS_BORDER); }
    public static void styleWarningButton(JButton b) { configureButton(b,WARNING_BG,new Color(255,237,190),WARNING_TEXT,WARNING_BORDER); }
    private static Color mix(Color a, Color b, float amount) {
        return new Color(Math.round(a.getRed()*(1-amount)+b.getRed()*amount),Math.round(a.getGreen()*(1-amount)+b.getGreen()*amount),Math.round(a.getBlue()*(1-amount)+b.getBlue()*amount));
    }

    public static void styleTextField(JTextField field) {
        styleTextComponent(field); Dimension d=field.getPreferredSize(); field.setPreferredSize(new Dimension(d.width,36));
    }
    public static void stylePasswordField(JPasswordField field) { styleTextField(field); }
    private static void styleTextComponent(JTextComponent field) {
        field.setFont(FONT_BODY); field.setForeground(TEXT_MAIN); field.setBackground(BG_CARD);
        field.setDisabledTextColor(TEXT_MUTED); field.setCaretColor(PRIMARY);
        field.setSelectionColor(PRIMARY_LIGHT); field.setSelectedTextColor(PRIMARY_ACTIVE);
        field.setBorder(new RoundedBorder(BORDER_INPUT,9,new Insets(7,11,7,11)));
        if (field.getClientProperty("theme.input")==null) {
            field.putClientProperty("theme.input",true);
            field.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { field.repaint(); }
                @Override public void focusLost(FocusEvent e) { field.repaint(); }
            });
        }
    }
    public static void styleComboBox(JComboBox<?> box) {
        box.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrow=new JButton(icon("chevron",14,TEXT_MUTED)); arrow.setUI(new RoundedButtonUI(6,null));
                arrow.setBackground(BG_CARD); arrow.setBorder(new EmptyBorder(0,6,0,6));
                arrow.setPreferredSize(new Dimension(28,30)); arrow.setFocusable(false); return arrow;
            }
        });
        box.setFont(FONT_BODY); box.setForeground(TEXT_MAIN); box.setBackground(BG_CARD);
        box.setBorder(new RoundedBorder(BORDER_INPUT,9,new Insets(3,7,3,3)));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                setText(value==null ? "" : displayText(value.toString())); setFont(FONT_BODY);
                setBorder(new EmptyBorder(index<0 ? 2 : 7,5,index<0 ? 2 : 7,5));
                setForeground(selected ? PRIMARY_ACTIVE : TEXT_MAIN); setBackground(selected ? PRIMARY_LIGHT : BG_CARD); return this;
            }
        });
        Dimension d=box.getPreferredSize(); box.setPreferredSize(new Dimension(d.width,36)); box.setMaximumRowCount(12);
        if (box.isEditable() && box.getEditor().getEditorComponent() instanceof JTextField) styleTextField((JTextField)box.getEditor().getEditorComponent());
    }
    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_BODY); spinner.setBackground(BG_CARD); spinner.setForeground(TEXT_MAIN);
        spinner.setBorder(new RoundedBorder(BORDER_INPUT,9,new Insets(3,4,3,4)));
        spinner.setPreferredSize(new Dimension(Math.max(76,spinner.getPreferredSize().width),36));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JFormattedTextField text=((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
            text.setFont(FONT_BODY); text.setForeground(TEXT_MAIN); text.setBackground(BG_CARD);
            text.setDisabledTextColor(TEXT_MUTED); text.setBorder(new EmptyBorder(2,7,2,5));
            text.setSelectionColor(PRIMARY_LIGHT); text.setSelectedTextColor(PRIMARY_ACTIVE);
            if (text.getClientProperty("theme.spinnerFocus")==null) {
                text.putClientProperty("theme.spinnerFocus",true);
                text.addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) {
                        spinner.putClientProperty("theme.focus",true); spinner.repaint();
                    }
                    @Override public void focusLost(FocusEvent e) {
                        spinner.putClientProperty("theme.focus",false); spinner.repaint();
                    }
                });
            }
        }
    }
    public static void styleScrollPane(JScrollPane pane) {
        pane.setBorder(new RoundedBorder(BORDER,12,new Insets(1,1,1,1)));
        pane.setBackground(BG_CARD); pane.getViewport().setBackground(BG_CARD);
        if (pane.getViewport().getView() instanceof JTable) {
            JTable table=(JTable)pane.getViewport().getView();
            pane.setColumnHeaderView(table.getTableHeader());
        }
        for (JScrollBar bar : new JScrollBar[]{pane.getVerticalScrollBar(),pane.getHorizontalScrollBar()}) {
            bar.setUnitIncrement(24); bar.setBackground(BG_CARD);
            bar.setUI(new BasicScrollBarUI() {
                @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
                @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
                private JButton zeroButton() {
                    JButton b=new JButton(); b.setPreferredSize(new Dimension()); b.setMinimumSize(new Dimension()); b.setMaximumSize(new Dimension()); return b;
                }
                @Override protected void paintTrack(Graphics g,JComponent c,Rectangle r) {
                    g.setColor(BG_CARD); g.fillRect(r.x,r.y,r.width,r.height);
                }
                @Override protected void paintThumb(Graphics graphics,JComponent c,Rectangle r) {
                    if (!c.isEnabled() || r.isEmpty()) return; Graphics2D g=smooth(graphics);
                    g.setColor(isThumbRollover() ? TEXT_SUBTLE : new Color(192,202,219));
                    g.fillRoundRect(r.x+2,r.y+2,Math.max(4,r.width-4),Math.max(4,r.height-4),8,8); g.dispose();
                }
            });
        }
        JLabel corner=new JLabel(); corner.setOpaque(true); corner.setBackground(BG_ROW_ALT);
        pane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER,corner);
    }
    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setFont(FONT_BODY_BOLD); tabs.setForeground(TEXT_MUTED); tabs.setBackground(BG_APP);
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults(); tabInsets=new Insets(11,18,11,18);
                contentBorderInsets=new Insets(12,0,0,0); tabAreaInsets=new Insets(0,0,0,0);
            }
            @Override protected void paintTabBackground(Graphics g,int p,int i,int x,int y,int w,int h,boolean selected) {
                g.setColor(selected ? BG_CARD : BG_APP); g.fillRect(x,y,w,h);
            }
            @Override protected void paintTabBorder(Graphics g,int p,int i,int x,int y,int w,int h,boolean selected) {
                g.setColor(selected ? PRIMARY : BORDER); g.fillRect(x,y+h-2,w,selected ? 2 : 1);
            }
            @Override protected void paintFocusIndicator(Graphics g,int p,Rectangle[] rectangles,int i,Rectangle ir,Rectangle tr,boolean selected) {
                if (tabPane.hasFocus() && selected) { Rectangle r=rectangles[i]; g.setColor(PRIMARY); g.drawRoundRect(r.x+4,r.y+4,r.width-9,r.height-9,6,6); }
            }
            @Override protected void paintContentBorder(Graphics g,int placement,int selected) { }
            @Override protected void paintText(Graphics g,int p,Font font,FontMetrics fm,int i,String title,Rectangle r,boolean selected) {
                g.setFont(font); g.setColor(selected ? PRIMARY : TEXT_MUTED);
                javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(g,title,-1,r.x,r.y+fm.getAscent());
            }
        });
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY); table.setForeground(TEXT_MAIN); table.setBackground(BG_CARD);
        table.setRowHeight(40); table.setGridColor(BORDER); table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false); table.setIntercellSpacing(new Dimension(0,0));
        table.setSelectionBackground(PRIMARY_LIGHT); table.setSelectionForeground(PRIMARY_ACTIVE);
        table.setFillsViewportHeight(true); table.setRowMargin(0);
        JTableHeader header=table.getTableHeader(); header.setFont(FONT_CAPTION_BOLD);
        header.setForeground(TEXT_MUTED); header.setBackground(BG_ROW_ALT);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width,42));
        header.setReorderingAllowed(false); header.setBorder(new EmptyBorder(0,0,0,0));
        TableCellRenderer nativeHeader=header.getDefaultRenderer();
        header.setDefaultRenderer((tbl,value,selected,focus,row,column) -> {
            Component c=nativeHeader.getTableCellRendererComponent(tbl,value,selected,focus,row,column);
            c.setFont(FONT_CAPTION_BOLD); c.setForeground(TEXT_MUTED); c.setBackground(BG_ROW_ALT);
            if (c instanceof JLabel) {
                JLabel label=(JLabel)c; label.setOpaque(true); label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER),new EmptyBorder(0,12,0,12)));
            }
            return c;
        });
        TableCellRenderer renderer=new TableCellRenderer() {
            private final DefaultTableCellRenderer cell=new DefaultTableCellRenderer();
            private final JPanel statusPanel=new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    // JTable's CellRendererPane does not validate nested components offscreen.
                    doLayout();
                    super.paintComponent(g);
                }
            };
            private final JLabel statusLabel=createBadge("",SUCCESS_TEXT,SUCCESS_BG,SUCCESS_BORDER);
            { statusPanel.add(statusLabel); }
            @Override public Component getTableCellRendererComponent(JTable tbl,Object value,boolean selected,boolean focus,int row,int column) {
                Color bg=selected ? PRIMARY_LIGHT : row%2==0 ? BG_CARD : BG_ROW_ALT;
                String text=value==null ? "" : value.toString().trim();
                if (isStatus(text)) {
                    Color[] colors=statusColors(text); statusLabel.setText(displayText(text));
                    statusLabel.setForeground(colors[0]); statusLabel.setBackground(colors[1]);
                    statusLabel.setBorder(new RoundedBorder(colors[2],14,new Insets(4,10,4,10)));
                    statusPanel.setBackground(bg); statusPanel.setBorder(focus ? BorderFactory.createLineBorder(PRIMARY_BORDER) : null); return statusPanel;
                }
                JLabel c=(JLabel)cell.getTableCellRendererComponent(tbl,value,selected,focus,row,column);
                c.setText(displayText(text)); c.setFont(FONT_BODY); c.setBackground(bg);
                c.setForeground(selected ? PRIMARY_ACTIVE : TEXT_MAIN);
                c.setBorder(focus ? BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY_BORDER),new EmptyBorder(0,11,0,11)) : new EmptyBorder(0,12,0,12));
                boolean numeric=value instanceof Number || text.matches("^-?\\d[\\d.,]*(?:\\s?(?:VNĐ|VND|đ|%))?$");
                boolean date=text.matches("^\\d{4}-\\d{2}-\\d{2}.*$") || text.matches("^\\d{2}:\\d{2}.*$");
                c.setHorizontalAlignment(date ? SwingConstants.CENTER : numeric ? SwingConstants.RIGHT : SwingConstants.LEFT); return c;
            }
        };
        table.setDefaultRenderer(Object.class,renderer); table.setDefaultRenderer(Number.class,renderer);
        for (int i=0;i<table.getColumnCount();i++) table.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }
    private static boolean isStatus(String text) {
        switch (text) {
            case "HOAT_DONG": case "DANG_LAM_VIEC": case "DA_CHOT": case "CO_MAT": case "DI_TRE":
            case "CHUA_CHOT": case "NGHI_VIEC": case "KHOA": case "NGUNG_HOAT_DONG": case "VANG_MAT":
            case "VANG": case "VE_SOM": case "NGHI_PHEP": case "Hoạt động": case "Đang làm việc":
            case "Đã chốt": case "Chưa chốt": case "Có mặt": case "Đi trễ": case "Nghỉ việc": case "Khóa":
            case "Đã khóa": case "Về sớm": case "Nghỉ phép": return true;
            default: return false;
        }
    }
    private static Color[] statusColors(String status) {
        switch (status) {
            case "DI_TRE": case "CHUA_CHOT": case "VE_SOM": case "NGHI_PHEP":
            case "Đi trễ": case "Chưa chốt": case "Về sớm": case "Nghỉ phép":
                return new Color[]{WARNING_TEXT,WARNING_BG,WARNING_BORDER};
            case "NGHI_VIEC": case "KHOA": case "NGUNG_HOAT_DONG": case "VANG_MAT": case "VANG":
            case "Nghỉ việc": case "Khóa": case "Đã khóa": return new Color[]{DANGER_TEXT,DANGER_BG,DANGER_BORDER};
            default: return new Color[]{SUCCESS_TEXT,SUCCESS_BG,SUCCESS_BORDER};
        }
    }
    public static JPanel createStatusPill(String status, boolean selected) {
        String text=status==null ? "" : status;
        Color[] colors=statusColors(text);
        JPanel panel=new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                doLayout();
                super.paintComponent(g);
            }
        };
        panel.setBackground(selected ? PRIMARY_LIGHT : BG_CARD);
        panel.add(createBadge(displayText(text),colors[0],colors[1],colors[2]));
        return panel;
    }
    public static String displayText(String text) {
        switch (text) {
            case "HOAT_DONG": return "Hoạt động";
            case "DANG_LAM_VIEC": return "Đang làm việc";
            case "NGUNG_HOAT_DONG": return "Ngừng hoạt động";
            case "NGHI_VIEC": return "Nghỉ việc";
            case "DA_CHOT": return "Đã chốt";
            case "CHUA_CHOT": return "Chưa chốt";
            case "CO_MAT": return "Có mặt";
            case "DI_TRE": return "Đi trễ";
            case "VE_SOM": return "Về sớm";
            case "NGHI_PHEP": return "Nghỉ phép";
            case "VANG_MAT": case "VANG": return "Vắng mặt";
            case "KHOA": return "Đã khóa";
            case "DB_Admin": return "Quản trị viên";
            case "HR_Manager": return "Quản lý nhân sự";
            case "Payroll_Officer": return "Kế toán tiền lương";
            case "Employee": return "Nhân viên";
            default: return text;
        }
    }

    public static class CardPanel extends JPanel {
        public CardPanel() { setOpaque(false); setBackground(BG_CARD); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g=smooth(graphics); g.setColor(new Color(224,229,240,65));
            g.fillRoundRect(1,3,getWidth()-2,getHeight()-3,16,16); g.setColor(getBackground());
            g.fillRoundRect(1,1,getWidth()-3,getHeight()-4,16,16); g.setColor(BORDER);
            g.drawRoundRect(1,1,getWidth()-3,getHeight()-4,16,16); g.dispose(); super.paintComponent(graphics);
        }
    }
    public static JPanel createCardPanel() {
        JPanel card=new CardPanel(); card.setBorder(new EmptyBorder(18,20,18,20)); return card;
    }
    public static JPanel createSectionHeader(String title) {
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(new EmptyBorder(0,0,12,0));
        JLabel label=new JLabel(title); label.setFont(FONT_SUBTITLE); label.setForeground(TEXT_MAIN); p.add(label,BorderLayout.WEST); return p;
    }
    public static JPanel createPageHeader(String title,String subtitle) {
        JPanel p=new JPanel(new BorderLayout(0,6)); p.setOpaque(false); p.setBorder(new EmptyBorder(0,0,18,0));
        JLabel heading=new JLabel(title); heading.setFont(FONT_TITLE_LARGE); heading.setForeground(TEXT_MAIN);
        JLabel description=new JLabel(subtitle); description.setFont(FONT_BODY); description.setForeground(TEXT_MUTED);
        p.add(heading,BorderLayout.NORTH); p.add(description,BorderLayout.CENTER); return p;
    }
    public static JPanel createStatCard(String title,String value,String subtitle,Color color,String symbol) {
        JPanel card=createCardPanel(); card.setLayout(new BorderLayout(0,10));
        JPanel top=new JPanel(new BorderLayout(12,0)); top.setOpaque(false);
        JLabel label=new JLabel(title); label.setFont(FONT_CAPTION_BOLD); label.setForeground(TEXT_MUTED);
        String key=title.toLowerCase(Locale.ROOT).contains("nhân") ? "users" : title.toLowerCase(Locale.ROOT).contains("lương") ? "wallet" : "chart";
        JLabel mark=new JLabel(icon(key,20,color),SwingConstants.CENTER); mark.setOpaque(true);
        mark.setBackground(mix(BG_CARD,color,.08f)); mark.setPreferredSize(new Dimension(38,38));
        mark.setBorder(new RoundedBorder(mix(BG_CARD,color,.16f),10,new Insets(7,7,7,7)));
        top.add(label,BorderLayout.CENTER); top.add(mark,BorderLayout.EAST);
        JLabel number=new JLabel(value); number.setFont(FONT_HERO); number.setForeground(TEXT_MAIN);
        JLabel caption=new JLabel(subtitle); caption.setFont(FONT_CAPTION); caption.setForeground(TEXT_MUTED);
        JPanel body=new JPanel(new BorderLayout(0,5)); body.setOpaque(false);
        body.add(number,BorderLayout.NORTH); body.add(caption,BorderLayout.SOUTH);
        card.add(top,BorderLayout.NORTH); card.add(body,BorderLayout.CENTER); return card;
    }
    public static JLabel createBadge(String text,Color fg,Color bg,Color border) {
        JLabel label=new JLabel(text,SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g=smooth(graphics); g.setColor(getBackground()); g.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g.dispose(); super.paintComponent(graphics);
            }
        };
        label.setOpaque(false); label.setFont(FONT_CAPTION_BOLD); label.setForeground(fg); label.setBackground(bg);
        label.setBorder(new RoundedBorder(border,14,new Insets(4,10,4,10))); return label;
    }

    /** Crisp HiDPI vector icons without font, network, or asset dependencies. */
    public static Icon icon(String name,int size,Color color) {
        return new Icon() {
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
            public void paintIcon(Component c,Graphics graphics,int x,int y) {
                Graphics2D g=smooth(graphics); g.translate(x,y); g.scale(size/24d,size/24d);
                g.setColor(c!=null && !c.isEnabled() ? TEXT_SUBTLE : color);
                g.setStroke(new BasicStroke(1.7f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                switch (name) {
                    case "home": path(g,3,10,12,3,21,10); path(g,5,9,5,21,10,21,10,14,14,14,14,21,19,21,19,9); break;
                    case "users": g.drawOval(5,3,7,7); g.drawArc(2,13,13,14,0,180); g.drawArc(13,3,6,7,270,180); g.drawArc(14,13,8,12,-10,100); break;
                    case "building":
                        g.drawRoundRect(5,3,14,18,2,2); g.drawRect(10,16,4,5);
                        for (int r=6;r<14;r+=4) for (int col=8;col<17;col+=4) g.drawLine(col,r,col,r+1); break;
                    case "calendar":
                        g.drawRoundRect(3,5,18,16,3,3); g.drawLine(3,10,21,10); g.drawLine(8,3,8,7); g.drawLine(16,3,16,7);
                        g.drawLine(8,14,9,14); g.drawLine(14,14,15,14); g.drawLine(8,17,9,17); break;
                    case "clock": g.drawOval(3,3,18,18); path(g,12,7,12,12,16,14); break;
                    case "wallet": g.drawRoundRect(3,5,18,15,3,3); g.drawRoundRect(14,10,8,6,2,2); g.drawLine(17,13,18,13); g.drawLine(5,5,17,2); break;
                    case "chart": path(g,3,3,3,21,21,21); g.drawLine(7,16,7,12); g.drawLine(12,16,12,8); g.drawLine(17,16,17,5); break;
                    case "settings": g.drawOval(8,8,8,8); g.drawOval(3,3,18,18); g.drawLine(12,1,12,4); g.drawLine(12,20,12,23); g.drawLine(1,12,4,12); g.drawLine(20,12,23,12); break;
                    case "search": g.drawOval(3,3,12,12); g.drawLine(13,13,21,21); break;
                    case "plus": g.drawLine(12,5,12,19); g.drawLine(5,12,19,12); break;
                    case "edit": path(g,14,5,18,9,8,19,3,21,5,16,15,6,18,3,21,6,18,9); break;
                    case "trash": path(g,5,7,6,21,18,21,19,7); g.drawLine(3,7,21,7); path(g,8,7,8,3,16,3,16,7); g.drawLine(10,11,10,17); g.drawLine(14,11,14,17); break;
                    case "refresh": g.drawArc(4,4,16,16,40,285); path(g,15,3,20,5,20,10); break;
                    case "download": path(g,12,3,12,15); path(g,7,10,12,15,17,10); path(g,4,15,4,21,20,21,20,15); break;
                    case "check": path(g,5,12,10,17,20,6); break;
                    case "arrow": path(g,4,12,20,12); path(g,14,6,20,12,14,18); break;
                    case "chevron": path(g,6,9,12,15,18,9); break;
                    case "shield": path(g,12,2,21,6,20,15,17,19,12,22,7,19,4,15,3,6,12,2); path(g,8,12,11,15,16,9); break;
                    case "logout": path(g,10,4,4,4,4,20,10,20); path(g,9,12,21,12); path(g,17,8,21,12,17,16); break;
                    case "briefcase": g.drawRoundRect(3,7,18,14,2,2); path(g,8,7,8,3,16,3,16,7); g.drawLine(3,12,21,12); g.drawLine(12,10,12,15); break;
                    case "close": g.drawOval(3,3,18,18); g.drawLine(8,8,16,16); g.drawLine(16,8,8,16); break;
                    case "info": g.drawOval(3,3,18,18); g.drawLine(12,11,12,17); g.drawLine(12,7,12,7); break;
                    default: g.drawRoundRect(4,4,16,16,4,4); path(g,8,12,11,15,17,9); break;
                }
                g.dispose();
            }
        };
    }
    private static void path(Graphics2D g,double... xy) {
        Path2D p=new Path2D.Double(); p.moveTo(xy[0],xy[1]); for (int i=2;i<xy.length;i+=2) p.lineTo(xy[i],xy[i+1]); g.draw(p);
    }
    private static final class CheckIcon implements Icon {
        public int getIconWidth() { return 18; }
        public int getIconHeight() { return 18; }
        public void paintIcon(Component c,Graphics graphics,int x,int y) {
            AbstractButton b=(AbstractButton)c; Graphics2D g=smooth(graphics);
            g.setColor(b.isSelected() ? b.isEnabled() ? PRIMARY : TEXT_SUBTLE : BG_CARD); g.fillRoundRect(x+1,y+1,15,15,5,5);
            g.setColor(b.isSelected() ? PRIMARY : BORDER_INPUT); g.drawRoundRect(x+1,y+1,15,15,5,5);
            if (b.isSelected()) {
                g.setColor(Color.WHITE); g.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); path(g,x+4,y+8,x+7,y+11,x+13,y+5);
            }
            if (b.hasFocus()) { g.setColor(PRIMARY); g.drawRoundRect(x,y,17,17,5,5); } g.dispose();
        }
    }
}
