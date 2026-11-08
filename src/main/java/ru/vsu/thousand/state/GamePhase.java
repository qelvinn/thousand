package ru.vsu.thousand.state;

/**
 * Определяет последовательность фаз партии в игре «Тысяча».
 */
public enum GamePhase {
    INITIALIZATION,
    DEALING,
    BIDDING,
    DISCARDING,
    PLAYING,
    SCORING,
    FINISHED
}