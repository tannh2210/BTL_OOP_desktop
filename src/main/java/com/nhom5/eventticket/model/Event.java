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
    private final List<TicketType> ticketTypes = new ArrayList<>();
    public Event(String eventId, String name, String description, String location,
                 LocalDateTime startTime, LocalDateTime endTime, String status) {
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("Mã sự kiện không được trống.");
        this.eventId = eventId;
        updateBasicInfo(name, description, location);
        this.startTime = java.util.Objects.requireNonNull(startTime);
        this.endTime = java.util.Objects.requireNonNull(endTime);
        if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("endTime must be after startTime");
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
    public void setStatus(String status) { this.status = java.util.Objects.requireNonNull(status); }
    public List<TicketType> getTicketTypes() { return Collections.unmodifiableList(ticketTypes); }
    public void addTicketType(TicketType type) {
        java.util.Objects.requireNonNull(type);
        if (findTicketType(type.getTicketTypeId()) != null) throw new IllegalArgumentException("Duplicate ticketTypeId");
        if (ticketTypes.stream().anyMatch(t -> t.getName().equals(type.getName())))
            throw new IllegalArgumentException("Sự kiện đã có loại vé " + type.getName() + ".");
        ticketTypes.add(type);
    }
    public TicketType findTicketType(String typeId) {
        for (TicketType type : ticketTypes) if (type.getTicketTypeId().equals(typeId)) return type;
        return null;
    }
    // UML-specified signatures, with service responsible for user input and policy.
    public Event createEvent() { return this; }
    public void updateEvent() { throw new UnsupportedOperationException("Chưa hỗ trợ: updateEvent inputs"); }
    public void deleteEvent() { throw new UnsupportedOperationException("Chưa hỗ trợ: cancellation / delete policy"); }
    public String getDetails() {
        return eventId + " | " + name + " | " + location + " | " + startTime + " | " + status;
    }
}
