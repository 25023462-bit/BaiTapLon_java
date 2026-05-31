package com.bidplaza;

import com.bidplaza.model.user.Bidder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho Bidder.
 */
class BidderTest {

    @Test
    void deposit_shouldIncreaseBalance() {
        Bidder bidder = new Bidder("alice", "pass", "alice@mail.com");
        bidder.deposit(1000.0);
        assertEquals(1000.0, bidder.getBalance());
    }

    @Test
    void canBid_sufficientBalance_shouldReturnTrue() {
        Bidder bidder = new Bidder("alice", "pass", "alice@mail.com");
        bidder.deposit(2000.0);
        assertTrue(bidder.canBid(1500.0));
    }

    @Test
    void canBid_insufficientBalance_shouldReturnFalse() {
        Bidder bidder = new Bidder("alice", "pass", "alice@mail.com");
        bidder.deposit(500.0);
        assertFalse(bidder.canBid(1000.0));
    }

    @Test
    void checkPassword_correct_shouldReturnTrue() {
        Bidder bidder = new Bidder("alice", "mypassword", "alice@mail.com");
        assertTrue(bidder.checkPassword("mypassword"));
    }

    @Test
    void checkPassword_wrong_shouldReturnFalse() {
        Bidder bidder = new Bidder("alice", "mypassword", "alice@mail.com");
        assertFalse(bidder.checkPassword("wrongpass"));
    }

    @Test
    void getRole_shouldReturnBidder() {
        Bidder bidder = new Bidder("alice", "pass", "alice@mail.com");
        assertEquals("BIDDER", bidder.getRole());
    }
}
