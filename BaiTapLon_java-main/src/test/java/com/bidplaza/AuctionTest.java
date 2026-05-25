package com.bidplaza;

import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.factory.ItemFactory;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho logic đấu giá.
 *
 * Mỗi @Test là 1 kịch bản cụ thể.
 * @BeforeEach: chạy trước MỖI test để reset trạng thái.
 *
 * Công thức test: Arrange → Act → Assert
 * - Arrange: chuẩn bị dữ liệu
 * - Act:     thực hiện hành động
 * - Assert:  kiểm tra kết quả đúng không
 */
class AuctionTest {

    private Auction auction;
    private Item item;

    @BeforeEach
    void setUp() {
        // Tạo item và auction mới trước mỗi test
        item = ItemFactory.create(
            "electronics", "Test Phone", "Desc",
            1000.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "seller-001"
        );
        auction = new Auction(item);
    }

    // ==================== TEST ĐẶT GIÁ HỢP LỆ ====================

    @Test
    void placeBid_validAmount_shouldSucceed() throws Exception {
        auction.start();
        auction.placeBid("bidder-1", 1200.0);

        assertEquals(1200.0, item.getCurrentPrice());
        assertEquals(1, auction.getBids().size());
        assertEquals("bidder-1", auction.getWinnerId());
    }

    @Test
    void placeBid_multipleBids_highestShouldWin() throws Exception {
        auction.start();
        auction.placeBid("bidder-1", 1200.0);
        auction.placeBid("bidder-2", 1500.0);
        auction.placeBid("bidder-1", 1800.0);

        assertEquals(1800.0, item.getCurrentPrice());
        assertEquals("bidder-1", auction.getWinnerId());
        assertEquals(3, auction.getBids().size());
    }

    // ==================== TEST GIÁ KHÔNG HỢP LỆ ====================

    @Test
    void placeBid_lowerThanCurrent_shouldThrowInvalidBidException() {
        auction.start();

        // assertThrows: kiểm tra method phải ném đúng loại exception
        assertThrows(InvalidBidException.class, () -> {
            auction.placeBid("bidder-1", 500.0); // thấp hơn giá khởi điểm 1000
        });
    }

    @Test
    void placeBid_equalToCurrent_shouldThrowInvalidBidException() {
        auction.start();

        assertThrows(InvalidBidException.class, () -> {
            auction.placeBid("bidder-1", 1000.0); // bằng giá hiện tại, không được
        });
    }

    // ==================== TEST TRẠNG THÁI PHIÊN ====================

    @Test
    void placeBid_whenNotRunning_shouldThrowAuctionClosedException() {
        // Chưa gọi start() → status = OPEN
        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid("bidder-1", 1200.0);
        });
    }

    @Test
    void placeBid_afterFinished_shouldThrowAuctionClosedException() throws Exception {
        auction.start();
        auction.placeBid("bidder-1", 1200.0);
        auction.finish();

        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid("bidder-2", 1500.0); // phiên đã kết thúc
        });
    }

    @Test
    void finish_afterStart_statusShouldBeFinished() throws Exception {
        auction.start();
        auction.placeBid("bidder-1", 1200.0);
        auction.finish();

        assertEquals(Auction.Status.FINISHED, auction.getStatus());
    }

    @Test
    void markPaid_afterFinished_statusShouldBePaid() throws Exception {
        auction.start();
        auction.placeBid("bidder-1", 1200.0);
        auction.finish();
        auction.markPaid();

        assertEquals(Auction.Status.PAID, auction.getStatus());
    }

    @Test
    void cancel_afterPaid_statusShouldStillBePaid() throws Exception {
        auction.start();
        auction.placeBid("bidder-1", 1200.0);
        auction.finish();
        auction.markPaid();
        auction.cancel(); // không được huỷ sau khi đã thanh toán

        assertEquals(Auction.Status.PAID, auction.getStatus()); // vẫn PAID
    }

    // ==================== TEST KHÔNG CÓ BID ====================

    @Test
    void finish_withNoBids_winnerShouldBeNull() {
        auction.start();
        auction.finish();

        assertNull(auction.getWinnerId());
        assertEquals(0, auction.getBids().size());
    }

    // ==================== TEST CONCURRENCY ====================

    @Test
    void placeBid_concurrently_shouldNotLoseUpdate() throws Exception {
        auction.start();

        // 10 thread đặt giá đồng thời
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final double amount = 1000 + (i + 1) * 100.0; // 1100, 1200, ..., 2000
            threads[i] = new Thread(() -> {
                try {
                    auction.placeBid("bidder-x", amount);
                } catch (Exception ignored) {}
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        // Giá cuối phải là một trong các mức đã đặt, không bị corrupt
        assertTrue(item.getCurrentPrice() >= 1100.0);
        assertTrue(item.getCurrentPrice() <= 2000.0);
    }
}
