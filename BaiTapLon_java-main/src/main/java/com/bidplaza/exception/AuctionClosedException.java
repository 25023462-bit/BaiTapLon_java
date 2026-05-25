package com.bidplaza.exception;

/**
 * Ném ra khi cố đặt giá vào phiên đã đóng/chưa mở.
 */
public class AuctionClosedException extends Exception {
    public AuctionClosedException(String message) {
        super(message);
    }
}
