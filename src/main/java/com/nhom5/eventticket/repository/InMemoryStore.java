package com.nhom5.eventticket.repository;

import com.nhom5.eventticket.model.*;
import java.util.*;
/** ONE store for ALL services. Do not instantiate a second store in a module. */
public final class InMemoryStore {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Event> events = new LinkedHashMap<>();
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private final Map<String, Ticket> ticketsByCode = new LinkedHashMap<>();
    private final Map<String, CheckIn> checkInsByTicketCode = new LinkedHashMap<>();
    private final Map<String, Notification> notifications = new LinkedHashMap<>();

    public synchronized void saveUser(User value) { users.put(value.getUserId(), value); }
    public synchronized void saveEvent(Event value) { events.put(value.getEventId(), value); }
    public synchronized void removeEvent(String eventId) { events.remove(eventId); }
    public synchronized void saveBooking(Booking value) { bookings.put(value.getBookingId(), value); }
    public synchronized void saveTicket(Ticket value) {
        if (ticketsByCode.containsKey(value.getTicketCode())) throw new IllegalArgumentException("Duplicate ticket code");
        ticketsByCode.put(value.getTicketCode(), value);
    }
    public synchronized void saveCheckIn(CheckIn value) {
        if (checkInsByTicketCode.containsKey(value.getTicket().getTicketCode()))
            throw new com.nhom5.eventticket.exception.DuplicateCheckInException("Ticket already checked in");
        checkInsByTicketCode.put(value.getTicket().getTicketCode(), value);
    }
    public synchronized void saveNotification(Notification value) { notifications.put(value.getNotificationId(), value); }

    public synchronized Optional<User> findUser(String id) { return Optional.ofNullable(users.get(id)); }
    public synchronized Optional<Event> findEvent(String id) { return Optional.ofNullable(events.get(id)); }
    public synchronized Optional<Booking> findBooking(String id) { return Optional.ofNullable(bookings.get(id)); }
    public synchronized Optional<Ticket> findTicketByCode(String code) { return Optional.ofNullable(ticketsByCode.get(code)); }
    public synchronized Optional<CheckIn> findCheckInByTicketCode(String code) { return Optional.ofNullable(checkInsByTicketCode.get(code)); }

    public synchronized List<User> allUsers() { return List.copyOf(users.values()); }
    public synchronized List<Event> allEvents() { return List.copyOf(events.values()); }
    public synchronized List<Booking> allBookings() { return List.copyOf(bookings.values()); }
    public synchronized List<Ticket> allTickets() { return List.copyOf(ticketsByCode.values()); }
    public synchronized List<Notification> allNotifications() { return List.copyOf(notifications.values()); }

    // BookingService must synchronize on this same store when checking stock + creating booking.
    // CheckInService must synchronize on this store when checking ticket + saving CheckIn.
}
