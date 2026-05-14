package com.bidplaza.network;

import java.io.Serializable;

/**
 * Tin nhắn trao đổi giữa Client và Server.
 *
 * Serializable: cho phép chuyển object thành bytes
 * để gửi qua Socket, rồi khôi phục lại ở đầu kia.
 *
 * Các loại tin nhắn (type):
 * - PLACE_BID: client gửi yêu cầu đặt giá
 * - BID_SUCCESS: server xác nhận bid thành công
 * - BID_FAILED: server báo bid thất bại
 * - AUCTION_UPDATE: server broadcast giá mới cho tất cả client
 * - ERROR: thông báo lỗi
 */
public class Message implements Serializable {

    // serialVersionUID: bắt buộc với Serializable
    private static final long serialVersionUID = 1L;

    public enum Type {
        LOGIN,
        PLACE_BID,
        BID_SUCCESS,
        BID_FAILED,
        AUCTION_UPDATE,
        ERROR
    }

    private final Type type;
    private final String auctionId;
    private final String bidderId;
    private final double amount;
    private final String info; // thông tin bổ sung (thông báo lỗi, v.v.)
    private final Object payload;

    public Message(Type type, String auctionId, String bidderId,
                   double amount, String info) {
        this.type = type;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.info = info;
        this.payload = null;
    }

    public Message(Type type, Object payload) {
        this.type = type;
        this.auctionId = null;
        this.bidderId = null;
        this.amount = 0;
        this.info = null;
        this.payload = payload;
    }

    // Factory methods - tạo nhanh từng loại message

    public static Message placeBid(String auctionId, String bidderId, double amount) {
        return new Message(Type.PLACE_BID, auctionId, bidderId, amount, null);
    }

    public static Message bidSuccess(String auctionId, double newPrice) {
        return new Message(Type.BID_SUCCESS, auctionId, null, newPrice, "Bid thành công!");
    }

    public static Message bidFailed(String auctionId, String reason) {
        return new Message(Type.BID_FAILED, auctionId, null, 0, reason);
    }

    public static Message auctionUpdate(String auctionId, double currentPrice, String leaderId) {
        return new Message(Type.AUCTION_UPDATE, auctionId, leaderId, currentPrice,
            "Giá mới: $" + currentPrice);
    }

    public static Message error(String reason) {
        return new Message(Type.ERROR, null, null, 0, reason);
    }

    // Getters
    public Type getType()       { return type; }
    public String getAuctionId(){ return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getAmount()   { return amount; }
    public String getInfo()     { return info; }
    public Object getPayload()  { return payload; }

    @Override
    public String toString() {
        return "Message{type=" + type + ", auction=" + auctionId
            + ", amount=" + amount + ", info=" + info + "}";
    }
}
