package com.exchange.ui.gui;

import com.exchange.model.Transaction;
import com.exchange.model.TransactionType;
import com.exchange.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Audit ledger table panel displaying chronological completed trade receipts and realized returns.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class TransactionLedgerPanel extends JPanel {

    /** Table data model storing transaction rows. */
    private final DefaultTableModel tableModel;

    /** Graphical JTable displaying the transaction ledger. */
    private final JTable table;

    /** Date-time formatter for presentation. */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] COLUMN_NAMES = {
            "ORDER ID", "TIMESTAMP", "ACTION", "TICKER", "SHARES", "EXEC PRICE", "TOTAL AMOUNT", "REALIZED P/L"
    };

    /**
     * Constructs a TransactionLedgerPanel with table styling and custom renderers.
     */
    public TransactionLedgerPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiTheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLabel = new JLabel("TRANSACTION EXECUTION AUDIT LEDGER");
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

                if (col == 0 || col == 3) {
                    setFont(GuiTheme.FONT_BOLD);
                    setForeground(GuiTheme.TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 1) {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_SECONDARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 2) {
                    setFont(GuiTheme.FONT_BOLD);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    String action = (val != null) ? val.toString() : "";
                    setForeground("BUY".equals(action) ? GuiTheme.ACCENT_GREEN : GuiTheme.ACCENT_RED);
                } else if (col >= 4 && col <= 6) {
                    setFont(GuiTheme.FONT_REGULAR);
                    setForeground(GuiTheme.TEXT_PRIMARY);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 7) {
                    setFont(GuiTheme.FONT_BOLD);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    String pnlText = (val != null) ? val.toString() : "";
                    if (pnlText.startsWith("+")) {
                        setForeground(GuiTheme.ACCENT_GREEN);
                    } else if (pnlText.startsWith("-")) {
                        setForeground(GuiTheme.ACCENT_RED);
                    } else {
                        setForeground(GuiTheme.TEXT_MUTED);
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(GuiTheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Rebuilds the transaction ledger rows from the user's persistent trade history.
     *
     * @param user the account holder
     */
    public void refreshLedger(User user) {
        if (user == null) return;

        tableModel.setRowCount(0);
        List<Transaction> transactions = user.getPortfolio().getTransactionHistory();

        for (Transaction tx : transactions) {
            String pnlStr = "-";
            if (tx.getType() == TransactionType.SELL) {
                double pnl = tx.getRealizedGainLoss();
                pnlStr = String.format("%s$%,.2f", (pnl >= 0 ? "+" : ""), pnl);
            }

            tableModel.addRow(new Object[]{
                    tx.getTransactionId(),
                    tx.getTimestamp().format(TIME_FMT),
                    tx.getType().name(),
                    tx.getSymbol(),
                    String.format("%,d", tx.getQuantity()),
                    String.format("$%,.2f", tx.getExecutionPrice()),
                    String.format("$%,.2f", tx.getTotalAmount()),
                    pnlStr
            });
        }
    }
}
