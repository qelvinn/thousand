package ru.vsu.thousand.engine;

import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.rules.GameRules;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Управляет передачей прикупа победителю торговли и сносом карт.
 */
public class TalonExchange {

    /**
     * Передаёт карты прикупа победителю торговли.
     *
     * @param winner победитель торговли
     * @param talon карты прикупа
     */
    public void takeTalon(Player winner, List<Card> talon) {
        Objects.requireNonNull(winner);
        Objects.requireNonNull(talon);

        if (talon.size() != GameRules.TALON_SIZE
                || talon.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Прикуп должен содержать три карты");
        }

        if (winner.getHand().size() != GameRules.HAND_SIZE) {
            throw new IllegalStateException("У победителя должно быть семь карт");
        }

        if (new HashSet<>(talon).size() != talon.size()) {
            throw new IllegalArgumentException("В прикупе не должно быть повторяющихся карт");
        }

        if (winner.getHand().stream().anyMatch(talon::contains)) {
            throw new IllegalArgumentException("Карта прикупа уже есть у игрока");
        }

        talon.forEach(winner::addCard);
    }

    /**
     * Удаляет три выбранные карты из руки победителя торговли.
     *
     * @param winner победитель торговли
     * @param discardedCards карты, выбранные для сноса
     */
    public void discard(Player winner, List<Card> discardedCards) {
        Objects.requireNonNull(winner);
        Objects.requireNonNull(discardedCards);

        if (winner.getHand().size() != GameRules.HAND_SIZE + GameRules.TALON_SIZE) {
            throw new IllegalStateException("Перед сносом у игрока должно быть десять карт");
        }

        if (discardedCards.size() != GameRules.TALON_SIZE) {
            throw new IllegalArgumentException("Необходимо снести ровно три карты");
        }

        Set<Card> uniqueCards = new HashSet<>(discardedCards);

        if (uniqueCards.size() != discardedCards.size()) {
            throw new IllegalArgumentException("Нельзя выбрать одну карту несколько раз");
        }

        if (!winner.getHand().containsAll(discardedCards)) {
            throw new IllegalArgumentException("Все выбранные карты должны быть на руке");
        }

        List<Card> cardsToRemove = List.copyOf(discardedCards);

        for (Card card : cardsToRemove) {
            winner.removeCard(card);
        }
    }
}