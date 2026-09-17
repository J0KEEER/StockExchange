package com.exchange.ui;

import com.exchange.market.Market;
import com.exchange.model.Holding;
import com.exchange.model.Stock;
import com.exchange.model.Transaction;
import com.exchange.model.User;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing professional ASCII tabular presentation, alignment, and formatting
 * for financial data across console displays.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class TextTableFormatter {

    /**
     * Private constructor to prevent instantiation of utility formatting class.
     */
    private TextTableFormatter() {
        // Utility class
    }

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Renders a formatted table presenting real-time market quotes and day metrics.
     *
     * @param stocks the collection of listed equities
     * @param tickCount the market simulation tick sequence number
     */
    public static void printMarketTable(Collection<Stock> stocks, int tickCount) {
        System.out.println();
        System.out.println(BOLD + CYAN + "======================================================================================================================" + RESET);
        System.out.printf(BOLD + "  LIVE EXCHANGE MARKET QUOTES (Simulation Tick #%d)%n" + RESET, tickCount);
        System.out.println(CYAN + "======================================================================================================================" + RESET);
        System.out.printf(BOLD + " %-6s | %-22s | %-15s | %-4s | %-11s | %-10s | %-9s | %-12s%n" + RESET,
                "TICKER", "COMPANY NAME", "SECTOR", "BETA", "SPOT PRICE", "DAY CHANGE", "% CHANGE", "VOLUME");
        System.out.println("----------------------------------------------------------------------------------------------------------------------");

        for (Stock s : stocks) {
            double change = s.getDailyChange();
            double pct = s.getDailyChangePercent();
            String color = change >= 0 ? GREEN : RED;
            String sign = change >= 0 ? "+" : "";

            System.out.printf(" %-6s | %-22s | %-15s | %4.2f | $%,10.2f | %s%s$%,8.2f%s | %s%s%6.2f%%%s | %,12d%n",
                    s.getSymbol(),
                    truncate(s.getCompanyName(), 22),
                    truncate(s.getSector().getDisplayName(), 15),
                    s.getBeta(),
                    s.getCurrentPrice(),
                    color, sign, change, RESET,
                    color, sign, pct, RESET,
                    s.getVolume());
        }
        System.out.println(CYAN + "======================================================================================================================" + RESET);
    }

    /**
     * Renders a comprehensive portfolio statement including cost basis, current value,
     * and unrealized profit/loss.
     *
     * @param user the account holder
     * @param market the live market engine
     */
    public static void printPortfolioTable(User user, Market market) {
        Map<String, Holding> holdings = user.getPortfolio().getHoldings();
        Map<String, Double> prices = market.getPriceSnapshot();

        System.out.println();
        System.out.println(BOLD + CYAN + "======================================================================================================" + RESET);
        System.out.printf(BOLD + "  PORTFOLIO POSITIONS & P/L ANALYTICS - %s (Cash: $%,.2f)%n" + RESET,
                user.getUsername(), user.getCashBalance());
        System.out.println(CYAN + "======================================================================================================" + RESET);

        if (holdings.isEmpty()) {
            System.out.println("  No active equity holdings. Liquid capital available to trade: $"
                    + String.format("%,.2f", user.getCashBalance()));
            System.out.println(CYAN + "======================================================================================================" + RESET);
            return;
        }

        System.out.printf(BOLD + " %-6s | %-6s | %-11s | %-11s | %-12s | %-12s | %-10s%n" + RESET,
                "TICKER", "SHARES", "AVG COST", "CURRENT", "BOOK VALUE", "MARKET VALUE", "UNREALIZED P/L");
        System.out.println("------------------------------------------------------------------------------------------------------");

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

            String color = unRealized >= 0 ? GREEN : RED;
            String sign = unRealized >= 0 ? "+" : "";

            System.out.printf(" %-6s | %6d | $%,9.2f | $%,9.2f | $%,10.2f | $%,10.2f | %s%s$%,9.2f (%s%5.1f%%)%s%n",
                    h.getSymbol(),
                    h.getQuantity(),
                    h.getAverageCostBasis(),
                    spot,
                    bookVal,
                    marketVal,
                    color, sign, unRealized, sign, roi, RESET);
        }

        System.out.println("------------------------------------------------------------------------------------------------------");
        String totalColor = totalUnrealized >= 0 ? GREEN : RED;
        String totalSign = totalUnrealized >= 0 ? "+" : "";
        double portfolioRoi = totalBook > 0 ? (totalUnrealized / totalBook) * 100.0 : 0.0;

        System.out.printf(BOLD + " TOTALS | %6s | %-11s | %-11s | $%,10.2f | $%,10.2f | %s%s$%,9.2f (%s%5.1f%%)%s%n" + RESET,
                "", "", "", totalBook, totalMarket, totalColor, totalSign, totalUnrealized, totalSign, portfolioRoi, RESET);
        System.out.println(CYAN + "======================================================================================================" + RESET);
    }

    /**
     * Renders the chronological trade execution ledger.
     *
     * @param transactions list of recorded trades
     */
    public static void printTransactionLedger(List<Transaction> transactions) {
        System.out.println();
        System.out.println(BOLD + CYAN + "==================================================================================================" + RESET);
        System.out.println(BOLD + "  EXECUTED TRANSACTION AUDIT LEDGER" + RESET);
        System.out.println(CYAN + "==================================================================================================" + RESET);

        if (transactions.isEmpty()) {
            System.out.println("  No transactions recorded yet.");
            System.out.println(CYAN + "==================================================================================================" + RESET);
            return;
        }

        System.out.printf(BOLD + " %-8s | %-19s | %-5s | %-6s | %-6s | %-10s | %-11s | %-13s%n" + RESET,
                "ORDER ID", "TIMESTAMP", "TYPE", "TICKER", "SHARES", "EXEC PRICE", "TOTAL AMOUNT", "REALIZED P/L");
        System.out.println("--------------------------------------------------------------------------------------------------");

        for (Transaction tx : transactions) {
            String pnlStr = "-";
            if (tx.getType().name().equals("SELL")) {
                double pnl = tx.getRealizedGainLoss();
                String col = pnl >= 0 ? GREEN : RED;
                String sgn = pnl >= 0 ? "+" : "";
                pnlStr = String.format("%s%s$%,.2f%s", col, sgn, pnl, RESET);
            }

            System.out.printf(" %-8s | %-19s | %-5s | %-6s | %6d | $%,8.2f | $%,10.2f | %-13s%n",
                    tx.getTransactionId(),
                    tx.getTimestamp().format(TIME_FMT),
                    tx.getType(),
                    tx.getSymbol(),
                    tx.getQuantity(),
                    tx.getExecutionPrice(),
                    tx.getTotalAmount(),
                    pnlStr);
        }
        System.out.println(CYAN + "==================================================================================================" + RESET);
    }

    /**
     * Renders a high-level account valuation summary card.
     *
     * @param user the account holder
     * @param market the market engine
     */
    public static void printAccountSummary(User user, Market market) {
        Map<String, Double> prices = market.getPriceSnapshot();
        double cash = user.getCashBalance();
        double holdingsValue = user.getPortfolio().getMarketValue(prices);
        double netWorth = user.getNetWorth(prices);
        double totalReturn = user.getTotalReturn(prices);
        double returnPct = user.getTotalReturnPercent(prices);
        double realizedPnl = user.getPortfolio().getCumulativeRealizedProfitLoss();
        double unrealizedPnl = user.getPortfolio().getUnrealizedProfitLoss(prices);

        String returnColor = totalReturn >= 0 ? GREEN : RED;
        String returnSign = totalReturn >= 0 ? "+" : "";

        System.out.println();
        System.out.println(BOLD + CYAN + "┌─────────────────────────────────────────────────────────────┐" + RESET);
        System.out.printf(BOLD + CYAN + "│" + RESET + BOLD + "  ACCOUNT OVERVIEW & VALUATION SUMMARY                       " + CYAN + "│%n" + RESET);
        System.out.println(BOLD + CYAN + "├─────────────────────────────────────────────────────────────┤" + RESET);
        System.out.printf("│  Investor Handle      : %-36s│%n", user.getUsername());
        System.out.printf("│  Account ID           : %-36s│%n", user.getUserId());
        System.out.printf("│  Initial Capital      : $%,-35.2f│%n", user.getInitialCapital());
        System.out.printf("│  Liquid Cash Balance  : $%,-35.2f│%n", cash);
        System.out.printf("│  Equities Market Value: $%,-35.2f│%n", holdingsValue);
        System.out.printf(BOLD + "│  Total Net Worth      : $%,-35.2f" + RESET + "│%n", netWorth);
        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.printf("│  Cumulative Realized  : %s%s$%,-34.2f%s│%n",
                (realizedPnl >= 0 ? GREEN : RED), (realizedPnl >= 0 ? "+" : ""), realizedPnl, RESET);
        System.out.printf("│  Unrealized (Paper)   : %s%s$%,-34.2f%s│%n",
                (unrealizedPnl >= 0 ? GREEN : RED), (unrealizedPnl >= 0 ? "+" : ""), unrealizedPnl, RESET);
        System.out.printf(BOLD + "│  All-Time Net Return  : %s%s$%,.2f (%s%.2f%%)%s",
                returnColor, returnSign, totalReturn, returnSign, returnPct, RESET);
        // Padding calculation
        String rawReturn = String.format("%s$%,.2f (%s%.2f%%)", returnSign, totalReturn, returnSign, returnPct);
        int pad = Math.max(1, 35 - rawReturn.length());
        System.out.printf("%" + pad + "s│%n", "");
        System.out.println(BOLD + CYAN + "└─────────────────────────────────────────────────────────────┘" + RESET);
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
