package com.bidplaza.model.user;

import com.bidplaza.model.Entity;
import com.bidplaza.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp User trừu tượng - kế thừa Entity.
 *
 * - Chứa thông tin chung: username, password, email
 * - abstract getRole(): mỗi loại user (Bidder/Seller/Admin)
 *   phải tự khai báo vai trò của mình
 */
public abstract class User extends Entity {

    private String username;
    private String password; // thực tế nên hash, ở đây để đơn giản
    private String email;
    private boolean banned = false;
    private List<Notification> notifications = new ArrayList<>();

    public User(String username, String password, String email) {
        super(); // gọi Entity() để sinh id và createdAt
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters
    public String getUsername() { return username; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    // Setter
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

    public void addNotification(Notification notification) {
        if (notification != null) {
            notifications().add(0, notification);
        }
    }

    public List<Notification> getNotifications() {
        return notifications();
    }

    public long getUnreadCount() {
        return notifications().stream().filter(n -> !n.isRead()).count();
    }

    // Kiểm tra password (đơn giản, chưa hash)
    public boolean checkPassword(String input) {
        return this.password.equals(input);
    }

    private List<Notification> notifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        return notifications;
    }

    // Mỗi class con phải tự trả về vai trò
    public abstract String getRole();

    // Đa hình: ghi đè printInfo() của Entity
    @Override
    public void printInfo() {
        System.out.println("[" + getRole() + "] " + username + " | Email: " + email);
    }
}
