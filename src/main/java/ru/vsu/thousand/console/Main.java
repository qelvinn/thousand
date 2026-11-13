package ru.vsu.thousand.console;

import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Player;

import java.util.List;

/**
 * Консольное приложение для запуска демонстрационной партии в игру «Тысяча».
 */
public class Main {

    /**
     * Точка входа в консольное приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        long seed = 12345L;

        List<Player> players = List.of(
                new Player("Игрок 1"),
                new Player("Игрок 2"),
                new Player("Игрок 3")
        );

        GameEngine game = new GameEngine(players, seed);

        game.runUntilFinished(10000);

        System.out.println("=== ИГРА «ТЫСЯЧА» ===");
        System.out.println("Seed: " + seed);
        System.out.println();

        System.out.println("=== ЖУРНАЛ ПАРТИИ ===");
        List<String> events = game.getEvents();
        int start = Math.max(0, events.size() - 40);

        for (int i = start; i < events.size(); i++) {
            System.out.println(events.get(i));
        }

        System.out.println();
        System.out.println("=== ИТОГИ ===");

        for (Player player : game.getPlayers()) {
            System.out.println(
                    player.getName()
                            + ": "
                            + player.getScore()
                            + " очков, болтов: "
                            + player.getBolts()
            );
        }

        System.out.println();
        Player winner = game.getWinner();
        System.out.println("Победитель: " + (winner != null ? winner.getName() : "не определён"));
    }
}
