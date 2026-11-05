package com.nhom5.eventticket.service;

import com.nhom5.eventticket.model.Booking;
import com.nhom5.eventticket.model.Statistics;
import com.nhom5.eventticket.model.Ticket;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Người 4 – Thống kê chỉ đọc (UC11).
 * <p>
 * Đọc Event / Booking / Ticket từ store dùng chung.
 * Không tạo sự kiện, không đặt/hủy vé, không đổi tồn kho.
 */
public class StatisticsService {
    private final InMemoryStore store;

    public StatisticsService(InMemoryStore store) {
        this.store = Objects.requireNonNull(store);
    }

    /**
     * Tách booking/vé còn hiệu lực và đã hủy; doanh thu chỉ từ booking CONFIRMED.
     */
    public Statistics getStatistics() {
        int totalEvents = store.allEvents().size();

        int confirmedBookings = 0;
        int cancelledBookings = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Booking booking : store.allBookings()) {
            if ("CONFIRMED".equals(booking.getStatus())) {
                confirmedBookings++;
                if (booking.getTotalAmount() != null) {
                    totalRevenue = totalRevenue.add(booking.getTotalAmount());
                }
            } else if ("CANCELLED".equals(booking.getStatus())) {
                cancelledBookings++;
            }
        }

        int activeTickets = 0;
        int usedTickets = 0;
        int cancelledTickets = 0;
        for (Ticket ticket : store.allTickets()) {
            if (ticket.isUsed()) {
                usedTickets++;
            } else if ("CANCELLED".equals(ticket.getStatus())) {
                cancelledTickets++;
            } else {
                activeTickets++;
            }
        }

        return new Statistics(
                totalEvents,
                confirmedBookings,
                cancelledBookings,
                activeTickets,
                usedTickets,
                cancelledTickets,
                totalRevenue
        );
    }

    public long countCheckedInTickets() {
        return store.allTickets().stream().filter(Ticket::isUsed).count();
    }

    public long countConfirmedBookings() {
        return store.allBookings().stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .count();
    }
}
