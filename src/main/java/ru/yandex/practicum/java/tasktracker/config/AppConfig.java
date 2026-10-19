package ru.yandex.practicum.java.tasktracker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String DEFAULT_DATA_FILE = "tasks.csv";

    private static final String VERSION_PROPERTIES = "app.properties";
    private static final String VERSION_KEY = "app.version";
    private static final String DEFAULT_VERSION = "unknown";

    public static String getVersion() {
        try (InputStream inputStream = AppConfig.class.getClassLoader()
                .getResourceAsStream(VERSION_PROPERTIES)) {
            if (inputStream == null) {
                return DEFAULT_VERSION;
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty(VERSION_KEY);
            return version != null && !version.isBlank() ? version : DEFAULT_VERSION;
        } catch (IOException exception) {
            return DEFAULT_VERSION;
        }
    }

    private AppConfig() {
    }

    public static int getServerPort() {
        return getInt("SERVER_PORT", "server.port", DEFAULT_SERVER_PORT);
    }

    public static String getDataFilePath() {
        return getString("DATA_FILE", "data.file", DEFAULT_DATA_FILE);
    }

    private static String getString(String envName, String sysPropName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = System.getProperty(sysPropName);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static int getInt(String envName, String sysPropName, int defaultValue) {
        String value = getString(envName, sysPropName, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid integer value for " + envName + "/" + sysPropName + ": " + value, exception);
        }
    }
}