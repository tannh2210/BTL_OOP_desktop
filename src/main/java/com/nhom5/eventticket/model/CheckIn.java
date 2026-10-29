package com.nhom5.eventticket.model;

import java.time.LocalDateTime;
/** UML M3: Ticket -- 0..1 CheckIn; one Admin performs it.*/
public class CheckIn {
    private final String checkInId;
    private LocalDateTime checkInTime;
    private String status = "PENDING";
    private final Ticket ticket;
    private final Admin admin;
    public CheckIn(String checkInId, Ticket ticket, Admin admin) {
        this.checkInId = java.util.Objects.requireNonNull(checkInId);
        this.ticket = java.util.Objects.requireNonNull(ticket);
        this.admin = java.util.Objects.requireNonNull(admin);
    }
    public String getCheckInId() { return checkInId; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public String getStatus() { return status; }
    public Ticket getTicket() { return ticket; }
    public Admin getAdmin() { return admin; }
    public boolean validateTicket() { return ticket.validateTicket(); }
    public boolean checkInTicket() {
        if (!validateTicket()) return false;
        ticket.checkIn();
        checkInTime = LocalDateTime.now();
        status = "SUCCESS";
        return true;
    }
}
