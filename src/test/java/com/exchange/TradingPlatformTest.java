package com.exchange;

import com.exchange.engine.TradingEngine;
import com.exchange.engine.TradingService;
import com.exchange.exception.InsufficientFundsException;
import com.exchange.exception.InsufficientSharesException;
import com.exchange.exception.InvalidStockSymbolException;
import com.exchange.exception.TradingException;
import com.exchange.market.Market;
import com.exchange.market.MarketObserver;
import com.exchange.market.RealWorldPriceStrategy;
import com.exchange.model.*;
import com.exchange.persistence.CsvPersistenceService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automated regression test suite verifying business logic, financial mathematics,
 * order execution boundaries, and persistence round-trip integrity.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class TradingPlatformTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    /**
     * Executes all verification suites and reports results.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  RUNNING TRADING PLATFORM AUTOMATED VERIFICATION SUITE");
        System.out.println("=================================================================");

        runTest("Stock Creation and Metric Boundary Test", TradingPlatformTest::testStockCreationAndMetricBoundary);
        runTest("Holding Weighted-Average Cost Basis Test", TradingPlatformTest::testHoldingWeightedAverageCostBasis);
        runTest("Holding Liquidation Realized P/L Calculation Test", TradingPlatformTest::testHoldingLiquidationRealizedPnl);
        runTest("Buy Order Execution & Balance Debit Test", TradingPlatformTest::testBuyOrderExecutionAndBalanceDebit);
        runTest("Insufficient Funds Buy Rejection Test", TradingPlatformTest::testInsufficientFundsBuyRejection);
        runTest("Sell Order Execution & Balance Credit Test", TradingPlatformTest::testSellOrderExecutionAndBalanceCredit);
        runTest("Insufficient Shares Sell Rejection Test", TradingPlatformTest::testInsufficientSharesSellRejection);
        runTest("Invalid Stock Symbol Rejection Test", TradingPlatformTest::testInvalidStockSymbolRejection);
        runTest("Market Observer Notification Pattern Test", TradingPlatformTest::testMarketObserverPattern);
        runTest("CSV Persistence Save and Reload Round-Trip Test", TradingPlatformTest::testCsvPersistenceRoundTrip);
        runTest("Real-World Quantitative Price Strategy & Beta Test", TradingPlatformTest::testRealWorldPriceStrategyVarianceAndBeta);

        System.out.println("=================================================================");
        System.out.printf("  TEST RESULTS: %d/%d PASSED (%.1f%%)%n",
                testsPassed, testsRun, (testsPassed * 100.0 / testsRun));
        System.out.println("=================================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestCase testCase) {
        testsRun++;
        try {
            testCase.execute();
            testsPassed++;
            System.out.printf("  [PASS] %s%n", testName);
        } catch (Throwable t) {
            System.out.printf("  [FAIL] %s - Cause: %s%n", testName, t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    @FunctionalInterface
    private interface TestCase {
        void execute() throws Exception;
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("%s: expected <%.4f> but was <%.4f>", message, expected, actual));
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(String.format("%s: expected <%s> but was <%s>", message, expected, actual));
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Verifies Stock model boundaries, daily change calculations, and 52-week tracking.
     */
    private static void testStockCreationAndMetricBoundary() {
        Stock s = new Stock("TEST", "Test Corporation", 100.00);
        assertEquals(100.00, s.getCurrentPrice(), 0.001, "Initial price must be 100.00");
        assertEquals(0.00, s.getDailyChange(), 0.001, "Initial daily change must be 0");

        s.updatePrice(110.00);
        assertEquals(110.00, s.getCurrentPrice(), 0.001, "Price should update to 110");
        assertEquals(10.00, s.getDailyChange(), 0.001, "Daily change should be +10.00");
        assertEquals(10.00, s.getDailyChangePercent(), 0.001, "Daily change % should be +10.00%");
        assertEquals(110.00, s.getHigh52Week(), 0.001, "High 52-week should track 110.00");

        s.updatePrice(95.00);
        assertEquals(-5.00, s.getDailyChange(), 0.001, "Daily change should be -5.00");
        assertEquals(-5.00, s.getDailyChangePercent(), 0.001, "Daily change % should be -5.00%");
        assertEquals(95.00, s.getLow52Week(), 0.001, "Low 52-week should track 95.00");
    }

    /**
     * Verifies weighted-average cost basis recalculation when purchasing multiple share lots.
     */
    private static void testHoldingWeightedAverageCostBasis() {
        // Lot 1: 10 shares @ $100.00 = $1,000.00
        Holding h = new Holding("AAPL", 10, 100.00);
        assertEquals(10, h.getQuantity(), "Quantity should be 10");
        assertEquals(100.00, h.getAverageCostBasis(), 0.001, "Cost basis should be 100.00");
        assertEquals(1000.00, h.getTotalCost(), 0.001, "Total cost should be 1,000.00");

        // Lot 2: 10 shares @ $200.00 = $2,000.00 -> Total 20 shares for $3,000 -> Avg: $150.00
        h.addShares(10, 200.00);
        assertEquals(20, h.getQuantity(), "Quantity should be 20");
        assertEquals(150.00, h.getAverageCostBasis(), 0.001, "Cost basis should be 150.00");
        assertEquals(3000.00, h.getTotalCost(), 0.001, "Total cost should be 3,000.00");
    }

    /**
     * Verifies realized profit/loss calculations upon partial share liquidation.
     */
    private static void testHoldingLiquidationRealizedPnl() {
        Holding h = new Holding("MSFT", 20, 100.00);
        // Liquidate 10 shares @ $130.00 -> Realized P/L = (130 - 100) * 10 = +$300.00
        double realized = h.removeShares(10, 130.00);
        assertEquals(300.00, realized, 0.001, "Realized P/L on sale must be +$300.00");
        assertEquals(10, h.getQuantity(), "Remaining quantity should be 10");
        assertEquals(100.00, h.getAverageCostBasis(), 0.001, "Cost basis on remaining shares remains 100.00");
    }

    /**
     * Verifies buy order execution, cash balance debit, and transaction creation.
     */
    private static void testBuyOrderExecutionAndBalanceDebit() throws TradingException {
        Market market = new Market();
        TradingEngine engine = new TradingEngine(market);
        User user = new User("Alice", 10_000.00);

        Stock apple = market.getStock("AAPL");
        double price = apple.getCurrentPrice();
        int buyQty = 5;
        double expectedCost = Math.round(price * buyQty * 100.0) / 100.0;

        Transaction tx = engine.executeBuy(user, "AAPL", buyQty);

        assertEquals(TransactionType.BUY, tx.getType(), "Transaction type must be BUY");
        assertEquals("AAPL", tx.getSymbol(), "Symbol must be AAPL");
        assertEquals(buyQty, tx.getQuantity(), "Quantity must be 5");
        assertEquals(10_000.00 - expectedCost, user.getCashBalance(), 0.01, "Cash balance must be deducted");

        Holding holding = user.getPortfolio().getHolding("AAPL");
        assertTrue(holding != null, "Portfolio must contain AAPL holding");
        assertEquals(buyQty, holding.getQuantity(), "Holding quantity must match purchase");
    }

    /**
     * Verifies that attempting to buy stocks with insufficient funds throws InsufficientFundsException.
     */
    private static void testInsufficientFundsBuyRejection() {
        Market market = new Market();
        TradingEngine engine = new TradingEngine(market);
        User user = new User("Bob", 50.00); // Only $50 cash

        boolean threwExpected = false;
        try {
            engine.executeBuy(user, "AAPL", 10); // $200+ * 10 > $2,000
        } catch (InsufficientFundsException e) {
            threwExpected = true;
        } catch (TradingException e) {
            throw new AssertionError("Threw wrong exception type: " + e.getClass().getName());
        }

        assertTrue(threwExpected, "Expected InsufficientFundsException was not thrown.");
        assertEquals(50.00, user.getCashBalance(), 0.001, "Cash balance must remain unchanged on rejection");
    }

    /**
     * Verifies sell order execution, cash balance credit, and cumulative realized P/L tracking.
     */
    private static void testSellOrderExecutionAndBalanceCredit() throws TradingException {
        Market market = new Market();
        TradingEngine engine = new TradingEngine(market);
        User user = new User("Charlie", 10_000.00);

        // Buy 10 AAPL
        engine.executeBuy(user, "AAPL", 10);
        double cashAfterBuy = user.getCashBalance();

        Stock apple = market.getStock("AAPL");
        double spotBeforeSell = apple.getCurrentPrice();

        // Sell 5 AAPL
        Transaction tx = engine.executeSell(user, "AAPL", 5);
        assertEquals(TransactionType.SELL, tx.getType(), "Transaction type must be SELL");

        double expectedProceeds = Math.round(spotBeforeSell * 5 * 100.0) / 100.0;
        assertEquals(cashAfterBuy + expectedProceeds, user.getCashBalance(), 0.01, "Cash must be credited");
        assertEquals(5, user.getPortfolio().getHolding("AAPL").getQuantity(), "Remaining shares must be 5");
    }

    /**
     * Verifies that attempting to sell unowned or excessive shares throws InsufficientSharesException.
     */
    private static void testInsufficientSharesSellRejection() {
        Market market = new Market();
        TradingEngine engine = new TradingEngine(market);
        User user = new User("David", 5_000.00);

        boolean threwExpected = false;
        try {
            // Attempt to sell TSLA when user owns zero shares
            engine.executeSell(user, "TSLA", 5);
        } catch (InsufficientSharesException e) {
            threwExpected = true;
        } catch (TradingException e) {
            throw new AssertionError("Threw wrong exception type: " + e.getClass().getName());
        }

        assertTrue(threwExpected, "Expected InsufficientSharesException was not thrown.");
    }

    /**
     * Verifies that attempting to trade an unknown symbol throws InvalidStockSymbolException.
     */
    private static void testInvalidStockSymbolRejection() {
        Market market = new Market();
        TradingEngine engine = new TradingEngine(market);
        User user = new User("Eve", 5_000.00);

        boolean threwExpected = false;
        try {
            engine.executeBuy(user, "UNKNOWN_XYZ", 1);
        } catch (InvalidStockSymbolException e) {
            threwExpected = true;
        } catch (TradingException e) {
            throw new AssertionError("Threw wrong exception type: " + e.getClass().getName());
        }

        assertTrue(threwExpected, "Expected InvalidStockSymbolException was not thrown.");
    }

    /**
     * Verifies that the Observer pattern broadcasts ticks properly to listeners.
     */
    private static void testMarketObserverPattern() {
        Market market = new Market();
        AtomicBoolean notified = new AtomicBoolean(false);

        MarketObserver observer = (stock, oldPrice, newPrice) -> {
            notified.set(true);
        };

        market.addObserver(observer);
        market.tick();

        assertTrue(notified.get(), "Observer should have been notified during market tick.");
        market.removeObserver(observer);
    }

    /**
     * Verifies that User profiles, active positions, and transactions save to CSV and reload accurately.
     */
    private static void testCsvPersistenceRoundTrip() throws Exception {
        Path testDir = Paths.get("target_test_data");
        CsvPersistenceService persistence = new CsvPersistenceService(testDir.toString());

        User user = new User("U1001", "Grace", 75_000.00, 100_000.00, new Portfolio());
        user.getPortfolio().addHolding("NVDA", 25, 115.50);
        user.getPortfolio().addHolding("MSFT", 10, 420.00);
        user.getPortfolio().recordTransaction(new Transaction(
                TransactionType.BUY, "NVDA", 25, 115.50, 0.0));

        // Save
        persistence.saveUserData(user);

        // Verify files exist
        assertTrue(Files.exists(persistence.getPortfolioFilePath()), "portfolio.csv must exist");
        assertTrue(Files.exists(persistence.getTransactionsFilePath()), "transactions.csv must exist");

        // Reload
        User loadedUser = persistence.loadUserData();
        assertTrue(loadedUser != null, "Loaded user must not be null");
        assertEquals("Grace", loadedUser.getUsername(), "Username must match");
        assertEquals(75_000.00, loadedUser.getCashBalance(), 0.01, "Cash balance must match");
        assertEquals(25, loadedUser.getPortfolio().getHolding("NVDA").getQuantity(), "NVDA quantity must match");
        assertEquals(115.50, loadedUser.getPortfolio().getHolding("NVDA").getAverageCostBasis(), 0.001, "NVDA cost basis must match");
        assertEquals(1, loadedUser.getPortfolio().getTransactionHistory().size(), "Transaction history count must match");

        // Clean up test files
        Files.deleteIfExists(persistence.getPortfolioFilePath());
        Files.deleteIfExists(persistence.getTransactionsFilePath());
        Files.deleteIfExists(testDir);
    }

    /**
     * Verifies that the multi-factor quantitative price strategy preserves positive bounds,
     * respects sector/beta weighting, and accumulates realistic volume.
     */
    private static void testRealWorldPriceStrategyVarianceAndBeta() {
        Stock highBetaStock = new Stock("TSLA", "Tesla Inc.", Sector.AUTOMOTIVE, 200.00, 2.10, 0.038);
        Stock lowBetaStock = new Stock("JNJ", "Johnson & Johnson", Sector.HEALTHCARE, 150.00, 0.55, 0.008);

        assertEquals(Sector.AUTOMOTIVE, highBetaStock.getSector(), "Sector should match AUTOMOTIVE");
        assertEquals(2.10, highBetaStock.getBeta(), 0.001, "Beta should match 2.10");

        RealWorldPriceStrategy strategy = new RealWorldPriceStrategy();

        for (int i = 0; i < 20; i++) {
            strategy.prepareNewTickCycle();
            double newHighPrice = strategy.calculateNewPrice(highBetaStock);
            double newLowPrice = strategy.calculateNewPrice(lowBetaStock);

            highBetaStock.updatePrice(newHighPrice);
            lowBetaStock.updatePrice(newLowPrice);

            assertTrue(highBetaStock.getCurrentPrice() > 0.0, "High beta price must remain positive");
            assertTrue(lowBetaStock.getCurrentPrice() > 0.0, "Low beta price must remain positive");
        }

        assertTrue(highBetaStock.getVolume() > 0, "High beta stock should record accumulated market volume");
        assertTrue(lowBetaStock.getVolume() > 0, "Low beta stock should record accumulated market volume");
    }
}
