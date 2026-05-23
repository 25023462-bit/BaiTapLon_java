package com.bidplaza.storage;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import java.io.Serializable;

public class AppData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AuctionManager auctionManager;
    private final UserManager userManager;

    public AppData(AuctionManager auctionManager, UserManager userManager) {
        this.auctionManager = auctionManager;
        this.userManager = userManager;
    }

    public AuctionManager getAuctionManager() { return auctionManager; }
    public UserManager getUserManager() { return userManager; }
}