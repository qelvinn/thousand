package ru.vsu.thousand.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * Восстанавливает записи журнала из сохранённого списка.
     *
     * @param savedEntries сохранённые записи журнала
     */
    public void restore(List<String> savedEntries) {
        Objects.requireNonNull(savedEntries, "Список событий не может быть null");
        entries.clear();
        for (String entry : savedEntries) {
            add(entry);
        }
    }

    /**
     * Очищает журнал.
     */
    public void clear() {
        entries.clear();
    }
}