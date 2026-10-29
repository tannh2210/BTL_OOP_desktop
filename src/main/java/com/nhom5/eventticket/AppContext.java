package com.nhom5.eventticket;
import com.nhom5.eventticket.factory.TicketFactory;
import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.repository.InMemoryStore;
import com.nhom5.eventticket.service.*;

/** Services share one in-memory store. */
public final class AppContext {
    private final InMemoryStore store;
    private final AuthService auth;
    private final EventService events;
    private final BookingService bookings;
    private final CheckInService checkIns;
    private final StatisticsService statistics;
    private final NotificationService notifications;
    private User currentUser;

    public AppContext(InMemoryStore store) {
        this.store = java.util.Objects.requireNonNull(store);
        auth = new AuthService(store);
        events = new EventService(store);
        bookings = new BookingService(store, new TicketFactory());
        checkIns = new CheckInService(store);
        statistics = new StatisticsService(store);
        notifications = new NotificationService(store);
    }
    public InMemoryStore store() { return store; }
    public AuthService auth() { return auth; }
    public EventService events() { return events; }
    public BookingService bookings() { return bookings; }
    public CheckInService checkIns() { return checkIns; }
    public StatisticsService statistics() { return statistics; }
    public NotificationService notifications() { return notifications; }
    public User currentUser() { return currentUser; }
    public void loginAs(User user) { currentUser = java.util.Objects.requireNonNull(user); }
    public void logout() { currentUser = null; }
}
