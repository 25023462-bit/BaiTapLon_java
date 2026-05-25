package com.bidplaza;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import com.bidplaza.storage.AppData;
import com.bidplaza.storage.DataStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataStorageTest {

    @TempDir
    Path tempDir;
    private String previousDataDir;
    private String previousDataFile;

    @BeforeEach
    void setUp() {
        previousDataDir = System.getProperty("bidplaza.data.dir");
        previousDataFile = System.getProperty("bidplaza.data.file");
        System.setProperty("bidplaza.data.dir", tempDir.resolve("data").toString());
        DataStorage.clear();
    }

    @AfterEach
    void tearDown() {
        DataStorage.clear();
        restoreProperty("bidplaza.data.dir", previousDataDir);
        restoreProperty("bidplaza.data.file", previousDataFile);
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

        File file = tempDir.resolve("data").resolve("app_data.dat").toFile();
        assertTrue(file.exists());
    }

    private void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
