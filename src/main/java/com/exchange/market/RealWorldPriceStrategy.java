package com.exchange.market;

import com.exchange.model.Sector;
import com.exchange.model.Stock;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Advanced quantitative price alteration strategy modeling multi-factor equities movements
 * using Geometric Brownian Motion (GBM), Capital Asset Pricing Model (CAPM) beta sensitivity,
 * sector covariance, and Merton Jump-Diffusion unexpected news shocks.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.1
 */
public class RealWorldPriceStrategy implements PriceUpdateStrategy {

    /** Random number generator for stochastic variables. */
    private final Random random;

    /** Current macro index systematic drift per tick. */
    private double currentMacroReturn;

    /** Map of sector-specific performance shocks per tick. */
    private final Map<Sector, Double> sectorReturns;

    /** Poisson probability of an unexpected news event jump per tick (3.5%). */
    private static final double JUMP_PROBABILITY = 0.035;

    /**
     * Constructs a default RealWorldPriceStrategy with initialized stochastic models.
     */
    public RealWorldPriceStrategy() {
        this.random = new Random();
        this.currentMacroReturn = 0.0;
        this.sectorReturns = new EnumMap<>(Sector.class);
        generateTickMacroFactors();
    }

    /**
     * Advances the macro-economic index drift and sector covariance factors before evaluating assets.
     */
    public synchronized void prepareNewTickCycle() {
        generateTickMacroFactors();
    }

    /**
     * Generates correlated broad-market index return and sector-level shocks.
     */
    private void generateTickMacroFactors() {
        // Broad market index shock (e.g., S&P 500 / NASDAQ macro drift)
        // Mean = +0.02% drift, standard deviation = 0.6%
        double macroZ = random.nextGaussian();
        this.currentMacroReturn = (0.0002 + 0.006 * macroZ);

        // Generate sector-specific correlated deviations
        for (Sector sector : Sector.values()) {
            double sectorZ = random.nextGaussian();
            // Sector shocks typically range ~ +/- 0.8%
            sectorReturns.put(sector, sectorZ * 0.008);
        }
    }

    @Override
    public synchronized double calculateNewPrice(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock cannot be null.");
        }

        double currentPrice = stock.getCurrentPrice();
        double openingPrice = stock.getOpeningPrice();
        double beta = stock.getBeta();
        double volatility = stock.getVolatility();
        Sector sector = stock.getSector();

        // 1. Systematic Market Return: Beta * Macro Index Drift
        double systematicReturn = beta * currentMacroReturn;

        // 2. Sector Specific Covariance
        double sectorReturn = sectorReturns.getOrDefault(sector, 0.0) * (beta * 0.5);

        // 3. Idiosyncratic Asset Diffusion (Individual Gaussian walk)
        double idiosyncraticZ = random.nextGaussian();
        double idiosyncraticReturn = volatility * idiosyncraticZ;

        // 4. Merton Jump-Diffusion (Unexpected Breaking News / Earnings Shock)
        double newsJumpReturn = 0.0;
        boolean hasNewsJump = random.nextDouble() < JUMP_PROBABILITY;
        if (hasNewsJump) {
            // Jump size scaled to 3x asset volatility
            double jumpZ = random.nextGaussian();
            newsJumpReturn = (jumpZ * volatility * 3.5);
        }

        // 5. Mild Intraday Mean Reversion (prevents runaway drift past bounds)
        double deviation = (openingPrice - currentPrice) / openingPrice;
        double meanReversionDrift = deviation * 0.04;

        // Combined Net Rate of Change
        double totalRateOfReturn = systematicReturn + sectorReturn + idiosyncraticReturn + newsJumpReturn + meanReversionDrift;

        // Enforce maximum realistic single-tick swing clamp (+/- 5.5%)
        totalRateOfReturn = Math.max(-0.055, Math.min(0.055, totalRateOfReturn));

        double newPrice = currentPrice * (1.0 + totalRateOfReturn);

        // Simulate realistic market volume surges (ARCH/GARCH volume clustering)
        int volumeIncrement = calculateSimulatedVolume(stock, Math.abs(totalRateOfReturn), hasNewsJump);
        stock.recordTradedVolume(volumeIncrement);

        // Maintain penny floor boundary
        return Math.max(0.50, Math.round(newPrice * 100.0) / 100.0);
    }

    /**
     * Models trading volume spikes proportional to price volatility and news surprises.
     *
     * @param stock the target equity
     * @param absReturn the absolute magnitude of price change
     * @param newsJump whether a discrete news jump occurred
     * @return traded shares volume to accumulate
     */
    private int calculateSimulatedVolume(Stock stock, double absReturn, boolean newsJump) {
        // Baseline liquidity depending on stock price tier
        int baseLiquidity = (int) Math.max(50, 100_000 / stock.getCurrentPrice());
        double volatilityMultiplier = 1.0 + (absReturn * 45.0);
        double jumpMultiplier = newsJump ? (2.5 + random.nextDouble() * 3.0) : 1.0;

        int randomNoise = random.nextInt(50);
        return (int) (baseLiquidity * volatilityMultiplier * jumpMultiplier) + randomNoise;
    }

    /**
     * Retrieves the current macro index return of the market.
     *
     * @return broad-market return percentage
     */
    public double getCurrentMacroReturn() {
        return currentMacroReturn;
    }

    /**
     * Retrieves the formatted macroeconomic sentiment summary.
     *
     * @return readable sentiment string
     */
    public String getMacroSentimentString() {
        double pct = currentMacroReturn * 100.0;
        if (pct >= 0.25) {
            return String.format("Bullish (+%.2f%%)", pct);
        } else if (pct <= -0.25) {
            return String.format("Bearish (%.2f%%)", pct);
        } else {
            return String.format("Neutral (%+.2f%%)", pct);
        }
    }
}
