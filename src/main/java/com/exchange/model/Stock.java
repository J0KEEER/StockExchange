package com.exchange.model;

import java.util.Objects;

/**
 * Represents an equity instrument traded on the exchange with live pricing metrics,
 * sector categorization, beta coefficient, and volatility parameters.
 *
 * @author Mridul Gupta
 * @version 1.1
 * @since 1.0
 */
public class Stock {

    /** Unique stock ticker identifier. */
    private final String symbol;

    /** Full legal entity company name. */
    private final String companyName;

    /** Industry market classification. */
    private final Sector sector;

    /** Beta sensitivity coefficient relative to the broad market index. */
    private final double beta;

    /** Asset-specific base volatility standard deviation. */
    private final double volatility;

    /** Prevailing spot market price. */
    private double currentPrice;

    /** Session opening price. */
    private double openingPrice;

    /** Spot price prior to the most recent simulation tick. */
    private double previousPrice;

    /** Highest recorded price over the trading horizon. */
    private double high52Week;

    /** Lowest recorded price over the trading horizon. */
    private double low52Week;

    /** Cumulative traded share volume. */
    private long volume;

    /**
     * Constructs a new Stock with base parameters and default market risk weights.
     *
     * @param symbol the unique ticker identifier (e.g., AAPL, NVDA)
     * @param companyName the full legal name of the issuing company
     * @param initialPrice the opening trading price of the asset
     */
    public Stock(String symbol, String companyName, double initialPrice) {
        this(symbol, companyName, Sector.TECHNOLOGY, initialPrice, 1.0, 0.02);
    }

    /**
     * Constructs a complete Stock instrument with industry sector, beta, and volatility risk profiles.
     *
     * @param symbol unique ticker symbol
     * @param companyName full issuing company name
     * @param sector market classification sector
     * @param initialPrice opening market spot price
     * @param beta sensitivity factor relative to systematic market variance
     * @param volatility baseline standard deviation of price movements
     */
    public Stock(String symbol, String companyName, Sector sector, double initialPrice, double beta, double volatility) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol cannot be null or empty.");
        }
        if (initialPrice <= 0) {
            throw new IllegalArgumentException("Initial stock price must be strictly positive.");
        }
        this.symbol = symbol.trim().toUpperCase();
        this.companyName = companyName != null ? companyName.trim() : this.symbol;
        this.sector = sector != null ? sector : Sector.TECHNOLOGY;
        this.beta = Math.max(0.1, beta);
        this.volatility = Math.max(0.005, volatility);
        this.currentPrice = initialPrice;
        this.openingPrice = initialPrice;
        this.previousPrice = initialPrice;
        this.high52Week = initialPrice;
        this.low52Week = initialPrice;
        this.volume = 0L;
    }

    /**
     * Updates the current market price of the stock and tracks historical boundaries.
     *
     * @param newPrice the updated quote price for this stock
     */
    public synchronized void updatePrice(double newPrice) {
        if (newPrice <= 0.01) {
            newPrice = 0.01; // Maintain lower bound penny stock value
        }
        this.previousPrice = this.currentPrice;
        this.currentPrice = Math.round(newPrice * 100.0) / 100.0;
        if (this.currentPrice > this.high52Week) {
            this.high52Week = this.currentPrice;
        }
        if (this.currentPrice < this.low52Week) {
            this.low52Week = this.currentPrice;
        }
    }

    /**
     * Increments the cumulative trading volume of this equity.
     *
     * @param quantity the number of traded shares executed
     */
    public synchronized void recordTradedVolume(int quantity) {
        if (quantity > 0) {
            this.volume += quantity;
        }
    }

    /**
     * Computes the net absolute price fluctuation compared to the day's opening price.
     *
     * @return the difference between current price and opening price
     */
    public double getDailyChange() {
        return currentPrice - openingPrice;
    }

    /**
     * Computes the percentage change of the stock price compared to the opening price.
     *
     * @return the percentage change relative to opening price
     */
    public double getDailyChangePercent() {
        if (openingPrice <= 0) {
            return 0.0;
        }
        return ((currentPrice - openingPrice) / openingPrice) * 100.0;
    }

    /**
     * Retrieves the equity ticker symbol.
     *
     * @return the ticker symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Retrieves the company name.
     *
     * @return the name of the company
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Retrieves the industry classification sector.
     *
     * @return the Sector enum
     */
    public Sector getSector() {
        return sector;
    }

    /**
     * Retrieves the beta sensitivity factor.
     *
     * @return the asset beta
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Retrieves the asset's idiosyncratic volatility standard deviation.
     *
     * @return the volatility rate
     */
    public double getVolatility() {
        return volatility;
    }

    /**
     * Retrieves the current market spot price.
     *
     * @return the current price in base currency
     */
    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Retrieves the session opening price.
     *
     * @return the opening price
     */
    public double getOpeningPrice() {
        return openingPrice;
    }

    /**
     * Retrieves the price before the most recent tick update.
     *
     * @return the previous price
     */
    public synchronized double getPreviousPrice() {
        return previousPrice;
    }

    /**
     * Retrieves the 52-week highest traded price.
     *
     * @return the highest recorded price
     */
    public synchronized double getHigh52Week() {
        return high52Week;
    }

    /**
     * Retrieves the 52-week lowest traded price.
     *
     * @return the lowest recorded price
     */
    public synchronized double getLow52Week() {
        return low52Week;
    }

    /**
     * Retrieves the cumulative trading volume.
     *
     * @return total traded shares count
     */
    public synchronized long getVolume() {
        return volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock stock)) return false;
        return Objects.equals(symbol, stock.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return String.format("%-6s %-22s [%-14s] $%,10.2f (%+6.2f%%)",
                symbol, companyName, sector.getDisplayName(), currentPrice, getDailyChangePercent());
    }
}
