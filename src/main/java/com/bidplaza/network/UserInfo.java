package com.bidplaza.network;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String username;
    private final String role;
    private final String email;
    private final boolean banned;
    private final double balance;

    public UserInfo(String id, String username, String role, String email,
                    boolean banned, double balance) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.email = email;
        this.banned = banned;
        this.balance = balance;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public boolean isBanned() { return banned; }
    public double getBalance() { return balance; }
}
