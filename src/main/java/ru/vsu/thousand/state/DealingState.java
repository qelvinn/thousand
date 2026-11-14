package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.Auction;
import ru.vsu.thousand.engine.GameEngine;

/**
 * Выполняет раздачу карт и начинает торговлю.
 */
public class DealingState implements GameState {

    /**
     * Очищает данные прошлого раунда, раздаёт карты игрокам и открывает торговлю.
     *
     * @param gameEngine игровой движок
     */
    @Override
    public void handle(GameEngine gameEngine) {
        gameEngine.getPlayers().forEach(player -> player.clearHand());

        gameEngine.resetRoundData();
        gameEngine.getMarriageManager().reset();
        gameEngine.getTrumpSuit().clear();

        gameEngine.setTalon(
                gameEngine.getDealer().deal(gameEngine.getPlayers())
        );

        int startingPlayer = gameEngine.getStartingPlayerIndex();

        gameEngine.setAuction(
                new Auction(gameEngine.getPlayers(), startingPlayer)
        );

        gameEngine.advanceRound();

        gameEngine.addEvent("Карты розданы. Начинается торговля");
        gameEngine.setState(new BiddingState());
    }

    /**
     * Возвращает фазу раздачи карт.
     *
     * @return фаза раздачи
     */
    @Override
    public GamePhase getPhase() {
        return GamePhase.DEALING;
    }
}
