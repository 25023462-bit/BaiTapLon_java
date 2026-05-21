package com.bidplaza.ui.model;

/**
 * Lưu thông tin user đang đăng nhập - Singleton.
 *
 * Fix Phase 1.4:
 * - BUG CŨ: login() chỉ lưu username + role, userId tự sinh bằng timestamp
 *   → userId không khớp với userId trên server → đặt giá bị nhầm bidderId.
 * - FIX: thêm overload login(username, role, userId) để lưu userId từ server.
 *   Giữ nguyên login(username, role) để tương thích code cũ.
 */
public class UserSession {

    private static UserSession instance;

    private String username;
    private String role;
    private String userId;  // userId thực từ server

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Login với userId từ server (dùng sau khi có LoginResponse).
     */
    public void login(String username, String role, String userId) {
        this.username = username;
        this.role     = role;
        this.userId   = (userId != null) ? userId : username + "-" + System.currentTimeMillis();
    }

    /**
     * Login không có userId (backward compatible - tự sinh userId).
     */
    public void login(String username, String role) {
        login(username, role, null);
    }

    public void logout() {
        this.username = null;
        this.role     = null;
        this.userId   = null;
    }

    public String getUsername() { return username; }
    public String getRole()     { return role; }
    public String getUserId()   { return userId; }
    public boolean isLoggedIn() { return username != null; }
}
