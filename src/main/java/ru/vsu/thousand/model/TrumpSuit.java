package ru.vsu.thousand.model;

import java.util.Optional;

/**
 * Хранит текущую козырную масть и позволяет изменять её.
 */
public class TrumpSuit {
    private Suit suit;

    /**
     * Создаёт состояние без назначенного козыря.
     */
    public TrumpSuit() {
        this.suit = null;
    }

    /**
     * Возвращает текущую козырную масть.
     *
     * @return козырная масть или пустое значение, если козырь не назначен
     */
    public Optional<Suit> getSuit() {
        return Optional.ofNullable(suit);
    }

    /**
     * Назначает новую козырную масть.
     *
     * @param suit новая козырная масть
     */
    public void setSuit(Suit suit) {
        this.suit = java.util.Objects.requireNonNull(suit);
    }

    /**
     * Сбрасывает назначенный козырь.
     */
    public void clear() {
        this.suit = null;
    }

    /**
     * Проверяет, является ли указанная масть козырной.
     *
     * @param candidate проверяемая масть
     * @return true, если масть является текущим козырем
     */
    public boolean isTrump(Suit candidate) {
        return suit != null && suit == candidate;
    }
}