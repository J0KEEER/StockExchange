package com.exchange.engine;

import com.exchange.exception.TradingException;
import com.exchange.market.Market;
import com.exchange.model.Transaction;
import com.exchange.model.User;

/**
 * Service contract defining core equity trading execution and exchange market operations.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public interface TradingService {

    /**
     * Executes an immediate market buy order for a designated stock ticker.
     *
     * @param user the account holder initiating the acquisition
     * @param symbol the ticker identifier of the target equity
     * @param quantity the number of shares to purchase
     * @return the finalized transaction ledger record
     * @throws TradingException if validation fails, stock does not exist, or funds are insufficient
     */
    Transaction executeBuy(User user, String symbol, int quantity) throws TradingException;

    /**
     * Executes an immediate market sell order for a designated stock ticker.
     *
     * @param user the account holder initiating the sale
     * @param symbol the ticker identifier of the target equity
     * @param quantity the number of shares to liquidate
     * @return the finalized transaction ledger record
     * @throws TradingException if validation fails, stock does not exist, or shares are insufficient
     */
    Transaction executeSell(User user, String symbol, int quantity) throws TradingException;

    /**
     * Retrieves the market engine associated with this trading service.
     *
     * @return the active Market instance
     */
    Market getMarket();
}
