package com.bidplaza.observer;

/**
 * Observable interface - "đối tượng được quan sát"
 *
 * Auction implement interface này để:
 * - Cho phép đăng ký observer (addObserver)
 * - Cho phép huỷ đăng ký (removeObserver)
 * - Thông báo tất cả observer khi có sự kiện (notifyObservers)
 */
public interface AuctionObservable {

    void addObserver(BidObserver observer);

    void removeObserver(BidObserver observer);

    void notifyObservers(com.bidplaza.model.BidTransaction bid);
}
