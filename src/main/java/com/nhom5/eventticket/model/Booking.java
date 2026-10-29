package com.nhom5.eventticket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** UML M3: Booking belongs to Customer/Event and composes 1..* Tickets.*/
public class Booking {
    private final String bookingId;
    private final LocalDateTime bookingTime;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String status = "PENDING";
    private final Customer customer;
    private final Event event;
    private final List<Ticket> tickets = new ArrayList<>();
    public Booking(String bookingId, LocalDateTime bookingTime, Customer customer, Event event) {
        this.bookingId = java.util.Objects.requireNonNull(bookingId);
        this.bookingTime = java.util.Objects.requireNonNull(bookingTime);
        this.customer = java.util.Objects.requireNonNull(customer);
        this.event = java.util.Objects.requireNonNull(event);
    }
    public String getBookingId() { return bookingId; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Customer getCustomer() { return customer; }
    public Event getEvent() { return event; }
    public List<Ticket> getTickets() { return Collections.unmodifiableList(tickets); }
    public void addTicket(Ticket ticket) {
        if (!"PENDING".equals(status)) throw new IllegalStateException("Cannot add tickets to a finalized booking");
        if (ticket.getTicketType() == null) throw new IllegalArgumentException("TicketType must be assigned");
        if (event.findTicketType(ticket.getTicketType().getTicketTypeId()) != ticket.getTicketType())
            throw new IllegalArgumentException("TicketType does not belong to this event");
        if (ticket.getBooking() != null) throw new IllegalArgumentException("Ticket already belongs to booking");
        ticket.assignBooking(this);
        tickets.add(ticket);
    }
    public Booking createBooking() { return this; }
    public BigDecimal calculateTotal() {
        BigDecimal result = BigDecimal.ZERO;
        for (Ticket t : tickets) result = result.add(t.getTicketType().getPrice());
        return result;
    }
    public void confirmBooking() {
        if (!"PENDING".equals(status) || tickets.isEmpty()) throw new IllegalStateException("Booking cannot be confirmed");
        totalAmount = calculateTotal();
        status = "CONFIRMED";
    }
    public void cancelBooking() {
        if (!"CONFIRMED".equals(status)) throw new IllegalStateException("Only confirmed booking can be cancelled");
        for (Ticket ticket : tickets) {
            if (ticket.isUsed()) throw new IllegalStateException("Không thể hủy booking đã có vé check-in.");
        }
        // IMPORTANT: BookingService must restore inventory exactly once, atomically.
        for (Ticket ticket : tickets) ticket.cancel();
        status = "CANCELLED";
    }
}
