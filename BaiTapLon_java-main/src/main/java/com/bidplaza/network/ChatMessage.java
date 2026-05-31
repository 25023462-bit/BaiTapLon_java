package com.bidplaza.network;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String auctionId;
    private final String senderId;
    private final String senderUsername;
    private final String content;
    private final LocalDateTime timestamp;

    public ChatMessage(String auctionId, String senderId,
                       String senderUsername, String content) {
        this.auctionId = auctionId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getAuctionId() { return auctionId; }
    public String getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
