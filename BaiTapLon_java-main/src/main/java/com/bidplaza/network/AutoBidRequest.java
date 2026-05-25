package com.bidplaza.network;

import java.io.Serializable;

/**
 * Payload gửi lên server khi người dùng đăng ký auto-bid.
 */
public class AutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double maxBid;
    private final double increment;

    public AutoBidRequest(String auctionId, String bidderId,
                          double maxBid, double increment) {
        this.auctionId = auctionId;
        this.bidderId  = bidderId;
        this.maxBid    = maxBid;
        this.increment = increment;
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId()  { return bidderId; }
    public double getMaxBid()    { return maxBid; }
    public double getIncrement() { return increment; }
}
