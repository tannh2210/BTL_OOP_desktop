package com.nhom5.eventticket.service;

import com.nhom5.eventticket.exception.DuplicateCheckInException;
import com.nhom5.eventticket.exception.NotFoundException;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.CheckIn;
import com.nhom5.eventticket.model.Notification;
import com.nhom5.eventticket.model.Ticket;
import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.time.LocalDateTime;
import java.util.UUID;

/** Check-in by ticket code. A ticket can be checked in only once. */
public class CheckInService {
    private final InMemoryStore store;

    public CheckInService(InMemoryStore store) { this.store = store; }

    public CheckIn checkIn(String code, String adminUserId) {
        synchronized (store) {
            if (code == null || code.isBlank()) throw new IllegalArgumentException("Vui lòng nhập mã vé.");
            User user = store.findUser(adminUserId).orElseThrow(() -> new IllegalArgumentException("Chưa đăng nhập."));
            if (!(user instanceof Admin admin)) throw new IllegalArgumentException("Chỉ quản trị viên được check-in.");
            String normalized = code.trim();
            Ticket ticket = store.findTicketByCode(normalized)
                    .orElseThrow(() -> new NotFoundException("Mã vé không tồn tại."));
            if (ticket.isUsed() || store.findCheckInByTicketCode(normalized).isPresent())
                throw new DuplicateCheckInException("Vé đã được sử dụng.");
            if (!ticket.validateTicket()) throw new IllegalArgumentException("Vé không hợp lệ.");
            CheckIn record = new CheckIn(UUID.randomUUID().toString(), ticket, admin);
            if (!record.checkInTicket()) throw new IllegalArgumentException("Vé không hợp lệ.");
            store.saveCheckIn(record);
            store.saveNotification(new Notification(UUID.randomUUID().toString(),
                    "Vé " + normalized + " đã check-in thành công.", LocalDateTime.now(), ticket.getBooking().getCustomer()));
            return record;
        }
    }
}
