package ru.vsu.thousand.bot;

import ru.vsu.thousand.engine.Trick;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Определяет стратегию принятия решений игроком во время партии.
 */
public interface PlayerStrategy {

    /**
     * Выбирает размер ставки в торговле за прикуп или решение спасовать.
     *
     * @param player игрок, делающий ход
     * @param currentHighestBid текущая максимальная ставка или 0, если ставок ещё не было
     * @return новая ставка (не меньше 100 и кратная 5) или пустое значение для паса
     */
    OptionalInt chooseBid(Player player, int currentHighestBid);

    /**
     * Выбирает ровно три карты для сноса после получения прикупа.
     *
     * @param player заказчик раунда с десятью картами на руке
     * @param contract размер выигранного заказа
     * @return список из трёх карт для сноса
     */
    List<Card> chooseDiscard(Player player, int contract);

    /**
     * Выбирает карту для хода в текущей взятке.
     *
     * @param player игрок, совершающий ход
     * @param trick текущая взятка
     * @param trickNumber номер взятки в раунде (от 1 до 7)
     * @param trumpSuit текущий козырь
     * @param declaredMarriages масти марьяжей, уже объявленных в раунде
     * @return карта из руки игрока для розыгрыша
     */
    Card chooseCard(
            Player player,
            Trick trick,
            int trickNumber,
            TrumpSuit trumpSuit,
            Set<Suit> declaredMarriages
    );
}
