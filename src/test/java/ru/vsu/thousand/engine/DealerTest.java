package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.GameSettings;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DealerTest {

    @Test
    void shouldDealSevenCardsToEachPlayerAndThreeToTalon() {
        List<Player> players = createPlayers();
        Dealer dealer = new Dealer(new GameSettings(12345L));

        List<Card> talon = dealer.deal(players);

        for (Player player : players) {
            assertEquals(7, player.getHand().size());
        }

        assertEquals(3, talon.size());
    }

    @Test
    void shouldDealAllTwentyFourUniqueCards() {
        List<Player> players = createPlayers();
        Dealer dealer = new Dealer(new GameSettings(12345L));

        List<Card> talon = dealer.deal(players);

        Set<Card> allCards = new HashSet<>(talon);

        for (Player player : players) {
            allCards.addAll(player.getHand());
        }

        assertEquals(24, allCards.size());
    }

    @Test
    void shouldProduceSameDealForSameSeed() {
        List<Player> firstPlayers = createPlayers();
        List<Player> secondPlayers = createPlayers();

        List<Card> firstTalon =
                new Dealer(new GameSettings(777L)).deal(firstPlayers);

        List<Card> secondTalon =
                new Dealer(new GameSettings(777L)).deal(secondPlayers);

        for (int i = 0; i < firstPlayers.size(); i++) {
            assertEquals(
                    firstPlayers.get(i).getHand(),
                    secondPlayers.get(i).getHand()
            );
        }

        assertEquals(firstTalon, secondTalon);
    }

    @Test
    void shouldRejectIncorrectPlayerCount() {
        Dealer dealer = new Dealer(new GameSettings(123L));

        assertThrows(
                IllegalArgumentException.class,
                () -> dealer.deal(List.of(new Player("Игрок 1")))
        );
    }

    @Test
    void shouldRejectPlayerWithNonEmptyHand() {
        List<Player> players = createPlayers();
        players.get(0).addCard(new Card(Suit.SPADES, Rank.ACE));

        Dealer dealer = new Dealer(new GameSettings(123L));

        assertThrows(
                IllegalStateException.class,
                () -> dealer.deal(players)
        );
    }
    @Test
    void shouldRejectNullPlayersList() {
        Dealer dealer = new Dealer(new GameSettings(123L));

        assertThrows(
                NullPointerException.class,
                () -> dealer.deal(null)
        );
    }

    @Test
    void shouldRejectNullPlayer() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Игрок 1"));
        players.add(null);
        players.add(new Player("Игрок 3"));

        Dealer dealer = new Dealer(new GameSettings(123L));

        assertThrows(
                NullPointerException.class,
                () -> dealer.deal(players)
        );
    }
    @Test
    void shouldReturnUnmodifiableTalon() {
        List<Player> players = createPlayers();
        Dealer dealer = new Dealer(new GameSettings(123L));

        List<Card> talon = dealer.deal(players);

        assertThrows(
                UnsupportedOperationException.class,
                () -> talon.add(new Card(Suit.SPADES, Rank.ACE))
        );
    }

    private List<Player> createPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Игрок 1"));
        players.add(new Player("Игрок 2"));
        players.add(new Player("Игрок 3"));
        return players;
    }
}