package ru.vsu.thousand.storage;

import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.state.GamePhase;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Представляет полный снимок состояния игровой партии для сериализации в JSON.
 *
 * @param seed              начальное значение генератора случайных чисел
 * @param roundNumber       номер текущего раунда
 * @param phase             текущая фаза партии
 * @param trumpSuit         текущая козырная масть или null
 * @param declaredMarriages масти объявленных в раунде марьяжей
 * @param declarerName      имя заказчика текущего раунда или null
 * @param contract          размер заказа текущего раунда
 * @param declarerPoints    очки заказчика в текущем раунде
 * @param talon             карты прикупа
 * @param players           состояние всех участников партии
 * @param roundPoints       очки игроков за текущий раунд
 * @param auction           состояние текущей торговли или null
 * @param events            журнал событий партии
 */
public record GameSnapshot(
        long seed,
        int roundNumber,
        GamePhase phase,
        Suit trumpSuit,
        List<Suit> declaredMarriages,
        String declarerName,
        int contract,
        int declarerPoints,
        List<CardSnapshot> talon,
        List<PlayerSnapshot> players,
        Map<String, Integer> roundPoints,
        AuctionSnapshot auction,
        List<String> events
) {

    /**
     * Проверяет корректность обязательных полей снимка партии.
     */
    public GameSnapshot {
        Objects.requireNonNull(phase, "Фаза партии не может быть null");
        declaredMarriages = declaredMarriages == null ? List.of() : List.copyOf(declaredMarriages);
        talon = talon == null ? List.of() : List.copyOf(talon);
        players = List.copyOf(Objects.requireNonNull(players, "Список игроков не может быть null"));
        roundPoints = roundPoints == null ? Map.of() : Map.copyOf(roundPoints);
        events = events == null ? List.of() : List.copyOf(events);
    }

    /**
     * Представляет снимок отдельной игральной карты.
     *
     * @param suit масть карты
     * @param rank достоинство карты
     */
    public record CardSnapshot(Suit suit, Rank rank) {

        /**
         * Проверяет корректность полей карты.
         */
        public CardSnapshot {
            Objects.requireNonNull(suit, "Масть карты не может быть null");
            Objects.requireNonNull(rank, "Достоинство карты не может быть null");
        }
    }

    /**
     * Представляет снимок состояния отдельного игрока.
     *
     * @param name                       имя игрока
     * @param score                      общий счёт игрока
     * @param consecutiveFailedContracts количество подряд невыполненных заказов
     * @param bolts                      количество болтов
     * @param hand                       карты на руке игрока
     */
    public record PlayerSnapshot(
            String name,
            int score,
            int consecutiveFailedContracts,
            int bolts,
            List<CardSnapshot> hand
    ) {

        /**
         * Проверяет корректность полей игрока.
         */
        public PlayerSnapshot {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Имя игрока не может быть пустым");
            }
            hand = hand == null ? List.of() : List.copyOf(hand);
        }
    }

    /**
     * Представляет снимок состояния текущей торговли за прикуп.
     *
     * @param currentPlayerIndex индекс текущего игрока в торговле
     * @param highestBidderName  имя автора наибольшей ставки или null
     * @param highestBidPoints   размер наибольшей ставки или 0
     * @param consecutivePasses  количество подряд идущих пасов
     * @param finished           признак завершения торговли
     */
    public record AuctionSnapshot(
            int currentPlayerIndex,
            String highestBidderName,
            int highestBidPoints,
            int consecutivePasses,
            boolean finished
    ) {

        /**
         * Проверяет корректность полей снимка торговли.
         */
        public AuctionSnapshot {
            if (currentPlayerIndex < 0) {
                throw new IllegalArgumentException("Индекс текущего игрока не может быть отрицательным");
            }
            if (consecutivePasses < 0) {
                throw new IllegalArgumentException("Количество пасов не может быть отрицательным");
            }
        }
    }
}