package com.nhom5.eventticket.service;

import com.nhom5.eventticket.exception.NotFoundException;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Event;
import com.nhom5.eventticket.model.TicketType;
import com.nhom5.eventticket.repository.InMemoryStore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Event listing, search, and a simple admin create/edit/delete flow. */
public class EventService {
    private final InMemoryStore store;

    public EventService(InMemoryStore store) { this.store = store; }

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

    public Event createEvent(Admin admin, Event event) {
        requireAdmin(admin);
        if (event.getName().isBlank() || event.getLocation().isBlank())
            throw new IllegalArgumentException("Tên và địa điểm không được để trống.");
        store.saveEvent(event);
        return event;
    }

    public Event createEventWithTicket(Admin admin, String name, String description, String location,
                                        LocalDateTime start, String typeName, BigDecimal price, int quantity) {
        requireAdmin(admin);
        if (blank(name) || blank(location)) throw new IllegalArgumentException("Vui lòng nhập tên và địa điểm.");
        if (start == null || !start.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Ngày sự kiện phải ở tương lai.");
        Event event = new Event(newId("EV-"), name.trim(),
                blank(description) ? "Sự kiện mới" : description.trim(),
                location.trim(), start, start.plusHours(3), "OPEN");
        event.addTicketType(newTicketType(typeName, price, quantity));
        return createEvent(admin, event);
    }

    public Event createSimpleEvent(Admin admin, String name, String location, LocalDateTime start, BigDecimal standardPrice) {
        return createEventWithTicket(admin, name, "Sự kiện mới", location, start, "STANDARD", standardPrice, 100);
    }

    public void updateEvent(Admin admin, String id, String name, String description, String location, LocalDateTime start) {
        requireAdmin(admin);
        Event event = getEventById(id);
        event.updateBasicInfo(name, description, location);
        if (start != null && !start.equals(event.getStartTime())) {
            if (!start.isAfter(LocalDateTime.now()))
                throw new IllegalArgumentException("Ngày sự kiện phải ở tương lai.");
            event.reschedule(start, start.plusHours(3));
        }
    }

    public void deleteEvent(Admin admin, String id) {
        requireAdmin(admin);
        getEventById(id);
        boolean used = store.allBookings().stream().anyMatch(b -> b.getEvent().getEventId().equals(id));
        if (used) throw new IllegalArgumentException("Không thể xóa sự kiện đã có booking.");
        store.removeEvent(id);
    }

    public TicketType addTicketType(Admin admin, String eventId, String typeName, BigDecimal price, int quantity) {
        requireAdmin(admin);
        Event event = getEventById(eventId);
        String normalized = normalizeType(typeName);
        boolean duplicate = event.getTicketTypes().stream().anyMatch(t -> t.getName().equalsIgnoreCase(normalized));
        if (duplicate) throw new IllegalArgumentException("Sự kiện đã có loại vé " + normalized + ".");
        TicketType type = newTicketType(normalized, price, quantity);
        event.addTicketType(type);
        return type;
    }

    public TicketType addTicketType(Admin admin, String eventId, TicketType type) {
        requireAdmin(admin);
        getEventById(eventId).addTicketType(type);
        return type;
    }

    public void updateTicketType(Admin admin, String eventId, String typeId, BigDecimal price, int quantity) {
        requireAdmin(admin);
        TicketType type = getEventById(eventId).findTicketType(typeId);
        if (type == null) throw new NotFoundException("Không tìm thấy loại vé.");
        type.changeStock(price, quantity);
    }

    private static TicketType newTicketType(String typeName, BigDecimal price, int quantity) {
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Giá vé không hợp lệ.");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng vé phải lớn hơn 0.");
        return new TicketType(newId("TT-"), normalizeType(typeName), price, quantity);
    }

    private static String normalizeType(String typeName) {
        if (typeName == null) throw new IllegalArgumentException("Loại vé không hợp lệ. Chỉ dùng VIP, STANDARD hoặc ECONOMY.");
        String normalized = typeName.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("VIP") && !normalized.equals("STANDARD") && !normalized.equals("ECONOMY"))
            throw new IllegalArgumentException("Loại vé không hợp lệ. Chỉ dùng VIP, STANDARD hoặc ECONOMY.");
        return normalized;
    }

    private static void requireAdmin(Admin admin) {
        if (admin == null) throw new IllegalArgumentException("Chỉ quản trị viên được quản lý sự kiện.");
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
