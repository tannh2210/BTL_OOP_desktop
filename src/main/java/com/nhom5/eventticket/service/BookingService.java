package com.nhom5.eventticket.service;
import com.nhom5.eventticket.factory.TicketFactory;
import com.nhom5.eventticket.model.*;
import com.nhom5.eventticket.repository.InMemoryStore;
import java.time.LocalDateTime;
import java.util.*;
/**
 * Basic booking flow (Người 3).
 * Thông báo đặt/hủy vé gọi API dùng chung của Người 4 ({@link NotificationService}),
 * không tự tạo hệ thống Notification riêng.
 */
public class BookingService {
    private final InMemoryStore store;
    private final TicketFactory factory;
    private final NotificationService notifications;

    public BookingService(InMemoryStore store, TicketFactory factory, NotificationService notifications) {
        this.store = store;
        this.factory = factory;
        this.notifications = Objects.requireNonNull(notifications);
    }

    public Booking bookTicket(String customerId, String eventId, String typeId, int quantity) {
        if (quantity < 1 || quantity > 10) throw new IllegalArgumentException("Mỗi lần đặt từ 1 đến 10 vé.");
        synchronized (store) {
            User user = store.findUser(customerId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
            if (!(user instanceof Customer customer)) throw new IllegalArgumentException("Cần tài khoản khách hàng để đặt vé.");
            Event event = store.findEvent(eventId).orElseThrow(() -> new IllegalArgumentException("Sự kiện không tồn tại."));
            if (!"OPEN".equals(event.getStatus()) || !event.getStartTime().isAfter(LocalDateTime.now()))
                throw new IllegalArgumentException("Sự kiện đã đóng đặt vé.");
            TicketType type = event.findTicketType(typeId);
            if (type == null) throw new IllegalArgumentException("Loại vé không tồn tại.");
            type.reserve(quantity);
            try {
                Booking booking = new Booking("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                         LocalDateTime.now(), customer, event);
                List<Ticket> created = new ArrayList<>();
                for (int i=0;i<quantity;i++) {
                    Ticket ticket = factory.createTicket(type.getName());
                    ticket.assignTicketType(type); booking.addTicket(ticket); created.add(ticket);
                }
                booking.confirmBooking();
                for (Ticket t:created) store.saveTicket(t);
                store.saveBooking(booking);
                notifications.notifyBookingConfirmed(booking);
                return booking;
            } catch (RuntimeException ex) {
                // Best-effort rollback for the starter's single-threaded UI flow.
                type.restore(quantity);
                throw ex;
            }
        }
    }
    public List<Booking> getBookingHistory(String userId) {
        return store.allBookings().stream().filter(b->b.getCustomer().getUserId().equals(userId)).toList();
    }
    public List<Booking> getAllBookings(String adminId) {
        User user = store.findUser(adminId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (!(user instanceof Admin)) throw new IllegalArgumentException("Chỉ quản trị viên được xem toàn bộ booking.");
        return store.allBookings().stream().sorted(Comparator.comparing(Booking::getBookingTime).reversed()).toList();
    }
    public void cancelBooking(String userId, String bookingId) {
        synchronized (store) {
            Booking b = store.findBooking(bookingId).orElseThrow(()->new IllegalArgumentException("Không tìm thấy booking."));
            if (!b.getCustomer().getUserId().equals(userId)) throw new IllegalArgumentException("Booking không thuộc tài khoản này.");

            cancelBookingLocked(b);

        }
    }
    public void cancelBookingAsAdmin(String adminId, String bookingId) {
        synchronized (store) {
            User user = store.findUser(adminId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
            if (!(user instanceof Admin)) throw new IllegalArgumentException("Chỉ quản trị viên được hủy booking hộ.");
            Booking b = store.findBooking(bookingId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking."));
            cancelBookingLocked(b);
        }
    }
    private void cancelBookingLocked(Booking booking) {
        if (!"CONFIRMED".equals(booking.getStatus())) throw new IllegalStateException("Chỉ booking đã xác nhận mới có thể hủy.");
        if (!booking.getEvent().getStartTime().isAfter(LocalDateTime.now()))
            throw new IllegalStateException("Chỉ được hủy booking trước giờ bắt đầu sự kiện.");
        booking.cancelBooking(); // Blocks the whole booking when any ticket has been checked in.
        for (Ticket ticket : booking.getTickets()) ticket.getTicketType().restore(1);
        notifications.notifyBookingCancelled(booking);
    }
}
