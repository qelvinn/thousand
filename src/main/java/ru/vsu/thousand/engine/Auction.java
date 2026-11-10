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
     * Создаёт торговлю для трёх игроков.
     *
     * @param players участники партии
     */
    public Auction(List<Player> players) {
        Objects.requireNonNull(players);

        if (players.size() != 3 || players.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Для торговли нужны три игрока");
        }

        this.players = List.copyOf(players);
        this.currentPlayerIndex = 0;
        this.highestBid = null;
        this.consecutivePasses = 0;
        this.finished = false;
    }

    /**
     * Принимает ставку текущего игрока.
     *
     * @param points размер ставки
     * @throws IllegalStateException если торговля завершена
     * @throws IllegalArgumentException если ставка недостаточно высока
     */
    public void makeBid(int points) {
        ensureActive();

        int minimumBid = highestBid == null ? 100 : highestBid.getPoints() + 5;

        if (points < minimumBid || points % 5 != 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть не меньше " + minimumBid + " и кратна 5"
            );
        }

        highestBid = new Bid(getCurrentPlayer(), points);
        consecutivePasses = 0;
        moveToNextPlayer();
    }

    /**
     * Фиксирует пас текущего игрока.
     *
     * @throws IllegalStateException если торговля завершена
     */
    public void pass() {
        ensureActive();

        consecutivePasses++;

        if (consecutivePasses >= (highestBid == null ? 3 : 2)) {
            finished = true;
        } else {
            moveToNextPlayer();
        }
    }

    /**
     * Возвращает текущего игрока.
     *
     * @return игрок, который должен сделать ход
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Возвращает наибольшую ставку.
     *
     * @return наибольшая ставка или null, если ставок ещё не было
     */
    public Bid getHighestBid() {
        return highestBid;
    }

    /**
     * Возвращает победителя торговли.
     *
     * @return игрок, предложивший наибольшую ставку, или null, если все спасовали
     */
    public Player getWinner() {
        return highestBid == null ? null : highestBid.getPlayer();
    }

    /**
     * Проверяет, завершена ли торговля.
     *
     * @return true, если торговля завершена
     */
    public boolean isFinished() {
        return finished;
    }

    private void moveToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private void ensureActive() {
        if (finished) {
            throw new IllegalStateException("Торговля уже завершена");
        }
    }
}