package com.exchange.engine;

import com.exchange.exception.InsufficientFundsException;
import com.exchange.exception.InsufficientSharesException;
import com.exchange.exception.InvalidStockSymbolException;
import com.exchange.exception.TradingException;
import com.exchange.market.Market;
import com.exchange.model.Holding;
import com.exchange.model.Stock;
import com.exchange.model.Transaction;
import com.exchange.model.TransactionType;
import com.exchange.model.User;

import java.util.Objects;

/**
 * Core transactional trading engine handling order matching, balance debits/credits,
 * portfolio mutation, and execution reporting with strong validation guarantees.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class TradingEngine implements TradingService {

    private final Market market;

    /**
     * Constructs a TradingEngine coupled with an active exchange market.
     *
     * @param market the market repository containing tradable equities
     */
    public TradingEngine(Market market) {
        this.market = Objects.requireNonNull(market, "Market engine cannot be null.");
    }

    @Override
    public synchronized Transaction executeBuy(User user, String symbol, int quantity) throws TradingException {
        if (user == null) {
            throw new TradingException("User account must not be null.");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingException("Stock symbol must be specified.");
        }
        if (quantity <= 0) {
            throw new TradingException("Order quantity must be greater than zero.");
        }

        String ticker = symbol.trim().toUpperCase();
        Stock stock = market.getStock(ticker);
        if (stock == null) {
            throw new InvalidStockSymbolException(ticker);
        }

        double spotPrice = stock.getCurrentPrice();
        double totalCost = Math.round(spotPrice * quantity * 100.0) / 100.0;

        if (user.getCashBalance() < totalCost) {
            throw new InsufficientFundsException(totalCost, user.getCashBalance());
        }

        // Atomically debit cash and update holdings
        user.debitCash(totalCost);
        user.getPortfolio().addHolding(ticker, quantity, spotPrice);
        stock.recordTradedVolume(quantity);

        Transaction transaction = new Transaction(
                TransactionType.BUY, ticker, quantity, spotPrice, 0.0);
        user.getPortfolio().recordTransaction(transaction);

        return transaction;
    }

    @Override
    public synchronized Transaction executeSell(User user, String symbol, int quantity) throws TradingException {
        if (user == null) {
            throw new TradingException("User account must not be null.");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingException("Stock symbol must be specified.");
        }
        if (quantity <= 0) {
            throw new TradingException("Order quantity must be greater than zero.");
        }

        String ticker = symbol.trim().toUpperCase();
        Stock stock = market.getStock(ticker);
        if (stock == null) {
            throw new InvalidStockSymbolException(ticker);
        }

        Holding holding = user.getPortfolio().getHolding(ticker);
        int ownedShares = (holding != null) ? holding.getQuantity() : 0;

        if (ownedShares < quantity) {
            throw new InsufficientSharesException(ticker, quantity, ownedShares);
        }

        double spotPrice = stock.getCurrentPrice();
        double grossProceeds = Math.round(spotPrice * quantity * 100.0) / 100.0;

        // Liquidate shares, compute realized profit/loss, and credit user balance
        double realizedGainLoss = user.getPortfolio().removeHolding(ticker, quantity, spotPrice);
        user.creditCash(grossProceeds);
        stock.recordTradedVolume(quantity);

        Transaction transaction = new Transaction(
                TransactionType.SELL, ticker, quantity, spotPrice, realizedGainLoss);
        user.getPortfolio().recordTransaction(transaction);

        return transaction;
    }

    @Override
    public Market getMarket() {
        return market;
    }
}
