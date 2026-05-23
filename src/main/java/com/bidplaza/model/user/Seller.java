package com.bidplaza.model.user;

/**
 * Người bán - kế thừa User.
 * Có thêm: tên cửa hàng (shopName).
 */
public class Seller extends User {

    private String shopName;
    private final java.util.List<com.bidplaza.model.Review> reviews = new java.util.ArrayList<>();

    public Seller(String username, String password, String email, String shopName) {
        super(username, password, email);
        this.shopName = shopName;
    }

    public String getShopName() { return shopName; }

    @Override
    public String getRole() { return "SELLER"; }

    public void addReview(com.bidplaza.model.Review review) {
        // Only one review per auction per bidder
        reviews.removeIf(r -> r.getAuctionId().equals(review.getAuctionId())
                           && r.getReviewerId().equals(review.getReviewerId()));
        reviews.add(review);
    }

    public java.util.List<com.bidplaza.model.Review> getReviews() {
        return java.util.Collections.unmodifiableList(reviews);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToDouble(com.bidplaza.model.Review::getRating).average().orElse(0.0);
    }

    public String getRatingBadge() {
        double avg = getAverageRating();
        if (avg == 0) return "Chưa có đánh giá";
        if (avg >= 4.5) return "⭐⭐⭐⭐⭐ Xuất sắc";
        if (avg >= 4.0) return "⭐⭐⭐⭐ Tốt";
        if (avg >= 3.0) return "⭐⭐⭐ Bình thường";
        return "⭐⭐ Cần cải thiện";
    }
}
