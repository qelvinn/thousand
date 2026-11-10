package ru.vsu.thousand.engine;

import org.junit.jupiter.api.Test;
import ru.vsu.thousand.model.Player;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void shouldAcceptFirstBid() {
        List<Player> players = createPlayers();
        Auction auction = new Auction(players);

        auction.makeBid(100);

        assertEquals(100, auction.getHighestBid().getPoints());
        assertEquals(players.get(0), auction.getWinner());
        assertEquals(players.get(1), auction.getCurrentPlayer());
    }

    @Test
    void shouldAcceptHigherBid() {
        List<Player> players = createPlayers();
        Auction auction = new Auction(players);

        auction.makeBid(100);
        auction.makeBid(105);

        assertEquals(105, auction.getHighestBid().getPoints());
        assertEquals(players.get(1), auction.getWinner());
    }

    @Test
    void shouldRejectBidBelowMinimum() {
        Auction auction = new Auction(createPlayers());

        assertThrows(
                IllegalArgumentException.class,
                () -> auction.makeBid(95)
        );
    }

    @Test
    void shouldRejectBidThatIsNotMultipleOfFive() {
        Auction auction = new Auction(createPlayers());

        assertThrows(
                IllegalArgumentException.class,
                () -> auction.makeBid(101)
        );
    }

    @Test
    void shouldFinishWhenAllPlayersPass() {
        Auction auction = new Auction(createPlayers());

        auction.pass();
        auction.pass();
        auction.pass();

        assertTrue(auction.isFinished());
        assertNull(auction.getWinner());
    }

    @Test
    void shouldFinishAfterTwoPassesFollowingBid() {
        Auction auction = new Auction(createPlayers());

        auction.makeBid(100);
        auction.pass();
        auction.pass();

        assertTrue(auction.isFinished());
        assertEquals(100, auction.getHighestBid().getPoints());
    }

    @Test
    void shouldRejectMoveAfterAuctionFinished() {
        Auction auction = new Auction(createPlayers());

        auction.pass();
        auction.pass();
        auction.pass();

        assertThrows(IllegalStateException.class, auction::pass);
    }

    @Test
    void shouldRejectIncorrectPlayerCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Auction(List.of(new Player("Игрок 1")))
        );
    }
    @Test
    void shouldResetPassesAfterNewBid() {
        List<Player> players = createPlayers();
        Auction auction = new Auction(players);

        auction.makeBid(100);
        auction.pass();
        auction.makeBid(105);
        auction.pass();

        assertFalse(auction.isFinished());
        assertEquals(105, auction.getHighestBid().getPoints());
        assertEquals(players.get(1), auction.getCurrentPlayer());
    }

    private List<Player> createPlayers() {
        return List.of(
                new Player("Игрок 1"),
                new Player("Игрок 2"),
                new Player("Игрок 3")
        );
    }
}