package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.util.CardComparators;

import java.util.ArrayList;
import java.util.List;

/**
 * Управляет сносом трёх карт после получения прикупа.
 */
public class DiscardingState implements GameState {

    @Override
    public void handle(GameEngine gameEngine) {
        Player declarer = gameEngine.getDeclarer();

        List<Card> cards = new ArrayList<>(declarer.getHand());
        cards.sort(CardComparators.BY_RANK_THEN_SUIT);

        List<Card> discarded = new ArrayList<>(
                cards.subList(0, 3)
        );

        gameEngine.getTalonExchange().discard(declarer, discarded);

        gameEngine.addEvent(
                declarer.getName() + " снёс три карты"
        );

        gameEngine.setState(new PlayingState());
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.DISCARDING;
    }
}