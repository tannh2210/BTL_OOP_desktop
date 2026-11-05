package com.nhom5.eventticket.observer;

import com.nhom5.eventticket.model.Notification;

/**
 * Observer (Chapter 10.5 – Subscriber).
 * Module khác có thể đăng ký để nhận thông báo khi hệ thống phát sự kiện.
 * Milestone 5 sẽ mở rộng thêm kênh gửi (email, UI push, ...).
 */
@FunctionalInterface
public interface NotificationListener {
    void update(Notification notification);
}
