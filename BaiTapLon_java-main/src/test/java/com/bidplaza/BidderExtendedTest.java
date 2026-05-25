package com.bidplaza;

import com.bidplaza.model.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidderExtendedTest {

    private Bidder bidder;

    @BeforeEach
    void setUp() {
        bidder = new Bidder("testuser", "pass123", "test@email.com");
    }

    @Test
    void deposit_validAmount_increasesBalance() {
        bidder.deposit(500.0);
        assertEquals(500.0, bidder.getBalance());
    }

    @Test
    void deposit_multipleTimes_accumulatesBalance() {
        bidder.deposit(100.0);
        bidder.deposit(200.0);
        assertEquals(300.0, bidder.getBalance());
    }

    @Test
    void canBid_sufficientBalance_returnsTrue() {
        bidder.deposit(500.0);
        assertTrue(bidder.canBid(300.0));
    }

    @Test
    void canBid_insufficientBalance_returnsFalse() {
        bidder.deposit(100.0);
        assertFalse(bidder.canBid(300.0));
    }

    @Test
    void watchlist_addAuction_isWatching() {
        bidder.addToWatchlist("auction-001");
        assertTrue(bidder.isWatching("auction-001"));
    }

    @Test
    void watchlist_removeAuction_notWatching() {
        bidder.addToWatchlist("auction-001");
        bidder.removeFromWatchlist("auction-001");
        assertFalse(bidder.isWatching("auction-001"));
    }

    @Test
    void watchlist_getWatchlist_returnsUnmodifiable() {
        bidder.addToWatchlist("auction-001");
        assertThrows(UnsupportedOperationException.class,
            () -> bidder.getWatchlist().add("auction-002"));
    }
}
