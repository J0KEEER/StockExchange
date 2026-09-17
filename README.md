# Equity Stock Exchange & Portfolio Management Platform

A robust, enterprise-caliber stock exchange simulation and investment portfolio platform developed in Java. Features a modern dark-themed desktop Graphical User Interface (GUI) trading workstation, terminal (CLI) interactive fallback, multi-factor quantitative price movement models (Geometric Brownian Motion + CAPM Beta Sensitivity + Sector Factors + Merton Jump-Diffusion news shocks), thread-safe data structures, CSV file persistence, complete Javadoc documentation, and integrated Graphify architectural knowledge graph mapping.

---

## Key Highlights

- **Real-World Quantitative Equities Market Simulation**:
  - **15 Real-World Large-Cap Equities**: Pre-populated with prevailing market valuations across 7 market sectors (Technology, Semiconductors, Financials, Healthcare, Consumer Staples, Energy, Automotive).
  - **Capital Asset Pricing Model (CAPM) & Beta Sensitivity ($\beta$)**:
    High-beta assets (e.g. `TSLA` $\beta = 2.10$, `NVDA` $\beta = 1.95$) experience dynamic swings in response to systematic trends, whereas defensive staples (e.g. `WMT` $\beta = 0.50$, `JNJ` $\beta = 0.55$) retain conservative price stability.
  - **Correlated Sector Covariance**:
    Stocks within the same industry sector (e.g. Technology software vs. Semiconductors vs. Financials) share sector shocks, mimicking real-world market movements.
  - **Merton Jump-Diffusion (Unexpected News Shocks)**:
    Models breaking news events, earnings surprises, and analyst upgrades/downgrades with Poisson distribution (~3.5% chance per tick), triggering volatility jumps and realistic volume spikes.
  - **ARCH/GARCH Simulated Volume Clustering**:
    Traded share volume surges exponentially during high-volatility ticks and news events.
- **Modern Desktop GUI Workstation & Dual-Mode Execution**:
  - **Zero External Dependencies**: Built entirely with standard library `javax.swing.*` and `java.awt.*` (no heavyweight JavaFX or external jars required).
  - **Dark Financial Workstation Aesthetics**: Bloomberg / TradingView inspired slate dark theme (`#121826`, `#1E293B`, `#243044`) with emerald green gains and crimson loss indicators.
  - **Interactive Modules**:
    - **KPI Metrics Header**: Real-time Net Worth, Cash Balance, Equities Valuation, Unrealized P/L, Cumulative Realized P/L cards, and Macro Market Sentiment indicator.
    - **Simulation Controls**: Discrete tick trigger and background auto-tick streaming timer (continuous real-time market stream).
    - **Live Market Watch**: Interactive table with color-coded spot quotes, sectors, betas, day changes, percentages, and trading volumes.
    - **Order Execution Ticket**: Instant market buy/sell order desk with capacity indicators, quick-lot controls (+1, +5, +10, Max), live cost estimation, and validation alerts.
    - **Portfolio & Ledger Tabs**: Position tracking (shares owned, average cost basis, spot valuation, unrealized P/L %, ROI %) and chronological trade audit history.
  - **Dual-Mode Startup**: Automatically launches the GUI on desktop environments, with automated fallback to the terminal CLI when running headlessly or when passed `--cli`.
- **Financial Mathematics & Portfolio Accounting**:
  - **Weighted-Average Cost Basis**: Continuously calculated across multi-lot share acquisitions:
    $$\text{Average Cost Basis} = \frac{(\text{Existing Quantity} \times \text{Existing Cost}) + (\text{New Quantity} \times \text{Purchase Price})}{\text{Existing Quantity} + \text{New Quantity}}$$
  - **Realized vs. Unrealized Profit & Loss (P/L)**:
    - *Unrealized P/L*: $(\text{Current Market Spot Price} - \text{Average Cost Basis}) \times \text{Owned Shares}$
    - *Realized P/L*: Captured upon liquidation: $(\text{Liquidation Price} - \text{Average Cost Basis}) \times \text{Sold Shares}$
    - *Return on Investment (ROI)*: Tracking net worth relative to initial capital deposit.
