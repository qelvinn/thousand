package ru.vsu.thousand.util;

import ru.vsu.thousand.model.Card;

import java.util.Comparator;

/**
 * Содержит компараторы для сравнения игральных карт.
 */
public final class CardComparators {

    /**
     * Сравнивает карты сначала по старшинству, затем по масти.
     */
    public static final Comparator<Card> BY_RANK_THEN_SUIT =
            Comparator.comparingInt((Card card) -> card.getRank().getStrength())
                    .thenComparingInt(card -> card.getSuit().ordinal());

    /**
     * Сравнивает карты сначала по масти, затем по старшинству.
     */
    public static final Comparator<Card> BY_SUIT_THEN_RANK =
            Comparator.comparingInt((Card card) -> card.getSuit().ordinal())
                    .thenComparingInt(card -> card.getRank().getStrength());

    private CardComparators() {
    }
}