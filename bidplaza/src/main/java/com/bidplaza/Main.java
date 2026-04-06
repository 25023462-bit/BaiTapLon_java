package com.bidplaza;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.user.*;
import java.time.LocalDateTime;

/**
 * Main - chạy thử để kiểm tra toàn bộ code tuần 6.
 */
public class Main {

    public static void main(String[] args) {

        // 1. Tạo users
        Bidder alice  = new Bidder("alice", "pass123", "alice@example.com");
        Bidder bob    = new Bidder("bob", "pass456", "bob@example.com");
        Seller seller = new Seller("seller1", "sell123", "seller@example.com", "MyShop");
        Admin  admin  = new Admin("admin", "admin123", "admin@example.com");

        alice.deposit(5000);
        bob.deposit(3000);

        // In thông tin user (đa hình: gọi printInfo() chung)
        System.out.println("=== USERS ===");
        alice.printInfo();
        bob.printInfo();
        seller.printInfo();
        admin.printInfo();

        // 2. Tạo Item bằng Factory Method
        Item phone = ItemFactory.create(
            "electronics",
            "iPhone 15 Pro",
            "Điện thoại mới 100%",
            1000.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            seller.getId()
        );

        Item painting = ItemFactory.create(
            "art",
            "Tranh sơn dầu",
            "Tác phẩm độc bản",
            500.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2),
            seller.getId()
        );

        System.out.println("\n=== ITEMS ===");
        phone.printInfo();
        painting.printInfo();

        // 3. Tạo phiên đấu giá bằng Singleton AuctionManager
        AuctionManager manager = AuctionManager.getInstance();
        Auction auction = manager.createAuction(phone);

        System.out.println("\n=== ĐẤU GIÁ ===");
        auction.start();

        // 4. Đặt giá
        auction.placeBid(alice.getId(), 1200.0);  // thành công
        auction.placeBid(bob.getId(),   1100.0);  // thất bại (thấp hơn 1200)
        auction.placeBid(bob.getId(),   1500.0);  // thành công

        // 5. Kết thúc
        auction.finish();

        System.out.println("\nTổng số bid: " + auction.getBids().size());
        auction.getBids().forEach(System.out::println);
    }
}
