package com.exchange.market;

import com.exchange.model.Sector;
import com.exchange.model.Stock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Exchange market engine that hosts traded equities, runs price updates, and broadcasts ticks.
 *
 * @author Mridul Gupta
 * @version 1.1
 * @since 1.0
 */
public class Market {

    /** Central registry mapping equity ticker symbols to active Stock entities. */
    private final Map<String, Stock> stockRegistry;

    /** Observers listening to price update broadcasts. */
    private final List<MarketObserver> observers;

    /** Mathematical price simulation strategy. */
    private PriceUpdateStrategy priceStrategy;

    /** Cumulative simulation ticks executed. */
    private int tickCount;

    /**
     * Constructs a new Market engine with the realistic quantitative pricing strategy.
     */
    public Market() {
        this(new RealWorldPriceStrategy());
    }

    /**
     * Constructs a Market engine with a specific price fluctuation strategy.
     *
     * @param priceStrategy the mathematical price update algorithm to employ
     */
    public Market(PriceUpdateStrategy priceStrategy) {
        this.stockRegistry = new ConcurrentHashMap<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.priceStrategy = priceStrategy != null ? priceStrategy : new RealWorldPriceStrategy();
        this.tickCount = 0;
        initializeDefaultEquities();
    }

    /**
     * Populates the exchange with real-world equities across key sectors with authentic market valuations.
     */
    private void initializeDefaultEquities() {
        // Technology & Cloud
        registerStock(new Stock("AAPL", "Apple Inc.", Sector.TECHNOLOGY, 224.50, 1.15, 0.016));
        registerStock(new Stock("MSFT", "Microsoft Corp.", Sector.TECHNOLOGY, 432.10, 1.10, 0.014));
        registerStock(new Stock("GOOGL", "Alphabet Inc.", Sector.TECHNOLOGY, 178.40, 1.12, 0.017));
        registerStock(new Stock("META", "Meta Platforms Inc.", Sector.TECHNOLOGY, 515.20, 1.35, 0.022));

        // Semiconductors & AI
        registerStock(new Stock("NVDA", "NVIDIA Corporation", Sector.SEMICONDUCTORS, 119.80, 1.95, 0.032));

        // Automotive & Clean Tech
        registerStock(new Stock("TSLA", "Tesla Inc.", Sector.AUTOMOTIVE, 210.60, 2.10, 0.038));

        // Financials & Payments
        registerStock(new Stock("JPM", "JPMorgan Chase & Co.", Sector.FINANCIALS, 212.30, 0.95, 0.012));
        registerStock(new Stock("GS", "Goldman Sachs Group", Sector.FINANCIALS, 485.60, 1.15, 0.015));
        registerStock(new Stock("V", "Visa Inc.", Sector.FINANCIALS, 268.90, 0.90, 0.011));

        // Healthcare & Pharmaceuticals
        registerStock(new Stock("LLY", "Eli Lilly and Co.", Sector.HEALTHCARE, 945.80, 0.65, 0.015));
        registerStock(new Stock("JNJ", "Johnson & Johnson", Sector.HEALTHCARE, 162.30, 0.55, 0.008));

        // Consumer Staples & Retail
        registerStock(new Stock("AMZN", "Amazon.com Inc.", Sector.CONSUMER_STAPLES, 186.25, 1.25, 0.019));
        registerStock(new Stock("WMT", "Walmart Inc.", Sector.CONSUMER_STAPLES, 68.50, 0.50, 0.009));
        registerStock(new Stock("COST", "Costco Wholesale", Sector.CONSUMER_STAPLES, 885.20, 0.75, 0.011));

        // Energy
        registerStock(new Stock("XOM", "Exxon Mobil Corp.", Sector.ENERGY, 114.50, 0.85, 0.016));
    }

