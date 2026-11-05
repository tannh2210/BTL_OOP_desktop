package com.nhom5.eventticket;

import com.nhom5.eventticket.factory.TicketFactory;
import com.nhom5.eventticket.model.*;
import com.nhom5.eventticket.repository.*;
import com.nhom5.eventticket.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class AdminSalesTest {
    InMemoryStore store;
    EventService events;
    BookingService bookings;
    Admin admin;
    Event event;
    TicketType type;
    @BeforeEach void setup() {
        store = DemoData.create(); events = new EventService(store);
        bookings = new BookingService(store, new TicketFactory(), new NotificationService(store));
        admin = (Admin) store.findUser("U2").orElseThrow();
        event = events.createEventWithTicket(admin, "Test", "", "Place", LocalDateTime.now().plusDays(2),
                "STANDARD", BigDecimal.TEN, 5);
        type = event.getTicketTypes().get(0);
    }
    Booking book() { return bookings.bookTicket("U1", event.getEventId(), type.getTicketTypeId(), 1); }
    @Test void allSalesCombinationsEnforceBothSwitchesScheduleStatusAndStock() {
        for (boolean eventOpen : new boolean[]{false, true})
        for (boolean typeOpen : new boolean[]{false, true})
        for (boolean future : new boolean[]{false, true})
        for (boolean valid : new boolean[]{false, true})
        for (boolean inStock : new boolean[]{false, true}) {
            setup(); // Independent fixture for each of the 32 combinations.
            if (!inStock) type.reserve(type.getQuantity());
            events.setTicketSalesOpen(admin, event.getEventId(), eventOpen);
            events.setTicketTypeSalesEnabled(admin, event.getEventId(), type.getTicketTypeId(), typeOpen);
            if (!future) event.reschedule(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
            if (!valid) event.setStatus("CANCELLED");
            int before = type.getAvailableQuantity();
            if (eventOpen && typeOpen && future && valid && inStock) {
                assertTrue(type.getSalesLabel().contains("Có thể đặt vé"));
                assertEquals("CONFIRMED", book().getStatus());
                assertEquals(before - 1, type.getAvailableQuantity());
            } else {
                assertFalse(type.getSalesLabel().contains("Có thể đặt vé"));
                assertThrows(RuntimeException.class, this::book);
                assertEquals(before, type.getAvailableQuantity());
                assertTrue(store.allBookings().isEmpty());
                assertTrue(store.allTickets().isEmpty());
            }
            assertEquals(typeOpen, type.isSalesEnabled());
        }
    }
    @Test void closingAndReopeningEventNeverChangesIndividualTypeSettingsOrHistory() {
        Booking existing = book();
        TicketType vip = events.addTicketType(admin, event.getEventId(), "VIP", BigDecimal.ONE, 2);
        events.setTicketTypeSalesEnabled(admin, event.getEventId(), vip.getTicketTypeId(), false);
        for (boolean open : new boolean[]{false, true, false, true}) {
            events.setTicketSalesOpen(admin, event.getEventId(), open);
            assertTrue(type.isSalesEnabled()); assertFalse(vip.isSalesEnabled());
            assertEquals(4, type.getAvailableQuantity()); assertEquals(2, vip.getAvailableQuantity());
            assertEquals("CONFIRMED", existing.getStatus());
            assertTrue(existing.getTickets().get(0).validateTicket());
            assertEquals(1, store.allBookings().size()); assertEquals(1, store.allTickets().size());
        }
    }
    @Test void closeReopenPreservesHistoryAndSchedule() {
        Booking old = book(); var start = event.getStartTime(); var end = event.getEndTime();
        events.setTicketSalesOpen(admin, event.getEventId(), false);
        assertThrows(IllegalArgumentException.class, this::book);
        assertEquals("CONFIRMED", old.getStatus());
        assertEquals(1, store.allTickets().size());
        assertEquals(start, event.getStartTime()); assertEquals(end, event.getEndTime());
        assertEquals("OPEN", event.getStatus());
        events.setTicketSalesOpen(admin, event.getEventId(), true);
        book(); assertEquals(3, type.getAvailableQuantity());
    }
    @Test void cannotReopenInvalidOrStartedEvent() {
        events.setTicketSalesOpen(admin, event.getEventId(), false);
        event.setStatus("CANCELLED");
        assertThrows(IllegalArgumentException.class, () -> events.setTicketSalesOpen(admin, event.getEventId(), true));
        event.setStatus("OPEN");
        event.reschedule(LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));
        assertThrows(IllegalArgumentException.class, () -> events.setTicketSalesOpen(admin, event.getEventId(), true));
        assertFalse(event.isTicketSalesOpen());
    }
    @Test void rejectsInvalidStockOnCreateAndUpdateAtomically() {
        for (int quantity : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class, () -> events.addTicketType(admin, event.getEventId(), "VIP", BigDecimal.ONE, quantity));
            assertThrows(IllegalArgumentException.class, () -> events.updateTicketType(admin, event.getEventId(), type.getTicketTypeId(), BigDecimal.ONE, quantity));
        }
        for (BigDecimal price : new BigDecimal[]{null, BigDecimal.ZERO, BigDecimal.ONE.negate()}) {
            assertThrows(IllegalArgumentException.class, () -> events.addTicketType(admin, event.getEventId(), "VIP", price, 1));
            assertThrows(IllegalArgumentException.class, () -> events.updateTicketType(admin, event.getEventId(), type.getTicketTypeId(), price, 1));
            assertThrows(IllegalArgumentException.class, () -> type.setPrice(price));
        }
        assertEquals(5, type.getQuantity()); assertEquals(BigDecimal.TEN, type.getPrice());
    }
    @Test void deleteUnusedAndStopUsedWithoutLosingHistory() {
        TicketType unused = events.addTicketType(admin, event.getEventId(), "VIP", BigDecimal.ONE, 2);
        events.deleteTicketType(admin, event.getEventId(), unused.getTicketTypeId());
        assertNull(event.findTicketType(unused.getTicketTypeId()));
        Booking booking = book();
        events.setTicketTypeSalesEnabled(admin, event.getEventId(), type.getTicketTypeId(), false);
        assertThrows(IllegalArgumentException.class, this::book);
        assertThrows(IllegalArgumentException.class, () -> events.deleteTicketType(admin, event.getEventId(), type.getTicketTypeId()));
        assertEquals("CONFIRMED", booking.getStatus());
        bookings.cancelBooking("U1", booking.getBookingId());
        assertEquals(5, type.getAvailableQuantity());
        assertThrows(IllegalArgumentException.class, () -> events.deleteTicketType(admin, event.getEventId(), type.getTicketTypeId()));
        assertSame(type, event.findTicketType(type.getTicketTypeId()));
        events.setTicketTypeSalesEnabled(admin, event.getEventId(), type.getTicketTypeId(), true);
        book();
    }
    @Test void pendingCannotConfirmAfterClosing() {
        Booking pending = new Booking("pending", LocalDateTime.now(), (Customer) store.findUser("U1").orElseThrow(), event);
        Ticket ticket = new TicketFactory().createTicket("STANDARD"); ticket.assignTicketType(type); pending.addTicket(ticket);
        events.setTicketSalesOpen(admin, event.getEventId(), false);
        assertThrows(IllegalStateException.class, pending::confirmBooking);
        assertEquals("PENDING", pending.getStatus());
    }
    @Test void issuedTicketStillChecksInAndOtherTypesStillSell() {
        Booking existing = book();
        TicketType vip = events.addTicketType(admin, event.getEventId(), "VIP", BigDecimal.ONE, 2);
        events.setTicketTypeSalesEnabled(admin, event.getEventId(), type.getTicketTypeId(), false);
        bookings.bookTicket("U1", event.getEventId(), vip.getTicketTypeId(), 1);
        events.setTicketSalesOpen(admin, event.getEventId(), false);
        // Check-in now requires the event to have reached its opening day.
        event.reschedule(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2));
        new CheckInService(store, new NotificationService(store)).checkIn(existing.getTickets().get(0).getTicketCode(), admin.getUserId());
        assertTrue(existing.getTickets().get(0).isUsed());
        assertEquals(1, vip.getAvailableQuantity());
    }
    @Test void reservedStockAndStandaloneIssuedTicketPreventDeletion() {
        type.reserve(1);
        assertThrows(IllegalArgumentException.class, () -> events.deleteTicketType(admin, event.getEventId(), type.getTicketTypeId()));
        type.restore(1);
        Ticket ticket = new TicketFactory().createTicket("STANDARD"); ticket.assignTicketType(type); store.saveTicket(ticket);
        assertThrows(IllegalArgumentException.class, () -> events.deleteTicketType(admin, event.getEventId(), type.getTicketTypeId()));
    }
    @Test void adminIdentityRequiredForAllNewOperations() {
        Admin forged = new Admin("fake", "U2", "fake", "123456", "Fake", "fake@local");
        for (Admin actor : new Admin[]{null, forged}) {
            assertThrows(IllegalArgumentException.class, () -> events.setTicketSalesOpen(actor, event.getEventId(), false));
            assertThrows(IllegalArgumentException.class, () -> events.setTicketTypeSalesEnabled(actor, event.getEventId(), type.getTicketTypeId(), false));
            assertThrows(IllegalArgumentException.class, () -> events.deleteTicketType(actor, event.getEventId(), type.getTicketTypeId()));
        }
    }
    @Test void concurrentReservationsNeverOversell() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var tasks = new java.util.ArrayList<Callable<Boolean>>();
            for (int i=0; i<20; i++) tasks.add(() -> { try { book(); return true; } catch (IllegalArgumentException | com.nhom5.eventticket.exception.OutOfStockException ex) { return false; } });
            int successes = 0;
            for (Future<Boolean> result : pool.invokeAll(tasks)) if (result.get()) successes++;
            assertEquals(5, successes); assertEquals(0, type.getAvailableQuantity());
        } finally { pool.shutdownNow(); }
    }
    @Test void closingDuringConcurrentReservationsStopsFurtherSales() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var attempts = new java.util.ArrayList<Future<?>>();
            Future<Integer> closedAt = pool.submit(() -> {
                start.await();
                synchronized (store) {
                    events.setTicketSalesOpen(admin, event.getEventId(), false);
                    return store.allBookings().size();
                }
            });
            for (int i=0; i<20; i++) attempts.add(pool.submit(() -> {
                start.await();
                try { book(); } catch (IllegalArgumentException | com.nhom5.eventticket.exception.OutOfStockException expected) { }
                return null;
            }));
            start.countDown();
            int confirmedAtClose = closedAt.get(10, TimeUnit.SECONDS);
            for (Future<?> attempt : attempts) attempt.get(10, TimeUnit.SECONDS);
            assertEquals(confirmedAtClose, store.allBookings().size());
            assertEquals(5 - confirmedAtClose, type.getAvailableQuantity());
            assertEquals(confirmedAtClose, store.allTickets().size());
            assertThrows(IllegalArgumentException.class, this::book);
        } finally { start.countDown(); pool.shutdownNow(); }
    }
}
