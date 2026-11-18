package ru.vsu.thousand.bot;

import ru.vsu.thousand.engine.Trick;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;
import ru.vsu.thousand.util.CardComparators;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Реализует базовую стратегию бота без предварительной оценки марьяжей.
 */
public class DefaultBotStrategy implements PlayerStrategy {

    /**
     * Делает минимальную ставку 100, если торги ещё не открыты, иначе пасует.
     *
     * @param player игрок, делающий ход
     * @param currentHighestBid текущая максимальная ставка
     * @return ставка 100 или пустое значение
     */
    @Override
    public OptionalInt chooseBid(Player player, int currentHighestBid) {
        if (currentHighestBid == 0) {
            return OptionalInt.of(100);
        }
        return OptionalInt.empty();
    }

    /**
     * Сносит три младшие карты по достоинству и масти.
     *
     * @param player заказчик раунда
     * @param contract размер заказа
     * @return три младшие карты
     */
    @Override
    public List<Card> chooseDiscard(Player player, int contract) {
        List<Card> cards = new ArrayList<>(player.getHand());
        cards.sort(CardComparators.BY_RANK_THEN_SUIT);
        return List.copyOf(cards.subList(0, 3));
    }

    /**
     * Выбирает карту по базовому правилу приоритета марьяжа и ответа в масть.
     *
     * @param player игрок, совершающий ход
     * @param trick текущая взятка
     * @param trickNumber номер взятки
     * @param trumpSuit текущий козырь
     * @param declaredMarriages объявленные марьяжи
     * @return выбранная карта
     */
    @Override
    public Card chooseCard(
            Player player,
            Trick trick,
            int trickNumber,
            TrumpSuit trumpSuit,
            Set<Suit> declaredMarriages
    ) {
        List<Card> hand = player.getHand().stream()
                .sorted(CardComparators.BY_RANK_THEN_SUIT)
                .toList();

        if (trick.getPlayedCards().isEmpty()) {
            if (trickNumber > 1) {
                for (Card card : hand) {
                    if ((card.getRank() == Rank.QUEEN || card.getRank() == Rank.KING)
                            && hasMarriage(player, card.getSuit())
                            && !declaredMarriages.contains(card.getSuit())) {
                        return card;
                    }
                }
            }
            return hand.get(hand.size() - 1);
        }

        Suit leadingSuit = trick.getPlayedCards().get(0).card().getSuit();

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
}
