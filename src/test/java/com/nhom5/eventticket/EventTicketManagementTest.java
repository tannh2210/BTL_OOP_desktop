package com.nhom5.eventticket;

import com.nhom5.eventticket.exception.NotFoundException;
import com.nhom5.eventticket.exception.OutOfStockException;
import com.nhom5.eventticket.model.*;
import com.nhom5.eventticket.repository.DemoData;
import com.nhom5.eventticket.repository.InMemoryStore;
import com.nhom5.eventticket.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTicketManagementTest {
    private InMemoryStore store;
    private EventService service;
    private Admin admin;
    private LocalDateTime start;

    @BeforeEach void setUp() {
        store = DemoData.create();
        service = new EventService(store);
        admin = (Admin) store.findUser("U2").orElseThrow();
        start = LocalDateTime.now().plusDays(30);
    }

    private Event create() {
        return service.createEventWithTicket(admin, " Hội thảo ", "Mô tả", " Huế ",
                start, start.plusHours(5), " standard ", new BigDecimal("100000"), 10);
    }

    @Test void eventCrudAndSearch() {
        Event event = create();
        assertEquals("Hội thảo", event.getName());
        assertTrue(service.searchEvents(" HUẾ ").contains(event));
        assertEquals(service.listEvents().size(), service.searchEvents(null).size());
        assertTrue(service.searchEvents("absent event").isEmpty());
        service.updateEvent(admin, event.getEventId(), "Mới", "Chi tiết", "Hà Nội",
                start.plusDays(1), start.plusDays(1).plusHours(2));
        assertEquals("Mới", service.getEventById(event.getEventId()).getName());
        assertEquals(start.plusDays(1).plusHours(2), event.getEndTime());
        service.deleteEvent(admin, event.getEventId());
        assertThrows(NotFoundException.class, () -> service.getEventById(event.getEventId()));
    }

    @Test void failedUpdatesAreAtomicIncludingLegacySignature() {
        Event event = create();
        assertThrows(IllegalArgumentException.class, () -> service.updateEvent(admin, event.getEventId(),
                "Changed", "Changed", "Elsewhere", start, start.minusHours(1)));
        assertEquals("Hội thảo", event.getName());
        assertThrows(IllegalArgumentException.class, () -> service.updateEvent(admin, event.getEventId(),
                "Changed", "Changed", "Elsewhere", LocalDateTime.now().minusDays(1)));
        assertEquals("Hội thảo", event.getName());
        assertEquals(start, event.getStartTime());
    }

    @Test void legacyReschedulePreservesDuration() {
        Event event = create();
        service.updateEvent(admin, event.getEventId(), "Name", "", "Location", start.plusDays(2));
        assertEquals(start.plusDays(2).plusHours(5), event.getEndTime());
    }

    @Test void invalidCreateDoesNotPersistAndDuplicateIdDoesNotOverwrite() {
        int before = service.listEvents().size();
        assertThrows(IllegalArgumentException.class, () -> service.createEventWithTicket(admin,
                "", "", "Huế", start, "VIP", BigDecimal.ZERO, 1));
        assertThrows(IllegalArgumentException.class, () -> service.createEventWithTicket(admin,
                "Test", "", "Huế", LocalDateTime.now().minusDays(1), "VIP", BigDecimal.ZERO, 1));
        assertEquals(before, service.listEvents().size());
        Event existing = service.getEventById("EV001");
        assertThrows(IllegalArgumentException.class, () -> service.createEvent(admin,
                new Event("EV001", "Other", "", "Place", start, start.plusHours(1), "OPEN")));
        assertSame(existing, service.getEventById("EV001"));
    }

    @Test void supportedTypesAndBothAddOverloadsRejectDuplicates() {
        Event event = create();
        service.addTicketType(admin, event.getEventId(), "vip", BigDecimal.ONE, 1);
        service.addTicketType(admin, event.getEventId(), "ECONOMY", BigDecimal.ONE, 3);
        assertEquals(3, event.getTicketTypes().size());
        assertThrows(IllegalArgumentException.class, () -> service.addTicketType(admin,
                event.getEventId(), "STANDARD", BigDecimal.ONE, 2));
        assertThrows(IllegalArgumentException.class, () -> service.addTicketType(admin,
                event.getEventId(), new TicketType("other", "standard", BigDecimal.ONE, 2)));
        assertThrows(IllegalArgumentException.class, () -> new TicketType("id", "OTHER", BigDecimal.ONE, 1));
        assertThrows(UnsupportedOperationException.class, () -> event.getTicketTypes().clear());
    }

    @Test void stockValidationAndPriceUpdatePreserveSoldTickets() {
        Event event = create();
        TicketType type = event.getTicketTypes().get(0);
        // Simulate inventory already reserved by the Booking owner.
        type.reserve(4);
        service.updateTicketType(admin, event.getEventId(), type.getTicketTypeId(), BigDecimal.TEN, 6);
        assertEquals(2, type.getAvailableQuantity());
        assertEquals(BigDecimal.TEN, type.getPrice());
        assertThrows(IllegalArgumentException.class, () -> service.updateTicketType(admin,
                event.getEventId(), type.getTicketTypeId(), BigDecimal.ONE, 3));
        assertEquals(6, type.getQuantity());
        assertEquals(BigDecimal.TEN, type.getPrice());
        assertThrows(IllegalArgumentException.class, () -> type.changeStock(BigDecimal.ONE.negate(), 6));
        assertThrows(IllegalArgumentException.class, () -> new TicketType("id", "VIP", BigDecimal.ONE, -1));
        service.updateTicketType(admin, event.getEventId(), type.getTicketTypeId(), BigDecimal.ONE, 4);
        assertFalse(type.isAvailable());
    }

    @Test void sharedAvailabilityIsReadOnlyAndHandlesBoundaries() {
        service.checkTicketAvailability("EV001", "T001", 80);
        service.checkTicketAvailability("EV001", "T001", 80);
        assertEquals(80, service.getEventById("EV001").findTicketType("T001").getAvailableQuantity());
        assertThrows(OutOfStockException.class, () -> service.checkTicketAvailability("EV001", "T001", 81));
        assertThrows(IllegalArgumentException.class, () -> service.checkTicketAvailability("EV001", "T001", 0));
        assertThrows(IllegalArgumentException.class, () -> service.checkTicketAvailability("EV001", "T001", -1));
        assertThrows(NotFoundException.class, () -> service.checkTicketAvailability("EV001", "T004", 1));
        assertThrows(NotFoundException.class, () -> service.checkTicketAvailability("missing", "T001", 1));
    }

    @Test void deletionProtectsBookingReferencesWithoutChangingBooking() {
        Event event = create();
        store.saveBooking(new Booking("booking", LocalDateTime.now(),
                (Customer) store.findUser("U1").orElseThrow(), event));
        assertThrows(IllegalArgumentException.class, () -> service.deleteEvent(admin, event.getEventId()));
        assertSame(event, service.getEventById(event.getEventId()));
    }

    @Test void writesRequireAdmin() {
        Event event = create();
        String id = event.getEventId();
        assertThrows(IllegalArgumentException.class, () -> service.createEvent(null, event));
        assertThrows(IllegalArgumentException.class, () -> service.deleteEvent(null, id));
        assertThrows(IllegalArgumentException.class, () -> service.updateEvent(null, id, "x", "", "y", start));
        assertThrows(IllegalArgumentException.class, () -> service.addTicketType(null, id, "VIP", BigDecimal.ONE, 1));
        assertThrows(IllegalArgumentException.class, () -> service.updateTicketType(null, id, "id", BigDecimal.ONE, 1));
    }

    @Test void existingBusinessFlowRemainsCompatible() {
        BasicLogicTest.main(new String[0]);
    }

    @Test void unregisteredAndImpersonatedAdminsCannotWrite() {
        Event event = create();
        for (String userId : new String[]{"unknown", "U1", "U2"}) {
            Admin forged = new Admin("fake", userId, "fake", "123456", "Fake", "fake@local");
            assertThrows(IllegalArgumentException.class, () -> service.createEvent(forged, event));
            assertThrows(IllegalArgumentException.class, () -> service.deleteEvent(forged, event.getEventId()));
            assertThrows(IllegalArgumentException.class, () -> service.updateEvent(forged,
                    event.getEventId(), "Changed", "", "Place", start));
            assertThrows(IllegalArgumentException.class, () -> service.addTicketType(forged,
                    event.getEventId(), "VIP", BigDecimal.ONE, 1));
            assertThrows(IllegalArgumentException.class, () -> service.updateTicketType(forged,
                    event.getEventId(), event.getTicketTypes().get(0).getTicketTypeId(), BigDecimal.ZERO, 0));
        }
        assertSame(event, service.getEventById(event.getEventId()));
        assertEquals("Hội thảo", event.getName());
        assertEquals(10, event.getTicketTypes().get(0).getQuantity());
    }

    @Test void ticketInventoryCannotBeSharedAcrossEvents() {
        Event first = create();
        Event second = create();
        TicketType vip = service.addTicketType(admin, first.getEventId(), "VIP", BigDecimal.ONE, 5);
        assertThrows(IllegalArgumentException.class, () -> service.addTicketType(admin, second.getEventId(), vip));
        assertNull(second.findTicketType(vip.getTicketTypeId()));
        assertSame(vip, first.findTicketType(vip.getTicketTypeId()));
        assertEquals(5, vip.getAvailableQuantity());
    }

    @Test void renameCannotIntroduceDuplicateTicketCategory() {
        Event event = create();
        TicketType vip = service.addTicketType(admin, event.getEventId(), "VIP", BigDecimal.ONE, 5);
        assertThrows(IllegalArgumentException.class, () -> vip.setName(" standard "));
        assertEquals("VIP", vip.getName());
        vip.setName(" economy ");
        assertEquals("ECONOMY", vip.getName());
        assertThrows(IllegalArgumentException.class, () -> service.addTicketType(admin,
                event.getEventId(), "ECONOMY", BigDecimal.ONE, 1));
    }

    @Test void eventConstructorAndRescheduleValidateTimesConsistently() {
        assertThrows(IllegalArgumentException.class, () -> new Event("id", "Name", "", "Place",
                null, start, "OPEN"));
        assertThrows(IllegalArgumentException.class, () -> new Event("id", "Name", "", "Place",
                start, null, "OPEN"));
        Event event = create();
        assertThrows(IllegalArgumentException.class, () -> event.reschedule(start, start));
        assertEquals(start.plusHours(5), event.getEndTime());
    }
}
