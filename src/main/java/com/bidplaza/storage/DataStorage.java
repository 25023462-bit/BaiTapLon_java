package com.bidplaza.storage;

import com.bidplaza.manager.AuctionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataStorage {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/auction_data.dat";

    public static AuctionManager load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("[Storage] No data file found, creating a new manager.");
            return AuctionManager.getInstance();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            AuctionManager manager = (AuctionManager) in.readObject();
            System.out.println("[Storage] Data loaded successfully.");
            return manager;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Storage] Load failed: " + e.getMessage());
            return AuctionManager.getInstance();
        }
    }

    public static void save(AuctionManager manager) {
        try {
            Files.createDirectories(Path.of(DATA_DIR));
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                out.writeObject(manager);
            }
            System.out.println("[Storage] Data saved successfully.");
        } catch (IOException e) {
            System.err.println("[Storage] Save failed: " + e.getMessage());
        }
    }

    public static void clear() {
        File file = new File(DATA_FILE);
        if (file.exists() && !file.delete()) {
            System.err.println("[Storage] Could not delete data file.");
        }
    }
}
