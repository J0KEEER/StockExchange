package com.exchange.persistence;

import com.exchange.exception.PersistenceException;
import com.exchange.model.Holding;
import com.exchange.model.Portfolio;
import com.exchange.model.Transaction;
import com.exchange.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * File-based CSV implementation of {@link PersistenceService} providing human-readable,
 * zero-dependency storage for trading accounts, holdings, and transaction ledgers.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class CsvPersistenceService implements PersistenceService {

    private static final String DEFAULT_DIRECTORY = "data";
    private static final String PORTFOLIO_FILENAME = "portfolio.csv";
    private static final String TRANSACTIONS_FILENAME = "transactions.csv";

    private final Path storageDirectory;
    private final Path portfolioFilePath;
    private final Path transactionsFilePath;

    /**
     * Constructs a CsvPersistenceService targeting the default {@code data/} directory.
     */
    public CsvPersistenceService() {
        this(DEFAULT_DIRECTORY);
    }

    /**
     * Constructs a CsvPersistenceService targeting a specific directory path.
     *
     * @param directoryPath the root directory where data files will be stored
     */
    public CsvPersistenceService(String directoryPath) {
        this.storageDirectory = Paths.get(directoryPath != null ? directoryPath : DEFAULT_DIRECTORY);
        this.portfolioFilePath = storageDirectory.resolve(PORTFOLIO_FILENAME);
        this.transactionsFilePath = storageDirectory.resolve(TRANSACTIONS_FILENAME);
    }

    @Override
    public synchronized void saveUserData(User user) throws PersistenceException {
        if (user == null) {
            throw new PersistenceException("Cannot persist null user account.");
        }

        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }

            // 1. Save Portfolio & User Account Header
            savePortfolio(user);

            // 2. Save Full Transaction Ledger
            saveTransactions(user.getPortfolio().getTransactionHistory());

        } catch (IOException e) {
            throw new PersistenceException("Failed to persist trading session data: " + e.getMessage(), e);
        }
    }

    /**
     * Saves user details and active equity positions into portfolio.csv.
     *
     * @param user the account holder to serialize
     * @throws IOException if writing to the output stream fails
     */
    private void savePortfolio(User user) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(portfolioFilePath)) {
            // Header for user metadata
            writer.write("RECORD_TYPE,FIELD_1,FIELD_2,FIELD_3,FIELD_4,FIELD_5");
            writer.newLine();

            // Row 1: USER metadata
            Portfolio portfolio = user.getPortfolio();
            writer.write(String.format("USER,%s,%s,%.2f,%.2f,%.2f",
                    user.getUserId(),
                    user.getUsername(),
                    user.getCashBalance(),
                    user.getInitialCapital(),
                    portfolio.getCumulativeRealizedProfitLoss()));
            writer.newLine();

            // Subsequent rows: HOLDING records
            for (Map.Entry<String, Holding> entry : portfolio.getHoldings().entrySet()) {
                Holding h = entry.getValue();
                writer.write(String.format("HOLDING,%s,%d,%.4f,%.2f,",
                        h.getSymbol(),
                        h.getQuantity(),
                        h.getAverageCostBasis(),
                        h.getTotalCost()));
                writer.newLine();
            }
        }
    }

    /**
     * Saves all executed trade logs into transactions.csv.
     *
     * @param transactions the history of trades
     * @throws IOException if writing fails
     */
    private void saveTransactions(List<Transaction> transactions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(transactionsFilePath)) {
            writer.write("transactionId,timestamp,type,symbol,quantity,executionPrice,totalAmount,realizedGainLoss");
            writer.newLine();

            for (Transaction tx : transactions) {
                writer.write(tx.toCsvLine());
                writer.newLine();
            }
        }
    }

    @Override
    public synchronized User loadUserData() throws PersistenceException {
        if (!hasSavedData()) {
            return null;
        }

        try {
            // 1. Read User Profile & Active Positions
            User user = loadPortfolio();
            if (user == null) {
                return null;
            }

            // 2. Read Transaction Ledger if present
            if (Files.exists(transactionsFilePath)) {
                loadTransactions(user.getPortfolio());
            }

            return user;
        } catch (Exception e) {
            throw new PersistenceException("Corrupted or unreadable persistent session data: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes user metadata and open positions from portfolio.csv.
     *
     * @return the reconstructed User instance, or null if file empty
     * @throws IOException if file reading fails
     */
    private User loadPortfolio() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(portfolioFilePath)) {
            String line = reader.readLine(); // Skip header
            if (line == null) {
                return null;
            }

            String userLine = reader.readLine();
            if (userLine == null || !userLine.startsWith("USER,")) {
                return null;
            }

            String[] userParts = userLine.split(",");
            String userId = userParts[1];
            String username = userParts[2];
            double cashBalance = Double.parseDouble(userParts[3]);
            double initialCapital = Double.parseDouble(userParts[4]);
            double cumulativeRealized = Double.parseDouble(userParts[5]);

            Portfolio portfolio = new Portfolio();
            portfolio.setCumulativeRealizedProfitLoss(cumulativeRealized);

            // Read subsequent holding rows
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.startsWith("HOLDING,")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String symbol = parts[1];
                    int qty = Integer.parseInt(parts[2]);
                    double avgCost = Double.parseDouble(parts[3]);
                    if (qty > 0) {
                        portfolio.addHolding(symbol, qty, avgCost);
                    }
                }
            }

            return new User(userId, username, cashBalance, initialCapital, portfolio);
        }
    }

    /**
     * Reads past transactions from transactions.csv and adds them to the portfolio.
     *
     * @param portfolio the target portfolio receiving historical records
     * @throws IOException if file reading fails
     */
    private void loadTransactions(Portfolio portfolio) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(transactionsFilePath)) {
            String line = reader.readLine(); // Header line
            if (line == null) {
                return;
            }

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    Transaction tx = Transaction.fromCsvLine(line);
                    portfolio.recordTransaction(tx);
                } catch (IllegalArgumentException ex) {
                    // Ignore corrupted individual lines to maximize recovery
                    System.err.println("Skipped corrupted transaction record: " + line);
                }
            }
        }
    }

    @Override
    public boolean hasSavedData() {
        return Files.exists(portfolioFilePath) && Files.isRegularFile(portfolioFilePath);
    }

    /**
     * Retrieves the path to the active portfolio storage file.
     *
     * @return Path pointing to portfolio.csv
     */
    public Path getPortfolioFilePath() {
        return portfolioFilePath;
    }

    /**
     * Retrieves the path to the active transactions ledger file.
     *
     * @return Path pointing to transactions.csv
     */
    public Path getTransactionsFilePath() {
        return transactionsFilePath;
    }
}
