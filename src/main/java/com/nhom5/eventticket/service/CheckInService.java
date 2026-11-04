package com.nhom5.eventticket.service;

import com.nhom5.eventticket.exception.DuplicateCheckInException;
import com.nhom5.eventticket.exception.InvalidTicketException;
import com.nhom5.eventticket.exception.NotFoundException;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.CheckIn;
import com.nhom5.eventticket.model.Ticket;
import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.util.Objects;
import java.util.UUID;

/**
 * Người 4 – Check-in bằng mã vé (UC10).
 * <p>
 * Chỉ cập nhật trạng thái sử dụng của Ticket / tạo bản ghi CheckIn.
 * Không tạo/hủy Booking, không trừ/hoàn {@code availableQuantity}.
 */
public class CheckInService {
    private final InMemoryStore store;
    private final NotificationService notifications;

    public CheckInService(InMemoryStore store, NotificationService notifications) {
        this.store = Objects.requireNonNull(store);
        this.notifications = Objects.requireNonNull(notifications);
    }

    /**
     * Tra cứu vé theo mã (chỉ đọc) – không đổi trạng thái.
     * Dùng để Admin xem trước khi xác nhận check-in.
     */
    public Ticket findTicket(String code) {
        String normalized = requireCode(code);
        return store.findTicketByCode(normalized)
                .orElseThrow(() -> new NotFoundException("Mã vé không tồn tại."));
    }

    /**
     * Xác nhận check-in: mỗi vé tối đa một lần (UML Ticket — CheckIn = 1 — 0..1).
     */
    public CheckIn checkIn(String code, String adminUserId) {
        synchronized (store) {
            Admin admin = requireAdmin(adminUserId);
            String normalized = requireCode(code);

            Ticket ticket = store.findTicketByCode(normalized)
                    .orElseThrow(() -> new NotFoundException("Mã vé không tồn tại."));

            if (ticket.isUsed() || store.findCheckInByTicketCode(normalized).isPresent()) {
                throw new DuplicateCheckInException("Vé đã được sử dụng, không thể check-in lần hai.");
            }
            if ("CANCELLED".equals(ticket.getStatus())) {
                throw new InvalidTicketException("Vé đã bị hủy, không thể check-in.");
            }
            if (!ticket.validateTicket()) {
                throw new InvalidTicketException("Vé không hợp lệ (booking chưa xác nhận hoặc vé không còn hiệu lực).");
            }

            CheckIn record = new CheckIn(UUID.randomUUID().toString(), ticket, admin);
            if (!record.checkInTicket()) {
                throw new InvalidTicketException("Không thể xác nhận check-in cho vé này.");
            }

            store.saveCheckIn(record);
            // Chỉ gửi thông báo; không đụng tồn kho / trạng thái Booking.
            notifications.notifyCheckInSuccess(ticket);
            return record;
        }
    }

    private static String requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã vé.");
        }
        return code.trim();
    }

    private Admin requireAdmin(String adminUserId) {
        if (adminUserId == null || adminUserId.isBlank()) {
            throw new IllegalArgumentException("Chưa đăng nhập.");
        }
        User user = store.findUser(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Chưa đăng nhập."));
        if (!(user instanceof Admin admin)) {
            throw new IllegalArgumentException("Chỉ quản trị viên được check-in.");
        }
        return admin;
    }
}
