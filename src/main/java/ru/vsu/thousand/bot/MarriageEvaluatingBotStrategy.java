package ru.vsu.thousand.bot;

import ru.vsu.thousand.engine.Trick;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;
import ru.vsu.thousand.util.CardComparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Реализует стратегию бота, оценивающего силу руки с учётом готовых и потенциальных марьяжей.
 */
public class MarriageEvaluatingBotStrategy implements PlayerStrategy {

    private final double riskFactor;

    /**
     * Создаёт стратегию со стандартным коэффициентом оценки руки.
     */
    public MarriageEvaluatingBotStrategy() {
        this(1.0);
    }

    /**
     * Создаёт стратегию с заданным коэффициентом готовности торговаться.
     *
     * @param riskFactor множитель оценки руки (должен быть положительным)
     */
    public MarriageEvaluatingBotStrategy(double riskFactor) {
        if (riskFactor <= 0.0 || Double.isNaN(riskFactor) || Double.isInfinite(riskFactor)) {
            throw new IllegalArgumentException("Коэффициент риска должен быть положительным числом");
        }
        this.riskFactor = riskFactor;
    }

    /**
     * Оценивает максимальный разумный заказ для заданной руки с учётом марьяжей и старших карт.
     *
     * @param hand карты на руке игрока
     * @return расчётная оценка силы руки в очках
     */
    public int evaluateHand(List<Card> hand) {
        Objects.requireNonNull(hand, "Рука игрока не может быть null");

        int totalEstimate = 0;

        for (Suit suit : Suit.values()) {
            boolean hasKing = hand.contains(new Card(suit, Rank.KING));
            boolean hasQueen = hand.contains(new Card(suit, Rank.QUEEN));
            boolean hasAce = hand.contains(new Card(suit, Rank.ACE));
            boolean hasTen = hand.contains(new Card(suit, Rank.TEN));

            if (hasKing && hasQueen) {
                totalEstimate += suit.getMarriagePoints();
                if (hasAce) {
                    totalEstimate += 15;
                }
            } else if (hasKing || hasQueen) {
                totalEstimate += suit.getMarriagePoints() / 4;
            }

            if (hasAce) {
                totalEstimate += 20;
            }
            if (hasTen && (hasAce || hasKing)) {
                totalEstimate += 12;
            }
        }

        int scaled = (int) Math.floor(totalEstimate * riskFactor);
        return (scaled / 5) * 5;
    }

    /**
     * Выбирает ставку на основе оценки руки с учётом марьяжей.
     *
     * @param player            игрок, делающий ход
     * @param currentHighestBid текущая максимальная ставка или 0
     * @return следующая ставка или пустое значение для паса
     */
    @Override
    public OptionalInt chooseBid(Player player, int currentHighestBid) {
        int maxAllowedBid = Math.min(220, evaluateHand(player.getHand()));

        if (currentHighestBid == 0) {
            return OptionalInt.of(100);
        }

        int nextBid = currentHighestBid + 5;
        if (nextBid <= maxAllowedBid) {
            return OptionalInt.of(nextBid);
        }

        return OptionalInt.empty();
    }

    /**
     * Сносит три наименее ценные карты, стараясь сохранить готовые и потенциальные марьяжи.
     *
     * @param player   заказчик раунда
     * @param contract размер выигранного заказа
     * @return список из трёх карт для сноса
     */
    @Override
    public List<Card> chooseDiscard(Player player, int contract) {
        List<Card> hand = new ArrayList<>(player.getHand());
        Set<Suit> marriageSuits = findMarriageSuits(hand);

        hand.sort(
                Comparator.comparingInt((Card card) -> getKeepPriority(card, marriageSuits))
                        .thenComparing(CardComparators.BY_RANK_THEN_SUIT)
        );

        return List.copyOf(hand.subList(0, 3));
    }

