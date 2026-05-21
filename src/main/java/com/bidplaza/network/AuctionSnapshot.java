package com.bidplaza.network;

import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private final int bidCount;

    public AuctionSnapshot(String id, String name, String description, String category, double startingPrice,
                           double currentPrice, String status, LocalDateTime startTime, LocalDateTime endTime,
                           String sellerId, String winnerId, int bidCount) {
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
        this.bidCount = bidCount;
    }

    public static AuctionSnapshot from(Auction auction) {
        Item item = auction.getItem();
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
            auction.getBids().size()
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
    public int getBidCount() { return bidCount; }
}
