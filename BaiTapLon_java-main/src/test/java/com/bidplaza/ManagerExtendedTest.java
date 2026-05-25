package com.bidplaza;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagerExtendedTest {

    private AuctionManager manager;

    @BeforeEach
    void resetManager() throws Exception {
        Field instanceField = AuctionManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        manager = AuctionManager.getInstance();
    }

    @Test
    void createAuction_addsAuctionAndFindsById() {
        Auction auction = manager.createAuction(createItem("Phone"));

        assertEquals(1, manager.getAllAuctions().size());
        assertSame(auction, manager.findById(auction.getId()));
        assertNull(manager.findById("missing"));
    }

    @Test
    void getAuctionsByStatus_filtersStatus() {
        Auction running = manager.createAuction(createItem("Phone"));
        Auction open = manager.createAuction(createItem("Laptop"));
        running.start();

        assertEquals(1, manager.getAuctionsByStatus(Auction.Status.RUNNING).size());
        assertSame(running, manager.getAuctionsByStatus(Auction.Status.RUNNING).get(0));
        assertEquals(1, manager.getAuctionsByStatus(Auction.Status.OPEN).size());
        assertSame(open, manager.getAuctionsByStatus(Auction.Status.OPEN).get(0));
    }

    @Test
    void addUser_andFindUserByUsername_workTogether() {
        User user = new Bidder("alice", "secret", "alice@test.local");

        manager.addUser(user);

        assertSame(user, manager.findUserByUsername("alice"));
        assertNull(manager.findUserByUsername("missing"));
        assertTrue(manager.getAllUsers().contains(user));
    }

    private Item createItem(String name) {
        LocalDateTime now = LocalDateTime.now();
        return ItemFactory.create(
            "electronics", name, "desc", 100.0, now, now.plusHours(1), "seller-1");
    }
}
