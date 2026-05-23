package com.bidplaza.network;

import java.io.Serializable;

public class ProfileData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String email;
    private final String role;
    private final double balance;
    private final int totalBidsPlaced;
    private final int totalAuctionsWon;
    private final int totalAuctionsCreated;
    private final int totalAuctionsSold;

    public ProfileData(String username, String email, String role,
                       double balance, int totalBidsPlaced,
                       int totalAuctionsWon, int totalAuctionsCreated,
                       int totalAuctionsSold) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.balance = balance;
        this.totalBidsPlaced = totalBidsPlaced;
        this.totalAuctionsWon = totalAuctionsWon;
        this.totalAuctionsCreated = totalAuctionsCreated;
        this.totalAuctionsSold = totalAuctionsSold;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public double getBalance() { return balance; }
    public int getTotalBidsPlaced() { return totalBidsPlaced; }
    public int getTotalAuctionsWon() { return totalAuctionsWon; }
    public int getTotalAuctionsCreated() { return totalAuctionsCreated; }
    public int getTotalAuctionsSold() { return totalAuctionsSold; }
}
