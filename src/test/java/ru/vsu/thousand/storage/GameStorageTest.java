package ru.vsu.thousand.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.vsu.thousand.bot.DefaultBotStrategy;
import ru.vsu.thousand.bot.MarriageEvaluatingBotStrategy;
import ru.vsu.thousand.bot.PlayerStrategy;
import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.state.GamePhase;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStorageTest {

    private List<Player> createPlayers() {
        return List.of(
                new Player("Игрок 1"),
                new Player("Игрок 2"),
                new Player("Игрок 3")
        );
    }

    @Test
    void shouldSerializeAndRestoreMidGameStateWithoutLoss() {
        GameEngine original = new GameEngine(createPlayers(), 424242L);
        original.start();

        for (int i = 0; i < 8; i++) {
            if (original.getPhase() != GamePhase.FINISHED) {
                original.runStep();
            }
        }

        GameStorage storage = new GameStorage();
        String json = storage.toJson(original);
        GameEngine restored = storage.fromJson(json);

        assertEquals(original.getPhase(), restored.getPhase());
        assertEquals(original.getRoundNumber(), restored.getRoundNumber());
        assertEquals(original.getContract(), restored.getContract());
        assertEquals(original.getTalon(), restored.getTalon());
        assertEquals(original.getTrumpSuit().getSuit(), restored.getTrumpSuit().getSuit());
        assertEquals(original.getEvents(), restored.getEvents());

        for (int i = 0; i < 3; i++) {
            Player origPlayer = original.getPlayers().get(i);
            Player restPlayer = restored.getPlayers().get(i);
            assertEquals(origPlayer.getName(), restPlayer.getName());
            assertEquals(origPlayer.getScore(), restPlayer.getScore());
            assertEquals(origPlayer.getBolts(), restPlayer.getBolts());
            assertEquals(
                    origPlayer.getConsecutiveFailedContracts(),
                    restPlayer.getConsecutiveFailedContracts()
            );
            assertEquals(origPlayer.getHand(), restPlayer.getHand());
        }
    }

    @Test
    void shouldPreserveAuctionProgressWhenSavedMidBidding() {
        GameEngine original = new GameEngine(createPlayers(), 33333L);
        original.start();

        while (original.getPhase() != GamePhase.BIDDING) {
            original.runStep();
        }

        original.runStep();
        original.runStep();

        assertEquals(GamePhase.BIDDING, original.getPhase());
        assertNotNull(original.getAuction().getHighestBid());

        GameStorage storage = new GameStorage();
        GameEngine restored = storage.fromJson(storage.toJson(original));

        assertEquals(GamePhase.BIDDING, restored.getPhase());
        assertEquals(
                original.getAuction().getHighestBid().getPoints(),
                restored.getAuction().getHighestBid().getPoints()
        );
        assertEquals(
                original.getAuction().getCurrentPlayer().getName(),
                restored.getAuction().getCurrentPlayer().getName()
        );
        assertEquals(
                original.getAuction().getConsecutivePasses(),
                restored.getAuction().getConsecutivePasses()
        );

        original.runUntilFinished(10000);
        restored.runUntilFinished(10000);

        assertEquals(original.getEvents(), restored.getEvents());
        assertEquals(original.getWinner().getName(), restored.getWinner().getName());
    }

    @Test
    void shouldContinueRestoredGameIdenticallyToOriginal() {
        GameEngine original = new GameEngine(createPlayers(), 99999L);
        original.start();

        while (original.getPhase() != GamePhase.DEALING || original.getRoundNumber() < 2) {
            original.runStep();
        }

        GameStorage storage = new GameStorage();
        GameEngine restored = storage.fromJson(storage.toJson(original));

        original.runUntilFinished(10000);
        restored.runUntilFinished(10000);

        assertEquals(GamePhase.FINISHED, restored.getPhase());
        assertEquals(original.getEvents(), restored.getEvents());
        assertEquals(original.getWinner().getName(), restored.getWinner().getName());

        for (int i = 0; i < 3; i++) {
            assertEquals(
                    original.getPlayers().get(i).getScore(),
                    restored.getPlayers().get(i).getScore()
            );
        }
    }

    @Test
    void shouldSaveAndLoadGameFromFile(@TempDir Path tempDir) throws IOException {
        GameEngine original = new GameEngine(createPlayers(), 12345L);
        original.runUntilFinished(10000);

        Path saveFile = tempDir.resolve("saved-game.json");
        GameStorage storage = new GameStorage();

        storage.saveToFile(original, saveFile);
        GameEngine loaded = storage.loadFromFile(saveFile);

        assertEquals(GamePhase.FINISHED, loaded.getPhase());
        assertEquals(original.getEvents(), loaded.getEvents());
        assertNotNull(loaded.getWinner());
        assertEquals(original.getWinner().getName(), loaded.getWinner().getName());
    }

    @Test
    void shouldReplayExactPositionFromSnapshotLog() {
        GameEngine original = new GameEngine(createPlayers(), 54321L);
        original.start();

        for (int i = 0; i < 12; i++) {
            if (original.getPhase() != GamePhase.FINISHED) {
                original.runStep();
            }
        }

        GameSnapshot snapshot = original.createSnapshot();
        GameStorage storage = new GameStorage();
        GameEngine replayed = storage.replayFromSnapshot(snapshot, 1000);

        assertEquals(original.getPhase(), replayed.getPhase());
        assertEquals(original.getRoundNumber(), replayed.getRoundNumber());
        assertEquals(original.getEvents(), replayed.getEvents());

        for (int i = 0; i < 3; i++) {
            assertEquals(
                    original.getPlayers().get(i).getScore(),
                    replayed.getPlayers().get(i).getScore()
            );
            assertEquals(
                    original.getPlayers().get(i).getHand(),
                    replayed.getPlayers().get(i).getHand()
            );
        }
    }

    @Test
    void shouldReplayGameWithCustomBotStrategies() {
        Player p1 = new Player("Умный 1");
        Player p2 = new Player("Умный 2");
        Player p3 = new Player("Базовый 3");

        Map<Player, PlayerStrategy> strategies = Map.of(
                p1, new MarriageEvaluatingBotStrategy(1.0),
                p2, new MarriageEvaluatingBotStrategy(0.9),
                p3, new DefaultBotStrategy()
        );

        GameEngine original = new GameEngine(List.of(p1, p2, p3), 88888L, strategies);
        original.runUntilFinished(15000);

        Map<String, Supplier<PlayerStrategy>> factories = Map.of(
                "Умный 1", () -> new MarriageEvaluatingBotStrategy(1.0),
                "Умный 2", () -> new MarriageEvaluatingBotStrategy(0.9),
                "Базовый 3", DefaultBotStrategy::new
        );

        GameStorage storage = new GameStorage();
        GameEngine replayed = storage.replayFromSnapshot(original.createSnapshot(), factories, 15000);

        assertEquals(GamePhase.FINISHED, replayed.getPhase());
        assertEquals(original.getEvents(), replayed.getEvents());
        assertEquals(original.getWinner().getName(), replayed.getWinner().getName());
    }

    @Test
    void shouldRejectBlankOrMalformedJson() {
        GameStorage storage = new GameStorage();

        assertThrows(IllegalArgumentException.class, () -> storage.fromJson("   "));
        assertThrows(IllegalArgumentException.class, () -> storage.fromJson("{broken json"));
    }

    @Test
    void shouldRejectInvalidReplayArguments() {
        GameEngine engine = new GameEngine(createPlayers(), 1L);
        GameSnapshot snapshot = engine.createSnapshot();
        GameStorage storage = new GameStorage();

        assertThrows(NullPointerException.class, () -> storage.replayFromSnapshot(null, 100));
        assertThrows(IllegalArgumentException.class, () -> storage.replayFromSnapshot(snapshot, 0));
        assertTrue(snapshot.events().isEmpty());
        assertFalse(snapshot.players().isEmpty());
    }
}
