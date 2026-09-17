package com.exchange.ui.gui;

import com.exchange.market.Market;
import com.exchange.model.User;
import com.exchange.persistence.PersistenceService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Top dashboard panel displaying high-level portfolio KPI cards and simulation controls.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class MetricsHeaderPanel extends JPanel {

    /** Label displaying the account's total net worth. */
    private final JLabel netWorthLabel;

    /** Label displaying total all-time return in dollars and percent. */
    private final JLabel returnLabel;

    /** Label displaying liquid cash available to trade. */
    private final JLabel cashLabel;

    /** Label displaying total spot valuation of equities. */
    private final JLabel equitiesLabel;

    /** Label displaying unrealized paper gains and losses. */
    private final JLabel unrealizedLabel;

    /** Label displaying cumulative realized gains and losses. */
    private final JLabel realizedLabel;

    /** Label displaying current simulation tick count. */
    private final JLabel tickCountLabel;

    /** Label displaying broad macroeconomic market trend and sentiment. */
    private final JLabel macroSentimentLabel;

    /** Button triggering a single discrete simulation tick. */
    private final JButton tickButton;

    /** Toggle button starting or stopping live continuous market streaming. */
    private final JToggleButton autoTickToggle;

    /** Button persisting active user state to CSV. */
    private final JButton saveButton;

    /** Background Swing Timer driving continuous market ticks. */
    private final Timer simulationTimer;

    /**
     * Constructs the MetricsHeaderPanel with bound user and simulation event handlers.
     *
     * @param onTickRequested callback executed when manual tick is triggered
     * @param onSaveRequested callback executed when save session is triggered
     */
    public MetricsHeaderPanel(Runnable onTickRequested, Runnable onSaveRequested) {
        setLayout(new BorderLayout(10, 10));
        setBackground(GuiTheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        // 1. Top Title & Control Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(GuiTheme.BG_MAIN);

        JLabel titleLabel = new JLabel("EQUITY STOCK EXCHANGE & PORTFOLIO WORKSTATION");
        titleLabel.setFont(GuiTheme.FONT_TITLE);
        titleLabel.setForeground(GuiTheme.TEXT_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(GuiTheme.BG_MAIN);

        macroSentimentLabel = new JLabel("Macro: Neutral (+0.00%)");
        macroSentimentLabel.setFont(GuiTheme.FONT_BOLD);
        macroSentimentLabel.setForeground(GuiTheme.TEXT_SECONDARY);

        tickCountLabel = new JLabel("Tick: #0");
        tickCountLabel.setFont(GuiTheme.FONT_BOLD);
        tickCountLabel.setForeground(GuiTheme.TEXT_SECONDARY);

        tickButton = new JButton("Tick Market (+1)");
        GuiTheme.styleButton(tickButton, GuiTheme.BG_CARD_HOVER, GuiTheme.TEXT_PRIMARY);
        tickButton.addActionListener(e -> {
            if (onTickRequested != null) {
                onTickRequested.run();
            }
        });

        autoTickToggle = new JToggleButton("Live Stream: OFF");
        GuiTheme.styleButton(autoTickToggle, GuiTheme.BG_CARD, GuiTheme.TEXT_SECONDARY);

        simulationTimer = new Timer(2500, e -> {
            if (onTickRequested != null) {
                onTickRequested.run();
            }
        });

        autoTickToggle.addActionListener(e -> {
            if (autoTickToggle.isSelected()) {
                autoTickToggle.setText("Live Stream: ON");
                autoTickToggle.setBackground(new Color(6, 78, 59));
                autoTickToggle.setForeground(GuiTheme.ACCENT_GREEN);
                simulationTimer.start();
            } else {
                autoTickToggle.setText("Live Stream: OFF");
                autoTickToggle.setBackground(GuiTheme.BG_CARD);
                autoTickToggle.setForeground(GuiTheme.TEXT_SECONDARY);
                simulationTimer.stop();
            }
        });

        saveButton = new JButton("Save Session");
        GuiTheme.styleButton(saveButton, GuiTheme.ACCENT_BLUE, Color.WHITE);
        saveButton.addActionListener(e -> {
            if (onSaveRequested != null) {
                onSaveRequested.run();
            }
        });

        controls.add(macroSentimentLabel);
        controls.add(Box.createHorizontalStrut(4));
        controls.add(tickCountLabel);
        controls.add(tickButton);
        controls.add(autoTickToggle);
        controls.add(saveButton);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);

        // 2. Metrics Cards Row
        JPanel cardsRow = new JPanel(new GridLayout(1, 5, 10, 0));
        cardsRow.setBackground(GuiTheme.BG_MAIN);

        // Card 1: Net Worth
        JPanel cardNetWorth = GuiTheme.createCardPanel();
        cardNetWorth.setLayout(new BoxLayout(cardNetWorth, BoxLayout.Y_AXIS));
        JLabel titleNetWorth = createCardTitle("TOTAL NET WORTH");
        netWorthLabel = createCardValue("$0.00", GuiTheme.TEXT_PRIMARY);
        returnLabel = new JLabel("Return: +$0.00 (+0.0%)");
        returnLabel.setFont(GuiTheme.FONT_REGULAR);
        returnLabel.setForeground(GuiTheme.TEXT_SECONDARY);
        cardNetWorth.add(titleNetWorth);
        cardNetWorth.add(Box.createVerticalStrut(4));
        cardNetWorth.add(netWorthLabel);
        cardNetWorth.add(Box.createVerticalStrut(2));
        cardNetWorth.add(returnLabel);

        // Card 2: Liquid Cash
        JPanel cardCash = GuiTheme.createCardPanel();
        cardCash.setLayout(new BoxLayout(cardCash, BoxLayout.Y_AXIS));
        JLabel titleCash = createCardTitle("LIQUID CASH BALANCE");
        cashLabel = createCardValue("$0.00", GuiTheme.ACCENT_BLUE);
        JLabel subCash = new JLabel("Available to Trade");
        subCash.setFont(GuiTheme.FONT_REGULAR);
        subCash.setForeground(GuiTheme.TEXT_MUTED);
        cardCash.add(titleCash);
        cardCash.add(Box.createVerticalStrut(4));
        cardCash.add(cashLabel);
        cardCash.add(Box.createVerticalStrut(2));
        cardCash.add(subCash);

        // Card 3: Holdings Value
        JPanel cardEquities = GuiTheme.createCardPanel();
        cardEquities.setLayout(new BoxLayout(cardEquities, BoxLayout.Y_AXIS));
        JLabel titleEquities = createCardTitle("EQUITIES VALUATION");
        equitiesLabel = createCardValue("$0.00", GuiTheme.TEXT_PRIMARY);
        JLabel subEquities = new JLabel("Market Spot Value");
        subEquities.setFont(GuiTheme.FONT_REGULAR);
        subEquities.setForeground(GuiTheme.TEXT_MUTED);
        cardEquities.add(titleEquities);
        cardEquities.add(Box.createVerticalStrut(4));
        cardEquities.add(equitiesLabel);
        cardEquities.add(Box.createVerticalStrut(2));
        cardEquities.add(subEquities);

        // Card 4: Unrealized P/L
        JPanel cardUnrealized = GuiTheme.createCardPanel();
        cardUnrealized.setLayout(new BoxLayout(cardUnrealized, BoxLayout.Y_AXIS));
        JLabel titleUnrealized = createCardTitle("UNREALIZED P/L (PAPER)");
        unrealizedLabel = createCardValue("+$0.00", GuiTheme.ACCENT_GREEN);
        JLabel subUnrealized = new JLabel("Open Positions");
        subUnrealized.setFont(GuiTheme.FONT_REGULAR);
        subUnrealized.setForeground(GuiTheme.TEXT_MUTED);
        cardUnrealized.add(titleUnrealized);
        cardUnrealized.add(Box.createVerticalStrut(4));
        cardUnrealized.add(unrealizedLabel);
        cardUnrealized.add(Box.createVerticalStrut(2));
        cardUnrealized.add(subUnrealized);

        // Card 5: Realized P/L
        JPanel cardRealized = GuiTheme.createCardPanel();
        cardRealized.setLayout(new BoxLayout(cardRealized, BoxLayout.Y_AXIS));
        JLabel titleRealized = createCardTitle("CUMULATIVE REALIZED P/L");
        realizedLabel = createCardValue("+$0.00", GuiTheme.ACCENT_GREEN);
        JLabel subRealized = new JLabel("Closed Liquidations");
        subRealized.setFont(GuiTheme.FONT_REGULAR);
        subRealized.setForeground(GuiTheme.TEXT_MUTED);
        cardRealized.add(titleRealized);
        cardRealized.add(Box.createVerticalStrut(4));
        cardRealized.add(realizedLabel);
        cardRealized.add(Box.createVerticalStrut(2));
        cardRealized.add(subRealized);

        cardsRow.add(cardNetWorth);
        cardsRow.add(cardCash);
        cardsRow.add(cardEquities);
        cardsRow.add(cardUnrealized);
        cardsRow.add(cardRealized);

        add(topBar, BorderLayout.NORTH);
        add(cardsRow, BorderLayout.CENTER);
    }

    private JLabel createCardTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(GuiTheme.TEXT_SECONDARY);
        return label;
    }

    private JLabel createCardValue(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(GuiTheme.FONT_METRIC);
        label.setForeground(color);
        return label;
    }

    /**
     * Refreshes all metrics and card values using the latest state of user and market.
     *
     * @param user the account holder
     * @param market the current exchange market
     */
    public void refreshMetrics(User user, Market market) {
        if (user == null || market == null) return;

        Map<String, Double> prices = market.getPriceSnapshot();
        double cash = user.getCashBalance();
        double equitiesVal = user.getPortfolio().getMarketValue(prices);
        double netWorth = user.getNetWorth(prices);
        double totalReturn = user.getTotalReturn(prices);
        double returnPct = user.getTotalReturnPercent(prices);
        double unrealizedPnl = user.getPortfolio().getUnrealizedProfitLoss(prices);
        double realizedPnl = user.getPortfolio().getCumulativeRealizedProfitLoss();

        String macro = market.getMacroSentiment();
        macroSentimentLabel.setText("Macro: " + macro);
        if (macro.startsWith("Bullish")) {
            macroSentimentLabel.setForeground(GuiTheme.ACCENT_GREEN);
        } else if (macro.startsWith("Bearish")) {
            macroSentimentLabel.setForeground(GuiTheme.ACCENT_RED);
        } else {
            macroSentimentLabel.setForeground(GuiTheme.TEXT_SECONDARY);
        }

        tickCountLabel.setText("Tick: #" + market.getTickCount());
        netWorthLabel.setText(String.format("$%,.2f", netWorth));
        cashLabel.setText(String.format("$%,.2f", cash));
        equitiesLabel.setText(String.format("$%,.2f", equitiesVal));

        // Return label
        String retSign = totalReturn >= 0 ? "+" : "";
        returnLabel.setText(String.format("Return: %s$%,.2f (%s%.2f%%)",
                retSign, totalReturn, retSign, returnPct));
        returnLabel.setForeground(totalReturn >= 0 ? GuiTheme.ACCENT_GREEN : GuiTheme.ACCENT_RED);

        // Unrealized label
        String unSign = unrealizedPnl >= 0 ? "+" : "";
        unrealizedLabel.setText(String.format("%s$%,.2f", unSign, unrealizedPnl));
        unrealizedLabel.setForeground(unrealizedPnl >= 0 ? GuiTheme.ACCENT_GREEN : GuiTheme.ACCENT_RED);

        // Realized label
        String relSign = realizedPnl >= 0 ? "+" : "";
        realizedLabel.setText(String.format("%s$%,.2f", relSign, realizedPnl));
        realizedLabel.setForeground(realizedPnl >= 0 ? GuiTheme.ACCENT_GREEN : GuiTheme.ACCENT_RED);
    }

    /**
     * Stops background simulation timers when the application closes.
     */
    public void disposeTimer() {
        if (simulationTimer != null && simulationTimer.isRunning()) {
            simulationTimer.stop();
        }
    }
}
