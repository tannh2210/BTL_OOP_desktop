package com.eventticketing.booking.model;

import com.eventticketing.booking.integration.EventInfo;
import com.eventticketing.booking.integration.TicketTypeInfo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Booking {
    private final String bookingId, customerId, eventId, eventName;
    private final String ticketTypeId, ticketTypeName;
    private final BigDecimal unitPrice, totalAmount;
    private final int quantity;
    private final LocalDateTime bookingTime;
    private BookingStatus status;
    private final List<Ticket> tickets;

    public Booking(String id, String customerId, EventInfo event,
                   TicketTypeInfo type, int quantity, List<Ticket> tickets) {
        if (blank(id) || blank(customerId) || event == null || type == null)
            throw new IllegalArgumentException("Booking information is incomplete.");
        if (quantity <= 0 || tickets == null || tickets.size() != quantity)
            throw new IllegalArgumentException("Invalid booking quantity/tickets.");
        if (type.getPrice() == null || type.getPrice().signum() < 0)
            throw new IllegalArgumentException("Invalid ticket price.");

        this.bookingId = id;
        this.customerId = customerId;
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.ticketTypeId = type.getTicketTypeId();
        this.ticketTypeName = type.getName();
        this.unitPrice = type.getPrice();
        this.quantity = quantity;
        this.totalAmount = calculateTotal();
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED;
        this.tickets = new ArrayList<>(tickets);
    }

    private Booking(String id, String customerId, String eventId, String eventName,
                    String typeId, String typeName, BigDecimal unitPrice, int quantity,
                    LocalDateTime time, BigDecimal total, BookingStatus status,
                    List<Ticket> tickets) {
        this.bookingId=id; this.customerId=customerId; this.eventId=eventId;
        this.eventName=eventName; this.ticketTypeId=typeId; this.ticketTypeName=typeName;
        this.unitPrice=unitPrice; this.quantity=quantity; this.bookingTime=time;
        this.totalAmount=total; this.status=status; this.tickets=new ArrayList<>(tickets);
    }

    public static Booking restore(String id, String customerId, String eventId,
            String eventName, String typeId, String typeName, BigDecimal unitPrice,
            int quantity, LocalDateTime time, BigDecimal total, BookingStatus status,
            List<Ticket> tickets) {
        return new Booking(id, customerId, eventId, eventName, typeId, typeName,
                unitPrice, quantity, time, total, status, tickets);
    }

    public BigDecimal calculateTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void cancelBooking() {
        if (status != BookingStatus.CONFIRMED)
            throw new IllegalStateException("Only a confirmed booking can be cancelled.");
        tickets.forEach(Ticket::cancel);
        status = BookingStatus.CANCELLED;
    }

    public String getBookingId(){return bookingId;}
    public String getCustomerId(){return customerId;}
    public String getEventId(){return eventId;}
    public String getEventName(){return eventName;}
    public String getTicketTypeId(){return ticketTypeId;}
    public String getTicketTypeName(){return ticketTypeName;}
    public BigDecimal getUnitPrice(){return unitPrice;}
    public int getQuantity(){return quantity;}
    public LocalDateTime getBookingTime(){return bookingTime;}
    public BigDecimal getTotalAmount(){return totalAmount;}
    public BookingStatus getStatus(){return status;}
    public List<Ticket> getTickets(){return Collections.unmodifiableList(tickets);}
    private static boolean blank(String s){return s == null || s.isBlank();}
}
