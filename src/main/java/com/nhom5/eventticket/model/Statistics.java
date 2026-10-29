package com.nhom5.eventticket.model;

import java.math.BigDecimal;
/** UML M3: Statistics aggregates Event/Booking/Ticket totals.*/
public class Statistics {
    private final int totalEvents;
    private final int totalBookings;
    private final int totalTickets;
    private final BigDecimal totalRevenue;
    public Statistics(int totalEvents, int totalBookings, int totalTickets, BigDecimal totalRevenue) {
        this.totalEvents = totalEvents;
        this.totalBookings = totalBookings;
        this.totalTickets = totalTickets;
        this.totalRevenue = java.util.Objects.requireNonNull(totalRevenue);
    }
    public int getTotalEvents() { return totalEvents; }
    public int getTotalBookings() { return totalBookings; }
    public int getTotalTickets() { return totalTickets; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public Statistics getStatistics() { return this; }
}
