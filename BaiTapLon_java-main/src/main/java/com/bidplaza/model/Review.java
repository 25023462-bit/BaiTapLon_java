package com.bidplaza.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String reviewerId;
    private final String reviewerUsername;
    private final double rating;
    private final String comment;
    private final LocalDateTime timestamp;
    private final String auctionId;

    public Review(String reviewerId, String reviewerUsername,
                  double rating, String comment, String auctionId) {
        this.reviewerId = reviewerId;
        this.reviewerUsername = reviewerUsername;
        this.rating = rating;
        this.comment = comment;
        this.auctionId = auctionId;
        this.timestamp = LocalDateTime.now();
    }

    public String getReviewerId() { return reviewerId; }
    public String getReviewerUsername() { return reviewerUsername; }
    public double getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getAuctionId() { return auctionId; }
}
