package view;

import service.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tema GUI SITAG.
 *
 * Non-functional requirement PRD: warna utama hanya BIRU TUA dan BIRU MUDA,
 * layout standar dan tidak berlebihan. Seluruh warna diambil dari konstanta
 * di class ini agar konsisten pada semua halaman.
 */
public final class Theme {

    // ---------------------------- Palet: BIRU TUA ----------------------------
    public static final Color DARK_BLUE = new Color(0x0B2E5C);
    public static final Color DARK_BLUE_HOVER = new Color(0x14406F);
    public static final Color DARK_BLUE_SOFT = new Color(0x1B4F96);

    // ---------------------------- Palet: BIRU MUDA ---------------------------
    public static final Color LIGHT_BLUE = new Color(0xBFDDF7);
    public static final Color LIGHT_BLUE_SOFT = new Color(0xE8F3FD);
    public static final Color LIGHT_BLUE_LINE = new Color(0x8FBFE8);

    // ------------------------------- Font ------------------------------------
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 19);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_CLOCK = new Font("Consolas", Font.BOLD, 20);
    public static final Font FONT_BIG = new Font("Segoe UI", Font.BOLD, 26);

    /** Status stok menipis (jumlah_stok <= stok_minimum) sesuai business rule PRD. */
    public static final String STATUS_MENIPIS = "MENIPIS";

    private Theme() {
        // Kelas tema tidak untuk di-instansiasi.
    }

    /** Format rupiah untuk seluruh halaman GUI. */
    public static String rupiah(double amount) {
        return CurrencyUtil.format(amount);
    }

    public static Border lineBorder() {
        return BorderFactory.createLineBorder(LIGHT_BLUE_LINE);
    }

    public static Border padding(int size) {
        return BorderFactory.createEmptyBorder(size, size, size, size);
    }

    // ============================ Panel & Label ==============================

    public static JPanel lightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(LIGHT_BLUE_SOFT);
        return panel;
    }

    public static JPanel darkPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(DARK_BLUE);
        return panel;
    }

    /** Kartu statistik untuk dashboard (judul kecil + nilai besar). */
    public static JPanel card(String judul, JLabel nilai) {
        JPanel card = new JPanel(new java.awt.BorderLayout(0, 6));
        card.setBackground(LIGHT_BLUE);
        card.setBorder(BorderFactory.createCompoundBorder(lineBorder(), padding(12)));
        JLabel title = new JLabel(judul);
        title.setFont(FONT_BODY_BOLD);
        title.setForeground(DARK_BLUE);
        nilai.setFont(FONT_BIG);
        nilai.setForeground(DARK_BLUE);
        card.add(title, java.awt.BorderLayout.NORTH);
        card.add(nilai, java.awt.BorderLayout.CENTER);
        return card;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(DARK_BLUE);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(DARK_BLUE);
        return label;
    }

    public static JLabel text(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(DARK_BLUE);
        return label;
    }

    public static JLabel textBold(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(DARK_BLUE);
        return label;
    }

    /** Label untuk dipasang pada latar biru tua. */
    public static JLabel lightText(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(LIGHT_BLUE);
        return label;
    }

    public static JLabel rightText(String text) {
        JLabel label = text(text);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    // ============================== Tombol ==================================

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, DARK_BLUE, LIGHT_BLUE, DARK_BLUE_HOVER, LIGHT_BLUE_SOFT);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, LIGHT_BLUE, DARK_BLUE, DARK_BLUE, LIGHT_BLUE);
        return button;
    }

    private static void styleButton(JButton button, Color normal, Color foreground,
                                    Color hover, Color hoverForeground) {
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(normal);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(lineBorder(), padding(9)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                    button.setForeground(hoverForeground);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
                button.setForeground(foreground);
            }
        });
    }

    // ========================= Input & Koleksi ==============================

    public static JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        styleField(field);
        return field;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleField(field);
        return field;
    }

    private static void styleField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBackground(LIGHT_BLUE_SOFT);
        field.setForeground(DARK_BLUE);
        field.setCaretColor(DARK_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(lineBorder(), padding(6)));
    }

    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(LIGHT_BLUE_SOFT);
        combo.setForeground(DARK_BLUE);
        combo.setBorder(lineBorder());
        combo.setPreferredSize(new Dimension(150, 30));
        return combo;
    }

    public static JScrollPane scrollPane(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(lineBorder());
        scroll.getViewport().setBackground(LIGHT_BLUE_SOFT);
        return scroll;
    }

    // =============================== Tabel ==================================

    /** Menerapkan tema biru pada tabel dan menjadikannya read-only. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(DARK_BLUE);
        table.setBackground(LIGHT_BLUE_SOFT);
        table.setSelectionBackground(DARK_BLUE);
        table.setSelectionForeground(LIGHT_BLUE);
        table.setGridColor(LIGHT_BLUE_LINE);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setDefaultEditor(Object.class, null);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(DARK_BLUE);
        header.setForeground(LIGHT_BLUE);
        header.setReorderingAllowed(false);
    }

    /**
     * Menandai baris yang statusnya "MENIPIS" memakai biru muda pekat dan huruf
     * tebal, tanpa menambah warna di luar palet biru tua / biru muda.
     */
    public static void highlightLowStock(JTable table, int statusColumn) {
        table.setDefaultRenderer(Object.class, new LowStockRenderer(statusColumn));
    }

    private static final class LowStockRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        private final int statusColumn;

        private LowStockRenderer(int statusColumn) {
            this.statusColumn = statusColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                return component;
            }
            Object status = table.getModel().getValueAt(table.convertRowIndexToModel(row), statusColumn);
            boolean menipis = status != null && STATUS_MENIPIS.equalsIgnoreCase(status.toString());
            component.setBackground(menipis ? LIGHT_BLUE : LIGHT_BLUE_SOFT);
            component.setForeground(DARK_BLUE);
            component.setFont(menipis ? FONT_BODY_BOLD : FONT_BODY);
            return component;
        }
    }

    // ===================== Header halaman & dialog ==========================

    /** Panel header biru tua: judul, keterangan, dan komponen sisi kanan (jam/tombol). */
    public static JPanel headerPanel(String judul, String keterangan, JComponent kanan) {
        JPanel header = new JPanel(new java.awt.BorderLayout(12, 0));
        header.setBackground(DARK_BLUE);
        header.setBorder(padding(14));

        JPanel kiri = new JPanel(new java.awt.GridLayout(2, 1));
        kiri.setOpaque(false);
        kiri.add(lightText(judul, FONT_TITLE));
        kiri.add(lightText(keterangan, FONT_BODY));
        header.add(kiri, java.awt.BorderLayout.WEST);
        if (kanan != null) {
            header.add(kanan, java.awt.BorderLayout.EAST);
        }
        return header;
    }

    /** Status bar bawah: keterangan koneksi database & informasi tambahan. */
    public static JPanel statusBar(String text) {
        JPanel bar = new JPanel(new java.awt.BorderLayout());
        bar.setBackground(LIGHT_BLUE);
        bar.setBorder(padding(6));
        bar.add(text(text), java.awt.BorderLayout.WEST);
        return bar;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Terjadi Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int pilihan = JOptionPane.showConfirmDialog(parent, message, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return pilihan == JOptionPane.YES_OPTION;
    }

    public static String input(Component parent, String message, String nilaiAwal) {
        return (String) JOptionPane.showInputDialog(parent, message, "Input",
                JOptionPane.PLAIN_MESSAGE, null, null, nilaiAwal);
    }
}