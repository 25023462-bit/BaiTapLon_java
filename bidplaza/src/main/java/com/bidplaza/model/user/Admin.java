package com.bidplaza.model.user;

/**
 * Quản trị viên - kế thừa User.
 * Có quyền ban user, quản lý hệ thống.
 */
public class Admin extends User {

    public Admin(String username, String password, String email) {
        super(username, password, email);
    }

    // Ví dụ hành động đặc quyền của Admin
    public void banUser(User user) {
        System.out.println("Admin [" + getUsername() + "] đã ban: " + user.getUsername());
    }

    @Override
    public String getRole() { return "ADMIN"; }
}
