package com.bidplaza.manager;

import com.bidplaza.exception.AuthenticationException;
import com.bidplaza.model.user.Admin;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.Seller;
import com.bidplaza.model.user.User;
import java.util.HashMap;

public class UserManager {

    private static UserManager instance;

    private final HashMap<String, User> users;

    private UserManager() {
        this.users = new HashMap<>();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User register(String username, String password, String role)
            throws AuthenticationException {
        if (users.containsKey(username)) {
            throw new AuthenticationException("Username already exists");
        }

        User user = createUser(username, password, role);
        users.put(username, user);
        return user;
    }

    public User login(String username, String password) throws AuthenticationException {
        User user = users.get(username);
        if (user == null || !user.checkPassword(password)) {
            throw new AuthenticationException("Invalid username or password");
        }
        return user;
    }

    public User findByUsername(String username) {
        return users.get(username);
    }

    private User createUser(String username, String password, String role)
            throws AuthenticationException {
        String email = username + "@bidplaza.local";

        if (role == null) {
            throw new AuthenticationException("Invalid role");
        }

        switch (role.toUpperCase()) {
            case "BIDDER":
                return new Bidder(username, password, email);
            case "SELLER":
                return new Seller(username, password, email, username + " Shop");
            case "ADMIN":
                return new Admin(username, password, email);
            default:
                throw new AuthenticationException("Invalid role");
        }
    }
}
