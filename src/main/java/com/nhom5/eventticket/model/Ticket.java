package com.nhom5.eventticket.model;

/** UML M3: parent of VIPTicket/StandardTicket/EconomyTicket.*/
public class Ticket {
    private final String ticketId;
    private final String ticketCode;
    private String status;
    private TicketType ticketType; // UML association: each issued Ticket has one TicketType
    private Booking booking;      // UML composition: a Ticket belongs to one Booking
    public Ticket(String ticketId, String ticketCode) {
        this.ticketId = java.util.Objects.requireNonNull(ticketId);
        this.ticketCode = java.util.Objects.requireNonNull(ticketCode);
        this.status = "VALID";
    }
    public String getTicketId() { return ticketId; }
    public String getTicketCode() { return ticketCode; }
    public String getStatus() { return status; }
    public TicketType getTicketType() { return ticketType; }
    public Booking getBooking() { return booking; }
    public void assignTicketType(TicketType type) { this.ticketType = java.util.Objects.requireNonNull(type); }
    void assignBooking(Booking booking) { this.booking = java.util.Objects.requireNonNull(booking); }
    public boolean validateTicket() {
        return "VALID".equals(status) && booking != null && "CONFIRMED".equals(booking.getStatus());
    }
    public void checkIn() {
        if (!validateTicket()) throw new com.nhom5.eventticket.exception.DuplicateCheckInException("Ticket is not valid or already used");
        status = "USED";
    }
    public void cancel() {
        if (isUsed()) throw new IllegalStateException("Không thể hủy vé đã sử dụng.");
        status = "CANCELLED";
    }
    public boolean isUsed() { return "USED".equals(status); }
}
