package com.nhom5.eventticket;

import com.nhom5.eventticket.exception.DuplicateCheckInException;
import com.nhom5.eventticket.exception.InvalidTicketException;
import com.nhom5.eventticket.exception.NotFoundException;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Booking;
import com.nhom5.eventticket.model.CheckIn;
import com.nhom5.eventticket.model.Customer;
import com.nhom5.eventticket.model.Notification;
import com.nhom5.eventticket.model.Statistics;
import com.nhom5.eventticket.observer.NotificationListener;
import com.nhom5.eventticket.repository.DemoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử phạm vi Người 4: Check-in, Statistics (read-only), Notification dùng chung. */
class Person4CheckInStatsNotificationTest {

    private AppContext app;
    private Customer customer;
    private Admin admin;

    @BeforeEach
    void setUp() {
        app = new AppContext(DemoData.create());
        customer = (Customer) app.auth().login("customer", "123456");
        admin = (Admin) app.auth().login("admin", "123456");
    }

    @Test
    void checkInSuccess_marksTicketUsed_andNotifiesCustomer() {
        Booking booking = app.bookings().bookTicket(customer.getUserId(), "EV001", "T001", 1);
        String code = booking.getTickets().get(0).getTicketCode();

        CheckIn record = app.checkIns().checkIn(code, admin.getUserId());

        assertEquals("SUCCESS", record.getStatus());
        assertTrue(record.getTicket().isUsed());
        assertEquals(admin.getAdminId(), record.getAdmin().getAdminId());
        assertTrue(app.notifications().forUser(customer.getUserId()).stream()
                .anyMatch(n -> n.getMessage().contains(code) && n.getMessage().contains("check-in")));
    }

    @Test
    void checkIn_rejectsMissingAndDuplicate() {
        Booking booking = app.bookings().bookTicket(customer.getUserId(), "EV001", "T002", 1);
        String code = booking.getTickets().get(0).getTicketCode();

        assertThrows(NotFoundException.class, () -> app.checkIns().checkIn("KHONG-TON-TAI", admin.getUserId()));
        app.checkIns().checkIn(code, admin.getUserId());
        assertThrows(DuplicateCheckInException.class, () -> app.checkIns().checkIn(code, admin.getUserId()));
    }

    @Test
    void checkIn_rejectsCancelledTicket() {
        Booking booking = app.bookings().bookTicket(customer.getUserId(), "EV001", "T003", 1);
        String code = booking.getTickets().get(0).getTicketCode();
        app.bookings().cancelBooking(customer.getUserId(), booking.getBookingId());

        assertThrows(InvalidTicketException.class, () -> app.checkIns().checkIn(code, admin.getUserId()));
    }

    @Test
    void checkIn_doesNotChangeTicketStock() {
        var type = app.events().getEventById("EV001").findTicketType("T001");
        Booking booking = app.bookings().bookTicket(customer.getUserId(), "EV001", "T001", 1);
        int stockAfterBook = type.getAvailableQuantity();

        app.checkIns().checkIn(booking.getTickets().get(0).getTicketCode(), admin.getUserId());

        assertEquals(stockAfterBook, type.getAvailableQuantity(),
                "Check-in không được trừ/hoàn số lượng vé còn bán");
    }

    @Test
    void statistics_areReadOnlyAggregates() {
        app.bookings().bookTicket(customer.getUserId(), "EV001", "T001", 2);
        Statistics stats = app.statistics().getStatistics();

        assertEquals(3, stats.getTotalEvents());
        assertTrue(stats.getTotalBookings() >= 1);
        assertTrue(stats.getTotalTickets() >= 2);
        assertEquals(0, new BigDecimal("1000000").compareTo(stats.getTotalRevenue()));
        assertEquals(0, app.statistics().countCheckedInTickets());
    }

    @Test
    void notificationService_isSharedHub_withObserver() {
        AtomicInteger observerHits = new AtomicInteger();
        List<String> seen = new ArrayList<>();
        NotificationListener listener = n -> {
            observerHits.incrementAndGet();
            seen.add(n.getMessage());
        };
        app.notifications().subscribe(listener);

        Booking booking = app.bookings().bookTicket(customer.getUserId(), "EV001", "T002", 1);
        app.bookings().cancelBooking(customer.getUserId(), booking.getBookingId());

        assertTrue(observerHits.get() >= 2);
        assertTrue(seen.stream().anyMatch(m -> m.contains("đặt")));
        assertTrue(seen.stream().anyMatch(m -> m.contains("hủy")));

        List<Notification> forCustomer = app.notifications().forUser(customer.getUserId());
        assertFalse(forCustomer.isEmpty());

        app.notifications().unsubscribe(listener);
        int afterUnsubscribe = observerHits.get();
        app.notifications().notifyUser(customer, "Tin thử sau khi hủy đăng ký");
        assertEquals(afterUnsubscribe, observerHits.get());
    }
}
