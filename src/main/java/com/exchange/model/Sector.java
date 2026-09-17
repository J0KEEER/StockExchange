package com.exchange.model;

/**
 * Enumeration of key industry sectors traded on the global equities exchange.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public enum Sector {
    /**
     * Enterprise software, hardware, cloud computing, and internet consumer platforms.
     */
    TECHNOLOGY("Technology"),

    /**
     * Microprocessor fabrication, graphics processors, and artificial intelligence accelerators.
     */
    SEMICONDUCTORS("Semiconductors"),

    /**
     * Commercial banking, investment services, asset management, and payment networks.
     */
    FINANCIALS("Financials"),

    /**
     * Pharmaceuticals, biotechnology, medical devices, and therapeutics.
     */
    HEALTHCARE("Healthcare"),

    /**
     * Consumer packaged goods, food and beverage retail, and household staples.
     */
    CONSUMER_STAPLES("Consumer Staples"),

    /**
     * Oil exploration, natural gas extraction, refining, and energy infrastructure.
     */
    ENERGY("Energy"),

    /**
     * Electric vehicles, automotive manufacturing, and clean mobility technologies.
     */
    AUTOMOTIVE("Automotive");

    /** The human-readable display name of the sector. */
    private final String displayName;

    /**
     * Constructs a Sector enum instance with its human-readable title.
     *
     * @param displayName formatted sector title
     */
    Sector(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retrieves the human-readable display name of the sector.
     *
     * @return the sector title string
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
