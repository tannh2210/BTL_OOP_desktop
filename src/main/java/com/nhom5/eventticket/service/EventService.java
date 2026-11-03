package com.nhom5.eventticket.service;

import com.nhom5.eventticket.exception.NotFoundException;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Event;
import com.nhom5.eventticket.model.TicketType;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Event administration uses the same store monitor as Booking inventory operations. */
public class EventService {
    private final InMemoryStore store;

    public EventService(InMemoryStore store) { this.store = Objects.requireNonNull(store); }

    public List<Event> listEvents() { return store.allEvents(); }

    public Event getEventById(String id) {
        return store.findEvent(id).orElseThrow(() -> new NotFoundException("Không tìm thấy sự kiện."));
    }

    public List<Event> searchEvents(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return store.allEvents().stream()
                .filter(e -> e.getName().toLowerCase(Locale.ROOT).contains(key)
                        || e.getLocation().toLowerCase(Locale.ROOT).contains(key))
                .toList();
    }

    /** Read-only stock validation. Booking must recheck and reserve under synchronized(store).
     * This method never reserves tickets or changes Booking state.
     */
    public void checkTicketAvailability(String eventId, String typeId, int quantity) {
        synchronized (store) {
            getTicketType(eventId, typeId).checkAvailability(quantity);
        }
    }

    public Event createEvent(Admin admin, Event event) {
        synchronized (store) {
            requireAdmin(admin);
            if (event == null) throw new IllegalArgumentException("Sự kiện không được để trống.");
            validateSchedule(event.getStartTime(), event.getEndTime());
            if (store.findEvent(event.getEventId()).isPresent())
                throw new IllegalArgumentException("Mã sự kiện đã tồn tại.");
            store.saveEvent(event);
            return event;
        }
    }

    public Event createEventWithTicket(Admin admin, String name, String description, String location,
            LocalDateTime start, LocalDateTime end, String typeName, BigDecimal price, int quantity) {
        synchronized (store) {
            requireAdmin(admin);
            validateSchedule(start, end);
            Event event = new Event(newId("EV-"), name, description, location, start, end, "OPEN");
            event.addTicketType(newTicketType(typeName, price, quantity));
            return createEvent(admin, event);
        }
    }

    /** Compatibility overload: a new event defaults to three hours. */
    public Event createEventWithTicket(Admin admin, String name, String description, String location,
            LocalDateTime start, String typeName, BigDecimal price, int quantity) {
        return createEventWithTicket(admin, name, description, location, start,
                start == null ? null : start.plusHours(3), typeName, price, quantity);
    }

    public Event createSimpleEvent(Admin admin, String name, String location, LocalDateTime start, BigDecimal standardPrice) {
        return createEventWithTicket(admin, name, "Sự kiện mới", location, start, "STANDARD", standardPrice, 100);
    }

    public void updateEvent(Admin admin, String id, String name, String description, String location,
            LocalDateTime start, LocalDateTime end) {
        synchronized (store) {
            requireAdmin(admin);
            Event event = getEventById(id);
            if (!Objects.equals(start, event.getStartTime()) || !Objects.equals(end, event.getEndTime()))
                validateSchedule(start, end);
            // Validate every field before changing the shared object, preserving Booking references.
            Event validated = new Event(id, name, description, location, start, end, event.getStatus());
            event.updateBasicInfo(validated.getName(), validated.getDescription(), validated.getLocation());
            event.reschedule(start, end);
        }
    }

    /** Compatibility overload: retain the event duration; null start leaves the schedule unchanged. */
    public void updateEvent(Admin admin, String id, String name, String description, String location, LocalDateTime start) {
        synchronized (store) {
            requireAdmin(admin);
            Event event = getEventById(id);
            LocalDateTime effectiveStart = start == null ? event.getStartTime() : start;
            LocalDateTime end = effectiveStart.plus(Duration.between(event.getStartTime(), event.getEndTime()));
            updateEvent(admin, id, name, description, location, effectiveStart, end);
        }
    }

    public void deleteEvent(Admin admin, String id) {
        synchronized (store) {
            requireAdmin(admin);
            getEventById(id);
            boolean used = store.allBookings().stream().anyMatch(b -> b.getEvent().getEventId().equals(id));
            if (used) throw new IllegalArgumentException("Không thể xóa sự kiện đã có booking.");
            store.removeEvent(id);
        }
    }

    public TicketType addTicketType(Admin admin, String eventId, String typeName, BigDecimal price, int quantity) {
        synchronized (store) {
            requireAdmin(admin);
            return addTicketType(admin, eventId, newTicketType(typeName, price, quantity));
        }
    }

    public TicketType addTicketType(Admin admin, String eventId, TicketType type) {
        synchronized (store) {
            requireAdmin(admin);
            if (type == null) throw new IllegalArgumentException("Loại vé không được để trống.");
            getEventById(eventId).addTicketType(type);
            return type;
        }
    }

    /** Quantity is total capacity; available stock is recalculated while keeping sold tickets. */
    public void updateTicketType(Admin admin, String eventId, String typeId, BigDecimal price, int quantity) {
        synchronized (store) {
            requireAdmin(admin);
            getTicketType(eventId, typeId).changeStock(price, quantity);
        }
    }

    private TicketType getTicketType(String eventId, String typeId) {
        TicketType type = getEventById(eventId).findTicketType(typeId);
        if (type == null) throw new NotFoundException("Không tìm thấy loại vé của sự kiện.");
        return type;
    }

    private static TicketType newTicketType(String typeName, BigDecimal price, int quantity) {
        return new TicketType(newId("TT-"), typeName, price, quantity);
    }

    private static void validateSchedule(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException("Kết thúc phải sau thời gian bắt đầu.");
        if (!start.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Ngày sự kiện phải ở tương lai.");
    }

    private static void requireAdmin(Admin admin) {
        if (admin == null) throw new IllegalArgumentException("Chỉ quản trị viên được quản lý sự kiện.");
    }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    }
}