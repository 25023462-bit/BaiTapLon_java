package com.bidplaza.observer;

import com.bidplaza.model.BidTransaction;

/**
 * Observer cụ thể: in thông báo ra console.
 *
 * Sau này khi có JavaFX, sẽ tạo thêm UINotifier
 * implement BidObserver để cập nhật màn hình.
 * Logic backend không cần thay đổi gì.
 */
public class ConsoleNotifier implements BidObserver {

    private final String name; // tên để phân biệt khi có nhiều observer

    public ConsoleNotifier(String name) {
        this.name = name;
    }

    @Override
    public void onBidPlaced(BidTransaction bid) {
        System.out.println("[" + name + "] 🔔 BID MỚI: $"
            + bid.getAmount()
            + " | Người đặt: " + bid.getBidderId()
            + " | Lúc: " + bid.getTimestamp());
    }
}
