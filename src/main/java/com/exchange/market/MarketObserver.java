package com.exchange.market;

import com.exchange.model.Stock;

/**
 * Observer listener interface for components subscribing to real-time equity price ticks.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public interface MarketObserver {

    /**
     * Invoked whenever an equity instrument experiences an updated spot quote.
     *
     * @param stock the stock that underwent price change
     * @param oldPrice the previous spot price prior to this tick
     * @param newPrice the newly computed spot price
     */
    void onPriceUpdated(Stock stock, double oldPrice, double newPrice);
}
