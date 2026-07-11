package com.restro.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Deployment-level configuration (app.properties) - public base URL used to
 * build QR payloads, mysql client binary location, etc. Restaurant
 * branding/business settings (name, logo, GST, tax %...) are NOT here; they
 * live in the {@code restaurant} DB table, see the settings service/DAO.
 */
public final class AppConfig {

    private static final AppLogger LOG = AppLogger.getLogger(AppConfig.class);
    private static final Properties PROPS = new Properties();
    private static volatile boolean loaded = false;

    private AppConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in == null) {
                throw new IllegalStateException("app.properties not found on classpath");
            }
            PROPS.load(in);
            loaded = true;
            LOG.info("Loaded app.properties (" + PROPS.size() + " keys)");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load app.properties", e);
        }
    }

    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Looks up {@code key}, but an environment variable wins if set - e.g.
     * {@code app.base.url} can be overridden by env var {@code APP_BASE_URL}.
     * Cloud hosts (Railway, Render, etc.) configure containers via
     * environment variables rather than editing a properties file, so this
     * lets the same WAR/image run unmodified both locally and in a container.
     */
    public static String get(String key, String defaultValue) {
        ensureLoaded();
        String envValue = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envValue != null) {
            return envValue;
        }
        return PROPS.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return ValidationUtil.parseIntOrDefault(get(key), defaultValue);
    }

    private static void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }
}
