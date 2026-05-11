package com.bidplaza.manager;

import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import com.bidplaza.model.user.User;

/**
 * AuctionManager - áp dụng Singleton Pattern.
 *
 * Singleton nghĩa là: toàn bộ chương trình chỉ có DUY NHẤT
 * 1 AuctionManager. Không ai có thể tạo thêm cái khác.
 *
 * Cách dùng: AuctionManager.getInstance()
 * Không dùng: new AuctionManager()  ← sẽ lỗi compile
 */
public class AuctionManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // instance duy nhất, static để thuộc về class chứ không thuộc object
    private static AuctionManager instance;

    private final List<Auction> auctions;
    private final List<User> users;

    // private constructor: ngăn không cho bên ngoài gọi new AuctionManager()
    private AuctionManager() {
        this.auctions = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    /**
     * Lấy instance duy nhất.
     * synchronized: thread-safe khi nhiều thread cùng gọi lần đầu.
     */
    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager(); // tạo lần đầu và duy nhất
        }
        return instance;
    }

    // Tạo phiên đấu giá mới
    public Auction createAuction(Item item) {
        Auction auction = new Auction(item);
        auctions.add(auction);
        return auction;
    }

    // Lấy danh sách tất cả phiên
    public List<Auction> getAllAuctions() {
        return auctions;
    }

    // Tìm phiên theo id
    public Auction findById(String id) {
        return auctions.stream()
            .filter(a -> a.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    // Thêm người dùng mới
    public void addUser(User user) {
        users.add(user);
    }

    // Tìm user theo username
    public User findUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // Lấy danh sách tất cả user
    public List<User> getAllUsers() {
        return users;
    }
    protected Object readResolve() {
        instance = this;
        return instance;
    }
}
