package com.bidplaza.network;

import java.io.Serializable;

public class DepositRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final double amount;

    public DepositRequest(String userId, double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
}
