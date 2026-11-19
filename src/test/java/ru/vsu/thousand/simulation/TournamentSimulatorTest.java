package ru.vsu.thousand.simulation;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.bot.DefaultBotStrategy;
import ru.vsu.thousand.bot.MarriageEvaluatingBotStrategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentSimulatorTest {

    private TournamentSimulator createDefaultSimulator() {
        return new TournamentSimulator(
                List.of("Умный бот 1", "Умный бот 2", "Базовый бот"),
                List.of(
                        () -> new MarriageEvaluatingBotStrategy(1.0),
                        () -> new MarriageEvaluatingBotStrategy(0.9),
                        DefaultBotStrategy::new
                )
        );
    }

    @Test
    void shouldSimulateThousandGamesAndCollectContractStatistics() {
        TournamentSimulator simulator = createDefaultSimulator();

        ContractStatistics stats = simulator.runParallel(
                TournamentSimulator.DEFAULT_GAMES_COUNT,
                1000L,
                4
        );

        assertEquals(1000, stats.totalGames());
        assertTrue(stats.totalContracts() > 1000);
        assertEquals(
                stats.totalContracts(),
                stats.successfulContracts() + stats.failedContracts()
        );
        assertTrue(stats.averageContractSize() >= 100.0);
        assertTrue(stats.getSuccessRatePercent() > 0.0 && stats.getSuccessRatePercent() <= 100.0);
        assertFalse(stats.contractSizeDistribution().isEmpty());

        long totalWins = stats.winsByPlayer().values().stream()
                .mapToLong(Long::longValue)
                .sum();
        assertEquals(1000L, totalWins);
    }

    @Test
    void shouldProduceIdenticalResultsInSequentialAndParallelModes() {
        TournamentSimulator simulator = createDefaultSimulator();

        ContractStatistics sequential = simulator.runSequential(120, 5555L);
        ContractStatistics parallel = simulator.runParallel(120, 5555L, 4);

        assertEquals(sequential, parallel);
    }

    @Test
    void shouldAggregateStatisticsViaStreamApiCorrectly() {
        List<ContractStatistics.GameOutcome> outcomes = List.of(
                new ContractStatistics.GameOutcome(
                        0,
                        1L,
                        "Игрок А",
                        List.of(
                                new ContractStatistics.ContractRecord("Игрок А", 100, true),
                                new ContractStatistics.ContractRecord("Игрок Б", 120, false)
                        )
                ),
                new ContractStatistics.GameOutcome(
                        1,
                        2L,
                        "Игрок А",
                        List.of(
                                new ContractStatistics.ContractRecord("Игрок А", 110, true)
                        )
                )
        );

        ContractStatistics stats = ContractStatistics.fromOutcomes(outcomes);

        assertEquals(2, stats.totalGames());
        assertEquals(3L, stats.totalContracts());
        assertEquals(2L, stats.successfulContracts());
        assertEquals(1L, stats.failedContracts());
        assertEquals(110.0, stats.averageContractSize(), 0.001);
        assertEquals(2L, stats.winsByPlayer().get("Игрок А"));
        assertEquals(2L, stats.contractsByPlayer().get("Игрок А"));
        assertEquals(2L, stats.successfulContractsByPlayer().get("Игрок А"));
        assertEquals(1L, stats.contractsByPlayer().get("Игрок Б"));
        assertEquals(1L, stats.contractSizeDistribution().get(100));
        assertEquals(1L, stats.contractSizeDistribution().get(110));
        assertEquals(1L, stats.contractSizeDistribution().get(120));
    }

    @Test
    void shouldRejectInvalidSimulatorConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TournamentSimulator(
                        List.of("Игрок 1", "Игрок 1", "Игрок 3"),
                        List.of(DefaultBotStrategy::new, DefaultBotStrategy::new, DefaultBotStrategy::new)
                )
        );

        TournamentSimulator simulator = createDefaultSimulator();

        assertThrows(IllegalArgumentException.class, () -> simulator.runSequential(0, 1L));
        assertThrows(IllegalArgumentException.class, () -> simulator.runParallel(10, 1L, 0));
    }
}