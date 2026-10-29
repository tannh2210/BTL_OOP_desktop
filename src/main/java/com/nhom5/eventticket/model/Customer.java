package com.nhom5.eventticket.model;

import java.util.List;
/** UML M3: Customer extends User.*/
public class Customer extends User {
    private final String customerId;
    public Customer(String customerId, String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email);
        this.customerId = java.util.Objects.requireNonNull(customerId);
    }
    public String getCustomerId() { return customerId; }
    // Legacy UML methods; application flows are in service classes.
    public List<Event> searchEvents() { throw new UnsupportedOperationException("Chưa hỗ trợ: EventService"); }
    public Event viewEventDetails() { throw new UnsupportedOperationException("Chưa hỗ trợ: EventService"); }
    public Booking bookTicket() { throw new UnsupportedOperationException("Chưa hỗ trợ: BookingService"); }
    public void cancelBooking() { throw new UnsupportedOperationException("Chưa hỗ trợ: BookingService"); }
    public List<Booking> viewBookingHistory() { throw new UnsupportedOperationException("Chưa hỗ trợ: BookingService"); }
}
