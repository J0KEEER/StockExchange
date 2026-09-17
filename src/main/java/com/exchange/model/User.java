package com.exchange.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an individual trading account holder with liquid capital and an equity portfolio.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class User {

    private final String userId;
    private final String username;
    private double cashBalance;
    private double initialCapital;
    private final Portfolio portfolio;

    /**
     * Constructs a new User account with starting investment capital.
     *
     * @param username the display name or trading handle
     * @param initialBalance the initial cash deposit provided to the account
     */
    public User(String username, double initialBalance) {
        this(UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                username, initialBalance, initialBalance, new Portfolio());
    }

    /**
     * Complete constructor used when restoring an existing account from persistence.
     *
     * @param userId unique account identifier
     * @param username display name or handle
     * @param cashBalance current available liquid cash
     * @param initialCapital historical starting deposit
     * @param portfolio existing investment portfolio instance
     */
    public User(String userId, String username, double cashBalance, double initialCapital, Portfolio portfolio) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (cashBalance < 0) {
            throw new IllegalArgumentException("Cash balance cannot be negative.");
        }
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null.");
        this.username = username.trim();
        this.cashBalance = Math.round(cashBalance * 100.0) / 100.0;
        this.initialCapital = initialCapital > 0 ? initialCapital : cashBalance;
        this.portfolio = portfolio != null ? portfolio : new Portfolio();
    }

    /**
     * Credits liquid funds to the user's cash balance (e.g., from share sales or deposits).
     *
     * @param amount the currency amount to add
     */
    public synchronized void creditCash(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Credit amount cannot be negative.");
        }
        this.cashBalance = Math.round((this.cashBalance + amount) * 100.0) / 100.0;
    }

    /**
     * Debits funds from the user's available cash balance (e.g., for share purchases).
     *
     * @param amount the currency amount to deduct
     * @throws IllegalArgumentException if amount is negative or exceeds available balance
     */
    public synchronized void debitCash(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Debit amount cannot be negative.");
        }
        if (amount > this.cashBalance) {
            throw new IllegalArgumentException(String.format("Insufficient balance. Required: $%,.2f, Available: $%,.2f",
                    amount, this.cashBalance));
        }
        this.cashBalance = Math.round((this.cashBalance - amount) * 100.0) / 100.0;
    }

    /**
     * Computes the total net worth of the account (cash balance plus stock holdings market value).
     *
     * @param stockLookup map containing live stock prices
     * @return aggregate net worth valuation
     */
    public synchronized double getNetWorth(Map<String, Double> stockLookup) {
        return Math.round((this.cashBalance + this.portfolio.getMarketValue(stockLookup)) * 100.0) / 100.0;
    }

    /**
     * Computes the total account return relative to initial starting capital.
     *
     * @param stockLookup map containing live stock prices
     * @return absolute dollar return (positive for profit, negative for loss)
     */
    public synchronized double getTotalReturn(Map<String, Double> stockLookup) {
        return Math.round((getNetWorth(stockLookup) - this.initialCapital) * 100.0) / 100.0;
    }

    /**
     * Computes the total return percentage relative to initial starting capital.
     *
     * @param stockLookup map of current stock prices
     * @return percentage return on total starting balance
     */
    public synchronized double getTotalReturnPercent(Map<String, Double> stockLookup) {
        if (this.initialCapital <= 0) {
            return 0.0;
        }
        return ((getNetWorth(stockLookup) - this.initialCapital) / this.initialCapital) * 100.0;
    }

    /**
     * Retrieves the unique user identifier.
     *
     * @return the user ID string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Retrieves the account display username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the liquid available cash balance.
     *
     * @return the available cash
     */
    public synchronized double getCashBalance() {
        return cashBalance;
    }

    /**
     * Retrieves the initial starting capital amount.
     *
     * @return initial capital deposit
     */
    public double getInitialCapital() {
        return initialCapital;
    }

    /**
     * Retrieves the associated equity investment portfolio.
     *
     * @return the user's Portfolio instance
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public String toString() {
        return String.format("User: %s (ID: %s) | Cash Balance: $%,.2f",
                username, userId, cashBalance);
    }
}
