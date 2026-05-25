package com.bidplaza;

import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.factory.ItemFactory;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionExceptionTest {

    private Auction auction;
    private Item item;

    @BeforeEach
    public void setUp() {
        item = ItemFactory.create(
            "electronics", "Test Phone", "Desc",
            100.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "seller-001"
        );
        auction = new Auction(item);
    }

    @Test
    public void placeBid_auctionNotRunning_throwsException() {
        // Auction created but not started -> should be OPEN, not RUNNING
        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid("bidder1", 120.0);
        });
    }

    @Test
    public void placeBid_auctionFinished_throwsException() {
        auction.start();
        auction.finish();
        
        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid("bidder1", 120.0);
        });
    }

    @Test
    public void placeBid_amountTooLow_throwsException() throws Exception {
        auction.start();
        auction.placeBid("bidder1", 150.0);

        // Bidding 140.0 which is lower than the current price of 150.0
        assertThrows(InvalidBidException.class, () -> {
            auction.placeBid("bidder2", 140.0);
        });
    }

    @Test
    public void placeBid_negativeAmount_throwsException() {
        auction.start();

        assertThrows(InvalidBidException.class, () -> {
            auction.placeBid("bidder1", -100.0);
        });
    }

    @Test
    public void placeBid_zeroAmount_throwsException() {
        auction.start();

        assertThrows(InvalidBidException.class, () -> {
            auction.placeBid("bidder1", 0.0);
        });
    }
}
