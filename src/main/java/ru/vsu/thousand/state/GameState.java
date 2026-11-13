package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;

/**
 * Определяет поведение отдельного состояния партии.
 */
public interface GameState {

    /**
     * Выполняет действия текущей фазы игры.
     *
     * @param gameEngine игровой движок
     */
    void handle(GameEngine gameEngine);

    /**
     * Возвращает название состояния.
     *
     * @return название состояния
     */
    GamePhase getPhase();
}