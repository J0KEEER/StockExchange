package com.exchange.ui.gui;

import com.exchange.market.Market;
import com.exchange.model.Holding;
import com.exchange.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Portfolio positions table panel tracking owned equity lots, cost bases, and live unrealized returns.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class HoldingsTablePanel extends JPanel {

    /** The underlying table data model for holdings rows. */
    private final DefaultTableModel tableModel;

    /** The graphical JTable presenting holdings positions. */
    private final JTable table;

    /** Footer status label summarizing total book cost, market valuation, and unrealized return. */
    private final JLabel totalsSummaryLabel;

    /** Callback consumer notifying when a user selects a holding to trade. */
    private final Consumer<String> onSelectHoldingToTrade;

    private static final String[] COLUMN_NAMES = {
            "TICKER", "SHARES", "AVG COST BASIS", "SPOT PRICE", "BOOK VALUE", "MARKET VALUE", "UNREALIZED P/L", "ROI (%)"
    };

    /**
     * Constructs a HoldingsTablePanel with an optional trade selection hook.
     *
     * @param onSelectHoldingToTrade callback invoked when a holding position row is selected
     */
    public HoldingsTablePanel(Consumer<String> onSelectHoldingToTrade) {
        this.onSelectHoldingToTrade = onSelectHoldingToTrade;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLabel = new JLabel("PORTFOLIO ASSET ALLOCATION & OPEN POSITIONS");
        titleLabel.setFont(GuiTheme.FONT_HEADER);
        titleLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        GuiTheme.styleTable(table);

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
                    setForeground(GuiTheme.TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col >= 2 && col <= 5) {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 6 || col == 7) {
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
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String symbol = (String) tableModel.getValueAt(row, 0);
                    if (onSelectHoldingToTrade != null && symbol != null) {
                        onSelectHoldingToTrade.accept(symbol);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(GuiTheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1));
        add(scrollPane, BorderLayout.CENTER);

        totalsSummaryLabel = new JLabel("Total Book: $0.00 | Total Market: $0.00 | Net Unrealized: +$0.00");
        totalsSummaryLabel.setFont(GuiTheme.FONT_BOLD);
        totalsSummaryLabel.setForeground(GuiTheme.TEXT_SECONDARY);
        totalsSummaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        add(totalsSummaryLabel, BorderLayout.SOUTH);
    }

    /**
     * Refreshes table rows with current holding positions, live spot valuations, and unrealized gains.
     *
     * @param user the account holder
     * @param market the current active Market instance
     */
    public void refreshHoldings(User user, Market market) {
        if (user == null || market == null) return;

        tableModel.setRowCount(0);
        Map<String, Holding> holdings = user.getPortfolio().getHoldings();
        Map<String, Double> prices = market.getPriceSnapshot();

        double totalBook = 0.0;
        double totalMarket = 0.0;
        double totalUnrealized = 0.0;

        for (Holding h : holdings.values()) {
            double spot = prices.getOrDefault(h.getSymbol(), h.getAverageCostBasis());
            double bookVal = h.getTotalCost();
            double marketVal = h.getMarketValue(spot);
            double unRealized = h.getUnrealizedProfitLoss(spot);
            double roi = h.getUnrealizedRoiPercent(spot);

            totalBook += bookVal;
            totalMarket += marketVal;
            totalUnrealized += unRealized;

            String unRealizedStr = String.format("%s$%,.2f", (unRealized >= 0 ? "+" : ""), unRealized);
            String roiStr = String.format("%s%.2f%%", (roi >= 0 ? "+" : ""), roi);

            tableModel.addRow(new Object[]{
                    h.getSymbol(),
                    String.format("%,d", h.getQuantity()),
                    String.format("$%,.2f", h.getAverageCostBasis()),
                    String.format("$%,.2f", spot),
                    String.format("$%,.2f", bookVal),
                    String.format("$%,.2f", marketVal),
                    unRealizedStr,
                    roiStr
            });
        }

        String sign = totalUnrealized >= 0 ? "+" : "";
        double portfolioRoi = totalBook > 0 ? (totalUnrealized / totalBook) * 100.0 : 0.0;
        totalsSummaryLabel.setText(String.format("Total Book Cost: $%,.2f | Market Value: $%,.2f | Unrealized P/L: %s$%,.2f (%s%.2f%%)",
                totalBook, totalMarket, sign, totalUnrealized, sign, portfolioRoi));
        totalsSummaryLabel.setForeground(totalUnrealized >= 0 ? GuiTheme.ACCENT_GREEN : GuiTheme.ACCENT_RED);
    }
}
