package com.eventticketing.booking.model;

public abstract class Ticket {
    private final String ticketId;
    private final String ticketCode;
    private final String ticketTypeId;
    private final String ticketTypeName;
    private TicketStatus status = TicketStatus.VALID;

    protected Ticket(String ticketId, String ticketCode,
                     String ticketTypeId, String ticketTypeName) {
        if (blank(ticketId) || blank(ticketCode) || blank(ticketTypeId)
                || blank(ticketTypeName)) {
            throw new IllegalArgumentException("Ticket information cannot be empty.");
        }
        this.ticketId = ticketId;
        this.ticketCode = ticketCode;
        this.ticketTypeId = ticketTypeId;
        this.ticketTypeName = ticketTypeName;
    }

    public String getTicketId() { return ticketId; }
    public String getTicketCode() { return ticketCode; }
    public String getTicketTypeId() { return ticketTypeId; }
    public String getTicketTypeName() { return ticketTypeName; }
    public TicketStatus getStatus() { return status; }
    public boolean validateTicket() { return status == TicketStatus.VALID; }

    public void cancel() {
        if (status == TicketStatus.CANCELLED)
            throw new IllegalStateException("Ticket already cancelled.");
        status = TicketStatus.CANCELLED;
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
