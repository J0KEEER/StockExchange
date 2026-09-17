package com.exchange;

import com.exchange.engine.TradingEngine;
import com.exchange.engine.TradingService;
import com.exchange.market.Market;
import com.exchange.model.User;
import com.exchange.persistence.CsvPersistenceService;
import com.exchange.persistence.PersistenceService;
import com.exchange.ui.ConsoleUI;
import com.exchange.ui.gui.TradingDashboardFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

/**
 * Main application driver providing dual-mode launching: modern Swing GUI desktop workstation
 * by default, or interactive terminal console in headless and command-line environments.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class Main {

    /**
     * Private constructor to prevent instantiation of static entrypoint class.
     */
    private Main() {
        // Utility class
    }

    /**
     * Entry point of the Equity Stock Exchange platform.
     *
     * @param args command-line arguments: pass {@code --cli} to force terminal mode
     */
    public static void main(String[] args) {
        boolean forceCli = false;
        if (args != null) {
            for (String arg : args) {
                if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                    forceCli = true;
                    break;
                }
            }
        }

        Market market = new Market();
        TradingService tradingService = new TradingEngine(market);
        PersistenceService persistenceService = new CsvPersistenceService("data");

        // Dual-mode selection: GUI by default unless forced or running in headless environment
        if (!forceCli && !GraphicsEnvironment.isHeadless()) {
            launchGuiMode(tradingService, persistenceService);
        } else {
            launchCliMode(tradingService, persistenceService);
        }
    }

    /**
     * Bootstraps and presents the desktop GUI trading workstation.
     *
     * @param tradingService the trading business service
     * @param persistenceService the persistence storage service
     */
    private static void launchGuiMode(TradingService tradingService, PersistenceService persistenceService) {
        try {
            // Configure modern dark system look and feel if available
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing look and feel
        }

        SwingUtilities.invokeLater(() -> {
            User user = null;
            try {
                if (persistenceService.hasSavedData()) {
                    user = persistenceService.loadUserData();
                }
            } catch (Exception e) {
                System.err.println("Notice: Could not load saved session, initializing default user: " + e.getMessage());
            }

            if (user == null) {
                user = new User("Investor", 100_000.00);
            }

            TradingDashboardFrame frame = new TradingDashboardFrame(tradingService, persistenceService, user);
            frame.setVisible(true);
        });
    }

    /**
     * Bootstraps and executes the terminal command-line interactive interface.
     *
     * @param tradingService the trading business service
     * @param persistenceService the persistence storage service
     */
    private static void launchCliMode(TradingService tradingService, PersistenceService persistenceService) {
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleUI ui = new ConsoleUI(tradingService, persistenceService, scanner);
            ui.start();
        } catch (Exception e) {
            System.err.println("Fatal application error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
