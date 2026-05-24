package com.bidplaza.ui.model;

import com.bidplaza.model.Notification;

import java.util.ArrayList;
import java.util.List;

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

    private static com.bidplaza.model.user.User currentUser;
    private static final List<Notification> notifications = new ArrayList<>();

    private String username;
    private String role;
    private String userId;  // userId thực từ server

    public static void setCurrentUser(com.bidplaza.model.user.User user) {
        currentUser = user;
        if (user != null) {
            getInstance().login(user.getUsername(), user.getRole(), user.getId());
        } else {
            getInstance().logout();
        }
    }

    public static com.bidplaza.model.user.User getCurrentUser() {
        return currentUser;
    }

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
        notifications.clear();
    }

    public String getUsername() { return username; }
    public String getRole()     { return role; }
    public String getUserId()   { return userId; }
    public boolean isLoggedIn() { return username != null; }

    public static void addNotification(Notification notification) {
        if (notification != null) {
            notifications.add(0, notification);
        }
    }

    public static void setNotifications(List<Notification> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
    }

    public static List<Notification> getNotifications() {
        return notifications;
    }

    public static long getUnreadCount() {
        return notifications.stream().filter(n -> !n.isRead()).count();
    }
}
