package com.exchange.ui.gui;

import com.exchange.market.Market;
import com.exchange.model.Stock;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Interactive equity quote directory table presenting live market spot prices, sectors,
 * beta sensitivities, and trading volumes.
 *
 * @author Mridul Gupta
 * @version 1.1
 * @since 1.0
 */
public class MarketWatchPanel extends JPanel {

    /** Table data model storing live quotes rows. */
    private final DefaultTableModel tableModel;

    /** Graphical JTable component presenting market equities. */
    private final JTable table;

    /** Callback consumer notifying when a stock row is selected. */
    private final Consumer<String> onStockSelected;

    private static final String[] COLUMN_NAMES = {
            "TICKER", "COMPANY NAME", "SECTOR", "BETA", "SPOT PRICE", "DAY CHANGE", "% CHANGE", "VOLUME"
    };

    /**
     * Constructs the MarketWatchPanel with a selection callback.
     *
     * @param onStockSelected callback invoked with the selected ticker symbol when a row is clicked
     */
    public MarketWatchPanel(Consumer<String> onStockSelected) {
        this.onStockSelected = onStockSelected;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Header Label
        JLabel titleLabel = new JLabel("LIVE MARKET WATCH & REAL-WORLD EQUITIES");
        titleLabel.setFont(GuiTheme.FONT_HEADER);
        titleLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        add(titleLabel, BorderLayout.NORTH);

        // Table Model with non-editable cells
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        GuiTheme.styleTable(table);

        // Custom Cell Renderer for color-coded changes
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? GuiTheme.BG_CARD : new Color(24, 34, 50));
                }

                if (col == 0) {
                    setFont(GuiTheme.FONT_BOLD);
                    setForeground(GuiTheme.TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 1) {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_SECONDARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 2) {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_MUTED);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 3) {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_SECONDARY);
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (col == 4) {
                    setFont(GuiTheme.FONT_BOLD);
                    setForeground(GuiTheme.TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 5 || col == 6) {
                    setFont(GuiTheme.FONT_BOLD);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    String text = (val != null) ? val.toString() : "";
                    if (text.startsWith("+")) {
                        setForeground(GuiTheme.ACCENT_GREEN);
                    } else if (text.startsWith("-")) {
                        setForeground(GuiTheme.ACCENT_RED);
                    } else {
                        setForeground(GuiTheme.TEXT_SECONDARY);
                    }
                } else {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_SECONDARY);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(55);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(85);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(85);

        // Row Selection Listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String symbol = (String) tableModel.getValueAt(selectedRow, 0);
                    if (onStockSelected != null && symbol != null) {
                        onStockSelected.accept(symbol);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(GuiTheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Updates table rows with the latest spot prices and day metrics from the market.
     *
     * @param market the current active Market instance
     */
    public void refreshMarketData(Market market) {
        if (market == null) return;

        int selectedRow = table.getSelectedRow();
        String selectedSymbol = (selectedRow != -1) ? (String) tableModel.getValueAt(selectedRow, 0) : null;

        tableModel.setRowCount(0);
        Collection<Stock> stocks = market.getAllStocks();

        int reselectIndex = -1;
        int rowIndex = 0;

        for (Stock s : stocks) {
            double change = s.getDailyChange();
            double pct = s.getDailyChangePercent();
            String changeStr = String.format("%s$%,.2f", (change >= 0 ? "+" : ""), change);
            String pctStr = String.format("%s%.2f%%", (pct >= 0 ? "+" : ""), pct);

            tableModel.addRow(new Object[]{
                    s.getSymbol(),
                    s.getCompanyName(),
                    s.getSector().getDisplayName(),
                    String.format("%.2f", s.getBeta()),
                    String.format("$%,.2f", s.getCurrentPrice()),
                    changeStr,
                    pctStr,
                    String.format("%,d", s.getVolume())
            });

            if (selectedSymbol != null && selectedSymbol.equals(s.getSymbol())) {
                reselectIndex = rowIndex;
            }
            rowIndex++;
        }

        // Preserve previous row selection if still listed
        if (reselectIndex != -1) {
            table.setRowSelectionInterval(reselectIndex, reselectIndex);
        }
    }

    /**
     * Programmatically selects a given stock row in the table.
     *
     * @param symbol the ticker symbol to highlight
     */
    public void setSelectedStock(String symbol) {
        if (symbol == null) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (symbol.equalsIgnoreCase((String) tableModel.getValueAt(i, 0))) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                break;
            }
        }
    }
}
