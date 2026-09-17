package com.exchange.market;

import com.exchange.model.Stock;
import java.util.Random;

/**
 * Stochastic price movement strategy modeling equity volatility with bounded random walks
 * and slight mean-reverting tendencies.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class VolatilityPriceStrategy implements PriceUpdateStrategy {

    private final Random random;
    private final double maxVolatilityPercent;
    private final double meanReversionWeight;

    /**
     * Constructs a default VolatilityPriceStrategy with standard volatility parameters.
     */
    public VolatilityPriceStrategy() {
        this(3.5, 0.05);
    }

    /**
     * Constructs a VolatilityPriceStrategy with custom parameters.
     *
     * @param maxVolatilityPercent maximum absolute percentage swing per tick (e.g., 3.5%)
     * @param meanReversionWeight pull factor dampening excessive runaway prices
     */
    public VolatilityPriceStrategy(double maxVolatilityPercent, double meanReversionWeight) {
        this.random = new Random();
        this.maxVolatilityPercent = Math.max(0.5, maxVolatilityPercent);
        this.meanReversionWeight = Math.max(0.0, Math.min(0.2, meanReversionWeight));
    }

    @Override
    public double calculateNewPrice(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock cannot be null.");
        }

        double currentPrice = stock.getCurrentPrice();
        double openingPrice = stock.getOpeningPrice();

        // Generate Gaussian noise with zero mean and normalized distribution
        double standardNormal = random.nextGaussian();

        // Scale by target volatility percentage (converted to decimal)
        double percentChange = (standardNormal * (maxVolatilityPercent / 2.0));

        // Clamp extreme outliers to avoid unrealistic spikes
        percentChange = Math.max(-maxVolatilityPercent, Math.min(maxVolatilityPercent, percentChange));

        // Mean reversion pull: pulls back slightly if deviates too far from open
        double deviation = (openingPrice - currentPrice) / openingPrice;
        double drift = deviation * meanReversionWeight;

        double totalRateOfChange = (percentChange / 100.0) + drift;
        double newPrice = currentPrice * (1.0 + totalRateOfChange);

        // Enforce penny stock floor
        return Math.max(0.50, Math.round(newPrice * 100.0) / 100.0);
    }
}
