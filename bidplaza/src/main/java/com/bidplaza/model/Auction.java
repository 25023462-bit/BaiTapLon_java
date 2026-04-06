package com.bidplaza.model;

import com.bidplaza.model.item.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Một phiên đấu giá.
 *
 * Vòng đời: OPEN → RUNNING → FINISHED → PAID / CANCELED
 *
 * synchronized ở placeBid() để tránh race condition:
 * nếu 2 người đặt giá cùng lúc, chỉ 1 người được xử lý tại một thời điểm.
 */
public class Auction {

    public enum Status { OPEN, RUNNING, FINISHED, PAID, CANCELED }

    private final String id;
    private final Item item;
    private Status status;
    private String winnerId;
    private final List<BidTransaction> bids;

    public Auction(Item item) {
        this.id = UUID.randomUUID().toString();
        this.item = item;
        this.status = Status.OPEN;
        this.bids = new ArrayList<>();
    }

    /**
     * Đặt giá - synchronized để thread-safe.
     * Trả về true nếu hợp lệ, false nếu không.
     */
    public synchronized boolean placeBid(String bidderId, double amount) {
        if (status != Status.RUNNING) {
            System.out.println("Phiên đấu giá chưa mở hoặc đã kết thúc.");
            return false;
        }
        if (amount <= item.getCurrentPrice()) {
            System.out.println("Giá đặt phải cao hơn giá hiện tại: $" + item.getCurrentPrice());
            return false;
        }

        // Cập nhật giá và người dẫn đầu
        item.setCurrentPrice(amount);
        this.winnerId = bidderId;
        bids.add(new BidTransaction(bidderId, item.getId(), amount));
        System.out.println("Bid thành công: $" + amount + " bởi " + bidderId);
        return true;
    }

    public void start() {
        this.status = Status.RUNNING;
        System.out.println("Phiên đấu giá BẮT ĐẦU: " + item.getName());
    }

    public void finish() {
        this.status = Status.FINISHED;
        if (winnerId != null) {
            System.out.println("Phiên KẾT THÚC. Người thắng: " + winnerId
                + " | Giá: $" + item.getCurrentPrice());
        } else {
            System.out.println("Phiên KẾT THÚC. Không có người thắng.");
        }
    }

    // Getters
    public String getId()                    { return id; }
    public Item getItem()                    { return item; }
    public Status getStatus()               { return status; }
    public String getWinnerId()             { return winnerId; }
    public List<BidTransaction> getBids()   { return bids; }
}
