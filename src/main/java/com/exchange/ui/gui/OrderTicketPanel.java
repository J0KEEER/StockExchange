package com.exchange.ui.gui;

import com.exchange.engine.TradingService;
import com.exchange.exception.TradingException;
import com.exchange.market.Market;
import com.exchange.model.Holding;
import com.exchange.model.Stock;
import com.exchange.model.Transaction;
import com.exchange.model.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Collection;

/**
 * Interactive order execution ticket enabling immediate market buy and sell orders
 * with live capacity checks, quick-lot controls, and validation feedback.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class OrderTicketPanel extends JPanel {

    /** The trading business engine service. */
    private final TradingService tradingService;

    /** Callback runnable executed upon successful order execution. */
    private final Runnable onOrderExecuted;

    /** The active user account. */
    private User currentUser;

    /** Radio button selecting BUY order direction. */
    private final JRadioButton buyRadio;

    /** Radio button selecting SELL order direction. */
    private final JRadioButton sellRadio;

    /** Dropdown combo box containing tradable stock tickers. */
    private final JComboBox<String> stockCombo;

    /** Label displaying selected stock spot quote and 52-week range. */
    private final JLabel quoteInfoLabel;

    /** Label displaying available share buying or selling capacity. */
    private final JLabel capacityInfoLabel;

    /** Input text field for entering desired shares quantity. */
    private final JTextField quantityField;

    /** Label displaying calculated order settlement estimate. */
    private final JLabel estimateLabel;

    /** Button triggering order validation and execution. */
    private final JButton executeButton;

    /** Label showing trade confirmation receipt or error diagnostics. */
    private final JLabel statusLabel;

    /**
     * Constructs an OrderTicketPanel bound to trading engine services and refresh hooks.
     *
     * @param tradingService the trading business service
     * @param onOrderExecuted callback invoked after a trade successfully concludes
     */
    public OrderTicketPanel(TradingService tradingService, Runnable onOrderExecuted) {
        this.tradingService = tradingService;
        this.onOrderExecuted = onOrderExecuted;

        setLayout(new BorderLayout());
        setBackground(GuiTheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // 1. Header
        JLabel title = new JLabel("ORDER EXECUTION TICKET");
        title.setFont(GuiTheme.FONT_HEADER);
        title.setForeground(GuiTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // 2. Form Body
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(GuiTheme.BG_CARD);

        // Action Selector (BUY / SELL toggle)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionRow.setBackground(GuiTheme.BG_CARD);
        buyRadio = new JRadioButton("BUY (Acquire)", true);
        sellRadio = new JRadioButton("SELL (Liquidate)", false);

        ButtonGroup actionGroup = new ButtonGroup();
        actionGroup.add(buyRadio);
        actionGroup.add(sellRadio);

        styleRadio(buyRadio, GuiTheme.ACCENT_GREEN);
        styleRadio(sellRadio, GuiTheme.ACCENT_RED);

        buyRadio.addActionListener(e -> updateOrderTypeStyling());
        sellRadio.addActionListener(e -> updateOrderTypeStyling());

        actionRow.add(buyRadio);
        actionRow.add(Box.createHorizontalStrut(15));
        actionRow.add(sellRadio);

        // Stock Selector
        JPanel stockRow = new JPanel(new BorderLayout(5, 5));
        stockRow.setBackground(GuiTheme.BG_CARD);
        JLabel stockLabel = new JLabel("Equity Instrument:");
        stockLabel.setFont(GuiTheme.FONT_BOLD);
        stockLabel.setForeground(GuiTheme.TEXT_SECONDARY);

        stockCombo = new JComboBox<>();
        stockCombo.setBackground(GuiTheme.BG_INPUT);
        stockCombo.setForeground(GuiTheme.TEXT_PRIMARY);
        stockCombo.setFont(GuiTheme.FONT_BOLD);
        stockCombo.addActionListener(e -> updateQuoteAndCapacity());

        stockRow.add(stockLabel, BorderLayout.NORTH);
        stockRow.add(stockCombo, BorderLayout.CENTER);

        // Quote & 52-Week info
        quoteInfoLabel = new JLabel("Spot: $0.00 | Day: +$0.00 (+0.0%)");
        quoteInfoLabel.setFont(GuiTheme.FONT_REGULAR);
        quoteInfoLabel.setForeground(GuiTheme.TEXT_PRIMARY);

        capacityInfoLabel = new JLabel("Capacity: 0 shares");
        capacityInfoLabel.setFont(GuiTheme.FONT_REGULAR);
        capacityInfoLabel.setForeground(GuiTheme.TEXT_SECONDARY);

        // Quantity Entry + Quick Lot Buttons
        JPanel qtyRow = new JPanel(new BorderLayout(5, 5));
        qtyRow.setBackground(GuiTheme.BG_CARD);
        JLabel qtyLabel = new JLabel("Quantity (Shares):");
        qtyLabel.setFont(GuiTheme.FONT_BOLD);
        qtyLabel.setForeground(GuiTheme.TEXT_SECONDARY);

        quantityField = new JTextField("1");
        GuiTheme.styleTextField(quantityField);

        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateEstimate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateEstimate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateEstimate(); }
        });

        // Quick Buttons (+1, +5, +10, +50, MAX)
        JPanel quickBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        quickBtnRow.setBackground(GuiTheme.BG_CARD);
        quickBtnRow.add(createQuickButton("+1", 1));
        quickBtnRow.add(createQuickButton("+5", 5));
        quickBtnRow.add(createQuickButton("+10", 10));
        quickBtnRow.add(createQuickButton("+50", 50));
        JButton maxBtn = new JButton("MAX");
        GuiTheme.styleButton(maxBtn, GuiTheme.BG_INPUT, GuiTheme.ACCENT_YELLOW);
        maxBtn.addActionListener(e -> setMaxQuantity());
        quickBtnRow.add(maxBtn);

        qtyRow.add(qtyLabel, BorderLayout.NORTH);
        qtyRow.add(quantityField, BorderLayout.CENTER);
        qtyRow.add(quickBtnRow, BorderLayout.SOUTH);

        // Estimated Settlement Amount
        JPanel estRow = new JPanel(new BorderLayout());
        estRow.setBackground(new Color(24, 34, 50));
        estRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiTheme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JLabel estTitle = new JLabel("ESTIMATED SETTLEMENT TOTAL:");
        estTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        estTitle.setForeground(GuiTheme.TEXT_SECONDARY);
        estimateLabel = new JLabel("$0.00");
        estimateLabel.setFont(GuiTheme.FONT_METRIC);
        estimateLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        estRow.add(estTitle, BorderLayout.NORTH);
        estRow.add(estimateLabel, BorderLayout.CENTER);

        // Execute Button
        executeButton = new JButton("EXECUTE MARKET BUY");
        GuiTheme.styleButton(executeButton, GuiTheme.ACCENT_GREEN, Color.WHITE);
        executeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        executeButton.setPreferredSize(new Dimension(200, 38));
        executeButton.addActionListener(e -> handleOrderExecution());

        // Status / Feedback label
        statusLabel = new JLabel("Ready for order entry.");
        statusLabel.setFont(GuiTheme.FONT_REGULAR);
        statusLabel.setForeground(GuiTheme.TEXT_MUTED);

        // Assemble Form
        form.add(actionRow);
        form.add(Box.createVerticalStrut(10));
        form.add(stockRow);
        form.add(Box.createVerticalStrut(6));
        form.add(quoteInfoLabel);
        form.add(Box.createVerticalStrut(2));
        form.add(capacityInfoLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(qtyRow);
        form.add(Box.createVerticalStrut(12));
        form.add(estRow);
        form.add(Box.createVerticalStrut(14));
        form.add(executeButton);
        form.add(Box.createVerticalStrut(8));
        form.add(statusLabel);

        add(form, BorderLayout.CENTER);
    }

    private void styleRadio(JRadioButton radio, Color activeColor) {
        radio.setBackground(GuiTheme.BG_CARD);
        radio.setForeground(GuiTheme.TEXT_PRIMARY);
        radio.setFont(GuiTheme.FONT_BOLD);
        radio.setFocusPainted(false);
    }

    private JButton createQuickButton(String text, int increment) {
        JButton btn = new JButton(text);
        GuiTheme.styleButton(btn, GuiTheme.BG_INPUT, GuiTheme.TEXT_SECONDARY);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.addActionListener(e -> {
            try {
                int curr = Integer.parseInt(quantityField.getText().trim());
                quantityField.setText(String.valueOf(Math.max(1, curr + increment)));
            } catch (NumberFormatException ex) {
                quantityField.setText(String.valueOf(increment));
            }
        });
        return btn;
    }

    private void updateOrderTypeStyling() {
        if (buyRadio.isSelected()) {
            executeButton.setText("EXECUTE MARKET BUY");
            executeButton.setBackground(GuiTheme.ACCENT_GREEN);
        } else {
            executeButton.setText("EXECUTE MARKET SELL");
            executeButton.setBackground(GuiTheme.ACCENT_RED);
        }
        updateQuoteAndCapacity();
    }

    private void setMaxQuantity() {
        if (currentUser == null) return;
        String symbol = (String) stockCombo.getSelectedItem();
        if (symbol == null) return;

        Stock stock = tradingService.getMarket().getStock(symbol);
        if (stock == null) return;

        if (buyRadio.isSelected()) {
            int maxAffordable = (int) (currentUser.getCashBalance() / stock.getCurrentPrice());
            quantityField.setText(String.valueOf(Math.max(1, maxAffordable)));
        } else {
            Holding holding = currentUser.getPortfolio().getHolding(symbol);
            int owned = (holding != null) ? holding.getQuantity() : 0;
            quantityField.setText(String.valueOf(Math.max(1, owned)));
        }
    }

    /**
     * Updates the current user state and repopulates stock choices if modified.
     *
     * @param user the account holder
     * @param market the current active Market instance
     */
    public void refreshContext(User user, Market market) {
        this.currentUser = user;
        if (market == null) return;

        String previouslySelected = (String) stockCombo.getSelectedItem();
        Collection<Stock> stocks = market.getAllStocks();

        // Update items only if count changed or empty
        if (stockCombo.getItemCount() != stocks.size()) {
            stockCombo.removeAllItems();
            for (Stock s : stocks) {
                stockCombo.addItem(s.getSymbol());
            }
            if (previouslySelected != null) {
                stockCombo.setSelectedItem(previouslySelected);
            }
        }

        updateQuoteAndCapacity();
    }

    /**
     * Sets the selected stock in the dropdown.
     *
     * @param symbol the ticker to select
     */
    public void setSelectedStock(String symbol) {
        if (symbol != null) {
            stockCombo.setSelectedItem(symbol.toUpperCase());
            updateQuoteAndCapacity();
        }
    }

    private void updateQuoteAndCapacity() {
        String symbol = (String) stockCombo.getSelectedItem();
        if (symbol == null) return;

        Stock stock = tradingService.getMarket().getStock(symbol);
        if (stock == null) return;

        quoteInfoLabel.setText(String.format("Spot: $%,.2f | High: $%,.2f | Low: $%,.2f",
                stock.getCurrentPrice(), stock.getHigh52Week(), stock.getLow52Week()));

        if (currentUser != null) {
            if (buyRadio.isSelected()) {
                int maxAfford = (int) (currentUser.getCashBalance() / stock.getCurrentPrice());
                capacityInfoLabel.setText(String.format("Capacity: Max %,d shares (Cash: $%,.2f)",
                        maxAfford, currentUser.getCashBalance()));
            } else {
                Holding h = currentUser.getPortfolio().getHolding(symbol);
                int owned = (h != null) ? h.getQuantity() : 0;
                capacityInfoLabel.setText(String.format("Capacity: Owned %,d shares", owned));
            }
        }
        updateEstimate();
    }

    private void updateEstimate() {
        String symbol = (String) stockCombo.getSelectedItem();
        if (symbol == null) return;

        Stock stock = tradingService.getMarket().getStock(symbol);
        if (stock == null) return;

        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) {
                estimateLabel.setText("$0.00");
                return;
            }
            double total = Math.round(qty * stock.getCurrentPrice() * 100.0) / 100.0;
            estimateLabel.setText(String.format("$%,.2f", total));
        } catch (NumberFormatException e) {
            estimateLabel.setText("$0.00");
        }
    }

    private void handleOrderExecution() {
        if (currentUser == null) {
            statusLabel.setText("Error: User session not loaded.");
            statusLabel.setForeground(GuiTheme.ACCENT_RED);
            return;
        }

        String symbol = (String) stockCombo.getSelectedItem();
        if (symbol == null) return;

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                statusLabel.setText("Quantity must be greater than zero.");
                statusLabel.setForeground(GuiTheme.ACCENT_RED);
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid quantity entered.");
            statusLabel.setForeground(GuiTheme.ACCENT_RED);
            return;
        }

        try {
            Transaction tx;
            if (buyRadio.isSelected()) {
                tx = tradingService.executeBuy(currentUser, symbol, quantity);
                statusLabel.setText(String.format("Success! Bought %d %s @ $%,.2f",
                        tx.getQuantity(), tx.getSymbol(), tx.getExecutionPrice()));
                statusLabel.setForeground(GuiTheme.ACCENT_GREEN);
            } else {
                tx = tradingService.executeSell(currentUser, symbol, quantity);
                statusLabel.setText(String.format("Success! Sold %d %s @ $%,.2f",
                        tx.getQuantity(), tx.getSymbol(), tx.getExecutionPrice()));
                statusLabel.setForeground(GuiTheme.ACCENT_GREEN);
            }

            if (onOrderExecuted != null) {
                onOrderExecuted.run();
            }
            updateQuoteAndCapacity();

        } catch (TradingException ex) {
            statusLabel.setText("Failed: " + ex.getMessage());
            statusLabel.setForeground(GuiTheme.ACCENT_RED);
        }
    }
}
