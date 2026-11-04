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
     * Tổng hợp số sự kiện, booking, vé và doanh thu từ booking CONFIRMED.
     * (Stream – Chapter 7.5)
     */
    public Statistics getStatistics() {
        int totalEvents = store.allEvents().size();
        int totalBookings = store.allBookings().size();
        int totalTickets = store.allTickets().size();

        BigDecimal totalRevenue = store.allBookings().stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .map(Booking::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Statistics(totalEvents, totalBookings, totalTickets, totalRevenue);
    }

    /** Số vé đã check-in (chỉ đọc, phục vụ UI mở rộng nếu cần). */
    public long countCheckedInTickets() {
        return store.allTickets().stream().filter(Ticket::isUsed).count();
    }

    /** Số booking còn hiệu lực CONFIRMED (chỉ đọc). */
    public long countConfirmedBookings() {
        return store.allBookings().stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .count();
    }
}
