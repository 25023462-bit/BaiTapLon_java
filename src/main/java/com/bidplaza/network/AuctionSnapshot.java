package com.bidplaza.network;

import com.bidplaza.manager.UserManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.BidTransaction;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.user.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final double startingPrice;
    private final double currentPrice;
    private final String status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String sellerId;
    private final String winnerId;
    private final String winnerUsername;
    private final int bidCount;
    private final List<BidTransactionInfo> bidHistory;

    public AuctionSnapshot(String id, String name, String description, String category,
                           double startingPrice, double currentPrice, String status,
                           LocalDateTime startTime, LocalDateTime endTime, String sellerId,
                           String winnerId, String winnerUsername, int bidCount,
                           List<BidTransactionInfo> bidHistory) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
        this.winnerId = winnerId;
        this.winnerUsername = winnerUsername;
        this.bidCount = bidCount;
        this.bidHistory = bidHistory != null
            ? new ArrayList<>(bidHistory) : new ArrayList<>();
    }

    public static AuctionSnapshot from(Auction auction) {
        Item item = auction.getItem();
        String winnerUsername = null;
        if (auction.getWinnerId() != null) {
            User winner = UserManager.getInstance().findById(auction.getWinnerId());
            if (winner != null) {
                winnerUsername = winner.getUsername();
            }
        }
        List<BidTransactionInfo> history = auction.getBids().stream()
            .map(tx -> new BidTransactionInfo(
                auction.getId(),
                item.getName(),
                tx.getAmount(),
                tx.getTimestamp(),
                "ACTIVE"
            ))
            .collect(Collectors.toList());
        return new AuctionSnapshot(
            auction.getId(),
            item.getName(),
            item.getDescription(),
            item.getCategory(),
            item.getStartingPrice(),
            item.getCurrentPrice(),
            auction.getStatus().name(),
            item.getStartTime(),
            item.getEndTime(),
            item.getSellerId(),
            auction.getWinnerId(),
            winnerUsername,
            auction.getBids().size(),
            history
        );
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getSellerId() { return sellerId; }
    public String getWinnerId() { return winnerId; }
    public String getWinnerUsername() { return winnerUsername; }
    public int getBidCount() { return bidCount; }
    public List<BidTransactionInfo> getBidHistory() {
        return Collections.unmodifiableList(bidHistory);
    }
}
