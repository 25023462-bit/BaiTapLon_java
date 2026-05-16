package com.bidplaza.network;

import java.io.Serializable;

/**
 * Message exchanged between client and server through object streams.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        LOGIN,
        LOGIN_RESPONSE,
        PLACE_BID,
        BID_SUCCESS,
        BID_FAILED,
        AUCTION_UPDATE,
        ERROR,
        GET_AUCTION_LIST,
        AUCTION_LIST_RESPONSE,
        LIST_AUCTIONS,
        CREATE_AUCTION,
        FINISH_AUCTION
    }

    private final Type type;
    private final String auctionId;
    private final String bidderId;
    private final double amount;
    private final String info;
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

    public static Message placeBid(String auctionId, String bidderId, double amount) {
        return new Message(Type.PLACE_BID, auctionId, bidderId, amount, null);
    }

    public static Message bidSuccess(String auctionId, double newPrice) {
        return new Message(Type.BID_SUCCESS, auctionId, null, newPrice, "Bid thanh cong!");
    }

    public static Message bidFailed(String auctionId, String reason) {
        return new Message(Type.BID_FAILED, auctionId, null, 0, reason);
    }

    public static Message auctionUpdate(String auctionId, double currentPrice, String leaderId) {
        return new Message(Type.AUCTION_UPDATE, auctionId, leaderId, currentPrice,
            "Gia moi: $" + currentPrice);
    }

    public static Message error(String reason) {
        return new Message(Type.ERROR, null, null, 0, reason);
    }

    public static Message login(String username, String password,
                                String role, boolean isRegister) {
        return new Message(Type.LOGIN, new LoginRequest(username, password, role, isRegister));
    }

    public static Message loginResponse(boolean success, String message) {
        return loginResponse(success, message, null);
    }

    public static Message loginResponse(boolean success, String message,
                                        com.bidplaza.model.user.User user) {
        return new Message(Type.LOGIN_RESPONSE, null, null, success ? 1 : 0,
            message, new LoginResponse(success, message, user));
    }

    private Message(Type type, String auctionId, String bidderId,
                    double amount, String info, Object payload) {
        this.type = type;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.info = info;
        this.payload = payload;
    }

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
