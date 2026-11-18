package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;

import java.util.List;

/**
 * Управляет сносом трёх карт после получения прикупа.
 */
public class DiscardingState implements GameState {

    /**
     * Передаёт выбор трёх карт для сноса стратегии заказчика.
     *
     * @param gameEngine игровой движок
     */
    @Override
    public void handle(GameEngine gameEngine) {
        Player declarer = gameEngine.getDeclarer();

        List<Card> discarded = gameEngine.getStrategy(declarer)
                .chooseDiscard(declarer, gameEngine.getContract());

        gameEngine.getTalonExchange().discard(declarer, discarded);

        gameEngine.addEvent(
                declarer.getName() + " снёс три карты"
        );

        gameEngine.setState(new PlayingState());
    }

    /**
     * Возвращает фазу сноса карт.
     *
     * @return фаза сноса
     */
    @Override
    public GamePhase getPhase() {
        return GamePhase.DISCARDING;
    }
}
