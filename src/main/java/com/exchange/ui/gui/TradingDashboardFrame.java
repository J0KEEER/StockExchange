package com.exchange.ui.gui;

import com.exchange.engine.TradingService;
import com.exchange.market.Market;
import com.exchange.market.MarketObserver;
import com.exchange.model.Stock;
import com.exchange.model.User;
import com.exchange.persistence.PersistenceService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application desktop window providing a financial trading workstation dashboard.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class TradingDashboardFrame extends JFrame implements MarketObserver {

    /** Core trading engine service interface. */
    private final TradingService tradingService;

    /** File-based persistence storage service. */
    private final PersistenceService persistenceService;

    /** Active account holder profile. */
    private User currentUser;

    /** Top metrics and simulation toolbar panel. */
    private final MetricsHeaderPanel headerPanel;

    /** Market watch table presenting real-time quotes. */
    private final MarketWatchPanel marketWatchPanel;

    /** Direct order execution ticket card. */
    private final OrderTicketPanel orderTicketPanel;

    /** Portfolio holdings and position return table. */
    private final HoldingsTablePanel holdingsPanel;

    /** Historical trade execution audit ledger table. */
    private final TransactionLedgerPanel ledgerPanel;

    /**
     * Constructs and initializes the desktop GUI trading workstation.
     *
     * @param tradingService the business trading engine service
     * @param persistenceService the persistence storage service
     * @param initialUser the active account holder profile
     */
    public TradingDashboardFrame(TradingService tradingService, PersistenceService persistenceService, User initialUser) {
        super("Equity Stock Exchange & Portfolio Workstation");
        this.tradingService = tradingService;
        this.persistenceService = persistenceService;
        this.currentUser = initialUser;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(GuiTheme.BG_MAIN);
        setLayout(new BorderLayout(8, 8));

        // 1. Header Panel
        headerPanel = new MetricsHeaderPanel(
                this::handleManualTick,
                this::handleSaveSession
        );
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center: Split Pane with Market Watch (Left) & Order Ticket (Right)
        marketWatchPanel = new MarketWatchPanel(this::onStockSelectedInMarket);
        orderTicketPanel = new OrderTicketPanel(tradingService, this::onOrderExecuted);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, marketWatchPanel, orderTicketPanel);
        centerSplit.setDividerLocation(740);
        centerSplit.setResizeWeight(0.65);
        centerSplit.setBorder(null);
        centerSplit.setBackground(GuiTheme.BG_MAIN);

        // 3. South: Tabbed Pane with Holdings and Transaction Ledger
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.setBackground(GuiTheme.BG_CARD);
        bottomTabs.setForeground(GuiTheme.TEXT_PRIMARY);
        bottomTabs.setFont(GuiTheme.FONT_BOLD);
        bottomTabs.setBorder(BorderFactory.createEmptyBorder(0, 14, 10, 14));

        holdingsPanel = new HoldingsTablePanel(this::onStockSelectedInMarket);
        ledgerPanel = new TransactionLedgerPanel();

        bottomTabs.addTab("  Portfolio Holdings & Positions  ", holdingsPanel);
        bottomTabs.addTab("  Executed Transaction Ledger  ", ledgerPanel);

        // Main Vertical Split between Market/Ticket and Bottom Tabs
        JSplitPane mainVerticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerSplit, bottomTabs);
        mainVerticalSplit.setDividerLocation(380);
        mainVerticalSplit.setResizeWeight(0.5);
        mainVerticalSplit.setBorder(null);
        mainVerticalSplit.setBackground(GuiTheme.BG_MAIN);

        add(mainVerticalSplit, BorderLayout.CENTER);

        // Subscribe as MarketObserver for real-time tick broadcasting
        tradingService.getMarket().addObserver(this);

        // Window closing hook to auto-save and stop timers
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                headerPanel.disposeTimer();
                try {
                    persistenceService.saveUserData(currentUser);
                } catch (Exception ex) {
                    System.err.println("Failed to auto-save on close: " + ex.getMessage());
                }
            }
        });

        // Initial Data Refresh
        refreshAllViews();
    }

    private void handleManualTick() {
        tradingService.getMarket().tick();
    }

    private void handleSaveSession() {
        try {
            persistenceService.saveUserData(currentUser);
            JOptionPane.showMessageDialog(this,
                    "Session successfully saved to CSV storage in data/ directory.",
                    "Session Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving session data: " + e.getMessage(),
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onStockSelectedInMarket(String symbol) {
        orderTicketPanel.setSelectedStock(symbol);
        marketWatchPanel.setSelectedStock(symbol);
    }

    private void onOrderExecuted() {
        refreshAllViews();
        // Auto-save on execution for durability
        try {
            persistenceService.saveUserData(currentUser);
        } catch (Exception ignored) {
            // Background save
        }
    }

    /**
     * Refreshes all child panels and tables with the latest state.
     */
    public void refreshAllViews() {
        Market market = tradingService.getMarket();
        headerPanel.refreshMetrics(currentUser, market);
        marketWatchPanel.refreshMarketData(market);
        orderTicketPanel.refreshContext(currentUser, market);
        holdingsPanel.refreshHoldings(currentUser, market);
        ledgerPanel.refreshLedger(currentUser);
    }

    @Override
    public void onPriceUpdated(Stock stock, double oldPrice, double newPrice) {
        // Observer tick callback: ensure UI updates on EDT
        SwingUtilities.invokeLater(this::refreshAllViews);
    }

    /**
     * Updates the current active account holder.
     *
     * @param user the new user profile
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        refreshAllViews();
    }
}
