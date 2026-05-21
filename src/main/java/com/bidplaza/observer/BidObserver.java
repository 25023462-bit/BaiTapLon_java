package com.bidplaza.observer;

import com.bidplaza.model.BidTransaction;

/**
 * Observer interface - "người quan sát"
 *
 * Bất kỳ ai muốn được thông báo khi có bid mới
 * đều phải implement interface này.
 *
 * Ví dụ: màn hình UI, logger, auto-bidder...
 * đều là Observer.
 */
public interface BidObserver {

    /**
     * Được gọi tự động khi có bid mới.
     * @param bid thông tin bid vừa xảy ra
     */
    void onBidPlaced(BidTransaction bid);
}
