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
 */
public class NotificationService {
    private final InMemoryStore store;
    private final List<NotificationListener> listeners = new ArrayList<>();
    /** Listener UI duy nhất — tránh rò khi mở lại màn Thông báo. */
    private NotificationListener uiListener;

    public NotificationService(InMemoryStore store) {
        this.store = Objects.requireNonNull(store);
    }

    public synchronized void subscribe(NotificationListener listener) {
        Objects.requireNonNull(listener, "listener");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void unsubscribe(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Đăng ký listener cho màn Thông báo (thay listener cũ nếu có).
     * Giúp danh sách tự cập nhật khi có thông báo mới mà không cần bấm Làm mới.
     */
    public synchronized void setUiListener(NotificationListener listener) {
        if (uiListener != null) {
            listeners.remove(uiListener);
        }
        uiListener = listener;
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

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

    public Notification notifyBookingConfirmed(Booking booking) {
        Objects.requireNonNull(booking, "booking");
        String message = "Bạn đã đặt " + booking.getTickets().size()
                + " vé cho \"" + booking.getEvent().getName() + "\". Mã booking: "
                + booking.getBookingId() + ".";
        return notifyUser(booking.getCustomer(), message);
    }

    public Notification notifyBookingCancelled(Booking booking) {
        Objects.requireNonNull(booking, "booking");
        String message = "Booking " + booking.getBookingId()
                + " (\"" + booking.getEvent().getName() + "\") đã được hủy.";
        return notifyUser(booking.getCustomer(), message);
    }

    public Notification notifyCheckInSuccess(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket");
        if (ticket.getBooking() == null || ticket.getBooking().getCustomer() == null) {
            throw new IllegalArgumentException("Vé thiếu thông tin khách hàng để gửi thông báo.");
        }
        String message = "Vé " + ticket.getTicketCode() + " đã check-in thành công tại sự kiện \""
                + ticket.getBooking().getEvent().getName() + "\".";
        return notifyUser(ticket.getBooking().getCustomer(), message);
    }

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
