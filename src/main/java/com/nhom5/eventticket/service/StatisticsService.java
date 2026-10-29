package com.nhom5.eventticket.service;
import com.nhom5.eventticket.model.*;
import com.nhom5.eventticket.repository.InMemoryStore;
import java.math.BigDecimal;
/** Counts from the current in-memory session. */
public class StatisticsService {
    private final InMemoryStore store;
    public StatisticsService(InMemoryStore store) { this.store = store; }
    public Statistics getStatistics() {
        BigDecimal revenue = store.allBookings().stream().filter(b->"CONFIRMED".equals(b.getStatus()))
          .map(Booking::getTotalAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new Statistics(store.allEvents().size(),store.allBookings().size(),store.allTickets().size(),revenue);
    }
}
