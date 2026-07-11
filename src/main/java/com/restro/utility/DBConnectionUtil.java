package com.restro.utility;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized JDBC access point. Every DAO obtains connections through
 * {@link #getConnection()} and returns them with the ordinary
 * {@code connection.close()} call - never via {@code DriverManager} directly.
 * <p>
 * This implements a small self-contained connection pool (no external
 * pooling library, no container-level JNDI DataSource setup) so the WAR can
 * be dropped into any plain Servlet container and configured with nothing
 * more than {@code db.properties}.
 * <p>
 * Pooled connections are handed out wrapped in a JDK dynamic proxy: calling
 * {@code close()} returns the connection to the pool instead of physically
 * closing the socket, so ordinary try-with-resources DAO code needs no
 * awareness that pooling is happening underneath it.
 */
public final class DBConnectionUtil {

    private static final AppLogger LOG = AppLogger.getLogger(DBConnectionUtil.class);

    // Capacity == maxPoolSize is intentional: creation is capped by
    // createdCount under the same lock used to check it, so the number of
    // connections in existence (checked-out + queued) never exceeds
    // maxPoolSize, meaning offer() below can never fail on a full queue.
    private static BlockingQueue<Connection> pool;

    private static String url;
    private static String username;
    private static String password;
    private static int maxPoolSize;
    private static int connectionTimeoutSeconds;
    private static final AtomicInteger createdCount = new AtomicInteger(0);
    private static volatile boolean initialized = false;

    private DBConnectionUtil() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = DBConnectionUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }

        String driver = props.getProperty("db.driver");
        url = props.getProperty("db.url");
        username = props.getProperty("db.username");
        password = props.getProperty("db.password");

        // Cloud hosts (Railway, Render, etc.) inject DB connection info as
        // environment variables rather than a properties file - override
        // with those when present, so the same WAR/image works unmodified
        // both locally (db.properties) and in a container (env vars).
        if (System.getenv("DB_URL") != null) {
            url = System.getenv("DB_URL");
        }
        if (System.getenv("DB_USERNAME") != null) {
            username = System.getenv("DB_USERNAME");
        }
        if (System.getenv("DB_PASSWORD") != null) {
            password = System.getenv("DB_PASSWORD");
        }

        int initialSize = ValidationUtil.parseIntOrDefault(props.getProperty("db.pool.initialSize"), 5);
        maxPoolSize = ValidationUtil.parseIntOrDefault(props.getProperty("db.pool.maxSize"), 20);
        connectionTimeoutSeconds =
                ValidationUtil.parseIntOrDefault(props.getProperty("db.pool.connectionTimeoutSeconds"), 10);

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found on classpath: " + driver, e);
        }

        pool = new LinkedBlockingQueue<>(maxPoolSize);
        try {
            for (int i = 0; i < initialSize; i++) {
                pool.offer(createRawPooledConnection());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open initial database connections at " + url, e);
        }
        initialized = true;
        LOG.info("DB connection pool initialized: initialSize=" + initialSize + ", maxSize=" + maxPoolSize);
    }

    /** Resolved JDBC URL (db.properties, overridden by env var DB_URL if set) - used by DatabaseBackupUtil. */
    static String getUrl() {
        if (!initialized) {
            initialize();
        }
        return url;
    }

    /** Resolved DB username (db.properties, overridden by env var DB_USERNAME if set) - used by DatabaseBackupUtil. */
    static String getUsername() {
        if (!initialized) {
            initialize();
        }
        return username;
    }

    /** Resolved DB password (db.properties, overridden by env var DB_PASSWORD if set) - used by DatabaseBackupUtil. */
    static String getPassword() {
        if (!initialized) {
            initialize();
        }
        return password;
    }

    /**
     * Borrows a connection from the pool, growing the pool on demand up to
     * {@code db.pool.maxSize}. Callers MUST release it with
     * {@code connection.close()} (try-with-resources works as normal).
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        try {
            Connection connection = pool.poll(connectionTimeoutSeconds, TimeUnit.SECONDS);
            if (connection == null) {
                Connection created = tryCreateNewConnection();
                if (created != null) {
                    return created;
                }
                throw new SQLException("Timed out waiting for a database connection from the pool (maxPoolSize="
                        + maxPoolSize + "). The database may be under heavy load, or maxPoolSize is too low.");
            }
            if (connection.isClosed() || !connection.isValid(2)) {
                LOG.warn("Discarding a dead pooled connection and creating a replacement");
                createdCount.decrementAndGet();
                return createRawPooledConnection();
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection", e);
        }
    }

    /**
     * Invoked only by the proxy's close() handler - application code should
     * never call this directly, just use connection.close().
     */
    static void releaseConnection(Connection pooledConnection) {
        if (pool != null) {
            pool.offer(pooledConnection);
        }
    }

    private static synchronized Connection tryCreateNewConnection() throws SQLException {
        if (createdCount.get() >= maxPoolSize) {
            return null;
        }
        return createRawPooledConnection();
    }

    private static Connection createRawPooledConnection() throws SQLException {
        Connection raw = DriverManager.getConnection(url, username, password);
        createdCount.incrementAndGet();
        return (Connection) Proxy.newProxyInstance(
                DBConnectionUtil.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(raw));
    }

    /** Drains and physically closes every pooled connection. Call on application shutdown. */
    public static synchronized void shutdown() {
        if (pool == null) {
            return;
        }
        Connection connection;
        while ((connection = pool.poll()) != null) {
            closeRealConnection(connection);
        }
        createdCount.set(0);
        initialized = false;
        LOG.info("DB connection pool shut down");
    }

    private static void closeRealConnection(Connection possiblyProxied) {
        try {
            if (Proxy.isProxyClass(possiblyProxied.getClass())) {
                PooledConnectionHandler handler = (PooledConnectionHandler) Proxy.getInvocationHandler(possiblyProxied);
                handler.closeForReal();
            } else {
                possiblyProxied.close();
            }
        } catch (SQLException e) {
            LOG.warn("Error closing a pooled connection during shutdown", e);
        }
    }

    private static final class PooledConnectionHandler implements InvocationHandler {
        private final Connection realConnection;

        PooledConnectionHandler(Connection realConnection) {
            this.realConnection = realConnection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("close".equals(methodName)) {
                releaseConnection((Connection) proxy);
                return null;
            }
            if ("toString".equals(methodName)) {
                return "PooledConnection[" + realConnection + "]";
            }
            try {
                return method.invoke(realConnection, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        void closeForReal() throws SQLException {
            realConnection.close();
        }
    }
}
