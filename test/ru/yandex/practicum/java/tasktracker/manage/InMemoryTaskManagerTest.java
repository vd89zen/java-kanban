package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.*;

@DisplayName("Тесты для InMemoryTaskManager. ID - idNumber, уникальный идентификатор")
class InMemoryTaskManagerTest extends TaskManagerTest {

    @BeforeEach
    public void Prepare_For_Test() {
        taskManager = ManagersUtil.getDefault();
        fillData();
    }
}