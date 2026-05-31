package com.bidplaza.network;

import java.io.Serializable;

public class CreateAuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String description;
    private final String category;
    private final double startingPrice;
    private final int durationHours;
    private final String sellerId;

    public CreateAuctionRequest(String name, String description, String category,
                                double startingPrice, int durationHours, String sellerId) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.startingPrice = startingPrice;
        this.durationHours = durationHours;
        this.sellerId = sellerId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getStartingPrice() { return startingPrice; }
    public int getDurationHours() { return durationHours; }
    public String getSellerId() { return sellerId; }
}
