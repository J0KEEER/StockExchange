package com.exchange.ui;

import com.exchange.engine.TradingService;
import com.exchange.exception.TradingException;
import com.exchange.market.Market;
import com.exchange.model.Stock;
import com.exchange.model.Transaction;
import com.exchange.model.User;
import com.exchange.persistence.PersistenceService;

import java.util.Scanner;

/**
 * Interactive command-line interface controlling user inputs, navigation menus, and execution flows.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class ConsoleUI {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    private final TradingService tradingService;
    private final PersistenceService persistenceService;
    private final Scanner scanner;
    private User currentUser;

    /**
     * Constructs a ConsoleUI instance bound to the trading engine and persistence service.
     *
     * @param tradingService the trading business logic provider
     * @param persistenceService the file persistence layer
     * @param scanner the shared input scanner
     */
    public ConsoleUI(TradingService tradingService, PersistenceService persistenceService, Scanner scanner) {
        this.tradingService = tradingService;
        this.persistenceService = persistenceService;
        this.scanner = scanner;
    }

    /**
     * Initializes user authentication/session setup and starts the interactive navigation loop.
     */
    public void start() {
        printBanner();
        setupUserSession();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Select an option (1-9): ", 1, 9);
            switch (choice) {
                case 1 -> viewMarket();
                case 2 -> handleBuy();
                case 3 -> handleSell();
                case 4 -> viewPortfolio();
                case 5 -> viewTransactions();
                case 6 -> advanceMarketSimulation();
                case 7 -> viewAccountSummary();
                case 8 -> saveSession();
                case 9 -> {
                    saveSession();
                    System.out.println(GREEN + "Session safely preserved. Thank you for trading on the Exchange!" + RESET);
                    running = false;
                }
                default -> System.out.println(RED + "Invalid selection. Please try again." + RESET);
            }
        }
    }

    /**
     * Restores a previously saved profile or prompts the user to initialize a new trading account.
     */
    private void setupUserSession() {
        if (persistenceService.hasSavedData()) {
            System.out.println(YELLOW + "Detected previously saved investment session on disk." + RESET);
            System.out.print("Would you like to restore this profile? (Y/n): ");
            String response = scanner.nextLine().trim();
            if (response.isEmpty() || response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
                try {
                    currentUser = persistenceService.loadUserData();
                    if (currentUser != null) {
                        System.out.printf(GREEN + "Successfully restored profile for %s! Liquid Balance: $%,.2f%n" + RESET,
                                currentUser.getUsername(), currentUser.getCashBalance());
                        return;
                    }
                } catch (Exception e) {
                    System.out.println(RED + "Warning: Failed to restore existing data: " + e.getMessage() + RESET);
                    System.out.println("Starting a new account profile instead.");
                }
            }
        }

        // New profile initialization
        System.out.println(CYAN + "--- Creating New Investor Account ---" + RESET);
        String name = "";
        while (name.trim().isEmpty()) {
            System.out.print("Enter your investor / trader name: ");
            name = scanner.nextLine().trim();
        }

        double initialCash = readDouble("Enter initial starting capital deposit (default $100,000.00): ",
                100.0, 10_000_000.0, 100_000.0);

        currentUser = new User(name, initialCash);
        System.out.printf(GREEN + "Account initialized for %s with starting capital of $%,.2f%n" + RESET,
                currentUser.getUsername(), currentUser.getCashBalance());
    }

    private void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("==========================================================================");
        System.out.println("     EQUITY STOCK EXCHANGE & PORTFOLIO SIMULATION PLATFORM");
        System.out.println("             Academic Portfolio Engineering Project");
        System.out.println("==========================================================================" + RESET);
    }

    private void printMenu() {
        Market market = tradingService.getMarket();
        System.out.println();
        System.out.println(BOLD + "MAIN NAVIGATION (Tick #" + market.getTickCount() + " | Cash: $"
                + String.format("%,.2f", currentUser.getCashBalance()) + ")" + RESET);
        System.out.println("  1. View Live Market Quotes");
        System.out.println("  2. Buy Equities (Market Order)");
        System.out.println("  3. Sell Equities (Market Order)");
        System.out.println("  4. View Portfolio & Real-Time P/L Analytics");
        System.out.println("  5. View Transaction Ledger");
        System.out.println("  6. Advance Market Simulation (Price Fluctuation Tick)");
        System.out.println("  7. View Account Summary & Net Worth");
        System.out.println("  8. Save Session Data");
        System.out.println("  9. Save & Exit Platform");
    }

    private void viewMarket() {
        TextTableFormatter.printMarketTable(tradingService.getMarket().getAllStocks(),
                tradingService.getMarket().getTickCount());
    }

    private void handleBuy() {
        System.out.println(CYAN + "\n--- EXECUTE MARKET BUY ORDER ---" + RESET);
        System.out.printf("Available Liquid Balance: $%,.2f%n", currentUser.getCashBalance());
        System.out.print("Enter equity ticker symbol (e.g. AAPL, NVDA): ");
        String symbol = scanner.nextLine().trim().toUpperCase();

        Stock stock = tradingService.getMarket().getStock(symbol);
        if (stock == null) {
            System.out.println(RED + "Error: Unrecognized ticker symbol '" + symbol + "'." + RESET);
            return;
        }

        System.out.printf("Current quote for %s (%s): $%,.2f%n",
                stock.getSymbol(), stock.getCompanyName(), stock.getCurrentPrice());

        int maxAffordable = (int) (currentUser.getCashBalance() / stock.getCurrentPrice());
        System.out.printf("Maximum shares you can afford: %,d%n", maxAffordable);
        if (maxAffordable <= 0) {
            System.out.println(RED + "Insufficient funds to purchase even 1 share." + RESET);
            return;
        }

        int quantity = readInt("Enter number of shares to buy: ", 1, Integer.MAX_VALUE);
        try {
            Transaction tx = tradingService.executeBuy(currentUser, symbol, quantity);
            System.out.println(GREEN + BOLD + "BUY ORDER EXECUTED SUCCESSFULLY!" + RESET);
            System.out.println("Order Receipt: " + tx);
            System.out.printf("Remaining Cash Balance: $%,.2f%n", currentUser.getCashBalance());
        } catch (TradingException e) {
            System.out.println(RED + "Order Execution Failed: " + e.getMessage() + RESET);
        }
    }

    private void handleSell() {
        System.out.println(CYAN + "\n--- EXECUTE MARKET SELL ORDER ---" + RESET);
        if (!currentUser.getPortfolio().hasHoldings()) {
            System.out.println(YELLOW + "You do not own any equities to liquidate." + RESET);
            return;
        }

        TextTableFormatter.printPortfolioTable(currentUser, tradingService.getMarket());
        System.out.print("Enter equity ticker symbol to sell: ");
        String symbol = scanner.nextLine().trim().toUpperCase();

        var holding = currentUser.getPortfolio().getHolding(symbol);
        if (holding == null || holding.getQuantity() <= 0) {
            System.out.println(RED + "You do not own any shares of " + symbol + "." + RESET);
            return;
        }

        Stock stock = tradingService.getMarket().getStock(symbol);
        double spotPrice = (stock != null) ? stock.getCurrentPrice() : holding.getAverageCostBasis();

        System.out.printf("Currently holding %,d shares of %s (Current Market Price: $%,.2f)%n",
                holding.getQuantity(), symbol, spotPrice);

        int quantity = readInt("Enter number of shares to liquidate (1 to " + holding.getQuantity() + "): ",
                1, holding.getQuantity());

        try {
            Transaction tx = tradingService.executeSell(currentUser, symbol, quantity);
            System.out.println(GREEN + BOLD + "SELL ORDER EXECUTED SUCCESSFULLY!" + RESET);
            System.out.println("Order Receipt: " + tx);
            System.out.printf("New Cash Balance: $%,.2f%n", currentUser.getCashBalance());
        } catch (TradingException e) {
            System.out.println(RED + "Order Execution Failed: " + e.getMessage() + RESET);
        }
    }

    private void viewPortfolio() {
        TextTableFormatter.printPortfolioTable(currentUser, tradingService.getMarket());
    }

    private void viewTransactions() {
        TextTableFormatter.printTransactionLedger(currentUser.getPortfolio().getTransactionHistory());
    }

    private void advanceMarketSimulation() {
        System.out.println(CYAN + "\n--- ADVANCING MARKET TICK ---" + RESET);
        int ticks = readInt("Enter number of simulation ticks to run (1-10, default 1): ", 1, 10);
        for (int i = 0; i < ticks; i++) {
            tradingService.getMarket().tick();
        }
        System.out.printf(GREEN + "Market advanced by %d tick(s). New Tick count: #%d%n" + RESET,
                ticks, tradingService.getMarket().getTickCount());
        viewMarket();
    }

    private void viewAccountSummary() {
        TextTableFormatter.printAccountSummary(currentUser, tradingService.getMarket());
    }

    private void saveSession() {
        try {
            persistenceService.saveUserData(currentUser);
            System.out.println(GREEN + "Session successfully persisted to CSV storage!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Failed to save session data: " + e.getMessage() + RESET);
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(RED + "Please enter an integer between %,d and %,d.%n" + RESET, min, max);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid number format. Please enter a valid integer." + RESET);
            }
        }
    }

    private double readDouble(String prompt, double min, double max, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(RED + "Please enter a value between $%,.2f and $%,.2f.%n" + RESET, min, max);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid decimal number format." + RESET);
            }
        }
    }
}
