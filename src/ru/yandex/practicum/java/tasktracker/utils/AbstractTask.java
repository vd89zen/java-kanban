package ru.yandex.practicum.java.tasktracker.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractTask {
    protected int idNumber;
    protected String name;
    protected String description;
    protected StatusProgress statusProgress;
    protected TypesTasks type;
    protected Duration durationInMinutes; //добавил InMinutes в имя, т.к. по условию длительность в минутах
    protected LocalDateTime startDateTime; //добавил Date в имя,т.к. по условию у нас и время и дата

    //возможность создания пустышки
    protected AbstractTask() {
        this.idNumber = -1;
        this.name = null;
        this.description = null;
        this.statusProgress = StatusProgress.NEW;//теперь можно и для эпика дополнить поля и включить в базу Менеджера
        this.durationInMinutes = null;
        this.startDateTime = null;
    }

    //возможность создания копии
    protected AbstractTask(AbstractTask task) {
        this.idNumber = task.idNumber;
        this.name = task.name;
        this.description = task.description;
        this.statusProgress = task.statusProgress;
        this.type = task.type;
        this.durationInMinutes = task.durationInMinutes;
        this.startDateTime = task.startDateTime;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        this.durationInMinutes = Duration.ZERO;
        this.idNumber = 0;
        this.startDateTime = null;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress, LocalDateTime startDateTime) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        this.startDateTime = Objects.requireNonNull(startDateTime, "warning 'startDateTime' is null");
        this.idNumber = 0;
        this.durationInMinutes = Duration.ZERO;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress, long durationInMinutes) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        if (durationInMinutes < 0) {
            throw new IllegalArgumentException("'durationInMinutes' can't be less than zero");
        } else {
            this.durationInMinutes = Duration.ofMinutes(durationInMinutes);
        }
        this.idNumber = 0;
        this.startDateTime = null;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress, long durationInMinutes,
                           LocalDateTime startDateTime) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        if (durationInMinutes < 0) {
            throw new IllegalArgumentException("'durationInMinutes' can't be less than zero");
        } else {
            this.durationInMinutes = Duration.ofMinutes(durationInMinutes);
        }
        this.startDateTime = Objects.requireNonNull(startDateTime, "warning 'startDateTime' is null");
    }

    protected AbstractTask(int idNumber, String name, String description, StatusProgress statusProgress) {
        if (idNumber < 0) {
            throw new IllegalArgumentException("'idNumber' can't be less zero");
        } else {
            this.idNumber = idNumber;
        }
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        this.durationInMinutes = Duration.ZERO;
        this.startDateTime = null;
    }

    protected AbstractTask(int idNumber, String name, String description, StatusProgress statusProgress,
                           LocalDateTime startDateTime) {
        if (idNumber < 0) {
            throw new IllegalArgumentException("'idNumber' can't be less zero");
        } else {
            this.idNumber = idNumber;
        }
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        this.startDateTime = Objects.requireNonNull(startDateTime, "warning 'startDateTime' is null");
        this.durationInMinutes = Duration.ZERO;
    }

    protected AbstractTask(int idNumber, String name, String description, StatusProgress statusProgress, long durationInMinutes) {
        if (idNumber < 0) {
            throw new IllegalArgumentException("'idNumber' can't be less zero");
        } else {
            this.idNumber = idNumber;
        }
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        if (durationInMinutes < 0) {
            throw new IllegalArgumentException("'durationInMinutes' can't be less than zero");
        } else {
            this.durationInMinutes = Duration.ofMinutes(durationInMinutes);
        }
        this.startDateTime = null;
    }

    protected AbstractTask(int idNumber, String name, String description, StatusProgress statusProgress,
                           long durationInMinutes, LocalDateTime startDateTime) {
        if (idNumber < 0) {
            throw new IllegalArgumentException("'idNumber' can't be less zero");
        } else {
            this.idNumber = idNumber;
        }
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        if (durationInMinutes < 0) {
            throw new IllegalArgumentException("'durationInMinutes' can't be less than zero");
        } else {
            this.durationInMinutes = Duration.ofMinutes(durationInMinutes);
        }
        this.startDateTime = Objects.requireNonNull(startDateTime, "warning 'startDateTime' is null");
    }

    public Optional<String> getName() {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.of(name);
    }

    public ResultOfOperation setName(String newName) {
        if (newName == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        name = newName;
        return ResultOfOperation.SUCCESS;
    }

    public Optional<String> getDescription() {
        if (description == null) {
            return Optional.empty();
        }
        return Optional.of(description);
    }

    public ResultOfOperation setDescription(String newDescription) {
        if (newDescription == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        description = newDescription;
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation setIdNumber(int idNumber) {
        if (idNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }
        this.idNumber = idNumber;
        return ResultOfOperation.SUCCESS;
    }

    public int getIdNumber() {
        return idNumber;
    }

    public StatusProgress getStatusProgress() {
        return statusProgress;
    }

    public TypesTasks getTypeTask() {
        return type;
    }

    public long getDurationInMinutes() {
        return durationInMinutes.toMinutes();
    }

    public Optional<LocalDateTime> getStartDateTime() {
        return Optional.ofNullable(startDateTime);
    }

    public Optional<LocalDateTime> getEndDateTime() {
        if (startDateTime == null) {
            return Optional.empty();
        }
        return Optional.of(startDateTime.plus(durationInMinutes));
    }
}
