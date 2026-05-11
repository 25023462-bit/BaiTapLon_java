package com.bidplaza.storage;

import com.bidplaza.manager.AuctionManager;
import java.io.*;
import java.nio.file.*;

public class DataStorage {

    private static final String DATA_DIR  = "data";
    private static final String DATA_FILE = DATA_DIR + "/auction_data.dat";

    public static AuctionManager load() {
        System.out.println("[Storage] Đang load...");
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("[Storage] Chưa có file, khởi tạo mới.");
            return AuctionManager.getInstance();
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            AuctionManager manager = (AuctionManager) in.readObject();
            System.out.println("[Storage] Load thành công!");
            return manager;
        } catch (Exception e) {
            System.err.println("[Storage] Lỗi load: " + e.getClass().getName());
            e.printStackTrace();
            return AuctionManager.getInstance();
        }
    }

    public static AuctionManager load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("[Storage] Chưa có dữ liệu, khởi tạo mới.");
            return AuctionManager.getInstance();
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            AuctionManager manager = (AuctionManager) in.readObject();
            System.out.println("[Storage] Đã tải dữ liệu thành công.");
            return manager;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Storage] Tải thất bại: " + e.getMessage());
            return AuctionManager.getInstance();
        }
    }

    public static void clear() {
        new File(DATA_FILE).delete();
        System.out.println("[Storage] Đã xóa dữ liệu.");
    }
}