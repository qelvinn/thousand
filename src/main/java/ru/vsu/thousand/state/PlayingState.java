package ru.vsu.thousand.state;

import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.engine.Trick;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.rules.GameRules;
import ru.vsu.thousand.util.CardComparators;

import java.util.List;

/**
 * Управляет розыгрышем семи взяток.
 */
public class PlayingState implements GameState {

    /**
     * Проводит розыгрыш взяток раунда и фиксирует набранные очки.
     *
     * @param gameEngine игровой движок
     */
    @Override
    public void handle(GameEngine gameEngine) {
        Player leader = gameEngine.getDeclarer();

        for (int trickNumber = 1; trickNumber <= GameRules.HAND_SIZE; trickNumber++) {
            Trick trick = new Trick(
                    gameEngine.getPlayers(),
                    leader,
                    gameEngine.getTrumpSuit()
            );

            for (int turn = 0; turn < GameRules.PLAYER_COUNT; turn++) {
                Player current = trick.getCurrentPlayer();
                Card card = chooseCard(current, trick, trickNumber, gameEngine);

                if (trickNumber > 1
                        && trick.getPlayedCards().isEmpty()
                        && (card.getRank() == Rank.KING
                        || card.getRank() == Rank.QUEEN)) {

                    Suit suit = card.getSuit();

                    if (hasMarriage(current, suit)
                            && !gameEngine.getMarriageManager().isDeclared(suit)) {
                        int points = gameEngine.getMarriageManager()
                                .declare(current, suit);
                        gameEngine.addRoundPoints(current, points);
                        gameEngine.addEvent(
                                current.getName() + " объявил марьяж "
                                        + suit.getDisplayName()
                                        + " и получил " + points + " очков"
                        );
                    }
                }

                trick.playCard(current, card);
                gameEngine.addEvent(
                        current.getName() + " сыграл " + card
                );
            }

            Player winner = trick.getWinner();
            int points = trick.getPoints();

            gameEngine.addRoundPoints(winner, points);
            gameEngine.addEvent(
                    "Взятку " + trickNumber + " выиграл "
                            + winner.getName() + ", очки: " + points
            );

            leader = winner;
        }

        gameEngine.addEvent("Розыгрыш завершён");

        for (Player player : gameEngine.getPlayers()) {
            gameEngine.addEvent(
                    player.getName() + " набрал за раунд "
                            + gameEngine.getRoundPoints().get(player)
                            + " очков"
            );
        }

        gameEngine.setState(new ScoringState());
    }

    private Card chooseCard(
            Player player,
            Trick trick,
            int trickNumber,
            GameEngine gameEngine
    ) {
        List<Card> hand = player.getHand().stream()
                .sorted(CardComparators.BY_RANK_THEN_SUIT)
                .toList();

        if (trick.getPlayedCards().isEmpty()) {
            if (trickNumber > 1) {
                for (Card card : hand) {
                    if ((card.getRank() == Rank.QUEEN || card.getRank() == Rank.KING)
                            && hasMarriage(player, card.getSuit())
                            && !gameEngine.getMarriageManager().isDeclared(card.getSuit())) {
                        return card;
                    }
                }
            }
            return hand.get(hand.size() - 1);
        }

        Suit leadingSuit = trick.getPlayedCards()
                .get(0).card().getSuit();

        for (Card card : hand) {
            if (card.getSuit() == leadingSuit) {
                return card;
            }
        }

        return hand.get(0);
    }

    private boolean hasMarriage(Player player, Suit suit) {
        boolean hasKing = player.getHand().contains(new Card(suit, Rank.KING));
        boolean hasQueen = player.getHand().contains(new Card(suit, Rank.QUEEN));
        return hasKing && hasQueen;
    }

    /**
     * Возвращает фазу розыгрыша взяток.
     *
     * @return фаза розыгрыша
     */
    @Override
    public GamePhase getPhase() {
        return GamePhase.PLAYING;
    }
}
