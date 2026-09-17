package com.exchange.exception;

/**
 * Exception thrown when an operation specifies an unrecognized or unlisted stock symbol.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class InvalidStockSymbolException extends TradingException {

    /** The unrecognized ticker symbol. */
    private final String symbol;

    /**
     * Constructs an InvalidStockSymbolException with the invalid symbol.
     *
     * @param symbol the unrecognized equity symbol
     */
    public InvalidStockSymbolException(String symbol) {
        super(String.format("Stock symbol '%s' was not found in the active market exchange.", symbol));
        this.symbol = symbol;
    }

    /**
     * Retrieves the invalid ticker symbol.
     *
     * @return the stock symbol string
     */
    public String getSymbol() {
        return symbol;
    }
}
