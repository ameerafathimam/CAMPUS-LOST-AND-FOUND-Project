package com.campus.lostfound.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates MySQL connections for the whole application.
 *
 * Configuration is read in this order (later values win):
 *   1. Built-in development defaults (localhost:3306, database campus_lost_found)
 *   2. The .env file in the project root (copy it from .env.example)
 *   3. Real operating-system environment variables
 *
 * No password is ever written in the source code, so the project can be
 * published on GitHub safely.
 */
public final class DBConnection {

    private static final String[] CONFIG_KEYS = {
            "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD"
    };

    /** Loaded once and reused for every connection. */
    private static Properties config;

    /** Utility class: it is never instantiated. */
    private DBConnection() {
    }

    /**
     * Opens a new JDBC connection using the current configuration.
     * The caller is responsible for closing it (try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        Properties cfg = loadConfig();

        String host = cfg.getProperty("DB_HOST");
        String port = cfg.getProperty("DB_PORT");
        String database = cfg.getProperty("DB_NAME");
        String user = cfg.getProperty("DB_USER");
        String password = cfg.getProperty("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&characterEncoding=UTF-8";

        // The password itself is never included in any message we print.
        if (password.isEmpty()) {
            throw new SQLException(
                    "Database password is not configured. Copy .env.example to .env and set DB_PASSWORD.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Used once at startup so the user learns about configuration problems
     * before the menu starts accepting input.
     */
    public static void checkConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("The database connection is not valid.");
            }
        }
    }

    /**
     * Returns a short, password-free description of the configuration.
     * Used in error messages so the user knows which file to check.
     */
    public static String describeConfiguration() {
        Properties cfg = loadConfig();
        return "host=" + cfg.getProperty("DB_HOST")
                + ", port=" + cfg.getProperty("DB_PORT")
                + ", database=" + cfg.getProperty("DB_NAME")
                + ", user=" + cfg.getProperty("DB_USER");
    }

    /** Reads defaults, then the .env file, then real environment variables. */
    private static synchronized Properties loadConfig() {
        if (config != null) {
            return config;
        }

        Properties props = new Properties();
        props.setProperty("DB_HOST", "localhost");
        props.setProperty("DB_PORT", "3306");
        props.setProperty("DB_NAME", "campus_lost_found");
        props.setProperty("DB_USER", "campus_app");
        props.setProperty("DB_PASSWORD", "");

        loadEnvFile(props, Paths.get(".env"));

        for (String key : CONFIG_KEYS) {
            String value = System.getenv(key);
            if (value != null && !value.isBlank()) {
                props.setProperty(key, value.trim());
            }
        }

        config = props;
        return config;
    }

    /**
     * Parses a simple KEY=VALUE file. Blank lines and lines starting
     * with '#' are ignored, so the file is easy to read and comment.
     */
    private static void loadEnvFile(Properties props, Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            for (String rawLine : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                if (key.startsWith("DB_")) {
                    props.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            // A broken .env file must not crash the program; defaults are used.
            System.out.println("[WARNING] Could not read the .env file: " + e.getMessage());
        }
    }
}
