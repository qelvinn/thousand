
package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrickTest {

    private Player createPlayer(String name, Card... cards) {
        Player player = new Player(name);
        for (Card card : cards) {
            player.addCard(card);
        }
        return player;
    }

    @Test
    void shouldDetermineWinnerByHighestCardOfLeadingSuit() {
        Card nine = new Card(Suit.HEARTS, Rank.NINE);
        Card king = new Card(Suit.HEARTS, Rank.KING);
        Card ace = new Card(Suit.HEARTS, Rank.ACE);

        Player first = createPlayer("Первый", nine);
        Player second = createPlayer("Второй", king);
        Player third = createPlayer("Третий", ace);

        Trick trick = new Trick(
                List.of(first, second, third),
                first,
                new TrumpSuit()
        );

        trick.playCard(first, nine);
        trick.playCard(second, king);
        trick.playCard(third, ace);

        assertEquals(third, trick.getWinner());
    }

    @Test
    void shouldDetermineWinnerByTrump() {
        Card heart = new Card(Suit.HEARTS, Rank.ACE);
        Card spade = new Card(Suit.SPADES, Rank.NINE);
        Card club = new Card(Suit.CLUBS, Rank.ACE);

        Player first = createPlayer("Первый", heart);
        Player second = createPlayer("Второй", spade);
        Player third = createPlayer("Третий", club);

        TrumpSuit trump = new TrumpSuit();
        trump.setSuit(Suit.SPADES);

        Trick trick = new Trick(List.of(first, second, third), first, trump);

        trick.playCard(first, heart);
        trick.playCard(second, spade);
        trick.playCard(third, club);

        assertEquals(second, trick.getWinner());
    }

    @Test
    void shouldRequireLeadingSuitWhenPlayerHasIt() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Card secondCard = new Card(Suit.SPADES, Rank.ACE);
        Card thirdCard = new Card(Suit.HEARTS, Rank.KING);

        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", secondCard, thirdCard);
        Player third = createPlayer("Третий", new Card(Suit.CLUBS, Rank.NINE));

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());
        trick.playCard(first, firstCard);

        assertThrows(
                IllegalArgumentException.class,
                () -> trick.playCard(second, secondCard)
        );
    }

    @Test
    void shouldAllowOtherSuitWhenPlayerDoesNotHaveLeadingSuit() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Card secondCard = new Card(Suit.SPADES, Rank.ACE);
        Card thirdCard = new Card(Suit.CLUBS, Rank.NINE);

        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", secondCard);
        Player third = createPlayer("Третий", thirdCard);

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());

        trick.playCard(first, firstCard);
        trick.playCard(second, secondCard);
        trick.playCard(third, thirdCard);

        assertTrue(trick.isComplete());
    }

    @Test
    void shouldCountPointsOfPlayedCards() {
        Card firstCard = new Card(Suit.HEARTS, Rank.ACE);
        Card secondCard = new Card(Suit.HEARTS, Rank.TEN);
        Card thirdCard = new Card(Suit.HEARTS, Rank.KING);

        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", secondCard);
        Player third = createPlayer("Третий", thirdCard);

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());

        trick.playCard(first, firstCard);
        trick.playCard(second, secondCard);
        trick.playCard(third, thirdCard);

        assertEquals(25, trick.getPoints());
    }

    @Test
    void shouldRejectWrongPlayerTurn() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", new Card(Suit.CLUBS, Rank.NINE));
        Player third = createPlayer("Третий", new Card(Suit.SPADES, Rank.NINE));

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());

        assertThrows(
                IllegalStateException.class,
                () -> trick.playCard(second, second.getHand().get(0))
        );
    }

    @Test
    void shouldRejectCardNotInHand() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", new Card(Suit.CLUBS, Rank.NINE));
        Player third = createPlayer("Третий", new Card(Suit.SPADES, Rank.NINE));

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());

        assertThrows(
                IllegalArgumentException.class,
                () -> trick.playCard(first, new Card(Suit.DIAMONDS, Rank.ACE))
        );
    }

    @Test
    void shouldRejectWinnerBeforeTrickIsComplete() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", new Card(Suit.CLUBS, Rank.NINE));
        Player third = createPlayer("Третий", new Card(Suit.SPADES, Rank.NINE));

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());
        trick.playCard(first, firstCard);

        assertThrows(IllegalStateException.class, trick::getWinner);
    }

    @Test
    void shouldRejectMoveAfterTrickIsComplete() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Card secondCard = new Card(Suit.HEARTS, Rank.JACK);
        Card thirdCard = new Card(Suit.HEARTS, Rank.QUEEN);

        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", secondCard);
        Player third = createPlayer("Третий", thirdCard);

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());

        trick.playCard(first, firstCard);
        trick.playCard(second, secondCard);
        trick.playCard(third, thirdCard);

        assertThrows(
                IllegalStateException.class,
                () -> trick.playCard(first, firstCard)
        );
    }

    @Test
    void shouldReturnPlayedCardsAsUnmodifiableList() {
        Card firstCard = new Card(Suit.HEARTS, Rank.NINE);
        Player first = createPlayer("Первый", firstCard);
        Player second = createPlayer("Второй", new Card(Suit.CLUBS, Rank.NINE));
        Player third = createPlayer("Третий", new Card(Suit.SPADES, Rank.NINE));

        Trick trick = new Trick(List.of(first, second, third), first, new TrumpSuit());
        trick.playCard(first, firstCard);

        assertThrows(
                UnsupportedOperationException.class,
                () -> trick.getPlayedCards().clear()
        );
    }
}
