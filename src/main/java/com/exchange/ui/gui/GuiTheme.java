package com.exchange.ui.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Design system tokens, color palettes, typography, and styling utilities for the Swing trading GUI.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class GuiTheme {

    /** Private constructor to prevent instantiation. */
    private GuiTheme() {
        // Utility constants class
    }

    // Color Palette: Professional Dark Financial Workstation
    /** Deep slate/charcoal background color for the main frame. */
    public static final Color BG_MAIN = new Color(18, 24, 38);

    /** Slate navy surface fill color for metric cards and tables. */
    public static final Color BG_CARD = new Color(30, 41, 59);

    /** Elevated lighter slate tone used for hover states. */
    public static final Color BG_CARD_HOVER = new Color(40, 53, 76);

    /** Dark input field background color. */
    public static final Color BG_INPUT = new Color(15, 23, 42);

    /** Subtle slate border color for cards and table grids. */
    public static final Color BORDER_SUBTLE = new Color(51, 65, 85);

    /** Focus accent blue border color for active inputs. */
    public static final Color BORDER_FOCUS = new Color(59, 130, 246);

    /** Primary bright text color for titles and key values. */
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);

    /** Muted silver text color for subtitles and labels. */
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);

    /** Dark muted text color for placeholder hints. */
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    /** Emerald green accent color indicating gains and buy actions. */
    public static final Color ACCENT_GREEN = new Color(16, 185, 129);

    /** Crimson red accent color indicating losses and sell actions. */
    public static final Color ACCENT_RED = new Color(239, 68, 68);

    /** Vibrant blue accent color for general buttons and cash values. */
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);

    /** Amber yellow accent color for max quantity buttons. */
    public static final Color ACCENT_YELLOW = new Color(245, 158, 11);

    // Typography
    /** Font style used for window and section titles. */
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);

    /** Font style used for panel and card headers. */
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 14);

    /** Default body text font style. */
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 12);

    /** Bold text font style for table headers and labels. */
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 12);

    /** Large bold font style used for numerical KPI metrics. */
    public static final Font FONT_METRIC = new Font("SansSerif", Font.BOLD, 16);

    /** Monospaced font style for code and identifiers. */
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    /**
     * Creates a standardized card panel with background and border.
     *
     * @return styled JPanel container
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return panel;
    }

    /**
     * Styles an action button with custom colors and font.
     *
     * @param button the AbstractButton (JButton or JToggleButton) to style
     * @param bgColor background fill color
     * @param fgColor text color
     */
    public static void styleButton(AbstractButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Styles a text field for dark theme data entry.
     *
     * @param textField the JTextField to style
     */
    public static void styleTextField(JTextField textField) {
        textField.setBackground(BG_INPUT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setFont(FONT_REGULAR);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    /**
     * Configures a JTable with dark workstation theme, custom headers, and selection styling.
     *
     * @param table the JTable to configure
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(28);
        table.setGridColor(BORDER_SUBTLE);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(45, 62, 90));
        table.setSelectionForeground(TEXT_PRIMARY);

        // Header Styling
        table.getTableHeader().setBackground(BG_MAIN);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        table.getTableHeader().setReorderingAllowed(false);
    }
}
