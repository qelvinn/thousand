package ru.vsu.thousand.rules;

/**
 * Содержит основные константы правил игры «Тысяча».
 */
public final class GameRules {
    public static final int PLAYER_COUNT = 3;
    public static final int DECK_SIZE = 24;
    public static final int HAND_SIZE = 7;
    public static final int TALON_SIZE = 3;

    public static final int WINNING_SCORE = 1000;
    public static final int BARRIER_SCORE = 880;
    public static final int FAILED_CONTRACTS_FOR_BOLT = 3;

    private GameRules() {
    }
}