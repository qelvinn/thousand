package ru.vsu.thousand.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Представляет участника игры «Тысяча».
 */
public class Player {
    private final String name;
    private final List<Card> hand;
    private int score;
    private int consecutiveFailedContracts;
    private int bolts;

    /**
     * Создаёт игрока с заданным именем.
     *
     * @param name имя игрока
     */
    public Player(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя игрока не может быть пустым");
        }
        this.name = name;
        this.hand = new ArrayList<>();
        this.score = 0;
        this.consecutiveFailedContracts = 0;
        this.bolts = 0;
    }

    /**
     * Возвращает имя игрока.
     *
     * @return имя игрока
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает неизменяемое представление карт игрока.
     *
     * @return карты на руках
     */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /**
     * Возвращает количество очков игрока.
     *
     * @return количество очков
     */
    public int getScore() {
        return score;
    }

    /**
     * Возвращает количество подряд невыполненных заказов.
     *
     * @return количество невыполненных заказов подряд
     */
    public int getConsecutiveFailedContracts() {
        return consecutiveFailedContracts;
    }

    /**
     * Возвращает количество болтов игрока.
     *
     * @return количество болтов
     */
    public int getBolts() {
        return bolts;
    }

    /**
     * Добавляет карту в руку игрока.
     *
     * @param card добавляемая карта
     */
    public void addCard(Card card) {
        hand.add(Objects.requireNonNull(card));
    }

    /**
     * Удаляет карту из руки игрока.
     *
     * @param card удаляемая карта
     * @return true, если карта была найдена и удалена
     */
    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    /**
     * Очищает руку игрока.
     */
    public void clearHand() {
        hand.clear();
    }

    /**
     * Изменяет количество очков игрока.
     *
     * @param points количество добавляемых очков
     */
    public void addScore(int points) {
        score += points;
    }

    /**
     * Фиксирует выполненный или невыполненный заказ.
     *
     * @param successful true, если заказ выполнен
     */
    public void recordContractResult(boolean successful) {
        if (successful) {
            consecutiveFailedContracts = 0;
        } else {
            consecutiveFailedContracts++;
            if (consecutiveFailedContracts == 3) {
                bolts++;
                consecutiveFailedContracts = 0;
            }
        }
    }
}