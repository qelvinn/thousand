package ru.vsu.thousand.model;

import java.util.Objects;

/**
 * Представляет игральную карту с мастью и достоинством.
 */
public class Card {
    private final Suit suit;
    private final Rank rank;

    /**
     * Создаёт карту заданной масти и достоинства.
     *
     * @param suit масть карты
     * @param rank достоинство карты
     */
    public Card(Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit);
        this.rank = Objects.requireNonNull(rank);
    }

    /**
     * Возвращает масть карты.
     *
     * @return масть карты
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Возвращает достоинство карты.
     *
     * @return достоинство карты
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Возвращает количество очков карты.
     *
     * @return количество очков
     */
    public int getPoints() {
        return rank.getPoints();
    }

    /**
     * Сравнивает карты по масти и достоинству.
     *
     * @param object объект для сравнения
     * @return true, если карты одинаковые
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Card card)) {
            return false;
        }
        return suit == card.suit && rank == card.rank;
    }

    /**
     * Возвращает хеш-код карты.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    /**
     * Возвращает текстовое представление карты.
     *
     * @return название карты и масти
     */
    @Override
    public String toString() {
        return rank.getDisplayName() + " " + suit.getDisplayName();
    }
}