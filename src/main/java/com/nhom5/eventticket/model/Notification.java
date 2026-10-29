package com.nhom5.eventticket.model;

import java.time.LocalDateTime;
/** UML M3: Notification belongs to one User.*/
public class Notification {
    private final String notificationId;
    private final String message;
    private final LocalDateTime createdAt;
    private final User recipient;
    public Notification(String notificationId, String message, LocalDateTime createdAt, User recipient) {
        this.notificationId = java.util.Objects.requireNonNull(notificationId);
        this.message = java.util.Objects.requireNonNull(message);
        this.createdAt = java.util.Objects.requireNonNull(createdAt);
        this.recipient = java.util.Objects.requireNonNull(recipient);
    }
    public String getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getRecipient() { return recipient; }
    public void sendNotification() { System.out.println("[NOTICE to " + recipient.getUsername() + "] " + message); }
    public void update() { throw new UnsupportedOperationException("Chưa hỗ trợ: Observer registration + update"); }
}
