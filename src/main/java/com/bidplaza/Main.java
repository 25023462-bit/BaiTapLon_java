package com.bidplaza;

import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.factory.ItemFactory;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.Seller;
import com.bidplaza.observer.ConsoleNotifier;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Seller seller = new Seller("seller1", "pass", "s@mail.com", "MyShop");
        Bidder alice  = new Bidder("alice", "pass", "alice@mail.com");
        Bidder bob    = new Bidder("bob", "pass", "bob@mail.com");
        alice.deposit(5000);
        bob.deposit(3000);

        Item phone = ItemFactory.create("electronics", "iPhone 15 Pro", "Moi 100%",
            1000.0, LocalDateTime.now(), LocalDateTime.now().plusHours(1), seller.getId());

        AuctionManager manager = AuctionManager.getInstance();
        Auction auction = manager.createAuction(phone);

        auction.addObserver(new ConsoleNotifier("Man hinh chinh"));
        auction.addObserver(new ConsoleNotifier("Logger"));

        System.out.println("========== DEMO OBSERVER + EXCEPTION ==========");
        auction.start();

        try {
            auction.placeBid(alice.getId(), 1200.0);
        } catch (AuctionClosedException | InvalidBidException e) {
            System.out.println("Loi: " + e.getMessage());
        }

        try {
            auction.placeBid(bob.getId(), 1100.0);
        } catch (InvalidBidException e) {
            System.out.println("Bid khong hop le: " + e.getMessage());
        } catch (AuctionClosedException e) {
            System.out.println("Phien dong: " + e.getMessage());
        }

        try {
            auction.placeBid(bob.getId(), 1500.0);
        } catch (AuctionClosedException | InvalidBidException e) {
            System.out.println("Loi: " + e.getMessage());
        }

        auction.finish();
        auction.markPaid();
        auction.cancel();

        System.out.println("\n========== DEMO CONCURRENCY ==========");

        Item car = ItemFactory.create("vehicle", "Toyota Camry", "Moi",
            10000.0, LocalDateTime.now(), LocalDateTime.now().plusHours(2), seller.getId());
        Auction carAuction = manager.createAuction(car);
        carAuction.addObserver(new ConsoleNotifier("Car Auction"));
        carAuction.start();

        ExecutorService pool = Executors.newFixedThreadPool(5);

        for (int i = 1; i <= 5; i++) {
            final int bidderNum = i;
            final double bidAmount = 10000 + (bidderNum * 500);

            pool.submit(() -> {
                try {
                    carAuction.placeBid("Bidder-" + bidderNum, bidAmount);
                } catch (InvalidBidException e) {
                    System.out.println("Bidder-" + bidderNum + " thua: " + e.getMessage());
                } catch (AuctionClosedException e) {
                    System.out.println("Bidder-" + bidderNum + " tre: " + e.getMessage());
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        carAuction.finish();
        System.out.println("Tong bid hop le: " + carAuction.getBids().size());
    }
}
