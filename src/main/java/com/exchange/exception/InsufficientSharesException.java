package com.exchange.exception;

/**
 * Exception thrown when a user attempts to sell more shares than their active portfolio holds.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class InsufficientSharesException extends TradingException {

    /** The equity ticker symbol. */
    private final String symbol;

    /** The quantity of shares requested to sell. */
    private final int requestedShares;

    /** The quantity of shares currently owned. */
    private final int ownedShares;

    /**
     * Constructs an InsufficientSharesException with context details.
     *
     * @param symbol the equity ticker symbol
     * @param requestedShares the quantity requested to be liquidated
     * @param ownedShares the quantity currently held in the portfolio
     */
    public InsufficientSharesException(String symbol, int requestedShares, int ownedShares) {
        super(String.format("Insufficient shares for symbol %s. Requested to sell: %d, Currently owned: %d",
                symbol, requestedShares, ownedShares));
        this.symbol = symbol;
        this.requestedShares = requestedShares;
        this.ownedShares = ownedShares;
    }

    /**
     * Retrieves the equity ticker symbol.
     *
     * @return the stock symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Retrieves the number of shares requested to sell.
     *
     * @return the requested share quantity
     */
    public int getRequestedShares() {
        return requestedShares;
    }

    /**
     * Retrieves the number of shares actually owned.
     *
     * @return the owned share quantity
     */
    public int getOwnedShares() {
        return ownedShares;
    }
}
