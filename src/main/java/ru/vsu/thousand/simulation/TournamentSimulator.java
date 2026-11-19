package ru.vsu.thousand.simulation;

import ru.vsu.thousand.bot.PlayerStrategy;
import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.rules.GameRules;
import ru.vsu.thousand.state.GamePhase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Выполняет серию симуляций партий в последовательном или параллельном режиме
 * и собирает статистику по заказам.
 */
public class TournamentSimulator {

    /** Стандартное количество партий для массовой симуляции. */
    public static final int DEFAULT_GAMES_COUNT = 1000;

    private static final int MAX_STEPS_PER_GAME = 20000;

    private final List<String> playerNames;
    private final List<Supplier<PlayerStrategy>> strategyFactories;

    /**
     * Создаёт симулятор для трёх участников с фабриками стратегий для каждого места.
     *
     * @param playerNames имена трёх игроков
     * @param strategyFactories фабрики создания экземпляров стратегий для каждого игрока
     */
    public TournamentSimulator(
            List<String> playerNames,
            List<Supplier<PlayerStrategy>> strategyFactories
    ) {
        Objects.requireNonNull(playerNames, "Список имён игроков не может быть null");
        Objects.requireNonNull(strategyFactories, "Список фабрик стратегий не может быть null");

        if (playerNames.size() != GameRules.PLAYER_COUNT
                || playerNames.stream().anyMatch(name -> name == null || name.isBlank())
                || playerNames.stream().distinct().count() != GameRules.PLAYER_COUNT) {
            throw new IllegalArgumentException("Необходимо передать три уникальных непустых имени игрока");
        }

        if (strategyFactories.size() != GameRules.PLAYER_COUNT
                || strategyFactories.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Необходимо передать три фабрики стратегий");
        }

        this.playerNames = List.copyOf(playerNames);
        this.strategyFactories = List.copyOf(strategyFactories);
    }

    /**
     * Запускает серию партий последовательно в одном потоке.
     *
     * @param gamesCount количество партий
     * @param baseSeed начальное значение генератора случайных чисел
     * @return агрегированная статистика по всем сыгранным партиям
     */
    public ContractStatistics runSequential(int gamesCount, long baseSeed) {
        validateGamesCount(gamesCount);

        List<ContractStatistics.GameOutcome> outcomes = IntStream.range(0, gamesCount)
                .mapToObj(index -> simulateSingleGame(index, baseSeed + index))
                .toList();

        return ContractStatistics.fromOutcomes(outcomes);
    }

    /**
     * Запускает серию партий параллельно с использованием пула потоков.
     *
     * @param gamesCount количество партий
     * @param baseSeed начальное значение генератора случайных чисел
     * @param threadCount количество рабочих потоков
     * @return агрегированная статистика по всем сыгранным партиям
     */
    public ContractStatistics runParallel(int gamesCount, long baseSeed, int threadCount) {
        validateGamesCount(gamesCount);
        if (threadCount < 1) {
            throw new IllegalArgumentException("Количество потоков должно быть положительным");
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<ContractStatistics.GameOutcome>> tasks = new ArrayList<>(gamesCount);
            for (int index = 0; index < gamesCount; index++) {
                final int gameIndex = index;
                final long seed = baseSeed + index;
                tasks.add(() -> simulateSingleGame(gameIndex, seed));
            }

            List<Future<ContractStatistics.GameOutcome>> futures = executor.invokeAll(tasks);
            List<ContractStatistics.GameOutcome> outcomes = new ArrayList<>(gamesCount);
            for (Future<ContractStatistics.GameOutcome> future : futures) {
                outcomes.add(future.get());
            }

            return ContractStatistics.fromOutcomes(outcomes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Параллельная симуляция была прервана", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Ошибка при выполнении партии в параллельной симуляции", e.getCause());
        } finally {
            executor.shutdown();
        }
    }

    private ContractStatistics.GameOutcome simulateSingleGame(int gameIndex, long seed) {
        List<Player> players = new ArrayList<>(GameRules.PLAYER_COUNT);
        Map<Player, PlayerStrategy> strategies = new LinkedHashMap<>();

        for (int i = 0; i < GameRules.PLAYER_COUNT; i++) {
            Player player = new Player(playerNames.get(i));
            players.add(player);
            strategies.put(player, Objects.requireNonNull(strategyFactories.get(i).get()));
        }

        GameEngine engine = new GameEngine(players, seed, strategies);
        engine.start();

        List<ContractStatistics.ContractRecord> contracts = new ArrayList<>();
        String pendingDeclarerName = null;
        int pendingContract = 0;

        int steps = 0;
        while (engine.getPhase() != GamePhase.FINISHED && steps < MAX_STEPS_PER_GAME) {
            GamePhase phaseBeforeStep = engine.getPhase();
            int eventsBeforeStep = engine.getEvents().size();
            engine.runStep();
            steps++;

            if (phaseBeforeStep == GamePhase.BIDDING && engine.getPhase() == GamePhase.DISCARDING) {
                pendingDeclarerName = engine.getDeclarer().getName();
                pendingContract = engine.getContract();
            } else if (phaseBeforeStep == GamePhase.SCORING && pendingDeclarerName != null) {
                List<String> scoringEvents = engine.getEvents()
                        .subList(eventsBeforeStep, engine.getEvents().size());
                boolean successful = scoringEvents.contains("Заказ выполнен");
                contracts.add(new ContractStatistics.ContractRecord(
                        pendingDeclarerName,
                        pendingContract,
                        successful
                ));
                pendingDeclarerName = null;
                pendingContract = 0;
            }
        }

        if (engine.getPhase() != GamePhase.FINISHED) {
            throw new IllegalStateException("Партия не завершилась за отведённое количество шагов");
        }

        Player winner = engine.getWinner();
        if (winner == null) {
            throw new IllegalStateException("Завершённая партия должна иметь победителя");
        }

        return new ContractStatistics.GameOutcome(
                gameIndex,
                seed,
                winner.getName(),
                contracts
        );
    }

    private void validateGamesCount(int gamesCount) {
        if (gamesCount < 1) {
            throw new IllegalArgumentException("Количество партий должно быть положительным");
        }
    }
}
