package com.bidplaza.network;

import java.io.Serializable;

public class ReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String reviewerId;
    private final String sellerId;
    private final String auctionId;
    private final double rating;
    private final String comment;

    public ReviewRequest(String reviewerId, String sellerId, String auctionId, double rating, String comment) {
        this.reviewerId = reviewerId;
        this.sellerId = sellerId;
        this.auctionId = auctionId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getReviewerId() { return reviewerId; }
    public String getSellerId() { return sellerId; }
    public String getAuctionId() { return auctionId; }
    public double getRating() { return rating; }
    public String getComment() { return comment; }
}
