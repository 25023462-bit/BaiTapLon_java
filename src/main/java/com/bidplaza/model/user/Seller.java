package com.bidplaza.model.user;

/**
 * Người bán - kế thừa User.
 * Có thêm: tên cửa hàng (shopName).
 */
public class Seller extends User {

    private String shopName;

    public Seller(String username, String password, String email, String shopName) {
        super(username, password, email);
        this.shopName = shopName;
    }

    public String getShopName() { return shopName; }

    @Override
    public String getRole() { return "SELLER"; }
}
