package com.bidplaza.model.item;

import java.time.LocalDateTime;

/**
 * Tác phẩm nghệ thuật - kế thừa Item.
 * Thêm: artist (tác giả), yearCreated (năm sáng tác).
 */
public class Art extends Item {

    private String artist;
    private int yearCreated;

    public Art(String name, String description, double startingPrice,
               LocalDateTime startTime, LocalDateTime endTime,
               String sellerId, String artist, int yearCreated) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    public String getArtist()   { return artist; }
    public int getYearCreated() { return yearCreated; }

    @Override
    public String getCategory() { return "ART"; }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("  Artist: " + artist + " | Year: " + yearCreated);
    }
}
