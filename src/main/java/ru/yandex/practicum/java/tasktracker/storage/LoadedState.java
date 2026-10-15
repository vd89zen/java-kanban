package ru.yandex.practicum.java.tasktracker.storage;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;

import java.util.Collection;

public record LoadedState(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks) {
}