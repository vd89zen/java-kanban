package ru.yandex.practicum.java.tasktracker.storage;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;

import java.util.ArrayList;
import java.util.Collection;

public class NoopStorage implements TaskStorage {

    @Override
    public void save(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks) {
        // no-op: состояние живёт только в памяти
    }

    @Override
    public LoadedState load() {
        return new LoadedState(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}