- **Automated Verification**:
  - Built-in standalone regression test suite (`TradingPlatformTest`) verifying 11 critical operational areas (100% pass rate).
- **Knowledge Graph Integration (Graphify)**:
  - Built-in AST-based knowledge graph mapping (**454 nodes**, **1016 edges**, **26 communities**) in `graphify-out/`.

---

## Listed Real-World Equities

| Ticker | Company Name | Sector | Spot Price | Beta ($\beta$) | Volatility ($\sigma$) |
|---|---|---|---|---|---|
| `AAPL` | Apple Inc. | Technology | $224.50 | 1.15 | 1.6% |
| `MSFT` | Microsoft Corp. | Technology | $432.10 | 1.10 | 1.4% |
| `GOOGL` | Alphabet Inc. | Technology | $178.40 | 1.12 | 1.7% |
| `META` | Meta Platforms Inc. | Technology | $515.20 | 1.35 | 2.2% |
| `NVDA` | NVIDIA Corporation | Semiconductors | $119.80 | 1.95 | 3.2% |
| `TSLA` | Tesla Inc. | Automotive | $210.60 | 2.10 | 3.8% |
| `JPM` | JPMorgan Chase & Co. | Financials | $212.30 | 0.95 | 1.2% |
| `GS` | Goldman Sachs Group | Financials | $485.60 | 1.15 | 1.5% |
| `V` | Visa Inc. | Financials | $268.90 | 0.90 | 1.1% |
| `LLY` | Eli Lilly and Co. | Healthcare | $945.80 | 0.65 | 1.5% |
| `JNJ` | Johnson & Johnson | Healthcare | $162.30 | 0.55 | 0.8% |
| `AMZN` | Amazon.com Inc. | Consumer Staples | $186.25 | 1.25 | 1.9% |
| `WMT` | Walmart Inc. | Consumer Staples | $68.50 | 0.50 | 0.9% |
| `COST` | Costco Wholesale | Consumer Staples | $885.20 | 0.75 | 1.1% |
| `XOM` | Exxon Mobil Corp. | Energy | $114.50 | 0.85 | 1.6% |

---

## Quick Start & Usage

### 1. Launch Modern Desktop GUI (Default)
```bash
./run.sh
# or
java -cp bin com.exchange.Main
```

### 2. Launch Interactive Terminal Console (CLI Mode)
```bash
./run.sh cli
# or
java -cp bin com.exchange.Main --cli
```

### 3. Run Automated Regression Test Suite
```bash
./run.sh test
# or
java -cp bin com.exchange.TradingPlatformTest
```

Expected output:
```text
=================================================================
  RUNNING TRADING PLATFORM AUTOMATED VERIFICATION SUITE
=================================================================
  [PASS] Stock Creation and Metric Boundary Test
  [PASS] Holding Weighted-Average Cost Basis Test
  [PASS] Holding Liquidation Realized P/L Calculation Test
  [PASS] Buy Order Execution & Balance Debit Test
  [PASS] Insufficient Funds Buy Rejection Test
  [PASS] Sell Order Execution & Balance Credit Test
  [PASS] Insufficient Shares Sell Rejection Test
  [PASS] Invalid Stock Symbol Rejection Test
  [PASS] Market Observer Notification Pattern Test
  [PASS] CSV Persistence Save and Reload Round-Trip Test
  [PASS] Real-World Quantitative Price Strategy & Beta Test
=================================================================
  TEST RESULTS: 11/11 PASSED (100.0%)
=================================================================
```

### 4. Generate Javadoc Documentation (0 warnings)
```bash
./run.sh docs
```

### 5. Update Graphify Knowledge Graph
```bash
./run.sh graph
```

---

## Knowledge Graph Navigation (Graphify)

The project includes an AST-generated architectural knowledge graph in `graphify-out/`:
- Open `graphify-out/graph.html` in any web browser for interactive visual graph exploration.
- Review `graphify-out/GRAPH_REPORT.md` for architectural community clustering, bridge nodes, and God Nodes.
- Run queries via Graphify CLI:
  ```bash
  graphify query "How does RealWorldPriceStrategy calculate multi-factor price movements for Stock?"
  ```

---

## Author

- **Mridul Gupta** — Department of Computer Science & Engineering
