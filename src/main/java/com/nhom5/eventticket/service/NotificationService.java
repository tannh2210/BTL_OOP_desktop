package com.nhom5.eventticket.service;

import com.nhom5.eventticket.model.Booking;
import com.nhom5.eventticket.model.Notification;
import com.nhom5.eventticket.model.Ticket;
import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.observer.NotificationListener;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Người 4 – Notification dùng chung (Subject của Observer).
 * <p>
 * Module khác (Booking, Event, Check-in) chỉ gọi các phương thức publish/notify* bên dưới.
 * Không tạo hệ thống Notification riêng.
 * <p>
 * Luồng: tạo {@link Notification} → lưu store → {@code sendNotification()} → báo các listener.
 */
public class NotificationService {
    private final InMemoryStore store;
    private final List<NotificationListener> listeners = new ArrayList<>();

    public NotificationService(InMemoryStore store) {
        this.store = Objects.requireNonNull(store);
    }

    /** Đăng ký Observer (Chapter 10.5: subscribe). */
    public synchronized void subscribe(NotificationListener listener) {
        Objects.requireNonNull(listener, "listener");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /** Hủy đăng ký Observer (Chapter 10.5: unsubscribe). */
    public synchronized void unsubscribe(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * API chung: phát thông báo tới một User.
     * Người 2 / Người 3 gọi phương thức này khi có thay đổi cần báo cho người dùng.
     */
    public Notification notifyUser(User recipient, String message) {
        Objects.requireNonNull(recipient, "recipient");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Nội dung thông báo không được trống.");
        }
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                message.trim(),
                LocalDateTime.now(),
                recipient
        );
        synchronized (store) {
            store.saveNotification(notification);
        }
        notification.sendNotification();
        notification.update();
        publishToListeners(notification);
        return notification;
    }

    /** Thông báo khi đặt vé thành công – Người 3 gọi sau khi tạo Booking. */
    public Notification notifyBookingConfirmed(Booking booking) {
        Objects.requireNonNull(booking, "booking");
        String message = "Bạn đã đặt " + booking.getTickets().size()
                + " vé cho \"" + booking.getEvent().getName() + "\". Mã booking: "
                + booking.getBookingId() + ".";
        return notifyUser(booking.getCustomer(), message);
    }

    /** Thông báo khi hủy vé – Người 3 gọi sau khi hủy Booking. */
    public Notification notifyBookingCancelled(Booking booking) {
        Objects.requireNonNull(booking, "booking");
        String message = "Booking " + booking.getBookingId()
                + " (\"" + booking.getEvent().getName() + "\") đã được hủy.";
        return notifyUser(booking.getCustomer(), message);
    }

    /** Thông báo khi check-in thành công – CheckInService gọi nội bộ. */
    public Notification notifyCheckInSuccess(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket");
        if (ticket.getBooking() == null || ticket.getBooking().getCustomer() == null) {
            throw new IllegalArgumentException("Vé thiếu thông tin khách hàng để gửi thông báo.");
        }
        String message = "Vé " + ticket.getTicketCode() + " đã check-in thành công tại sự kiện \""
                + ticket.getBooking().getEvent().getName() + "\".";
        return notifyUser(ticket.getBooking().getCustomer(), message);
    }

    /** Danh sách thông báo của một user (mới nhất trước). */
    public List<Notification> forUser(String userId) {
        Objects.requireNonNull(userId, "userId");
        return store.allNotifications().stream()
                .filter(n -> n.getRecipient().getUserId().equals(userId))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
    }

    public List<Notification> all() {
        return store.allNotifications().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
    }

    private synchronized void publishToListeners(Notification notification) {
        for (NotificationListener listener : List.copyOf(listeners)) {
            listener.update(notification);
        }
    }
}
