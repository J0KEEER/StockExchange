package com.exchange.exception;

/**
 * Base checked exception representing business logic errors within the trading exchange platform.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class TradingException extends Exception {

    /**
     * Constructs a new TradingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the failure
     */
    public TradingException(String message) {
        super(message);
    }

    /**
     * Constructs a new TradingException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the failure
     * @param cause the root cause of this exception
     */
    public TradingException(String message, Throwable cause) {
        super(message, cause);
    }
}
