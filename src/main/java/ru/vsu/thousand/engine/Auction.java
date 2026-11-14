package ru.vsu.thousand.engine;

import ru.vsu.thousand.model.Bid;
import ru.vsu.thousand.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Управляет торговлей игроков за прикуп.
 */
public class Auction {
    private final List<Player> players;
    private int currentPlayerIndex;
    private Bid highestBid;
    private int consecutivePasses;
    private boolean finished;

    /**
     * Создаёт торговлю с первым игроком в списке.
     *
     * @param players участники партии
     */
    public Auction(List<Player> players) {
        this(players, 0);
    }

    /**
     * Создаёт торговлю с указанным первым игроком.
     *
     * @param players участники партии
     * @param startingPlayerIndex индекс первого игрока
     */
    public Auction(List<Player> players, int startingPlayerIndex) {
        Objects.requireNonNull(players);

        if (players.size() != 3
                || players.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Для торговли нужны три игрока"
            );
        }

        if (startingPlayerIndex < 0
                || startingPlayerIndex >= players.size()) {
            throw new IllegalArgumentException(
                    "Некорректный индекс первого игрока"
            );
        }

        this.players = List.copyOf(players);
        this.currentPlayerIndex = startingPlayerIndex;
        this.highestBid = null;
        this.consecutivePasses = 0;
        this.finished = false;
    }

    /**
     * Принимает ставку текущего игрока.
     *
     * @param points размер ставки
     */
    public void makeBid(int points) {
        ensureActive();

        int minimumBid = highestBid == null
                ? 100
                : highestBid.getPoints() + 5;

        if (points < minimumBid || points % 5 != 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть не меньше "
                            + minimumBid + " и кратна 5"
            );
        }

        highestBid = new Bid(getCurrentPlayer(), points);
        consecutivePasses = 0;
        moveToNextPlayer();
    }

    /** Фиксирует пас текущего игрока. */
    public void pass() {
        ensureActive();

        consecutivePasses++;

        if (consecutivePasses >= (highestBid == null ? 3 : 2)) {
            finished = true;
        } else {
            moveToNextPlayer();
        }
    }

    /** @return текущий игрок */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /** @return наибольшая ставка или null */
    public Bid getHighestBid() {
        return highestBid;
    }

    /** @return победитель торговли или null */
    public Player getWinner() {
        return highestBid == null
                ? null
                : highestBid.getPlayer();
    }

    /** @return true, если торговля завершена */
    public boolean isFinished() {
        return finished;
    }

    private void moveToNextPlayer() {
        currentPlayerIndex =
                (currentPlayerIndex + 1) % players.size();
    }

    private void ensureActive() {
        if (finished) {
            throw new IllegalStateException(
                    "Торговля уже завершена"
            );
        }
    }
}