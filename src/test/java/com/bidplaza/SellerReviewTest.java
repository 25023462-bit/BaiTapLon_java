package com.bidplaza;

import com.bidplaza.model.Review;
import com.bidplaza.model.user.Seller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SellerReviewTest {

    @Test
    void review_exposesConstructorValues() {
        Review review = new Review("reviewer-1", "Alice", 4.5, "good", "auction-1");

        assertEquals("reviewer-1", review.getReviewerId());
        assertEquals("Alice", review.getReviewerUsername());
        assertEquals(4.5, review.getRating());
        assertEquals("good", review.getComment());
        assertEquals("auction-1", review.getAuctionId());
        assertNotNull(review.getTimestamp());
    }

    @Test
    void seller_addReview_replacesSameReviewerAuctionAndAverages() {
        Seller seller = new Seller("seller", "pass", "seller@test.local", "Shop");
        seller.addReview(new Review("reviewer-1", "Alice", 5.0, "great", "auction-1"));
        seller.addReview(new Review("reviewer-1", "Alice", 3.0, "updated", "auction-1"));
        seller.addReview(new Review("reviewer-2", "Bob", 4.0, "good", "auction-2"));

        assertEquals("Shop", seller.getShopName());
        assertEquals("SELLER", seller.getRole());
        assertEquals(2, seller.getReviews().size());
        assertEquals(3.5, seller.getAverageRating());
        assertThrows(UnsupportedOperationException.class,
            () -> seller.getReviews().clear());
    }

    @Test
    void seller_ratingBadge_coversRatingRanges() {
        Seller seller = new Seller("seller", "pass", "seller@test.local", "Shop");
        assertNotNull(seller.getRatingBadge());

        seller.addReview(new Review("reviewer-1", "Alice", 5.0, "great", "auction-1"));
        assertNotNull(seller.getRatingBadge());

        seller.addReview(new Review("reviewer-2", "Bob", 4.0, "good", "auction-2"));
        assertNotNull(seller.getRatingBadge());

        seller.addReview(new Review("reviewer-3", "Carol", 1.5, "ok", "auction-3"));
        assertNotNull(seller.getRatingBadge());

        seller.addReview(new Review("reviewer-4", "Dan", 1.0, "bad", "auction-4"));
        assertNotNull(seller.getRatingBadge());
    }
}
