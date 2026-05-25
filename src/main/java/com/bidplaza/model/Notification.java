package com.bidplaza.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String title;
    private final String message;
    private final String type;
    private final LocalDateTime timestamp;
    private boolean read;
    public Notification(String title, String message, String type) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
