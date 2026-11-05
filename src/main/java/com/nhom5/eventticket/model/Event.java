package com.nhom5.eventticket.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** UML M3: Event composes TicketType (1 -- 0..*).*/
public class Event {
    private final String eventId;
    private String name;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private boolean ticketSalesOpen = true;
    private final List<TicketType> ticketTypes = new ArrayList<>();
    public Event(String eventId, String name, String description, String location,
                 LocalDateTime startTime, LocalDateTime endTime, String status) {
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("Mã sự kiện không được trống.");
        this.eventId = eventId;
        updateBasicInfo(name, description, location);
        reschedule(startTime, endTime);
        this.status = java.util.Objects.requireNonNull(status);
    }
    public String getEventId() { return eventId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void updateBasicInfo(String name, String description, String location) {
        if (name == null || name.isBlank() || location == null || location.isBlank())
            throw new IllegalArgumentException("Tên sự kiện và địa điểm không được để trống.");
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.location = location.trim();
    }
    public void reschedule(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException("Thời gian sự kiện không hợp lệ.");
        this.startTime = start;
        this.endTime = end;
    }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public boolean isTicketSalesOpen() { return ticketSalesOpen; }
    public void setTicketSalesOpen(boolean open) { ticketSalesOpen = open; }
    public boolean canBook() {
        return ticketSalesOpen && "OPEN".equals(status) && startTime.isAfter(LocalDateTime.now());
    }
    public String getSalesLabel() { return canBook() ? "Đang mở bán vé" : "Đã đóng bán vé"; }
    /** Only EventService may remove after checking dependencies under the store lock. */
    public void removeTicketType(TicketType type) { ticketTypes.remove(type); }
    public void setStatus(String status) { this.status = java.util.Objects.requireNonNull(status); }
    public List<TicketType> getTicketTypes() { return Collections.unmodifiableList(ticketTypes); }
    public void addTicketType(TicketType type) {
        java.util.Objects.requireNonNull(type);
        if (findTicketType(type.getTicketTypeId()) != null) throw new IllegalArgumentException("Duplicate ticketTypeId");
        if (ticketTypes.stream().anyMatch(t -> t.getName().equals(type.getName())))
            throw new IllegalArgumentException("Sự kiện đã có loại vé " + type.getName() + ".");
        type.attachTo(this);
        ticketTypes.add(type);
    }
    public TicketType findTicketType(String typeId) {
        for (TicketType type : ticketTypes) if (type.getTicketTypeId().equals(typeId)) return type;
        return null;
    }
    // UML-specified signatures, with service responsible for user input and policy.
    public Event createEvent() { return this; }
    /** @deprecated Use EventService.updateEvent with admin identity and explicit input. */
    @Deprecated
    public void updateEvent() { throw new UnsupportedOperationException("Dùng EventService.updateEvent với thông tin cần sửa."); }
    /** @deprecated Use EventService.deleteEvent to enforce authorization and booking references. */
    @Deprecated
    public void deleteEvent() { throw new UnsupportedOperationException("Dùng EventService.deleteEvent để kiểm tra quyền và booking."); }
    public String getDetails() {
        return eventId + " | " + name + " | " + location + " | " + startTime + " | " + status;
    }
}
