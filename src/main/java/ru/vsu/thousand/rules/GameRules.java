package ru.vsu.thousand.rules;

/**
 * Содержит основные константы правил игры «Тысяча».
 */
public final class GameRules {

    /**
     * Количество игроков.
     */
    public static final int PLAYER_COUNT = 3;

    /**
     * Количество карт в колоде.
     */
    public static final int DECK_SIZE = 24;

    /**
     * Количество карт на руках у каждого игрока после раздачи.
     */
    public static final int HAND_SIZE = 7;

    /**
     * Количество карт в прикупе.
     */
    public static final int TALON_SIZE = 3;

    /**
     * Количество очков, необходимое для победы.
     */
    public static final int WINNING_SCORE = 1000;

    /**
     * Очковый барьер.
     */
    public static final int BARRIER_SCORE = 880;

    /**
     * Количество подряд невыполненных заказов для получения болта.
     */
    public static final int FAILED_CONTRACTS_FOR_BOLT = 3;

    /**
     * Количество болтов, приводящее к списанию штрафных очков.
     */
    public static final int BOLTS_FOR_PENALTY = 3;

    /**
     * Штраф в очках за накопление трёх болтов.
     */
    public static final int BOLT_PENALTY_POINTS = 120;

    private GameRules() {
    }
}
