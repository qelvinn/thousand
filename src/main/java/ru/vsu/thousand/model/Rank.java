package ru.vsu.thousand.model;

/**
 * Определяет достоинства карт, их очковую стоимость и старшинство при взятке.
 */
public enum Rank {

    /** Девятка: 0 очков, минимальное старшинство. */
    NINE("Девятка", 0, 1),

    /** Валет: 2 очка. */
    JACK("Валет", 2, 2),

    /** Дама: 3 очка, участвует в марьяже. */
    QUEEN("Дама", 3, 3),

    /** Король: 4 очка, участвует в марьяже. */
    KING("Король", 4, 4),

    /** Десятка: 10 очков. */
    TEN("Десятка", 10, 5),

    /** Туз: 11 очков, максимальное старшинство. */
    ACE("Туз", 11, 6);

    private final String displayName;
    private final int points;
    private final int strength;

    Rank(String displayName, int points, int strength) {
        this.displayName = displayName;
        this.points = points;
        this.strength = strength;
    }

    /**
     * Возвращает отображаемое название достоинства карты.
     *
     * @return название достоинства
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Возвращает количество очков, которое приносит карта во взятке.
     *
     * @return очковая стоимость карты
     */
    public int getPoints() {
        return points;
    }

    /**
     * Возвращает относительную силу карты при сравнении в одной масти.
     *
     * @return числовой показатель старшинства
     */
    public int getStrength() {
        return strength;
    }
}
