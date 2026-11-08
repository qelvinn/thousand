package ru.vsu.thousand.model;

import java.util.Objects;

/**
 * Представляет ставку игрока во время торговли за прикуп.
 */
public class Bid {
    private final Player player;
    private final int points;

    /**
     * Создаёт ставку игрока.
     *
     * @param player игрок, сделавший ставку
     * @param points количество заказанных очков
     */
    public Bid(Player player, int points) {
        this.player = Objects.requireNonNull(player);

        if (points < 100 || points % 5 != 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть не меньше 100 и кратна 5"
            );
        }

        this.points = points;
    }

    /**
     * Возвращает игрока, сделавшего ставку.
     *
     * @return игрок
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Возвращает количество заказанных очков.
     *
     * @return размер ставки
     */
    public int getPoints() {
        return points;
    }
}