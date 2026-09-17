package com.exchange.persistence;

import com.exchange.exception.PersistenceException;
import com.exchange.model.User;

/**
 * Service contract for persistent storage and retrieval of trading accounts and ledgers.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public interface PersistenceService {

    /**
     * Persists the user account state, active portfolio positions, and transaction ledger.
     *
     * @param user the account instance to save
     * @throws PersistenceException if an I/O or serialization error occurs
     */
    void saveUserData(User user) throws PersistenceException;

    /**
     * Restores the user account state, active portfolio positions, and historical trades from disk.
     *
     * @return the restored User instance, or {@code null} if no saved state exists
     * @throws PersistenceException if file parsing or corruption prevents recovery
     */
    User loadUserData() throws PersistenceException;

    /**
     * Verifies whether existing persistent data files exist on the filesystem.
     *
     * @return {@code true} if valid saved data is present, otherwise {@code false}
     */
    boolean hasSavedData();
}
