# Graph Report - StockExchange  (2026-09-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 418 nodes · 961 edges · 18 communities (12 shown, 1 thin omitted)
- Extraction: 80% EXTRACTED · 20% INFERRED · 0% AMBIGUOUS · INFERRED: 188 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- jquery-3.7.1.min.js
- TradingDashboardFrame
- User
- Stock
- Portfolio
- OrderTicketPanel
- Transaction
- script.js
- search.js
- TradingEngine.java
- ConsoleUI
- search-page.js
- run.sh

## God Nodes (most connected - your core abstractions)
1. `Stock` - 36 edges
2. `User` - 34 edges
3. `Market` - 30 edges
4. `Transaction` - 28 edges
5. `Portfolio` - 22 edges
6. `Holding` - 21 edges
7. `OrderTicketPanel` - 21 edges
8. `TradingDashboardFrame` - 20 edges
9. `ConsoleUI` - 19 edges
10. `TradingPlatformTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `TradingDashboardFrame` --references--> `MetricsHeaderPanel`  [EXTRACTED]
  src/main/java/com/exchange/ui/gui/TradingDashboardFrame.java → src/main/java/com/exchange/ui/gui/MetricsHeaderPanel.java
- `TradingDashboardFrame` --references--> `OrderTicketPanel`  [EXTRACTED]
  src/main/java/com/exchange/ui/gui/TradingDashboardFrame.java → src/main/java/com/exchange/ui/gui/OrderTicketPanel.java
- `TradingEngine` --implements--> `TradingService`  [EXTRACTED]
  src/main/java/com/exchange/engine/TradingEngine.java → src/main/java/com/exchange/engine/TradingService.java
- `CsvPersistenceService` --implements--> `PersistenceService`  [EXTRACTED]
  src/main/java/com/exchange/persistence/CsvPersistenceService.java → src/main/java/com/exchange/persistence/PersistenceService.java
- `ConsoleUI` --references--> `User`  [EXTRACTED]
  src/main/java/com/exchange/ui/ConsoleUI.java → src/main/java/com/exchange/model/User.java

## Import Cycles
- None detected.

## Communities (18 total, 1 thin omitted)

### Community 0 - "jquery-3.7.1.min.js"
Cohesion: 0.07
Nodes (39): Ae(), B(), Be(), c(), $e(), ee(), F(), fe() (+31 more)

### Community 1 - "TradingDashboardFrame"
Cohesion: 0.08
Nodes (22): com.exchange.engine.TradingService, com.exchange.market.Market, com.exchange.market.MarketObserver, com.exchange.model.Stock, com.exchange.model.User, com.exchange.persistence.PersistenceService, javax.swing.table.DefaultTableModel, JFrame (+14 more)

### Community 2 - "User"
Cohesion: 0.11
Nodes (8): FunctionalInterface, Override, Override, User, CsvPersistenceService, Override, TestCase, TradingPlatformTest

### Community 3 - "Stock"
Cohesion: 0.07
Nodes (8): TradingEngine, Market, MarketObserver, PriceUpdateStrategy, Override, VolatilityPriceStrategy, Override, Stock

### Community 4 - "Portfolio"
Cohesion: 0.08
Nodes (4): PersistenceException, Holding, Override, Portfolio

### Community 5 - "OrderTicketPanel"
Cohesion: 0.10
Nodes (20): AbstractButton, Font, JComboBox, JPanel, JRadioButton, JToggleButton, GuiTheme, Color (+12 more)

### Community 6 - "Transaction"
Cohesion: 0.09
Nodes (6): Override, Transaction, TransactionType, BUY, SELL, TextTableFormatter

### Community 7 - "script.js"
Cohesion: 0.16
Nodes (19): collapse(), copySnippet(), copyToClipboard(), createElem(), expand(), handleResize(), handleScroll(), initSectionData() (+11 more)

### Community 8 - "search.js"
Cohesion: 0.16
Nodes (21): categories, checkUnnamed(), createMatcher(), doSearch(), getPrefix(), searchIndex(), useQualifiedName(), escapeHtml() (+13 more)

### Community 9 - "TradingEngine.java"
Cohesion: 0.17
Nodes (4): InsufficientFundsException, InsufficientSharesException, InvalidStockSymbolException, TradingException

### Community 10 - "ConsoleUI"
Cohesion: 0.25
Nodes (3): TradingService, PersistenceService, ConsoleUI

### Community 11 - "search-page.js"
Cohesion: 0.31
Nodes (7): doPageSearch(), renderItem(), renderResults(), renderResult(), renderTable(), schedulePageSearch(), setSearchUrl()

## Knowledge Gaps
- **6 isolated node(s):** `run.sh script`, `BUY`, `SELL`, `categories`, `messages` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 88 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Stock` connect `Stock` to `TradingDashboardFrame`, `User`, `OrderTicketPanel`, `Transaction`, `TradingEngine.java`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `User` connect `User` to `TradingDashboardFrame`, `Stock`, `Portfolio`, `Transaction`, `TradingEngine.java`, `ConsoleUI`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `Market` connect `Stock` to `TradingDashboardFrame`, `User`, `Transaction`, `TradingEngine.java`, `ConsoleUI`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **What connects `run.sh script`, `BUY`, `SELL` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `jquery-3.7.1.min.js` be split into smaller, more focused modules?**
  _Cohesion score 0.0726775956284153 - nodes in this community are weakly interconnected._
- **Should `TradingDashboardFrame` be split into smaller, more focused modules?**
  _Cohesion score 0.07609427609427609 - nodes in this community are weakly interconnected._
- **Should `User` be split into smaller, more focused modules?**
  _Cohesion score 0.10815602836879433 - nodes in this community are weakly interconnected._