    /**
     * Registers a new stock into the active exchange directory.
     *
     * @param stock the stock entity to list
     */
    public void registerStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock cannot be null.");
        }
        stockRegistry.put(stock.getSymbol(), stock);
    }

    /**
     * Retrieves a stock by its ticker symbol.
     *
     * @param symbol the ticker symbol to look up
     * @return the corresponding Stock, or {@code null} if not found
     */
    public Stock getStock(String symbol) {
        if (symbol == null) return null;
        return stockRegistry.get(symbol.trim().toUpperCase());
    }

    /**
     * Checks if a given ticker symbol is currently registered and actively traded.
     *
     * @param symbol the equity ticker symbol
     * @return {@code true} if listed, otherwise {@code false}
     */
    public boolean hasStock(String symbol) {
        if (symbol == null) return false;
        return stockRegistry.containsKey(symbol.trim().toUpperCase());
    }

    /**
     * Retrieves an unmodifiable collection of all listed stocks.
     *
     * @return collection of active Stock instruments
     */
    public Collection<Stock> getAllStocks() {
        return Collections.unmodifiableCollection(stockRegistry.values());
    }

    /**
     * Creates an immutable map snapshot of current spot prices for all listed stocks.
     *
     * @return map of stock symbols to their current market price
     */
    public Map<String, Double> getPriceSnapshot() {
        Map<String, Double> snapshot = new HashMap<>();
        for (Map.Entry<String, Stock> entry : stockRegistry.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().getCurrentPrice());
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Advances the market by one discrete simulation tick, re-evaluating spot prices and notifying observers.
     */
    public synchronized void tick() {
        tickCount++;

        // If using multi-factor strategy, advance macro and sector parameters
        if (priceStrategy instanceof RealWorldPriceStrategy realWorld) {
            realWorld.prepareNewTickCycle();
        }

        for (Stock stock : stockRegistry.values()) {
            double oldPrice = stock.getCurrentPrice();
            double newPrice = priceStrategy.calculateNewPrice(stock);
            stock.updatePrice(newPrice);
            notifyObservers(stock, oldPrice, newPrice);
        }
    }

    /**
     * Subscribes a listener to receive real-time price tick notifications.
     *
     * @param observer the observer to register
     */
    public void addObserver(MarketObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Unsubscribes a listener from price tick notifications.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(MarketObserver observer) {
        observers.remove(observer);
    }

    /**
     * Broadcasts a price change event to all registered observers.
     *
     * @param stock the stock that changed price
     * @param oldPrice the previous price
     * @param newPrice the updated price
     */
    private void notifyObservers(Stock stock, double oldPrice, double newPrice) {
        for (MarketObserver observer : observers) {
            try {
                observer.onPriceUpdated(stock, oldPrice, newPrice);
            } catch (Exception e) {
                // Prevent one failing observer from breaking the broadcast loop
                System.err.println("Market observer notification error: " + e.getMessage());
            }
        }
    }

    /**
     * Identifies the stock currently possessing the highest percentage gain relative to open.
     *
     * @return the top performing Stock, or {@code null} if registry is empty
     */
    public Stock getTopGainer() {
        return stockRegistry.values().stream()
                .max(Comparator.comparingDouble(Stock::getDailyChangePercent))
                .orElse(null);
    }

    /**
     * Identifies the stock currently possessing the largest percentage loss relative to open.
     *
     * @return the lowest performing Stock, or {@code null} if registry is empty
     */
    public Stock getTopLoser() {
        return stockRegistry.values().stream()
                .min(Comparator.comparingDouble(Stock::getDailyChangePercent))
                .orElse(null);
    }

    /**
     * Configures a new price update strategy at runtime.
     *
     * @param priceStrategy the new pricing strategy algorithm
     */
    public void setPriceStrategy(PriceUpdateStrategy priceStrategy) {
        if (priceStrategy != null) {
            this.priceStrategy = priceStrategy;
        }
    }

    /**
     * Retrieves the current macroeconomic sentiment description if available.
     *
     * @return market sentiment string
     */
    public String getMacroSentiment() {
        if (priceStrategy instanceof RealWorldPriceStrategy realWorld) {
            return realWorld.getMacroSentimentString();
        }
        return "Normal Fluctuation";
    }

    /**
     * Retrieves the total count of market ticks executed during this session.
     *
     * @return the tick count
     */
    public int getTickCount() {
        return tickCount;
    }
}
