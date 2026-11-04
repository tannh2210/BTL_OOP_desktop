package com.nhom5.eventticket.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UML M3: mỗi Notification gắn với đúng một User nhận.
 * {@link #update()} giữ chữ ký UML; cơ chế Observer đầy đủ nằm ở NotificationService.
 */
public class Notification {
    private final String notificationId;
    private final String message;
    private final LocalDateTime createdAt;
    private final User recipient;

    public Notification(String notificationId, String message, LocalDateTime createdAt, User recipient) {
        this.notificationId = Objects.requireNonNull(notificationId);
        this.message = Objects.requireNonNull(message);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.recipient = Objects.requireNonNull(recipient);
    }

    public String getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getRecipient() { return recipient; }

    /** UML: gửi nội dung tới người nhận (console log cho phiên bản Desktop hiện tại). */
    public void sendNotification() {
        System.out.println("[NOTICE → " + recipient.getUsername() + "] " + message);
    }

    /**
     * UML: hook cập nhật khi có thay đổi liên quan.
     * Milestone 4: nội dung đã gửi qua {@link #sendNotification()} từ NotificationService;
     * Milestone 5 có thể mở rộng đẩy thông báo lên UI.
     */
    public void update() {
        // Giữ chữ ký UML; Observer listeners được NotificationService.publishToListeners gọi riêng.
    }
}
