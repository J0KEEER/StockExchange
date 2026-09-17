package com.exchange.exception;

/**
 * Exception thrown when an account lacks sufficient liquid cash to execute an acquisition order.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class InsufficientFundsException extends TradingException {

    /** The total funds required for order fulfillment. */
    private final double requiredAmount;

    /** The cash balance available in the account. */
    private final double availableBalance;

    /**
     * Constructs an InsufficientFundsException with the required amount and available balance.
     *
     * @param requiredAmount the total cost needed to execute the buy order
     * @param availableBalance the current cash balance in the user's account
     */
    public InsufficientFundsException(double requiredAmount, double availableBalance) {
        super(String.format("Insufficient funds to execute order. Required: $%,.2f, Available: $%,.2f",
                requiredAmount, availableBalance));
        this.requiredAmount = requiredAmount;
        this.availableBalance = availableBalance;
    }

    /**
     * Retrieves the total cost needed for the attempted transaction.
     *
     * @return the required funds amount
     */
    public double getRequiredAmount() {
        return requiredAmount;
    }

    /**
     * Retrieves the liquid cash balance available at the time of failure.
     *
     * @return the available cash balance
     */
    public double getAvailableBalance() {
        return availableBalance;
    }
}
