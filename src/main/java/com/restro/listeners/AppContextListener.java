package com.restro.listeners;

import com.restro.utility.AppConfig;
import com.restro.utility.AppLogger;
import com.restro.utility.DBConnectionUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Application bootstrap. Registered explicitly in web.xml (not via
 * annotation scanning) so startup wiring is one readable, ordered place:
 * configure logging -> load app.properties -> open the DB connection pool.
 * Reversed on shutdown so no connection or thread is left dangling.
 */
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        configureLogging();
        AppLogger log = AppLogger.getLogger(AppContextListener.class);
        log.info("Starting Restaurant Ordering System...");
        AppConfig.load();
        DBConnectionUtil.initialize();
        log.info("Startup complete.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AppLogger log = AppLogger.getLogger(AppContextListener.class);
        log.info("Shutting down Restaurant Ordering System...");
        DBConnectionUtil.shutdown();
        deregisterJdbcDriverAndCleanupThread(log);
    }

    /**
     * MySQL Connector/J registers its JDBC driver and starts a background
     * "abandoned connection cleanup" thread the first time it's loaded, both
     * tied to this webapp's classloader. Neither stops itself automatically,
     * which leaks the classloader on redeploy/undeploy (Tomcat logs a
     * WARNING for exactly this). Explicitly deregistering/shutting them down
     * here is the documented fix.
     */
    private void deregisterJdbcDriverAndCleanupThread(AppLogger log) {
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            log.warn("Failed to shut down MySQL's abandoned-connection cleanup thread", e);
        }
        java.util.Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            java.sql.Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                try {
                    java.sql.DriverManager.deregisterDriver(driver);
                } catch (java.sql.SQLException e) {
                    log.warn("Failed to deregister JDBC driver " + driver, e);
                }
            }
        }
    }

    private void configureLogging() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("logging.properties")) {
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            }
        } catch (IOException e) {
            System.err.println("Failed to load logging.properties, using default JUL config: " + e.getMessage());
        }
    }
}
