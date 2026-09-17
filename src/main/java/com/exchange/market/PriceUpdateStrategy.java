package com.exchange.market;

import com.exchange.model.Stock;

/**
 * Strategy interface defining mathematical price simulation algorithms for traded equities.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public interface PriceUpdateStrategy {

    /**
     * Computes the subsequent spot price for a given stock upon a market tick.
     *
     * @param stock the stock entity undergoing price movement
     * @return the newly calculated price
     */
    double calculateNewPrice(Stock stock);
}
