
package ru.vsu.thousand.engine;

import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Управляет розыгрышем одной взятки в игре «Тысяча».
 */
public class Trick {

    /**
     * Представляет карту, сыгранную конкретным игроком.
     *
     * @param player игрок, сыгравший карту
     * @param card сыгранная карта
     */
    public record PlayedCard(Player player, Card card) {
    }

    private final List<Player> players;
    private final TrumpSuit trumpSuit;
    private final List<PlayedCard> playedCards;
    private int currentPlayerIndex;

    /**
     * Создаёт взятку для трёх игроков.
     *
     * @param players участники в порядке ходов
     * @param startingPlayer игрок, который ходит первым
     * @param trumpSuit текущий козырь
     */
    public Trick(List<Player> players, Player startingPlayer, TrumpSuit trumpSuit) {
        Objects.requireNonNull(players);
        Objects.requireNonNull(startingPlayer);
        this.trumpSuit = Objects.requireNonNull(trumpSuit);

        if (players.size() != 3
                || players.stream().anyMatch(Objects::isNull)
                || players.stream().distinct().count() != 3) {
            throw new IllegalArgumentException("Для взятки нужны три разных игрока");
        }

        this.players = List.copyOf(players);
        this.currentPlayerIndex = this.players.indexOf(startingPlayer);

        if (currentPlayerIndex < 0) {
            throw new IllegalArgumentException("Первый игрок должен участвовать во взятке");
        }

        this.playedCards = new ArrayList<>();
    }

    /**
     * Возвращает игрока, который должен ходить следующим.
     *
     * @return текущий игрок
     * @throws IllegalStateException если взятка завершена
     */
    public Player getCurrentPlayer() {
        if (isComplete()) {
            throw new IllegalStateException("Взятка уже завершена");
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Разыгрывает карту текущего игрока.
     *
     * @param player игрок, совершающий ход
     * @param card карта для розыгрыша
     * @throws IllegalStateException если взятка завершена или сейчас ход другого игрока
     * @throws IllegalArgumentException если карты нет на руке или нарушено правило масти
     */
    public void playCard(Player player, Card card) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(card);

        if (isComplete()) {
            throw new IllegalStateException("Взятка уже завершена");
        }

        if (player != getCurrentPlayer()) {
            throw new IllegalStateException("Сейчас ход другого игрока");
        }

        if (!player.getHand().contains(card)) {
            throw new IllegalArgumentException("У игрока нет такой карты");
        }

        if (!playedCards.isEmpty()) {
            Suit leadingSuit = playedCards.get(0).card().getSuit();

            boolean hasLeadingSuit = player.getHand().stream()
                    .anyMatch(handCard -> handCard.getSuit() == leadingSuit);

            if (hasLeadingSuit && card.getSuit() != leadingSuit) {
                throw new IllegalArgumentException(
                        "Необходимо положить карту ведущей масти"
                );
            }
        }

        player.removeCard(card);
        playedCards.add(new PlayedCard(player, card));
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Проверяет, завершён ли розыгрыш взятки.
     *
     * @return true, если все три игрока сделали ход
     */
    public boolean isComplete() {
        return playedCards.size() == players.size();
    }

    /**
     * Возвращает победителя взятки.
     *
     * @return игрок, выигравший взятку
     * @throws IllegalStateException если взятка ещё не завершена
     */
    public Player getWinner() {
        if (!isComplete()) {
            throw new IllegalStateException("Взятка ещё не завершена");
        }

        PlayedCard bestPlay = playedCards.get(0);

        for (int i = 1; i < playedCards.size(); i++) {
            PlayedCard candidate = playedCards.get(i);

            if (beats(candidate.card(), bestPlay.card())) {
                bestPlay = candidate;
            }
        }

        return bestPlay.player();
    }

    /**
     * Возвращает сумму очков карт во взятке.
     *
     * @return количество очков
     */
    public int getPoints() {
        return playedCards.stream()
                .mapToInt(play -> play.card().getPoints())
                .sum();
    }

    /**
     * Возвращает список сыгранных карт.
     *
     * @return неизменяемый список сыгранных карт
     */
    public List<PlayedCard> getPlayedCards() {
        return List.copyOf(playedCards);
    }

    private boolean beats(Card candidate, Card currentBest) {
        int candidateCategory = getCategory(candidate);
        int bestCategory = getCategory(currentBest);

        if (candidateCategory != bestCategory) {
            return candidateCategory > bestCategory;
        }

        if (candidate.getSuit() != currentBest.getSuit()) {
            return false;
        }

        return candidate.getRank().getStrength()
                > currentBest.getRank().getStrength();
    }

    private int getCategory(Card card) {
        if (trumpSuit.isTrump(card.getSuit())) {
            return 2;
        }

        if (card.getSuit() == playedCards.get(0).card().getSuit()) {
            return 1;
        }

        return 0;
    }
}
