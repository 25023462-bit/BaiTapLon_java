package com.bidplaza.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ghi lại một lần đặt giá.
 *
 * Mỗi khi ai đó đặt giá thành công → tạo 1 BidTransaction mới.
 * Không thay đổi được sau khi tạo (final fields).
 */
public class BidTransaction {

    private final String id;
    private final String bidderId;  // ai đặt
    private final String itemId;    // đặt cho sản phẩm nào
    private final double amount;    // đặt bao nhiêu
    private final LocalDateTime timestamp; // lúc nào

    public BidTransaction(String bidderId, String itemId, double amount) {
        this.id = UUID.randomUUID().toString();
        this.bidderId = bidderId;
        this.itemId = itemId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public String getId()             { return id; }
    public String getBidderId()       { return bidderId; }
    public String getItemId()         { return itemId; }
    public double getAmount()         { return amount; }
    public LocalDateTime getTimestamp(){ return timestamp; }

    @Override
    public String toString() {
        return "Bid[" + bidderId + " đặt $" + amount
            + " cho " + itemId + " lúc " + timestamp + "]";
    }
}
