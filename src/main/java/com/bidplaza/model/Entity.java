package com.bidplaza.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lớp cha gốc của toàn bộ hệ thống.
 * Mọi object (User, Item...) đều kế thừa từ đây.
 *
 * - abstract: không thể tạo new Entity() trực tiếp
 * - id: tự động sinh UUID duy nhất cho mỗi object
 * - createdAt: ghi lại thời điểm tạo
 */
public abstract class Entity {

    private final String id;
    private final LocalDateTime createdAt;

    // Constructor: mỗi khi tạo object con, id và createdAt tự động gán
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // abstract: buộc mọi class con phải tự implement printInfo()
    public abstract void printInfo();
}
