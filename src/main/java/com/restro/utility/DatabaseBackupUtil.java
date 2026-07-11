package com.restro.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Wraps the {@code mysqldump}/{@code mysql} command-line tools for the
 * Admin &gt; Backup &amp; Restore feature. The DB password is passed via the
 * MYSQL_PWD environment variable rather than a {@code --password=} CLI
 * argument, since command-line arguments are visible to other users on the
 * same machine via the process list - environment variables of a child
 * process are not.
 */
public final class DatabaseBackupUtil {

    private static final AppLogger LOG = AppLogger.getLogger(DatabaseBackupUtil.class);

    private DatabaseBackupUtil() {
    }

    public static void backup(OutputStream destination) throws IOException, InterruptedException {
        DbConnectionInfo info = readConnectionInfo();
        ProcessBuilder builder = new ProcessBuilder(
                resolveBinary("mysqldump"),
                "--host=" + info.host,
                "--port=" + info.port,
                "--user=" + info.username,
                "--single-transaction",
                "--routines",
                info.database);
        builder.environment().put("MYSQL_PWD", info.password);
        builder.redirectErrorStream(false);

        Process process = builder.start();
        try (InputStream stdout = process.getInputStream()) {
            stdout.transferTo(destination);
        }
        String errorOutput = readAll(process.getErrorStream());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("mysqldump exited with code " + exitCode + ": " + errorOutput);
        }
        LOG.info("Database backup completed successfully");
    }

    public static void restore(InputStream sqlScript) throws IOException, InterruptedException {
        DbConnectionInfo info = readConnectionInfo();
        ProcessBuilder builder = new ProcessBuilder(
                resolveBinary("mysql"),
                "--host=" + info.host,
                "--port=" + info.port,
                "--user=" + info.username,
                info.database);
        builder.environment().put("MYSQL_PWD", info.password);

        Process process = builder.start();
        try (OutputStream stdin = process.getOutputStream()) {
            sqlScript.transferTo(stdin);
        }
        String errorOutput = readAll(process.getErrorStream());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("mysql restore exited with code " + exitCode + ": " + errorOutput);
        }
        LOG.info("Database restore completed successfully");
    }

    private static String resolveBinary(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String exeName = windows ? name + ".exe" : name;
        String binDir = AppConfig.get("mysql.bin.dir", "");
        return (binDir != null && !binDir.isBlank()) ? Paths.get(binDir, exeName).toString() : exeName;
    }

    private static String readAll(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static DbConnectionInfo readConnectionInfo() throws IOException {
        Properties props = new Properties();
        try (InputStream in = DatabaseBackupUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IOException("db.properties not found on classpath");
            }
            props.load(in);
        }
        String jdbcUrl = props.getProperty("db.url");
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()).split("\\?")[0]);
        DbConnectionInfo info = new DbConnectionInfo();
        info.host = uri.getHost();
        info.port = uri.getPort() > 0 ? uri.getPort() : 3306;
        info.database = uri.getPath().substring(1);
        info.username = props.getProperty("db.username");
        info.password = props.getProperty("db.password");
        return info;
    }

    private static final class DbConnectionInfo {
        String host;
        int port;
        String database;
        String username;
        String password;
    }
}
