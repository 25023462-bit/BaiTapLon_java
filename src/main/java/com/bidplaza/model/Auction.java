package com.bidplaza.model;

import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.model.item.Item;
import com.bidplaza.observer.AuctionObservable;
import com.bidplaza.observer.BidObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Một phiên đấu giá - trung tâm của hệ thống.
 *
 * Fix Phase 1.2:
 * - BUG CŨ: triggerAutoBids() dùng synchronized ĐỒNG THỜI với ReentrantLock trong placeBid()
 *   → deadlock khi auto-bidder gọi lại placeBid() từ bên trong lock.
 * - FIX: bỏ synchronized khỏi triggerAutoBids(), để ReentrantLock duy nhất quản lý.
 *   placeBid() unlock trước khi gọi triggerAutoBids() (lock đã giải phóng).
 * - FIX: double-check status sau khi acquire lock để tránh lost-update giữa 2 thread.
 */
public class Auction implements AuctionObservable {

    public enum Status {
        OPEN, RUNNING, FINISHED, PAID, CANCELED
    }

    private final String id;
    private final Item item;
    private Status status;
    private String winnerId;
    private final List<BidTransaction> bids;
    private final List<AutoBidder> autoBidders = new ArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final List<BidObserver> observers = new CopyOnWriteArrayList<>();
    private static final int SNIPE_WINDOW_SECONDS = 30;
    private static final int EXTENSION_SECONDS = 60;
    private java.time.LocalDateTime endTime;

    public Auction(Item item) {
        this.id = UUID.randomUUID().toString();
        this.item = item;
        this.status = Status.OPEN;
        this.bids = new ArrayList<>();
    }

    @Override
    public void addObserver(BidObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(BidObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(BidTransaction bid) {
        for (BidObserver observer : observers) {
            observer.onBidPlaced(bid);
        }
    }

    /**
     * Đặt giá - thread-safe bằng ReentrantLock.
     *
     * Flow:
     * 1. Acquire lock
     * 2. Validate (status, amount)
     * 3. Cập nhật state
     * 4. Release lock
     * 5. Notify observers (ngoài lock để tránh deadlock)
     * 6. Trigger auto-bids (ngoài lock vì auto-bid sẽ gọi lại placeBid())
     */
    public void placeBid(String bidderId, double amount)
            throws AuctionClosedException, InvalidBidException {

        BidTransaction bid;

        lock.lock();
        try {
            // Validate
            if (amount <= 0) {
                throw new InvalidBidException("So tien phai lon hon 0");
            }
            // Double-check: re-read status sau khi acquire lock
            if (status != Status.RUNNING) {
                throw new AuctionClosedException(
                    "Phien khong o trang thai RUNNING. Hien tai: " + status);
            }
            if (amount <= item.getCurrentPrice()) {
                throw new InvalidBidException(
                    "Gia $" + amount + " phai cao hon gia hien tai $" + item.getCurrentPrice());
            }

            // Update state
            item.setCurrentPrice(amount);
            this.winnerId = bidderId;
            bid = new BidTransaction(bidderId, item.getId(), amount);
            bids.add(bid);

            // Anti-sniping (vẫn trong lock vì cần đọc/ghi endTime an toàn)
            if (endTime != null && java.time.LocalDateTime.now()
                    .isAfter(endTime.minusSeconds(SNIPE_WINDOW_SECONDS))) {
                endTime = endTime.plusSeconds(EXTENSION_SECONDS);
                System.out.println("Anti-sniping: gia han phien them 60 giay");
            }
        } finally {
            lock.unlock();
        }

        // Notify và trigger auto-bids NGOÀI lock để tránh deadlock
        notifyObservers(bid);
        triggerAutoBids();
    }

    public void start() {
        if (status != Status.OPEN) {
            System.out.println("Khong the bat dau: trang thai hien tai " + status);
            return;
        }
        this.status = Status.RUNNING;
        System.out.println("Phien BAT DAU: " + item.getName());
    }

    public void finish() {
        if (status != Status.RUNNING) {
            System.out.println("Khong the ket thuc: trang thai hien tai " + status);
            return;
        }
        this.status = Status.FINISHED;
        if (winnerId != null) {
            System.out.println("Phien KET THUC. Nguoi thang: " + winnerId
                + " | Gia: $" + item.getCurrentPrice());
        } else {
            System.out.println("Phien KET THUC. Khong co ai dat gia.");
        }
    }

    public void markPaid() {
        if (status != Status.FINISHED) {
            System.out.println("Chua the thanh toan: phien chua ket thuc.");
            return;
        }
        this.status = Status.PAID;
        System.out.println("Thanh toan xong. Phien hoan tat.");
    }

    public void cancel() {
        if (status == Status.PAID) {
            System.out.println("Khong the huy phien da thanh toan.");
            return;
        }
        this.status = Status.CANCELED;
        System.out.println("Phien bi HUY: " + item.getName());
    }

    public String getId()                  { return id; }
    public Item getItem()                  { return item; }
    public Status getStatus()              { return status; }
    public String getWinnerId()            { return winnerId; }
    public List<BidTransaction> getBids()  { return bids; }

    public void registerAutoBid(String bidderId, double maxBid, double increment) {
        autoBidders.add(new AutoBidder(bidderId, maxBid, increment));
        System.out.println("Da dang ky auto-bid cho " + bidderId
            + ": max=$" + maxBid + ", step=$" + increment);
    }

    /**
     * Kích hoạt auto-bidder sau mỗi bid mới.
     * KHÔNG dùng synchronized vì được gọi SAU KHI lock đã được giải phóng.
     * Auto-bidder sẽ gọi placeBid() → tự acquire lock bình thường.
     */
    private void triggerAutoBids() {
        for (AutoBidder autoBidder : autoBidders) {
            // Bỏ qua auto-bidder đang là người thắng hiện tại
            if (autoBidder.getBidderId().equals(winnerId)) {
                continue;
            }
            double currentPrice = item.getCurrentPrice();
            if (autoBidder.getMaxBid() > currentPrice) {
                double newBid = Math.min(
                    currentPrice + autoBidder.getIncrement(),
                    autoBidder.getMaxBid()
                );
                try {
                    placeBid(autoBidder.getBidderId(), newBid);
                    break; // Chỉ 1 auto-bidder phản ứng mỗi lần
                } catch (AuctionClosedException | InvalidBidException e) {
                    // ignore - phiên đóng hoặc bid không hợp lệ
                }
            }
        }
    }

    public static class AutoBidder {
        private final String bidderId;
        private final double maxBid;
        private final double increment;

        public AutoBidder(String bidderId, double maxBid, double increment) {
            this.bidderId = bidderId;
            this.maxBid = maxBid;
            this.increment = increment;
        }

        public String getBidderId() { return bidderId; }
        public double getMaxBid()   { return maxBid; }
        public double getIncrement() { return increment; }
    }

    public java.time.LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
}
