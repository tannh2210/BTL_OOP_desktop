package com.nhom5.eventticket.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UML M3: Ticket — CheckIn = 1 — 0..1; một Admin thực hiện check-in.
 * Chỉ ghi nhận việc sử dụng vé; không trừ/hoàn số lượng vé bán.
 */
public class CheckIn {
    private final String checkInId;
    private LocalDateTime checkInTime;
    private String status = "PENDING";
    private final Ticket ticket;
    private final Admin admin;

    public CheckIn(String checkInId, Ticket ticket, Admin admin) {
        this.checkInId = Objects.requireNonNull(checkInId);
        this.ticket = Objects.requireNonNull(ticket);
        this.admin = Objects.requireNonNull(admin);
    }

    public String getCheckInId() { return checkInId; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public String getStatus() { return status; }
    public Ticket getTicket() { return ticket; }
    public Admin getAdmin() { return admin; }

    public boolean validateTicket() {
        return ticket.validateTicket();
    }

    /** Xác nhận check-in: đánh dấu vé USED và ghi thời gian. */
    public boolean checkInTicket() {
        if (!validateTicket()) {
            return false;
        }
        ticket.checkIn();
        checkInTime = LocalDateTime.now();
        status = "SUCCESS";
        return true;
    }
}
