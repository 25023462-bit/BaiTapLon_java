package com.bidplaza.model.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Người đặt giá - kế thừa User.
 * Có thêm: số dư tài khoản (balance).
 */
public class Bidder extends User {

    private double balance;
    private final Set<String> watchlist = new HashSet<>();

    public Bidder(String username, String password, String email) {
        super(username, password, email);
        this.balance = 0.0;
    }

    public double getBalance() { return balance; }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addToWatchlist(String auctionId) {
        watchlist.add(auctionId);
    }

    public void removeFromWatchlist(String auctionId) {
        watchlist.remove(auctionId);
    }

    public boolean isWatching(String auctionId) {
        return watchlist.contains(auctionId);
    }

    public Set<String> getWatchlist() {
        return Collections.unmodifiableSet(watchlist);
    }

    // Nạp tiền vào tài khoản
    public void deposit(double amount) {
        this.balance += amount;
    }

    // Kiểm tra có đủ tiền để đặt giá không
    public boolean canBid(double amount) {
        return balance >= amount;
    }

    @Override
    public String getRole() { return "BIDDER"; }
}
