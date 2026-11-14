package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.state.GamePhase;
import ru.vsu.thousand.util.CardComparators;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private List<Player> createPlayers() {
        return List.of(
                new Player("Игрок 1"),
                new Player("Игрок 2"),
                new Player("Игрок 3")
        );
    }

    @Test
    void shouldStartInInitializationPhase() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);

        assertEquals(GamePhase.INITIALIZATION, engine.getPhase());

        engine.start();

        assertEquals(GamePhase.INITIALIZATION, engine.getPhase());
    }

    @Test
    void shouldFinishFullGameWithFixedSeed() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);

        engine.runUntilFinished(10000);

        assertEquals(GamePhase.FINISHED, engine.getPhase());
        assertNotNull(engine.getWinner());
        assertTrue(engine.getWinner().getScore() >= 1000);
        assertFalse(engine.getEvents().isEmpty());
    }

    @Test
    void shouldDeclareMarriagesDuringGameSimulation() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);

        engine.runUntilFinished(10000);

        boolean hasMarriageEvent = engine.getEvents().stream()
                .anyMatch(event -> event.contains("объявил марьяж"));

        assertTrue(hasMarriageEvent);
    }

    @Test
    void shouldRejectInvalidStepLimit() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.runUntilFinished(0)
        );
    }

    @Test
    void shouldProduceSameResultWithSameSeed() {
        GameEngine first = new GameEngine(createPlayers(), 12345L);
        GameEngine second = new GameEngine(createPlayers(), 12345L);

        first.runUntilFinished(10000);
        second.runUntilFinished(10000);

        assertEquals(first.getEvents(), second.getEvents());

        for (int i = 0; i < 3; i++) {
            assertEquals(
                    first.getPlayers().get(i).getScore(),
                    second.getPlayers().get(i).getScore()
            );
        }
    }

    @Test
    void shouldRejectDoubleStart() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);
        engine.start();

        assertThrows(IllegalStateException.class, engine::start);
    }

    @Test
    void shouldRejectStepBeforeStart() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);

        assertThrows(IllegalStateException.class, engine::runStep);
    }

    @Test
    void shouldRejectStepAfterFinished() {
        GameEngine engine = new GameEngine(createPlayers(), 12345L);
        engine.runUntilFinished(10000);

        assertThrows(IllegalStateException.class, engine::runStep);
    }

    @Test
    void shouldRejectDuplicatePlayers() {
        Player player = new Player("Игрок 1");

        assertThrows(
                IllegalArgumentException.class,
                () -> new GameEngine(List.of(player, player, new Player("Игрок 2")), 1L)
        );
    }

    @Test
    void shouldSortCardsUsingComparators() {
        Card aceSpades = new Card(Suit.SPADES, Rank.ACE);
        Card nineHearts = new Card(Suit.HEARTS, Rank.NINE);
        Card kingSpades = new Card(Suit.SPADES, Rank.KING);

        List<Card> byRank = new ArrayList<>(List.of(aceSpades, nineHearts, kingSpades));
        byRank.sort(CardComparators.BY_RANK_THEN_SUIT);
        assertEquals(List.of(nineHearts, kingSpades, aceSpades), byRank);

        List<Card> bySuit = new ArrayList<>(List.of(nineHearts, aceSpades, kingSpades));
        bySuit.sort(CardComparators.BY_SUIT_THEN_RANK);
        assertEquals(List.of(kingSpades, aceSpades, nineHearts), bySuit);
    }
}
