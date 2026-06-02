package com.bidplaza.model.item;

import com.bidplaza.model.Entity;
import java.time.LocalDateTime;

/**
 * Lớp Item trừu tượng - kế thừa Entity.
 *
 * Chứa thông tin chung của mọi sản phẩm đấu giá:
 * - name, description: tên và mô tả
 * - startingPrice: giá khởi điểm
 * - currentPrice: giá hiện tại cao nhất
 * - startTime, endTime: thời gian phiên đấu giá
 * - sellerId: id của người bán
 */
public abstract class Item extends Entity {

    private String name;
    private String description;
    private final double startingPrice;
    private double currentPrice;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String sellerId;

    public Item(String name, String description, double startingPrice,
                LocalDateTime startTime, LocalDateTime endTime, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice; // ban đầu giá hiện tại = giá khởi điểm
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
    }

    // Getters
    public String getName()            { return name; }
    public String getDescription()     { return description; }
    public double getStartingPrice()   { return startingPrice; }
    public double getCurrentPrice()    { return currentPrice; }
    public LocalDateTime getStartTime(){ return startTime; }
    public LocalDateTime getEndTime()  { return endTime; }
    public String getSellerId()        { return sellerId; }

    // Setter: chỉ dùng khi có bid mới hợp lệ
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    // Mỗi class con tự khai báo loại sản phẩm
    public abstract String getCategory();

    @Override
    public void printInfo() {
        System.out.println("[" + getCategory() + "] " + name
            + " | Khởi điểm: $" + startingPrice
            + " | Hiện tại: $" + currentPrice);
    }
}
