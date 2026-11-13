package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Player;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoundScorerTest {

    @Test
    void successfulContractAddsContractPoints() {
        Player declarer = new Player("Заказчик");
        RoundScorer scorer = new RoundScorer();

        boolean result = scorer.scoreRound(
                declarer, 100,
                Map.of(declarer, 110,
                        new Player("Второй"), 50,
                        new Player("Третий"), 40)
        );

        assertTrue(result);
        assertEquals(100, declarer.getScore());
    }

    @Test
    void failedContractSubtractsContractPoints() {
        Player declarer = new Player("Заказчик");
        RoundScorer scorer = new RoundScorer();

        boolean result = scorer.scoreRound(
                declarer, 120,
                Map.of(declarer, 100,
                        new Player("Второй"), 50,
                        new Player("Третий"), 50)
        );

        assertFalse(result);
        assertEquals(-120, declarer.getScore());
    }

    @Test
    void successfulContractResetsFailedStreak() {
        Player declarer = new Player("Заказчик");
        RoundScorer scorer = new RoundScorer();

        declarer.recordContractResult(false);
        declarer.recordContractResult(false);

        scorer.scoreRound(
                declarer, 100,
                Map.of(declarer, 100,
                        new Player("Второй"), 50,
                        new Player("Третий"), 50)
        );

        assertEquals(0, declarer.getConsecutiveFailedContracts());
        assertEquals(0, declarer.getBolts());
    }

    @Test
    void thirdFailedContractAddsBolt() {
        Player declarer = new Player("Заказчик");
        RoundScorer scorer = new RoundScorer();

        for (int i = 0; i < 3; i++) {
            scorer.scoreRound(
                    declarer, 100,
                    Map.of(declarer, 0,
                            new Player("Второй"), 100,
                            new Player("Третий"), 100)
            );
        }

        assertEquals(1, declarer.getBolts());
        assertEquals(0, declarer.getConsecutiveFailedContracts());
    }

    @Test
    void playerBelow880ReceivesTrickPoints() {
        Player declarer = new Player("Заказчик");
        Player second = new Player("Второй");
        Player third = new Player("Третий");

        new RoundScorer().scoreRound(
                declarer, 100,
                Map.of(declarer, 100, second, 60, third, 40)
        );

        assertEquals(60, second.getScore());
        assertEquals(40, third.getScore());
    }

    @Test
    void playerAt880DoesNotReceivePointsAsNonDeclarer() {
        Player declarer = new Player("Заказчик");
        Player second = new Player("Второй");
        Player third = new Player("Третий");

        second.addScore(880);

        new RoundScorer().scoreRound(
                declarer, 100,
                Map.of(declarer, 100, second, 60, third, 40)
        );

        assertEquals(880, second.getScore());
        assertEquals(40, third.getScore());
    }

    @Test
    void invalidContractIsRejected() {
        Player declarer = new Player("Заказчик");

        assertThrows(
                IllegalArgumentException.class,
                () -> new RoundScorer().scoreRound(
                        declarer, 95,
                        Map.of(declarer, 100,
                                new Player("Второй"), 50,
                                new Player("Третий"), 50)
                )
        );
    }

    @Test
    void negativeTrickPointsAreRejected() {
        Player declarer = new Player("Заказчик");

        assertThrows(
                IllegalArgumentException.class,
                () -> new RoundScorer().scoreRound(
                        declarer, 100,
                        Map.of(declarer, 100,
                                new Player("Второй"), -10,
                                new Player("Третий"), 50)
                )
        );
    }

    @Test
    void nullPointsMapIsRejected() {
        Player declarer = new Player("Заказчик");

        assertThrows(
                NullPointerException.class,
                () -> new RoundScorer().scoreRound(declarer, 100, null)
        );
    }
}