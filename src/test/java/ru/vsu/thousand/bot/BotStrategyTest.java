package ru.vsu.thousand.bot;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.engine.GameEngine;
import ru.vsu.thousand.engine.Trick;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotStrategyTest {

    @Test
    void shouldEvaluateHandHigherWhenMarriagesArePresent() {
        MarriageEvaluatingBotStrategy bot = new MarriageEvaluatingBotStrategy();

        List<Card> weakHand = List.of(
                new Card(Suit.SPADES, Rank.NINE),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.NINE)
        );

        List<Card> strongHand = List.of(
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.CLUBS, Rank.TEN)
        );

        int weakScore = bot.evaluateHand(weakHand);
        int strongScore = bot.evaluateHand(strongHand);

        assertTrue(strongScore > weakScore);
        assertTrue(strongScore >= 200);
    }

    @Test
    void shouldRaiseBidWhenHandHasStrongMarriages() {
        MarriageEvaluatingBotStrategy bot = new MarriageEvaluatingBotStrategy();
        Player player = new Player("Умный бот");
        player.addCard(new Card(Suit.HEARTS, Rank.KING));
        player.addCard(new Card(Suit.HEARTS, Rank.QUEEN));
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));
        player.addCard(new Card(Suit.DIAMONDS, Rank.KING));
        player.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));
        player.addCard(new Card(Suit.SPADES, Rank.ACE));
        player.addCard(new Card(Suit.CLUBS, Rank.NINE));

        OptionalInt nextBid = bot.chooseBid(player, 110);

        assertTrue(nextBid.isPresent());
        assertEquals(115, nextBid.getAsInt());
    }

    @Test
    void shouldPassWhenCurrentBidExceedsHandEvaluation() {
        MarriageEvaluatingBotStrategy bot = new MarriageEvaluatingBotStrategy();
        Player player = new Player("Осторожный бот");
        player.addCard(new Card(Suit.SPADES, Rank.NINE));
        player.addCard(new Card(Suit.CLUBS, Rank.NINE));
        player.addCard(new Card(Suit.DIAMONDS, Rank.NINE));
        player.addCard(new Card(Suit.HEARTS, Rank.NINE));
        player.addCard(new Card(Suit.SPADES, Rank.JACK));
        player.addCard(new Card(Suit.CLUBS, Rank.JACK));
        player.addCard(new Card(Suit.DIAMONDS, Rank.JACK));

        OptionalInt decision = bot.chooseBid(player, 100);

        assertTrue(decision.isEmpty());
    }

    @Test
    void shouldPreserveMarriageCardsDuringDiscard() {
        MarriageEvaluatingBotStrategy bot = new MarriageEvaluatingBotStrategy();
        Player player = new Player("Заказчик");

        Card heartsKing = new Card(Suit.HEARTS, Rank.KING);
        Card heartsQueen = new Card(Suit.HEARTS, Rank.QUEEN);
        Card spadesNine = new Card(Suit.SPADES, Rank.NINE);
        Card clubsNine = new Card(Suit.CLUBS, Rank.NINE);
        Card diamondsNine = new Card(Suit.DIAMONDS, Rank.NINE);

        player.addCard(heartsKing);
        player.addCard(heartsQueen);
        player.addCard(spadesNine);
        player.addCard(clubsNine);
        player.addCard(diamondsNine);
        player.addCard(new Card(Suit.SPADES, Rank.ACE));
        player.addCard(new Card(Suit.CLUBS, Rank.ACE));
        player.addCard(new Card(Suit.DIAMONDS, Rank.ACE));
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));
        player.addCard(new Card(Suit.HEARTS, Rank.TEN));

        List<Card> discarded = bot.chooseDiscard(player, 120);

        assertEquals(3, discarded.size());
        assertFalse(discarded.contains(heartsKing));
        assertFalse(discarded.contains(heartsQueen));
        assertTrue(discarded.containsAll(List.of(spadesNine, clubsNine, diamondsNine)));
    }

    @Test
    void shouldPrioritizeHigherValueMarriageWhenLeadingTrick() {
        MarriageEvaluatingBotStrategy bot = new MarriageEvaluatingBotStrategy();
        Player first = new Player("Первый");
        Player second = new Player("Второй");
        Player third = new Player("Третий");

        first.addCard(new Card(Suit.SPADES, Rank.KING));
        first.addCard(new Card(Suit.SPADES, Rank.QUEEN));
        first.addCard(new Card(Suit.HEARTS, Rank.KING));
        first.addCard(new Card(Suit.HEARTS, Rank.QUEEN));
        second.addCard(new Card(Suit.CLUBS, Rank.NINE));
        third.addCard(new Card(Suit.DIAMONDS, Rank.NINE));

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());
        Card chosen = bot.chooseCard(first, trick, 2, new TrumpSuit(), Set.of());

        assertEquals(Suit.HEARTS, chosen.getSuit());
        assertTrue(chosen.getRank() == Rank.KING || chosen.getRank() == Rank.QUEEN);
    }

    @Test
    void shouldCompleteFullGameWithMarriageEvaluatingBots() {
        Player p1 = new Player("Бот 1");
        Player p2 = new Player("Бот 2");
        Player p3 = new Player("Бот 3");

        Map<Player, PlayerStrategy> strategies = Map.of(
                p1, new MarriageEvaluatingBotStrategy(1.0),
                p2, new MarriageEvaluatingBotStrategy(0.9),
                p3, new MarriageEvaluatingBotStrategy(1.1)
        );

        GameEngine engine = new GameEngine(List.of(p1, p2, p3), 77777L, strategies);
        engine.runUntilFinished(15000);

        assertNotNull(engine.getWinner());
        assertTrue(engine.getWinner().getScore() >= 1000);
        assertTrue(engine.getEvents().stream().anyMatch(e -> e.contains("сделал заказ 105")
                || e.contains("сделал заказ 110")
                || e.contains("сделал заказ 115")));
    }

    @Test
    void shouldRejectInvalidRiskFactor() {
        assertThrows(IllegalArgumentException.class, () -> new MarriageEvaluatingBotStrategy(0.0));
        assertThrows(IllegalArgumentException.class, () -> new MarriageEvaluatingBotStrategy(-0.5));
        assertThrows(IllegalArgumentException.class, () -> new MarriageEvaluatingBotStrategy(Double.NaN));
    }
}