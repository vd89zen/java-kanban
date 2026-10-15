package ru.yandex.practicum.java.tasktracker.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.CommaSeparatedValuesCreateProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CsvStorage implements TaskStorage {
    private static final Logger log = LoggerFactory.getLogger(CsvStorage.class);
    private static final int HEADER_TYPE_INDEX = 1;
    private static final int MIN_VALID_COLUMNS = 10;

    private final File file;
    private final CommaSeparatedValuesCreateProcess csvManager;

    public CsvStorage(File file) {
        this.file = Objects.requireNonNull(file, "'file' can't be null");
        this.csvManager = new CommaSeparatedValuesCreateProcess();
    }

    @Override
    public void save(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks) {
        Path parentPath = file.toPath().getParent();
        if (parentPath != null) {
            try {
                Files.createDirectories(parentPath);
            } catch (IOException exception) {
                throw new ManagerSaveException("Ошибка создания директории: " + file.getPath());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            writer.write(csvManager.getHeadersForCSV());
            writer.newLine();

            List<Task> sortedTasks = new ArrayList<>(tasks);
            sortedTasks.sort(Comparator.comparingInt(Task::getIdNumber));
            for (Task task : sortedTasks) {
                writer.write(csvManager.toString(task));
                writer.newLine();
            }

            List<Epic> sortedEpics = new ArrayList<>(epics);
            sortedEpics.sort(Comparator.comparingInt(Epic::getIdNumber));
            for (Epic epic : sortedEpics) {
                writer.write(csvManager.toString(epic));
                writer.newLine();
            }

            List<Subtask> sortedSubtasks = new ArrayList<>(subtasks);
            sortedSubtasks.sort(Comparator.comparingInt(Subtask::getIdNumber));
            for (Subtask subtask : sortedSubtasks) {
                writer.write(csvManager.toString(subtask));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка записи в файл: " + file.getName());
        }
    }

    @Override
    public LoadedState load() {
        List<Task> tasks = new ArrayList<>();
        List<Epic> epics = new ArrayList<>();
        List<Subtask> subtasks = new ArrayList<>();

        if (!Files.exists(file.toPath())) {
            return new LoadedState(tasks, epics, subtasks);
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < MIN_VALID_COLUMNS) {
                    log.warn("Skipping malformed line ({} columns, expected {}): {}",
                            fields.length, MIN_VALID_COLUMNS, line);
                    continue;
                }

                switch (fields[HEADER_TYPE_INDEX]) {
                    case "TASK":
                        tasks.add(csvManager.getTaskFromStrings(fields));
                        break;
                    case "EPIC":
                        epics.add(csvManager.getEpicFromStrings(fields));
                        break;
                    case "SUBTASK":
                        subtasks.add(csvManager.getSubtaskFromStrings(fields));
                        break;
                    case "type":
                        break;
                    default:
                        log.warn("Skipping line with unknown type '{}': {}", fields[HEADER_TYPE_INDEX], line);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Ошибка чтения файла: " + file.getName(), exception);
        }

        return new LoadedState(tasks, epics, subtasks);
    }
}