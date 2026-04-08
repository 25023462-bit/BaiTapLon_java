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
 * Tuần 7 bổ sung:
 * 1. Observer Pattern: tự động thông báo khi có bid mới
 * 2. Custom Exception: ném lỗi rõ ràng thay vì return false
 * 3. State machine: OPEN → RUNNING → FINISHED → PAID/CANCELED
 * 4. ReentrantLock: xử lý concurrency an toàn hơn synchronized
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

    private final ReentrantLock lock = new ReentrantLock();
    private final List<BidObserver> observers = new CopyOnWriteArrayList<>();

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

    public void placeBid(String bidderId, double amount)
            throws AuctionClosedException, InvalidBidException {
        lock.lock();
        try {
            if (status != Status.RUNNING) {
                throw new AuctionClosedException(
                    "Phiên không ở trạng thái RUNNING. Hiện tại: " + status);
            }
            if (amount <= item.getCurrentPrice()) {
                throw new InvalidBidException(
                    "Giá $" + amount + " phải cao hơn giá hiện tại $" + item.getCurrentPrice());
            }
            item.setCurrentPrice(amount);
            this.winnerId = bidderId;
            BidTransaction bid = new BidTransaction(bidderId, item.getId(), amount);
            bids.add(bid);
            notifyObservers(bid);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        if (status != Status.OPEN) {
            System.out.println("Không thể bắt đầu: trạng thái hiện tại " + status);
            return;
        }
        this.status = Status.RUNNING;
        System.out.println("▶ Phiên BẮT ĐẦU: " + item.getName());
    }

    public void finish() {
        if (status != Status.RUNNING) {
            System.out.println("Không thể kết thúc: trạng thái hiện tại " + status);
            return;
        }
        this.status = Status.FINISHED;
        if (winnerId != null) {
            System.out.println("⏹ Phiên KẾT THÚC. Người thắng: " + winnerId
                + " | Giá: $" + item.getCurrentPrice());
        } else {
            System.out.println("⏹ Phiên KẾT THÚC. Không có ai đặt giá.");
        }
    }

    public void markPaid() {
        if (status != Status.FINISHED) {
            System.out.println("Chưa thể thanh toán: phiên chưa kết thúc.");
            return;
        }
        this.status = Status.PAID;
        System.out.println("✅ Thanh toán xong. Phiên hoàn tất.");
    }

    public void cancel() {
        if (status == Status.PAID) {
            System.out.println("Không thể huỷ phiên đã thanh toán.");
            return;
        }
        this.status = Status.CANCELED;
        System.out.println("❌ Phiên bị HUỶ: " + item.getName());
    }

    public String getId()                  { return id; }
    public Item getItem()                  { return item; }
    public Status getStatus()             { return status; }
    public String getWinnerId()           { return winnerId; }
    public List<BidTransaction> getBids() { return bids; }
}
