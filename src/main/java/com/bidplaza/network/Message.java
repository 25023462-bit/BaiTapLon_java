package com.bidplaza.network;

import java.io.Serializable;

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
        GET_AUCTIONS,
        AUCTION_LIST_RESPONSE,
        LIST_AUCTIONS,
        CREATE_AUCTION,
        FINISH_AUCTION,
        REGISTER_AUTO_BID,      // Phase 3: yêu cầu đăng ký auto-bid
        AUTO_BID_SUCCESS,       // Phase 3: server xác nhận đăng ký thành công
        AUTO_BID_FAILED,        // Phase 3: server báo lỗi
        DEPOSIT,
        DEPOSIT_SUCCESS,
        DEPOSIT_FAILED,
        GET_MY_BIDS,
        MY_BIDS_RESPONSE,
        GET_AUCTION_HISTORY,
        AUCTION_HISTORY_RESPONSE,
        OUTBID,
        TOGGLE_WATCHLIST,
        WATCHLIST_RESPONSE,
        CHAT_MESSAGE,
        REVIEW_REQUEST,
        REVIEW_RESPONSE,
        JOIN_AUCTION,
        LEAVE_AUCTION
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
        this.bidderId  = bidderId;
        this.amount    = amount;
        this.info      = info;
        this.payload   = null;
    }

    public Message(Type type, Object payload) {
        this.type      = type;
        this.auctionId = null;
        this.bidderId  = null;
        this.amount    = 0;
        this.info      = null;
        this.payload   = payload;
    }

    private Message(Type type, String auctionId, String bidderId,
                    double amount, String info, Object payload) {
        this.type      = type;
        this.auctionId = auctionId;
        this.bidderId  = bidderId;
        this.amount    = amount;
        this.info      = info;
        this.payload   = payload;
    }

    // ── Factory methods ───────────────────────────────────────────

    public static Message placeBid(String auctionId, String bidderId, double amount) {
        return new Message(Type.PLACE_BID, auctionId, bidderId, amount, null);
    }

    public static Message bidSuccess(String auctionId, double newPrice) {
        return new Message(Type.BID_SUCCESS, auctionId, null, newPrice, "Bid thanh cong!");
    }

    public static Message bidFailed(String auctionId, String reason) {
        return new Message(Type.BID_FAILED, auctionId, null, 0, reason);
    }

    public static Message auctionUpdate(String auctionId, double currentPrice,
                                        String leaderId, AuctionSnapshot snapshot) {
        return new Message(Type.AUCTION_UPDATE, auctionId, leaderId,
                           currentPrice, "Gia moi: $" + currentPrice, snapshot);
    }

    public static Message auctionUpdate(String auctionId, double currentPrice, String leaderId) {
        return auctionUpdate(auctionId, currentPrice, leaderId, null);
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

    /** Phase 3: client gửi yêu cầu đăng ký auto-bid */
    public static Message registerAutoBid(String auctionId, String bidderId,
                                          double maxBid, double increment) {
        return new Message(Type.REGISTER_AUTO_BID,
                new AutoBidRequest(auctionId, bidderId, maxBid, increment));
    }

    // ── Getters ───────────────────────────────────────────────────

    public Type getType()        { return type; }
    public String getAuctionId() { return auctionId; }
    public String getBidderId()  { return bidderId; }
    public double getAmount()    { return amount; }
    public String getInfo()      { return info; }
    public Object getPayload()   { return payload; }

    public boolean isSuccess() {
        if (type == Type.ERROR || type == Type.BID_FAILED || type == Type.AUTO_BID_FAILED
                || type == Type.DEPOSIT_FAILED) {
            return false;
        }
        if (payload instanceof LoginResponse) {
            return ((LoginResponse) payload).isSuccess();
        }
        if (type == Type.LOGIN_RESPONSE) {
            return amount == 1;
        }
        return true;
    }

    public Object getData() {
        if (payload instanceof LoginResponse) {
            return ((LoginResponse) payload).getUser();
        }
        if (type == Type.DEPOSIT_SUCCESS) {
            return amount;
        }
        return payload;
    }

    public String getMessage() {
        return info;
    }

    @Override
    public String toString() {
        return "Message{type=" + type + ", auction=" + auctionId
                + ", amount=" + amount + ", info=" + info + "}";
    }
}
