package com.exchange.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable ledger record representing an executed trade (buy or sell) on the exchange.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class Transaction {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String transactionId;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final String symbol;
    private final int quantity;
    private final double executionPrice;
    private final double totalAmount;
    private final double realizedGainLoss;

    /**
     * Constructs a newly executed transaction with generated identifier and current timestamp.
     *
     * @param type the transaction action classification (BUY or SELL)
     * @param symbol the traded equity ticker symbol
     * @param quantity the number of shares transacted
     * @param executionPrice the spot price per share at execution time
     * @param realizedGainLoss the net profit or loss realized (relevant on sales)
     */
    public Transaction(TransactionType type, String symbol, int quantity, double executionPrice, double realizedGainLoss) {
        this(UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                LocalDateTime.now(), type, symbol, quantity, executionPrice, realizedGainLoss);
    }

    /**
     * Complete constructor used during object restoration from persistent storage.
     *
     * @param transactionId unique reference code for the trade
     * @param timestamp the date and time when the trade executed
     * @param type the transaction action classification
     * @param symbol the traded equity ticker symbol
     * @param quantity the number of shares transacted
     * @param executionPrice the spot price per share at execution time
     * @param realizedGainLoss the net profit or loss realized
     */
    public Transaction(String transactionId, LocalDateTime timestamp, TransactionType type,
                       String symbol, int quantity, double executionPrice, double realizedGainLoss) {
        this.transactionId = Objects.requireNonNull(transactionId, "Transaction ID cannot be null.");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null.");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null.");
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null.").toUpperCase();
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.totalAmount = Math.round(quantity * executionPrice * 100.0) / 100.0;
        this.realizedGainLoss = Math.round(realizedGainLoss * 100.0) / 100.0;
    }

    /**
     * Serializes this transaction into a comma-separated values (CSV) string representation.
     *
     * @return a single CSV formatted record line
     */
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%d,%.2f,%.2f,%.2f",
                transactionId,
                timestamp.format(FORMATTER),
                type.name(),
                symbol,
                quantity,
                executionPrice,
                totalAmount,
                realizedGainLoss);
    }

    /**
     * Parses a CSV row into a strongly-typed {@link Transaction} instance.
     *
     * @param csvLine the comma-delimited row from the storage file
     * @return the deserialized Transaction instance
     * @throws IllegalArgumentException if the row format is malformed or invalid
     */
    public static Transaction fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty.");
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 8) {
            throw new IllegalArgumentException("Malformed transaction record line: " + csvLine);
        }
        try {
            String id = parts[0].trim();
            LocalDateTime time = LocalDateTime.parse(parts[1].trim(), FORMATTER);
            TransactionType type = TransactionType.valueOf(parts[2].trim().toUpperCase());
            String symbol = parts[3].trim().toUpperCase();
            int qty = Integer.parseInt(parts[4].trim());
            double price = Double.parseDouble(parts[5].trim());
            double realized = Double.parseDouble(parts[7].trim());
            return new Transaction(id, time, type, symbol, qty, price, realized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse transaction from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the unique alphanumeric identifier for this trade.
     *
     * @return the trade ID string
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Retrieves the date and time when the trade was concluded.
     *
     * @return the execution timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Retrieves the trade direction (BUY or SELL).
     *
     * @return the transaction type enum
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Retrieves the equity ticker symbol transacted.
     *
     * @return the stock symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Retrieves the number of shares exchanged in this trade.
     *
     * @return the share volume
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Retrieves the unit share price negotiated at trade time.
     *
     * @return the price per share
     */
    public double getExecutionPrice() {
        return executionPrice;
    }

    /**
     * Retrieves the total settlement value of the transaction.
     *
     * @return gross trade amount
     */
    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Retrieves the profit or loss realized upon liquidation.
     *
     * @return realized gain or loss amount (0.0 for purchases)
     */
    public double getRealizedGainLoss() {
        return realizedGainLoss;
    }

    @Override
    public String toString() {
        String pnlStr = (type == TransactionType.SELL)
                ? String.format(" | Realized P/L: %s$%,.2f", (realizedGainLoss >= 0 ? "+" : ""), realizedGainLoss)
                : "";
        return String.format("[%s] %-4s %-5s %4d shrs @ $%,8.2f = $%,10.2f%s (%s)",
                transactionId,
                type,
                symbol,
                quantity,
                executionPrice,
                totalAmount,
                pnlStr,
                timestamp.format(DISPLAY_FORMATTER));
    }
}
