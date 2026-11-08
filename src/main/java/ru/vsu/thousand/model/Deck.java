package ru.vsu.thousand.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Представляет колоду из 24 карт для игры «Тысяча».
 */
public class Deck {
    private final List<Card> cards;

    /**
     * Создаёт и перемешивает колоду с помощью заданного генератора.
     *
     * @param random генератор случайных чисел
     */
    public Deck(Random random) {
        Objects.requireNonNull(random);
        cards = new ArrayList<>();

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }

        Collections.shuffle(cards, random);
    }

    /**
     * Выдаёт верхнюю карту колоды.
     *
     * @return выданная карта
     * @throws IllegalStateException если колода пуста
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("В колоде закончились карты");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Возвращает количество оставшихся карт.
     *
     * @return количество карт
     */
    public int size() {
        return cards.size();
    }

    /**
     * Проверяет, пуста ли колода.
     *
     * @return true, если карт не осталось
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}