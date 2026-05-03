package com.bidplaza.model.user;

/**
 * Người đặt giá - kế thừa User.
 * Có thêm: số dư tài khoản (balance).
 */
public class Bidder extends User {

    private double balance;

    public Bidder(String username, String password, String email) {
        super(username, password, email);
        this.balance = 0.0;
    }

    public double getBalance() { return balance; }

    // Nạp tiền vào tài khoản
    public void deposit(double amount) {
        this.balance += amount;
    }

    // Kiểm tra có đủ tiền để đặt giá không
    public boolean canBid(double amount) {
        return balance >= amount;
    }

    @Override
    public String getRole() { return "BIDDER"; }
}
