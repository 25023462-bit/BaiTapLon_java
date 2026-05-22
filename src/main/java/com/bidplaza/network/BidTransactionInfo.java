package com.bidplaza.network;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidTransactionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String auctionName;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String status;

    public BidTransactionInfo(String auctionId, String auctionName, double amount,
                              LocalDateTime timestamp, String status) {
        this.auctionId = auctionId;
        this.auctionName = auctionName;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getAuctionId() { return auctionId; }
    public String getAuctionName() { return auctionName; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
