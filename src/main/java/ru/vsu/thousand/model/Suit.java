package ru.vsu.thousand.model;

/**
 * Определяет карточные масти и стоимость марьяжа для каждой масти.
 */
public enum Suit {

    /** Масть пики: марьяж оценивается в 40 очков. */
    SPADES("Пики", 40),

    /** Масть трефы: марьяж оценивается в 60 очков. */
    CLUBS("Трефы", 60),

    /** Масть бубны: марьяж оценивается в 80 очков. */
    DIAMONDS("Бубны", 80),

    /** Масть червы: марьяж оценивается в 100 очков. */
    HEARTS("Червы", 100);

    private final String displayName;
    private final int marriagePoints;

    Suit(String displayName, int marriagePoints) {
        this.displayName = displayName;
        this.marriagePoints = marriagePoints;
    }

    /**
     * Возвращает отображаемое название масти.
     *
     * @return название масти
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Возвращает количество очков за объявление марьяжа данной масти.
     *
     * @return стоимость марьяжа в очках
     */
    public int getMarriagePoints() {
        return marriagePoints;
    }
}
