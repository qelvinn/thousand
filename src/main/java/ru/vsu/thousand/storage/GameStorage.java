package ru.vsu.thousand.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.vsu.thousand.bot.DefaultBotStrategy;
import ru.vsu.thousand.bot.PlayerStrategy;
import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Обеспечивает сохранение и загрузку состояния партии в формате JSON,
 * а также повтор партии по сохранённому снимку.
 */
public class GameStorage {
    private final ObjectMapper objectMapper;

    /**
     * Создаёт хранилище с настроенным JSON-сериализатором.
     */
    public GameStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Преобразует текущее состояние партии в строку формата JSON.
     *
     * @param engine игровой движок
     * @return JSON-представление состояния партии
     */
    public String toJson(GameEngine engine) {
        Objects.requireNonNull(engine, "Игровой движок не может быть null");
        try {
            return objectMapper.writeValueAsString(engine.createSnapshot());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Ошибка сериализации партии в JSON", e);
        }
    }

    /**
     * Восстанавливает игровой движок из строки формата JSON со стратегиями по умолчанию.
     *
     * @param json JSON-строка с состоянием партии
     * @return восстановленный игровой движок
     */
    public GameEngine fromJson(String json) {
        return fromJson(json, player -> new DefaultBotStrategy());
    }

    /**
     * Восстанавливает игровой движок из строки формата JSON с пользовательскими стратегиями.
     *
     * @param json JSON-строка с состоянием партии
     * @param strategyResolver функция назначения стратегии каждому восстановленному игроку
     * @return восстановленный игровой движок
     */
    public GameEngine fromJson(
            String json,
            Function<Player, PlayerStrategy> strategyResolver
    ) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON-строка не может быть пустой");
        }
        Objects.requireNonNull(strategyResolver, "Функция выбора стратегии не может быть null");
        try {
            GameSnapshot snapshot = objectMapper.readValue(json, GameSnapshot.class);
            return GameEngine.fromSnapshot(snapshot, strategyResolver);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Некорректный формат JSON для восстановления партии", e);
        }
    }

    /**
     * Сохраняет текущее состояние партии в JSON-файл.
     *
     * @param engine игровой движок
     * @param filePath путь к целевому файлу
     * @throws IOException при ошибке записи файла
     */
    public void saveToFile(GameEngine engine, Path filePath) throws IOException {
        Objects.requireNonNull(engine, "Игровой движок не может быть null");
        Objects.requireNonNull(filePath, "Путь к файлу не может быть null");
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(filePath, toJson(engine));
    }

    /**
     * Загружает состояние партии из JSON-файла.
     *
     * @param filePath путь к файлу с сохранённой партией
     * @return восстановленный игровой движок
     * @throws IOException при ошибке чтения файла
     */
    public GameEngine loadFromFile(Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "Путь к файлу не может быть null");
        String json = Files.readString(filePath);
        return fromJson(json);
    }

    /**
     * Воспроизводит партию с самого начала до шага, зафиксированного в снимке,
     * используя исходный seed и стратегии по умолчанию.
     *
     * @param snapshot снимок партии с журналом событий
     * @param maxSteps максимальное количество шагов при повторе
     * @return движок в воспроизведённой позиции
     */
    public GameEngine replayFromSnapshot(GameSnapshot snapshot, int maxSteps) {
        return replayFromSnapshot(snapshot, Map.of(), maxSteps);
    }

    /**
     * Воспроизводит партию с самого начала до шага, зафиксированного в снимке,
     * используя исходный seed и заданные фабрики стратегий игроков.
     *
     * @param snapshot снимок партии с журналом событий
     * @param strategyFactories карта фабрик стратегий по именам игроков
     * @param maxSteps максимальное количество шагов при повторе
     * @return движок в воспроизведённой позиции
     */
    public GameEngine replayFromSnapshot(
            GameSnapshot snapshot,
            Map<String, Supplier<PlayerStrategy>> strategyFactories,
            int maxSteps
    ) {
        Objects.requireNonNull(snapshot, "Снимок состояния не может быть null");
        Objects.requireNonNull(strategyFactories, "Карта фабрик стратегий не может быть null");
        if (maxSteps < 1) {
            throw new IllegalArgumentException("Лимит шагов должен быть положительным");
        }

        List<Player> freshPlayers = new ArrayList<>();
        Map<Player, PlayerStrategy> strategies = new LinkedHashMap<>();

        for (GameSnapshot.PlayerSnapshot playerSnapshot : snapshot.players()) {
            Player player = new Player(playerSnapshot.name());
            freshPlayers.add(player);
            Supplier<PlayerStrategy> factory = strategyFactories.get(player.getName());
            strategies.put(player, factory != null ? factory.get() : new DefaultBotStrategy());
        }

        GameEngine replayed = new GameEngine(freshPlayers, snapshot.seed(), strategies);
        int targetEventsCount = snapshot.events().size();

        if (targetEventsCount == 0) {
            return replayed;
        }

        replayed.start();
        int steps = 0;
        while (replayed.getEvents().size() < targetEventsCount && steps < maxSteps) {
            replayed.runStep();
            steps++;
        }

        if (!replayed.getEvents().equals(snapshot.events())) {
            throw new IllegalStateException(
                    "Не удалось точно воспроизвести позицию из журнала партии"
            );
        }

        return replayed;
    }
}
