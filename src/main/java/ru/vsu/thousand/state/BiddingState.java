package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.model.Bid;
import ru.vsu.thousand.model.Player;

/**
 * Управляет фазой торговли за прикуп.
 */
public class BiddingState implements GameState {

    @Override
    public void handle(GameEngine gameEngine) {
        if (gameEngine.getAuction().isFinished()) {
            Player winner = gameEngine.getAuction().getWinner();

            if (winner == null) {
                gameEngine.addEvent("Все игроки спасовали. Раунд пропущен");
                gameEngine.setState(new ScoringState());
                return;
            }

            Bid bid = gameEngine.getAuction().getHighestBid();

            gameEngine.setContract(bid.getPoints());
            gameEngine.setDeclarer(winner);
            gameEngine.getTalonExchange().takeTalon(winner, gameEngine.getTalon());

            gameEngine.addEvent(
                    winner.getName() + " выиграл торговлю с заказом "
                            + bid.getPoints()
            );

            gameEngine.setState(new DiscardingState());
            return;
        }

        Player player = gameEngine.getAuction().getCurrentPlayer();

        if (gameEngine.getAuction().getHighestBid() == null) {
            gameEngine.getAuction().makeBid(100);
            gameEngine.addEvent(player.getName() + " сделал заказ 100");
        } else {
            gameEngine.getAuction().pass();
            gameEngine.addEvent(player.getName() + " спасовал");
        }
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.BIDDING;
    }
}