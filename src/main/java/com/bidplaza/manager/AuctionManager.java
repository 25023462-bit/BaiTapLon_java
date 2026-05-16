package com.bidplaza.manager;

import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.user.User;
import java.util.ArrayList;
import java.util.List;

/**
 * AuctionManager applies the Singleton pattern for shared auction state.
 */
public class AuctionManager {

    private static AuctionManager instance;

    private final List<Auction> auctions;
    private final List<User> users;

    private AuctionManager() {
        this.auctions = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public Auction createAuction(Item item) {
        Auction auction = new Auction(item);
        auctions.add(auction);
        return auction;
    }

    public List<Auction> getAllAuctions() {
        return auctions;
    }

    public Auction findById(String id) {
        return auctions.stream()
            .filter(a -> a.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public List<Auction> getAuctionsByStatus(Auction.Status status) {
        return auctions.stream()
            .filter(a -> a.getStatus() == status)
            .collect(java.util.stream.Collectors.toList());
    }

    public void addUser(User user) {
        users.add(user);
    }

    public User findUserByUsername(String username) {
        return users.stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }

    public List<User> getAllUsers() {
        return users;
    }
}
