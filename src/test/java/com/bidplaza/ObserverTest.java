package com.bidplaza;

import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.observer.BidObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObserverTest {

    private Auction auction;
    private Item item;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        item = new com.bidplaza.model.item.Electronics(
            "Test Item", "desc", 100.0, now, now.plusHours(1),
            "seller-1", "Test Brand", "Test Model");
        auction = new Auction(item);
        auction.start();
    }

    @Test
    void observer_addedToAuction_receivesUpdate() throws Exception {
        List<String> received = new ArrayList<>();
        BidObserver observer = bid ->
            received.add(bid.getBidderId() + ":" + bid.getAmount());

        auction.addObserver(observer);
        auction.placeBid("bidder1", 150.0);

        assertEquals(1, received.size());
        assertTrue(received.get(0).contains("bidder1"));
    }

    @Test
    void observer_removedFromAuction_noLongerReceives() throws Exception {
        List<String> received = new ArrayList<>();
        BidObserver observer = bid -> received.add(bid.getBidderId());

        auction.addObserver(observer);
        auction.removeObserver(observer);
        auction.placeBid("bidder1", 150.0);

        assertTrue(received.isEmpty());
    }

    @Test
    void multipleObservers_allReceiveUpdate() throws Exception {
        List<String> log1 = new ArrayList<>();
        List<String> log2 = new ArrayList<>();

        auction.addObserver(bid -> log1.add(bid.getBidderId()));
        auction.addObserver(bid -> log2.add(bid.getBidderId()));
        auction.placeBid("bidder1", 200.0);

        assertEquals(1, log1.size());
        assertEquals(1, log2.size());
    }
}
