package com.bidplaza.network;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client kết nối đến Server để đặt giá.
 *
 * Chạy AuctionServer trước, sau đó chạy AuctionClient.
 * Có thể chạy nhiều AuctionClient cùng lúc để test concurrency.
 */
public class AuctionClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(HOST, PORT);
        System.out.println("Đã kết nối đến server " + HOST + ":" + PORT);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream());

        // Thread riêng để lắng nghe message từ server (realtime update)
        Thread listener = new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    System.out.println("[SERVER] " + msg.getType() + ": " + msg.getInfo());
                }
            } catch (Exception e) {
                System.out.println("Mất kết nối với server.");
            }
        });
        listener.setDaemon(true); // tắt khi main thread kết thúc
        listener.start();

        // Đọc lệnh từ người dùng
        Scanner scanner = new Scanner(System.in);
        System.out.println("Nhập: <auctionId> <bidderId> <amount> (hoặc 'exit' để thoát)");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) break;

            String[] parts = line.split(" ");
            if (parts.length != 3) {
                System.out.println("Sai định dạng. Ví dụ: abc-123 bidder-1 1500");
                continue;
            }

            try {
                String auctionId = parts[0];
                String bidderId  = parts[1];
                double amount    = Double.parseDouble(parts[2]);

                Message bid = Message.placeBid(auctionId, bidderId, amount);
                out.writeObject(bid);
                out.flush();

            } catch (NumberFormatException e) {
                System.out.println("Số tiền không hợp lệ.");
            }
        }

        socket.close();
        System.out.println("Đã ngắt kết nối.");
    }
}
