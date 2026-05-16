package com.bidplaza.ui.model;

/**
 * Lưu thông tin user đang đăng nhập - Singleton.
 * Dùng để các màn hình khác biết ai đang dùng app.
 */
public class UserSession {

    private static UserSession instance;

    private String username;
    private String role;
    private String userId;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(String username, String role) {
        this.username = username;
        this.role = role;
        this.userId = username + "-" + System.currentTimeMillis();
    }

    public void logout() {
        this.username = null;
        this.role = null;
        this.userId = null;
    }

    public String getUsername() { return username; }
    public String getRole()     { return role; }
    public String getUserId()   { return userId; }
    public boolean isLoggedIn() { return username != null; }
}
