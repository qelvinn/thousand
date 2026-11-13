package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Player;

import java.util.Map;

/**
 * Подсчитывает результаты раунда и проверяет окончание партии.
 */
public class ScoringState implements GameState {

    @Override
    public void handle(GameEngine gameEngine) {
        Player declarer = gameEngine.getDeclarer();

        if (declarer == null) {
            gameEngine.addEvent("Раунд пропущен: все игроки спасовали");
            gameEngine.setState(new DealingState());
            return;
        }

        Map<Player, Integer> points = gameEngine.getRoundPoints();

        boolean successful = gameEngine.getRoundScorer().scoreRound(
                declarer,
                gameEngine.getContract(),
                points
        );

        gameEngine.addEvent(
                successful ? "Заказ выполнен" : "Заказ не выполнен"
        );

        for (Player player : gameEngine.getPlayers()) {
            gameEngine.addEvent(
                    player.getName()
                            + ": " + player.getScore()
                            + " очков, болтов: " + player.getBolts()
            );
        }

        Player winner = gameEngine.getWinner();

        if (winner != null) {
            gameEngine.addEvent(
                    "Победитель партии: " + winner.getName()
            );
            gameEngine.setState(new FinishedState());
        } else {
            gameEngine.setState(new DealingState());
        }
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.SCORING;
    }
}