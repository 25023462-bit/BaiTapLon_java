package com.bidplaza.exception;

/**
 * Ném ra khi giá đặt không hợp lệ.
 * Ví dụ: đặt thấp hơn hoặc bằng giá hiện tại.
 */
public class InvalidBidException extends Exception {
    public InvalidBidException(String message) {
        super(message);
    }
}
