package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;

/**
 * Представляет завершённую партию.
 */
public class FinishedState implements GameState {

    /**
     * Фиксирует завершение партии.
     *
     * @param gameEngine игровой движок
     */
    @Override
    public void handle(GameEngine gameEngine) {
        gameEngine.addEvent("Партия завершена");
    }

    /** @return финальная фаза партии */
    @Override
    public GamePhase getPhase() {
        return GamePhase.FINISHED;
    }
}