    /**
     * Выбирает карту для розыгрыша с приоритетом дорогих марьяжей и взятия ценных взяток.
     *
     * @param player            игрок, совершающий ход
     * @param trick             текущая взятка
     * @param trickNumber       номер взятки в раунде
     * @param trumpSuit         текущий козырь
     * @param declaredMarriages уже объявленные в раунде марьяжи
     * @return карта для хода
     */
    @Override
    public Card chooseCard(
            Player player,
            Trick trick,
            int trickNumber,
            TrumpSuit trumpSuit,
            Set<Suit> declaredMarriages
    ) {
        List<Card> hand = player.getHand();
        Set<Suit> marriageSuits = findMarriageSuits(hand);

        if (trick.getPlayedCards().isEmpty()) {
            if (trickNumber > 1) {
                Card bestMarriageCard = hand.stream()
                        .filter(card -> card.getRank() == Rank.KING || card.getRank() == Rank.QUEEN)
                        .filter(card -> marriageSuits.contains(card.getSuit()))
                        .filter(card -> !declaredMarriages.contains(card.getSuit()))
                        .max(Comparator.comparingInt((Card card) -> card.getSuit().getMarriagePoints())
                                .thenComparingInt(card -> card.getRank().getStrength()))
                        .orElse(null);

                if (bestMarriageCard != null) {
                    return bestMarriageCard;
                }
            }

            return hand.stream()
                    .max(Comparator.comparingInt((Card card) -> trumpSuit.isTrump(card.getSuit()) ? 1 : 0)
                            .thenComparingInt(card -> card.getRank().getStrength()))
                    .orElseThrow();
        }

        Suit leadingSuit = trick.getPlayedCards().get(0).card().getSuit();
        List<Card> matchingSuitCards = hand.stream()
                .filter(card -> card.getSuit() == leadingSuit)
                .sorted(CardComparators.BY_RANK_THEN_SUIT)
                .toList();

        if (!matchingSuitCards.isEmpty()) {
            return chooseBestResponse(matchingSuitCards, trick, trumpSuit, marriageSuits);
        }

        List<Card> trumpCards = hand.stream()
                .filter(card -> trumpSuit.isTrump(card.getSuit()))
                .sorted(CardComparators.BY_RANK_THEN_SUIT)
                .toList();

        if (!trumpCards.isEmpty() && trick.getPoints() >= 10) {
            return chooseBestResponse(trumpCards, trick, trumpSuit, marriageSuits);
        }

        return hand.stream()
                .min(Comparator.comparingInt((Card card) -> getKeepPriority(card, marriageSuits))
                        .thenComparing(CardComparators.BY_RANK_THEN_SUIT))
                .orElseThrow();
    }

    private Card chooseBestResponse(
            List<Card> candidates,
            Trick trick,
            TrumpSuit trumpSuit,
            Set<Suit> marriageSuits
    ) {
        for (Card candidate : candidates) {
            if (wouldWinTrick(candidate, trick, trumpSuit)) {
                return candidate;
            }
        }

        return candidates.stream()
                .min(Comparator.comparingInt((Card card) -> getKeepPriority(card, marriageSuits))
                        .thenComparing(CardComparators.BY_RANK_THEN_SUIT))
                .orElse(candidates.get(0));
    }

    private boolean wouldWinTrick(Card candidate, Trick trick, TrumpSuit trumpSuit) {
        Suit leadingSuit = trick.getPlayedCards().get(0).card().getSuit();
        Card currentBest = trick.getPlayedCards().get(0).card();

        for (int i = 1; i < trick.getPlayedCards().size(); i++) {
            Card played = trick.getPlayedCards().get(i).card();
            if (beats(played, currentBest, leadingSuit, trumpSuit)) {
                currentBest = played;
            }
        }

        return beats(candidate, currentBest, leadingSuit, trumpSuit);
    }

    private boolean beats(Card candidate, Card currentBest, Suit leadingSuit, TrumpSuit trumpSuit) {
        int candidateCategory = getCategory(candidate, leadingSuit, trumpSuit);
        int bestCategory = getCategory(currentBest, leadingSuit, trumpSuit);

        if (candidateCategory != bestCategory) {
            return candidateCategory > bestCategory;
        }
        if (candidate.getSuit() != currentBest.getSuit()) {
            return false;
        }
        return candidate.getRank().getStrength() > currentBest.getRank().getStrength();
    }

    private int getCategory(Card card, Suit leadingSuit, TrumpSuit trumpSuit) {
        if (trumpSuit.isTrump(card.getSuit())) {
            return 2;
        }
        if (card.getSuit() == leadingSuit) {
            return 1;
        }
        return 0;
    }

    private Set<Suit> findMarriageSuits(List<Card> hand) {
        Set<Suit> result = EnumSet.noneOf(Suit.class);
        for (Suit suit : Suit.values()) {
            boolean hasKing = hand.contains(new Card(suit, Rank.KING));
            boolean hasQueen = hand.contains(new Card(suit, Rank.QUEEN));
            if (hasKing && hasQueen) {
                result.add(suit);
            }
        }
        return result;
    }

    private int getKeepPriority(Card card, Set<Suit> marriageSuits) {
        if (marriageSuits.contains(card.getSuit())
                && (card.getRank() == Rank.KING || card.getRank() == Rank.QUEEN)) {
            return 100 + card.getSuit().getMarriagePoints();
        }
        if (card.getRank() == Rank.ACE) {
            return 80;
        }
        if (card.getRank() == Rank.TEN) {
            return 60;
        }
        if (card.getRank() == Rank.KING || card.getRank() == Rank.QUEEN) {
            return 30 + card.getSuit().getMarriagePoints() / 10;
        }
        return card.getRank().getStrength();
    }
}