package com.bidplaza.factory;

import com.bidplaza.model.item.*;
import java.time.LocalDateTime;

/**
 * ItemFactory - áp dụng Factory Method Pattern.
 *
 * Thay vì gọi new Electronics(...) hoặc new Art(...) trực tiếp,
 * ta dùng ItemFactory.create("electronics", ...) để tạo.
 *
 * Lợi ích: sau này thêm loại mới chỉ cần thêm vào đây,
 * không cần sửa code ở chỗ khác.
 */
public class ItemFactory {

    /**
     * Tạo Item theo type.
     *
     * @param type        "electronics" | "art" | "vehicle"
     * @param name        tên sản phẩm
     * @param description mô tả
     * @param startingPrice giá khởi điểm
     * @param startTime   thời gian bắt đầu
     * @param endTime     thời gian kết thúc
     * @param sellerId    id người bán
     * @return Item tương ứng
     */
    public static Item create(String type, String name, String description,
                              double startingPrice, LocalDateTime startTime,
                              LocalDateTime endTime, String sellerId) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new Electronics(name, description, startingPrice,
                    startTime, endTime, sellerId,
                    "Unknown Brand", "Unknown Model");

            case "art":
                return new Art(name, description, startingPrice,
                    startTime, endTime, sellerId,
                    "Unknown Artist", 2024);

            case "vehicle":
                return new Vehicle(name, description, startingPrice,
                    startTime, endTime, sellerId,
                    "Unknown Make", 2024, 0);

            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
        }
    }
}
