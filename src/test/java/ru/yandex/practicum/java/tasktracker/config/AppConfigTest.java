package ru.yandex.practicum.java.tasktracker.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тесты для AppConfig")
class AppConfigTest {
    private static final String SERVER_PORT_PROPERTY = "server.port";
    private static final String DATA_FILE_PROPERTY = "data.file";

    private String savedServerPort;
    private String savedDataFile;

    @BeforeEach
    public void saveProperties() {
        savedServerPort = System.getProperty(SERVER_PORT_PROPERTY);
        savedDataFile = System.getProperty(DATA_FILE_PROPERTY);
        System.clearProperty(SERVER_PORT_PROPERTY);
        System.clearProperty(DATA_FILE_PROPERTY);
    }

    @AfterEach
    public void restoreProperties() {
        restoreProperty(SERVER_PORT_PROPERTY, savedServerPort);
        restoreProperty(DATA_FILE_PROPERTY, savedDataFile);
    }

    private void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @Nested
    @DisplayName("getServerPort")
    class ServerPortTests {
        @Test
        @DisplayName("Дефолт 8080, если sysprop не задан")
        void default_Test() {
            assertEquals(8080, AppConfig.getServerPort());
        }

        @Test
        @DisplayName("Sysprop server.port переопределяет дефолт")
        void sysprop_Override_Test() {
            System.setProperty(SERVER_PORT_PROPERTY, "9090");
            assertEquals(9090, AppConfig.getServerPort());
        }

        @Test
        @DisplayName("Пустое значение sysprop трактуется как не заданное")
        void blankSysprop_FallsBackToDefault_Test() {
            System.setProperty(SERVER_PORT_PROPERTY, "   ");
            assertEquals(8080, AppConfig.getServerPort());
        }
    }

    @Nested
    @DisplayName("getDataFilePath")
    class DataFilePathTests {
        @Test
        @DisplayName("Дефолт tasks.csv, если sysprop не задан")
        void default_Test() {
            assertEquals("tasks.csv", AppConfig.getDataFilePath());
        }

        @Test
        @DisplayName("Sysprop data.file переопределяет дефолт")
        void sysprop_Override_Test() {
            System.setProperty(DATA_FILE_PROPERTY, "/custom/path/data.csv");
            assertEquals("/custom/path/data.csv", AppConfig.getDataFilePath());
        }
    }

    @Nested
    @DisplayName("getVersion")
    class VersionTests {
        @Test
        @DisplayName("Возвращает версию из app.properties (подставлена Maven)")
        void readsFromProperties_Test() {
            String version = AppConfig.getVersion();
            assertNotNull(version);
            assertFalse(version.isBlank());
            assertFalse(version.contains("${"), "Версия не должна содержать неразвёрнутый плейсхолдер");
        }
    }
}