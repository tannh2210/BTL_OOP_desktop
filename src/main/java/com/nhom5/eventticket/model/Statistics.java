package com.nhom5.eventticket.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * UML M3: Statistics aggregates Event/Booking/Ticket totals.
 * Bổ sung tách số còn hiệu lực / đã hủy để UI không hiểu nhầm.
 */
public class Statistics {
    private final int totalEvents;
    private final int confirmedBookings;
    private final int cancelledBookings;
    private final int activeTickets;
    private final int usedTickets;
    private final int cancelledTickets;
    private final BigDecimal totalRevenue;

    public Statistics(int totalEvents,
                      int confirmedBookings,
                      int cancelledBookings,
                      int activeTickets,
                      int usedTickets,
                      int cancelledTickets,
                      BigDecimal totalRevenue) {
        this.totalEvents = totalEvents;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.activeTickets = activeTickets;
        this.usedTickets = usedTickets;
        this.cancelledTickets = cancelledTickets;
        this.totalRevenue = Objects.requireNonNull(totalRevenue);
    }

    public int getTotalEvents() { return totalEvents; }
    public int getConfirmedBookings() { return confirmedBookings; }
    public int getCancelledBookings() { return cancelledBookings; }
    public int getActiveTickets() { return activeTickets; }
    public int getUsedTickets() { return usedTickets; }
    public int getCancelledTickets() { return cancelledTickets; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }

    /** UML / tương thích cũ: tổng mọi booking. */
    public int getTotalBookings() { return confirmedBookings + cancelledBookings; }

    /** UML / tương thích cũ: tổng mọi vé. */
    public int getTotalTickets() { return activeTickets + usedTickets + cancelledTickets; }

    public Statistics getStatistics() { return this; }
}
