# Graph Report - StockExchange  (2026-09-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 454 nodes · 1016 edges · 26 communities (15 shown, 6 thin omitted)
- Extraction: 80% EXTRACTED · 20% INFERRED · 0% AMBIGUOUS · INFERRED: 204 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- OrderTicketPanel
- jquery-3.7.1.min.js
- User
- Portfolio
- Transaction
- .getPortfolio
- script.js
- search.js
- MetricsHeaderPanel
- Market
- Stock
- RealWorldPriceStrategy
- Sector
- search-page.js
- .calculateNewPrice
- run.sh
- DefaultTableModel
- JTable
- Color
- JButton
- JLabel

## God Nodes (most connected - your core abstractions)
1. `Stock` - 44 edges
2. `Market` - 35 edges
3. `User` - 31 edges
4. `Transaction` - 26 edges
5. `Portfolio` - 22 edges
6. `OrderTicketPanel` - 21 edges
7. `TradingDashboardFrame` - 20 edges
8. `Holding` - 20 edges
9. `ConsoleUI` - 19 edges
10. `TradingPlatformTest` - 17 edges

## Surprising Connections (you probably didn't know these)
- `TradingDashboardFrame` --references--> `MetricsHeaderPanel`  [EXTRACTED]
  src/main/java/com/exchange/ui/gui/TradingDashboardFrame.java → src/main/java/com/exchange/ui/gui/MetricsHeaderPanel.java
- `Market` --references--> `Stock`  [EXTRACTED]
  src/main/java/com/exchange/market/Market.java → src/main/java/com/exchange/model/Stock.java
- `Stock` --references--> `Sector`  [EXTRACTED]
  src/main/java/com/exchange/model/Stock.java → src/main/java/com/exchange/model/Sector.java
- `RealWorldPriceStrategy` --references--> `Sector`  [EXTRACTED]
  src/main/java/com/exchange/market/RealWorldPriceStrategy.java → src/main/java/com/exchange/model/Sector.java
- `CsvPersistenceService` --implements--> `PersistenceService`  [EXTRACTED]
  src/main/java/com/exchange/persistence/CsvPersistenceService.java → src/main/java/com/exchange/persistence/PersistenceService.java

## Import Cycles
- None detected.

## Communities (26 total, 6 thin omitted)

### Community 0 - "OrderTicketPanel"
Cohesion: 0.07
Nodes (30): com.exchange.engine.TradingService, com.exchange.market.Market, com.exchange.market.MarketObserver, com.exchange.model.Stock, com.exchange.model.User, com.exchange.persistence.PersistenceService, DefaultTableModel, javax.swing.table.DefaultTableModel (+22 more)

### Community 1 - "jquery-3.7.1.min.js"
Cohesion: 0.07
Nodes (39): Ae(), B(), Be(), c(), $e(), ee(), F(), fe() (+31 more)

### Community 2 - "User"
Cohesion: 0.09
Nodes (6): com.exchange.model.Transaction, Override, User, PersistenceService, ConsoleUI, TextTableFormatter

### Community 3 - "Portfolio"
Cohesion: 0.07
Nodes (6): PersistenceException, Holding, Override, Portfolio, CsvPersistenceService, Override

### Community 4 - "Transaction"
Cohesion: 0.07
Nodes (10): TradingService, InsufficientFundsException, InsufficientSharesException, InvalidStockSymbolException, TradingException, Override, Transaction, TransactionType (+2 more)

### Community 5 - ".getPortfolio"
Cohesion: 0.17
Nodes (5): FunctionalInterface, Override, TradingEngine, TestCase, TradingPlatformTest

### Community 6 - "script.js"
Cohesion: 0.16
Nodes (19): collapse(), copySnippet(), copyToClipboard(), createElem(), expand(), handleResize(), handleScroll(), initSectionData() (+11 more)

### Community 7 - "search.js"
Cohesion: 0.16
Nodes (21): categories, checkUnnamed(), createMatcher(), doSearch(), getPrefix(), searchIndex(), useQualifiedName(), escapeHtml() (+13 more)

### Community 8 - "MetricsHeaderPanel"
Cohesion: 0.14
Nodes (12): AbstractButton, Color, Font, JButton, JLabel, JToggleButton, GuiTheme, Color (+4 more)

### Community 9 - "Market"
Cohesion: 0.15
Nodes (3): MarketObserver, PriceUpdateStrategy, Market

### Community 10 - "Stock"
Cohesion: 0.18
Nodes (3): MarketObserver, Override, Stock

### Community 11 - "RealWorldPriceStrategy"
Cohesion: 0.17
Nodes (3): PriceUpdateStrategy, Override, RealWorldPriceStrategy

### Community 12 - "Sector"
Cohesion: 0.14
Nodes (10): Override, Sector, AUTOMOTIVE, CONSUMER_STAPLES, ENERGY, FINANCIALS, HEALTHCARE, SEMICONDUCTORS (+2 more)

### Community 13 - "search-page.js"
Cohesion: 0.31
Nodes (7): doPageSearch(), renderItem(), renderResults(), renderResult(), renderTable(), schedulePageSearch(), setSearchUrl()

### Community 14 - ".calculateNewPrice"
Cohesion: 0.22
Nodes (3): PriceUpdateStrategy, Override, VolatilityPriceStrategy

## Knowledge Gaps
- **13 isolated node(s):** `AUTOMOTIVE`, `CONSUMER_STAPLES`, `ENERGY`, `FINANCIALS`, `HEALTHCARE` (+8 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 104 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Stock` connect `Stock` to `User`, `Transaction`, `.getPortfolio`, `Market`, `RealWorldPriceStrategy`, `Sector`, `.calculateNewPrice`?**
  _High betweenness centrality (0.107) - this node is a cross-community bridge._
- **Why does `Market` connect `Market` to `User`, `Portfolio`, `Transaction`, `.getPortfolio`, `Stock`, `Sector`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Why does `User` connect `User` to `Sector`, `Portfolio`, `Transaction`, `.getPortfolio`?**
  _High betweenness centrality (0.054) - this node is a cross-community bridge._
- **What connects `AUTOMOTIVE`, `CONSUMER_STAPLES`, `ENERGY` to the rest of the system?**
  _13 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `OrderTicketPanel` be split into smaller, more focused modules?**
  _Cohesion score 0.07377049180327869 - nodes in this community are weakly interconnected._
- **Should `jquery-3.7.1.min.js` be split into smaller, more focused modules?**
  _Cohesion score 0.0726775956284153 - nodes in this community are weakly interconnected._
- **Should `User` be split into smaller, more focused modules?**
  _Cohesion score 0.08979591836734693 - nodes in this community are weakly interconnected._