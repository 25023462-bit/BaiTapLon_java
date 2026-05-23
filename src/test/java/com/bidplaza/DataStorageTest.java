package com.bidplaza;

import com.bidplaza.manager.AuctionManager;
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
        DataStorage.save(manager);

        AuctionManager loaded = DataStorage.load();
        assertNotNull(loaded);
    }

    @Test
    void load_noFile_returnsNewManager() {
        DataStorage.clear();
        AuctionManager loaded = DataStorage.load();
        assertNotNull(loaded);
    }

    @Test
    void save_writesFileToDisk() {
        AuctionManager manager = AuctionManager.getInstance();
        DataStorage.save(manager);

        File file = new File("data/auction_data.dat");
        assertTrue(file.exists());
    }
}
