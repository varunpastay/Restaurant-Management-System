package com.restro.utility;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin wrapper around java.util.logging so every layer (DAO, service,
 * controller, filter) logs through one consistent, JDK-only API instead of
 * pulling in an external logging framework.
 */
public final class AppLogger {

    private final Logger delegate;

    private AppLogger(Class<?> owner) {
        this.delegate = Logger.getLogger(owner.getName());
    }

    public static AppLogger getLogger(Class<?> owner) {
        return new AppLogger(owner);
    }

    public void info(String message) {
        delegate.log(Level.INFO, message);
    }

    public void fine(String message) {
        delegate.log(Level.FINE, message);
    }

    public void warn(String message) {
        delegate.log(Level.WARNING, message);
    }

    public void warn(String message, Throwable t) {
        delegate.log(Level.WARNING, message, t);
    }

    public void error(String message, Throwable t) {
        delegate.log(Level.SEVERE, message, t);
    }

    public void error(String message) {
        delegate.log(Level.SEVERE, message);
    }
}
