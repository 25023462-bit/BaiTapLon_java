package com.bidplaza.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model cho 1 hàng trong TableView danh sách đấu giá.
 *
 * StringProperty: JavaFX cần kiểu này để TableView
 * tự động cập nhật khi dữ liệu thay đổi.
 */
public class AuctionItem {

    private final String id;
    private final StringProperty name;
    private final StringProperty category;
    private final StringProperty startPrice;
    private final StringProperty currentPrice;
    private final StringProperty status;
    private final StringProperty endTime;

    public AuctionItem(String id, String name, String category,
                       String startPrice, String currentPrice,
                       String status, String endTime) {
        this.id           = id;
        this.name         = new SimpleStringProperty(name);
        this.category     = new SimpleStringProperty(category);
        this.startPrice   = new SimpleStringProperty(startPrice);
        this.currentPrice = new SimpleStringProperty(currentPrice);
        this.status       = new SimpleStringProperty(status);
        this.endTime      = new SimpleStringProperty(endTime);
    }

    public String getId()             { return id; }
    public String getName()           { return name.get(); }
    public String getCategory()       { return category.get(); }
    public String getStartPrice()     { return startPrice.get(); }
    public String getCurrentPrice()   { return currentPrice.get(); }
    public String getStatus()         { return status.get(); }
    public String getEndTime()        { return endTime.get(); }

    public void setCurrentPrice(String price) { currentPrice.set(price); }
    public void setStatus(String s)           { status.set(s); }

    public StringProperty currentPriceProperty() { return currentPrice; }
    public StringProperty statusProperty()        { return status; }
}
