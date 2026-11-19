package ru.vsu.thousand.console;

import ru.vsu.thousand.bot.DefaultBotStrategy;
import ru.vsu.thousand.bot.MarriageEvaluatingBotStrategy;
import ru.vsu.thousand.bot.PlayerStrategy;
import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.simulation.ContractStatistics;
import ru.vsu.thousand.simulation.TournamentSimulator;
import ru.vsu.thousand.storage.GameStorage;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Консольное приложение для демонстрации партии, сериализации состояния
 * и симуляции тысячи партий в игру «Тысяча».
 */
public class Main {

    /**
     * Точка входа в консольное приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        runSingleDemoGame(12345L);
        System.out.println();
        runThousandGamesSimulation(1000L);
    }

    private static void runSingleDemoGame(long seed) {
        Player bot1 = new Player("Бот-стратег 1");
        Player bot2 = new Player("Бот-стратег 2");
        Player bot3 = new Player("Базовый бот 3");

        List<Player> players = List.of(bot1, bot2, bot3);
        Map<Player, PlayerStrategy> strategies = Map.of(
                bot1, new MarriageEvaluatingBotStrategy(1.0),
                bot2, new MarriageEvaluatingBotStrategy(0.9),
                bot3, new DefaultBotStrategy()
        );

        GameEngine game = new GameEngine(players, seed, strategies);
        game.runUntilFinished(15000);

        GameStorage storage = new GameStorage();
        String json = storage.toJson(game);
        GameEngine restored = storage.fromJson(json);

        System.out.println("=== ДЕМОНСТРАЦИОННАЯ ПАРТИЯ «ТЫСЯЧА» ===");
        System.out.println("Seed: " + seed);
        System.out.println("Размер JSON-снимка партии: " + json.length() + " символов");
        System.out.println("Состояние после восстановления совпадает: "
                + restored.getEvents().equals(game.getEvents()));
        System.out.println();

        System.out.println("=== ПОСЛЕДНИЕ СОБЫТИЯ ЖУРНАЛА ===");
        List<String> events = restored.getEvents();
        int start = Math.max(0, events.size() - 25);

        for (int i = start; i < events.size(); i++) {
            System.out.println(events.get(i));
        }

        System.out.println();
        System.out.println("=== ИТОГИ ДЕМОНСТРАЦИОННОЙ ПАРТИИ ===");

        for (Player player : restored.getPlayers()) {
            System.out.println(
                    player.getName()
                            + ": "
                            + player.getScore()
                            + " очков, болтов: "
                            + player.getBolts()
            );
        }

        Player winner = restored.getWinner();
        System.out.println("Победитель: " + (winner != null ? winner.getName() : "не определён"));
    }

    private static void runThousandGamesSimulation(long baseSeed) {
        List<String> names = List.of("Бот-стратег 1", "Бот-стратег 2", "Базовый бот 3");
        TournamentSimulator simulator = new TournamentSimulator(
                names,
                List.of(
                        () -> new MarriageEvaluatingBotStrategy(1.0),
                        () -> new MarriageEvaluatingBotStrategy(0.9),
                        DefaultBotStrategy::new
                )
        );

        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ContractStatistics stats = simulator.runParallel(
                TournamentSimulator.DEFAULT_GAMES_COUNT,
                baseSeed,
                threads
        );

        System.out.println("=== СИМУЛЯЦИЯ " + stats.totalGames() + " ПАРТИЙ ===");
        System.out.println("Всего разыграно заказов: " + stats.totalContracts());
        System.out.println("Выполнено заказов: " + stats.successfulContracts());
        System.out.println("Не выполнено заказов: " + stats.failedContracts());
        System.out.printf(
                Locale.US,
                "Процент успешных заказов: %.2f%%%n",
                stats.getSuccessRatePercent()
        );
        System.out.printf(
                Locale.US,
                "Средний размер заказа: %.2f очков%n",
                stats.averageContractSize()
        );
        System.out.println();

        System.out.println("=== СТАТИСТИКА ПО ИГРОКАМ ===");
        for (String name : names) {
            long wins = stats.winsByPlayer().getOrDefault(name, 0L);
            long contracts = stats.contractsByPlayer().getOrDefault(name, 0L);
            long successful = stats.successfulContractsByPlayer().getOrDefault(name, 0L);
            System.out.println(
                    name + " -> побед: " + wins
                            + ", заказов: " + contracts
                            + ", выполнено: " + successful
            );
        }

        System.out.println();
        System.out.println("=== РАСПРЕДЕЛЕНИЕ ЗАКАЗОВ ПО НОМИНАЛАМ ===");
        Map<Integer, Long> sortedDistribution = new TreeMap<>(stats.contractSizeDistribution());
        for (Map.Entry<Integer, Long> entry : sortedDistribution.entrySet()) {
            System.out.println("Заказ " + entry.getKey() + ": " + entry.getValue() + " раз");
        }
    }
}