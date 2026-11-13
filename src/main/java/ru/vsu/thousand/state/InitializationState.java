package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;

/**
 * Выполняет начальную подготовку партии.
 */
public class InitializationState implements GameState {

    /**
     * Переводит игру к раздаче карт.
     *
     * @param gameEngine игровой движок
     */
    @Override
    public void handle(GameEngine gameEngine) {
        gameEngine.addEvent("Партия начинается");
        gameEngine.setState(new DealingState());
    }

    /** @return фаза инициализации */
    @Override
    public GamePhase getPhase() {
        return GamePhase.INITIALIZATION;
    }
}