# Конфигурация

Приложение не использует `application.yml` — настройка через переменные окружения
и системные свойства. Логирование — `src/main/resources/logback.xml`.

## Параметры

| Переменная env | Sysprop | Назначение | Дефолт |
|---|---|---|---|
| `SERVER_PORT` | `-Dserver.port` | Порт HTTP-сервера | `8080` |
| `DATA_FILE` | `-Ddata.file` | Путь к CSV | `./tasks.csv` |

**Приоритет:** env > sysprop > дефолт. Пустое значение (`DATA_FILE=`) трактуется
как «не задано».

Читается через `AppConfig`:
- `getServerPort()` → `int`
- `getDataFilePath()` → `String`
- `getVersion()` → `String` (из `app.properties`)

## Версия

`src/main/resources/app.properties`:

```properties
app.version=${project.version}
```

Maven resource filtering подставляет реальную версию из `pom.xml` при сборке
(`<filtering>true</filtering>` в `<resources>`, `<propertiesEncoding>UTF-8</propertiesEncoding>`
в `maven-resources-plugin`). В `target/classes/app.properties` попадает
`app.version=1.0.0-SNAPSHOT`.

`AppConfig.getVersion()` читает этот файл через `getResourceAsStream`. Если файла
нет или версия не задана — возвращает `"unknown"`.

**Единственный источник правды — `pom.xml`.** Менять версию нужно только там.

## Логирование

`src/main/resources/logback.xml`:

- `ConsoleAppender` с паттерном `%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`.
- `root` уровень `INFO`.
- `ru.yandex.practicum` уровень `DEBUG`.

## Maven-плагины

| Плагин | Фаза | Что делает |
|---|---|---|
| `maven-resources-plugin` | `process-resources` | Resource filtering (`app.properties`) |
| `maven-compiler-plugin` | `compile` | Компиляция с `--release 17` |
| `maven-checkstyle-plugin` | `validate` | Проверка стиля (`checkstyle.xml`, `failOnViolation=true`) |
| `maven-surefire-plugin` | `test` | Тесты, `argLine` с UTF-8 + `@{argLine}` (JaCoCo) |
| `jacoco-maven-plugin` | `verify` | Отчёт покрытия в `target/site/jacoco/` |
| `maven-shade-plugin` | `package` | Fat-jar со всеми зависимостями |
| `spotbugs-maven-plugin` | `verify` | Статический анализ, `spotbugs-exclude.xml` |
| `exec-maven-plugin` | — | `mvn exec:java` для запуска без сборки jar |

## Сборка

```bash
./mvnw clean test       # только тесты
./mvnw clean package    # fat-jar в target/java-kanban.jar
./mvnw clean verify     # + Checkstyle + SpotBugs + JaCoCo
./mvnw exec:java        # запуск main-класса без сборки jar
```