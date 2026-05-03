package com.bidplaza.model.item;

import java.time.LocalDateTime;

/**
 * Xe cộ - kế thừa Item.
 * Thêm: make (hãng xe), year (năm sản xuất), mileage (số km).
 */
public class Vehicle extends Item {

    private String make;
    private int year;
    private int mileage;

    public Vehicle(String name, String description, double startingPrice,
                   LocalDateTime startTime, LocalDateTime endTime,
                   String sellerId, String make, int year, int mileage) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.make = make;
        this.year = year;
        this.mileage = mileage;
    }

    public String getMake()  { return make; }
    public int getYear()     { return year; }
    public int getMileage()  { return mileage; }

    @Override
    public String getCategory() { return "VEHICLE"; }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("  Make: " + make + " | Year: " + year + " | KM: " + mileage);
    }
}
