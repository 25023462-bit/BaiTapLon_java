package com.bidplaza.network;

import java.io.Serializable;

/**
 * Request payload sent by a client when registering or logging in.
 */
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final String role;
    private final boolean isRegister;

    public LoginRequest(String username, String password, String role, boolean isRegister) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isRegister = isRegister;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public boolean isRegister() {
        return isRegister;
    }
}
