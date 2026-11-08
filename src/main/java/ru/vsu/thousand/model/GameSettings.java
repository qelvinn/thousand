package ru.vsu.thousand.model;

import java.util.Random;

/**
 * Хранит настройки партии и генератор случайных чисел.
 */
public class GameSettings {
    private final long seed;
    private final Random random;

    /**
     * Создаёт настройки партии с заданным seed.
     *
     * @param seed начальное значение генератора случайных чисел
     */
    public GameSettings(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Возвращает seed партии.
     *
     * @return начальное значение генератора
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Возвращает генератор случайных чисел партии.
     *
     * @return генератор случайных чисел
     */
    public Random getRandom() {
        return random;
    }
}