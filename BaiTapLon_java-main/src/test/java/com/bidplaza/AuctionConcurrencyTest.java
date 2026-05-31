package com.bidplaza;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionConcurrencyTest {

    private Auction auction;
    private Item item;

    @BeforeEach
    public void setUp() {
        // Create a new auction with a start price of 100
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
    public void placeBid_fiveConcurrentBids_onlyHighestWins() throws Exception {
        auction.start();
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Five threads submit bids simultaneously
        // Thread 1 (bidder0): bids 110
        // Thread 2 (bidder1): bids 120
        // Thread 3 (bidder2): bids 140
        // Thread 4 (bidder3): bids 130
        // Thread 5 (bidder4): bids 150
        double[] bids = { 110.0, 120.0, 140.0, 130.0, 150.0 };

        for (int i = 0; i < 5; i++) {
            final int index = i;
            final double amount = bids[i];
            executor.submit(() -> {
                try {
                    auction.placeBid("bidder" + index, amount);
                } catch (Exception ignored) {
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(finished, "Executor service did not terminate in time");

        // Verify the highest bid wins
        assertEquals("bidder4", auction.getWinnerId());
        assertEquals(150.0, auction.getItem().getCurrentPrice());
    }

    @Test
    public void placeBid_tenRapidBids_allAccepted() throws Exception {
        // We set starting price to 90.0 so that 100.0 (first bid) is accepted.
        item = ItemFactory.create(
            "electronics", "Test Phone", "Desc",
            90.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "seller-001"
        );
        auction = new Auction(item);
        auction.start();

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Submit 10 bids from bidder0 to bidder9 with values 100 to 145 (100 + i * 5)
        for (int i = 0; i < 10; i++) {
            final int index = i;
            final double amount = 100.0 + i * 5.0; // 100, 105, 110, 115, 120, 125, 130, 135, 140, 145
            executor.submit(() -> {
                try {
                    // Small delay to ensure sequential processing order while executing concurrently
                    Thread.sleep(index * 50);
                    auction.placeBid("bidder" + index, amount);
                } catch (Exception ignored) {
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(finished, "Executor service did not terminate in time");

        // All 10 bids should be successfully accepted
        assertEquals(10, auction.getBids().size(), "Not all 10 bids were accepted");
        assertEquals("bidder9", auction.getWinnerId());
        assertEquals(145.0, auction.getItem().getCurrentPrice());
    }
}
