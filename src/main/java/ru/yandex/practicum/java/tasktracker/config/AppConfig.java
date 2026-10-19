package ru.yandex.practicum.java.tasktracker.config;

public final class AppConfig {

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String DEFAULT_DATA_FILE = "tasks.csv";

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

    public static String getVersion() {
        return "0.2.0";
    }
}