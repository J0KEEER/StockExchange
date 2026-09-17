package com.exchange.model;

/**
 * Enumeration representing the direction of a financial transaction.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public enum TransactionType {
    /**
     * Acquisition of equity shares funded by user balance.
     */
    BUY,

    /**
     * Liquidation of equity shares returning proceeds to user balance.
     */
    SELL
}
