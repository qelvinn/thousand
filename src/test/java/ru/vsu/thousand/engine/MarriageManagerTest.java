package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;

import static org.junit.jupiter.api.Assertions.*;

class MarriageManagerTest {

    @Test
    void declareHeartsMarriageChangesTrumpAndReturnsPoints() {
        Player player = new Player("Игрок");
        player.addCard(new Card(Suit.HEARTS, Rank.KING));
        player.addCard(new Card(Suit.HEARTS, Rank.QUEEN));

        TrumpSuit trump = new TrumpSuit();
        MarriageManager manager = new MarriageManager(trump);

        int points = manager.declare(player, Suit.HEARTS);

        assertEquals(100, points);
        assertEquals(Suit.HEARTS, trump.getSuit().orElseThrow());
        assertTrue(manager.isDeclared(Suit.HEARTS));
    }

    @Test
    void declareSpadesMarriageReturns40Points() {
        assertEquals(40, declareMarriage(Suit.SPADES));
    }

    @Test
    void declareClubsMarriageReturns60Points() {
        assertEquals(60, declareMarriage(Suit.CLUBS));
    }

    @Test
    void declareDiamondsMarriageReturns80Points() {
        assertEquals(80, declareMarriage(Suit.DIAMONDS));
    }

    @Test
    void declareMarriageWithoutKingThrowsException() {
        Player player = new Player("Игрок");
        player.addCard(new Card(Suit.HEARTS, Rank.QUEEN));

        MarriageManager manager = new MarriageManager(new TrumpSuit());

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.declare(player, Suit.HEARTS)
        );
    }

    @Test
    void declareMarriageWithoutQueenThrowsException() {
        Player player = new Player("Игрок");
        player.addCard(new Card(Suit.HEARTS, Rank.KING));

        MarriageManager manager = new MarriageManager(new TrumpSuit());

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.declare(player, Suit.HEARTS)
        );
    }

    @Test
    void cannotDeclareSameMarriageTwice() {
        Player player = new Player("Игрок");
        player.addCard(new Card(Suit.HEARTS, Rank.KING));
        player.addCard(new Card(Suit.HEARTS, Rank.QUEEN));

        MarriageManager manager = new MarriageManager(new TrumpSuit());

        manager.declare(player, Suit.HEARTS);

        assertThrows(
                IllegalStateException.class,
                () -> manager.declare(player, Suit.HEARTS)
        );
    }

    @Test
    void nullPlayerIsRejected() {
        MarriageManager manager = new MarriageManager(new TrumpSuit());

        assertThrows(
                NullPointerException.class,
                () -> manager.declare(null, Suit.HEARTS)
        );
    }

    private int declareMarriage(Suit suit) {
        Player player = new Player("Игрок");
        player.addCard(new Card(suit, Rank.KING));
        player.addCard(new Card(suit, Rank.QUEEN));

        MarriageManager manager = new MarriageManager(new TrumpSuit());

        return manager.declare(player, suit);
    }
}