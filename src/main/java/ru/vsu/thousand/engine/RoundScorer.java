package ru.vsu.thousand.engine;

import ru.vsu.thousand.model.Player;

import java.util.Map;
import java.util.Objects;

/**
 * Подсчитывает очки игроков по итогам раунда.
 */
public class RoundScorer {

    /**
     * Подсчитывает очки по результатам заказа и взяток.
     *
     * @param declarer игрок, сделавший заказ
     * @param contract заказанное количество очков
     * @param pointsTaken количество очков, набранных каждым игроком
     * @return true, если заказ выполнен
     */
    public boolean scoreRound(
            Player declarer,
            int contract,
            Map<Player, Integer> pointsTaken
    ) {
        Objects.requireNonNull(declarer);
        Objects.requireNonNull(pointsTaken);

        if (contract < 100 || contract % 5 != 0) {
            throw new IllegalArgumentException(
                    "Заказ должен быть не меньше 100 и кратен 5"
            );
        }

        if (pointsTaken.size() != 3
                || !pointsTaken.containsKey(declarer)) {
            throw new IllegalArgumentException(
                    "Необходимо передать очки трёх игроков, включая заказчика"
            );
        }

        for (Map.Entry<Player, Integer> entry : pointsTaken.entrySet()) {
            Objects.requireNonNull(entry.getKey());
            Objects.requireNonNull(entry.getValue());

            if (entry.getValue() < 0) {
                throw new IllegalArgumentException(
                        "Количество набранных очков не может быть отрицательным"
                );
            }
        }

        boolean successful = pointsTaken.get(declarer) >= contract;

        if (successful) {
            declarer.addScore(contract);
        } else {
            declarer.addScore(-contract);
        }

        declarer.recordContractResult(successful);

        for (Map.Entry<Player, Integer> entry : pointsTaken.entrySet()) {
            Player player = entry.getKey();

            if (player != declarer && player.getScore() < 880) {
                player.addScore(entry.getValue());
            }
        }

        return successful;
    }
}