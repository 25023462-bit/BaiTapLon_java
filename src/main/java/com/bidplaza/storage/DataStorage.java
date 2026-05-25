package com.bidplaza.storage;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataStorage {

    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DEFAULT_DATA_FILE = "app_data.dat";

    public static AppData load() {
        Path file = dataFilePath();
        if (!Files.exists(file)) {
            System.out.println("[Storage] No data file found, fresh start.");
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            AppData data = (AppData) in.readObject();
            System.out.println("[Storage] Data loaded successfully.");
            return data;
        } catch (Exception e) {
            System.err.println("[Storage] Load failed: " + e.getMessage());
            return null;
        }
    }

    public static void save(AuctionManager am, UserManager um) {
        try {
            Path file = dataFilePath();
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
                out.writeObject(new AppData(am, um));
            }
            System.out.println("[Storage] Data saved.");
        } catch (IOException e) {
            System.err.println("[Storage] Save failed: " + e.getMessage());
        }
    }

    public static void clear() {
        Path file = dataFilePath();
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("[Storage] Could not delete data file: " + e.getMessage());
        }
    }

    public static Path dataFilePath() {
        String explicitFile = System.getProperty("bidplaza.data.file");
        if (explicitFile != null && !explicitFile.isBlank()) {
            return Path.of(explicitFile);
        }

        String dataDir = System.getProperty("bidplaza.data.dir", DEFAULT_DATA_DIR);
        return Path.of(dataDir, DEFAULT_DATA_FILE);
    }
}
