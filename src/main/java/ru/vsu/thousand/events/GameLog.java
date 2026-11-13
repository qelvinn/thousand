package ru.vsu.thousand.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит журнал событий игровой партии.
 */
public class GameLog {

    private final List<String> entries = new ArrayList<>();

    /**
     * Добавляет событие в журнал.
     *
     * @param event описание события
     */
    public void add(String event) {
        if (event == null || event.isBlank()) {
            throw new IllegalArgumentException("Событие не может быть пустым");
        }
        entries.add(event);
    }

    /**
     * Возвращает все записи журнала.
     *
     * @return неизменяемый список событий
     */
    public List<String> getEntries() {
        return List.copyOf(entries);
    }

    /**
     * Очищает журнал.
     */
    public void clear() {
        entries.clear();
    }
}