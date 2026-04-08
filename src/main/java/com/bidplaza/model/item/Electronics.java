package com.bidplaza.model.item;

import java.time.LocalDateTime;

/**
 * Sản phẩm điện tử - kế thừa Item.
 * Thêm: brand (hãng), model (dòng máy).
 */
public class Electronics extends Item {

    private String brand;
    private String model;

    public Electronics(String name, String description, double startingPrice,
                       LocalDateTime startTime, LocalDateTime endTime,
                       String sellerId, String brand, String model) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.brand = brand;
        this.model = model;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }

    @Override
    public String getCategory() { return "ELECTRONICS"; }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("  Brand: " + brand + " | Model: " + model);
    }
}
