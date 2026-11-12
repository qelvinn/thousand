package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TalonExchangeTest {

    @Test
    void shouldGiveTalonToWinner() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();

        exchange.takeTalon(player, createTalon());

        assertEquals(10, player.getHand().size());
    }

    @Test
    void shouldDiscardThreeCards() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();
        exchange.takeTalon(player, createTalon());

        List<Card> discarded = List.copyOf(
                player.getHand().subList(0, 3)
        );

        exchange.discard(player, discarded);

        assertEquals(7, player.getHand().size());

        for (Card card : discarded) {
            assertFalse(player.getHand().contains(card));
        }
    }

    @Test
    void shouldRejectTalonWithIncorrectSize() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();

        assertThrows(
                IllegalArgumentException.class,
                () -> exchange.takeTalon(player, List.of(
                        new Card(Suit.SPADES, Rank.ACE)
                ))
        );
    }

    @Test
    void shouldRejectTalonIfPlayerDoesNotHaveSevenCards() {
        Player player = new Player("Игрок 1");
        TalonExchange exchange = new TalonExchange();

        assertThrows(
                IllegalStateException.class,
                () -> exchange.takeTalon(player, createTalon())
        );
    }

    @Test
    void shouldRejectDiscardWithIncorrectSize() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();
        exchange.takeTalon(player, createTalon());

        assertThrows(
                IllegalArgumentException.class,
                () -> exchange.discard(
                        player,
                        List.of(player.getHand().get(0), player.getHand().get(1))
                )
        );
    }

    @Test
    void shouldRejectDiscardOfCardsNotInHand() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();
        exchange.takeTalon(player, createTalon());

        List<Card> invalidCards = List.of(
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.JACK),
                new Card(Suit.DIAMONDS, Rank.QUEEN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> exchange.discard(player, invalidCards)
        );
    }

    @Test
    void shouldRejectDuplicateCardsInDiscard() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();
        exchange.takeTalon(player, createTalon());

        Card card = player.getHand().get(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> exchange.discard(player, List.of(card, card, card))
        );
    }

    @Test
    void shouldRejectSecondDiscard() {
        Player player = createPlayer();
        TalonExchange exchange = new TalonExchange();
        exchange.takeTalon(player, createTalon());

        exchange.discard(player, player.getHand().subList(0, 3));

        assertThrows(
                IllegalStateException.class,
                () -> exchange.discard(player, List.of(
                        new Card(Suit.SPADES, Rank.ACE),
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.ACE)
                ))
        );
    }

    private Player createPlayer() {
        Player player = new Player("Игрок 1");

        player.addCard(new Card(Suit.SPADES, Rank.NINE));
        player.addCard(new Card(Suit.SPADES, Rank.JACK));
        player.addCard(new Card(Suit.SPADES, Rank.QUEEN));
        player.addCard(new Card(Suit.SPADES, Rank.KING));
        player.addCard(new Card(Suit.SPADES, Rank.TEN));
        player.addCard(new Card(Suit.SPADES, Rank.ACE));
        player.addCard(new Card(Suit.CLUBS, Rank.NINE));

        return player;
    }

    private List<Card> createTalon() {
        return List.of(
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.KING)
        );
    }
}