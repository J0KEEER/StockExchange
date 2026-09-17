package com.exchange.exception;

/**
 * Exception thrown when reading from or writing to persistent storage fails.
 *
 * @author Mridul Gupta
 * @version 1.0
 * @since 1.0
 */
public class PersistenceException extends Exception {

    /**
     * Constructs a new PersistenceException with a message.
     *
     * @param message description of the persistence failure
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a new PersistenceException with a message and cause.
     *
     * @param message description of the persistence failure
     * @param cause the underlying I/O or parsing error
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
