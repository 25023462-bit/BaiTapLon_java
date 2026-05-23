package com.bidplaza;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import com.bidplaza.storage.AppData;
import com.bidplaza.storage.DataStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataStorageTest {

    @BeforeEach
    void setUp() {
        DataStorage.clear();
    }

    @AfterEach
    void tearDown() {
        DataStorage.clear();
    }

    @Test
    void save_thenLoad_returnsManager() {
        AuctionManager manager = AuctionManager.getInstance();
        UserManager userManager = UserManager.getInstance();
        DataStorage.save(manager, userManager);

        AppData loaded = DataStorage.load();
        assertNotNull(loaded);
        assertNotNull(loaded.getAuctionManager());
    }

    @Test
    void load_noFile_returnsNull() {
        DataStorage.clear();
        AppData loaded = DataStorage.load();
        assertTrue(loaded == null);
    }

    @Test
    void save_writesFileToDisk() {
        AuctionManager manager = AuctionManager.getInstance();
        UserManager userManager = UserManager.getInstance();
        DataStorage.save(manager, userManager);

        File file = new File("data/app_data.dat");
        assertTrue(file.exists());
    }
}