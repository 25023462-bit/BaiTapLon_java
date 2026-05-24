package com.bidplaza.storage;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataStorage {

    private static final String DATA_DIR  = "data";
    private static final String DATA_FILE = DATA_DIR + "/app_data.dat";

    public static AppData load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("[Storage] No data file found, fresh start.");
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
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
            Files.createDirectories(Path.of(DATA_DIR));
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(DATA_FILE))) {
                out.writeObject(new AppData(am, um));
            }
            System.out.println("[Storage] Data saved.");
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
