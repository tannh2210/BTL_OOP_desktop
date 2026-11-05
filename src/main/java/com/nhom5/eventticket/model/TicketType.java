package com.nhom5.eventticket.model;

import java.math.BigDecimal;
/** UML M3: TicketType belongs to one Event.*/
public class TicketType {
    private final String ticketTypeId;
    private String name;
    private BigDecimal price;
    private int quantity;
    private int availableQuantity;
    private Event owner;
    private boolean salesEnabled = true;
    public boolean isSalesEnabled() { return salesEnabled; }
    /** Per-type switch is independent; actual sales also depend on the owning event. */
    public String getSalesLabel() {
        if (!salesEnabled) return "Đã ngừng bán riêng • Không thể đặt vé";
        if (owner != null && !owner.isTicketSalesOpen())
            return "Bật bán riêng • Không thể đặt: sự kiện đã đóng bán";
        if (owner != null && !owner.canBook())
            return "Bật bán riêng • Không thể đặt: sự kiện không còn nhận đặt vé";
        if (availableQuantity == 0) return "Bật bán riêng • Hết vé";
        return "Bật bán riêng • Có thể đặt vé";
    }
    public void setSalesEnabled(boolean enabled) { salesEnabled = enabled; }
    public TicketType(String ticketTypeId, String name, BigDecimal price, int quantity) {
        validateStock(price, quantity);
        if (ticketTypeId == null || ticketTypeId.isBlank()) throw new IllegalArgumentException("Mã loại vé không được trống.");
        this.ticketTypeId = ticketTypeId;
        this.name = normalizeName(name);
        this.price = price;
        this.quantity = quantity;
        this.availableQuantity = quantity;
    }
    public String getTicketTypeId() { return ticketTypeId; }
    public String getName() { return name; }
    public void setName(String name) {
        String normalized = normalizeName(name);
        if (owner != null && owner.getTicketTypes().stream()
                .anyMatch(type -> type != this && type.getName().equals(normalized)))
            throw new IllegalArgumentException("Sự kiện đã có loại vé " + normalized + ".");
        this.name = normalized;
    }
    /** Composition is assigned only by Event.addTicketType; inventory cannot be shared by events. */
    void attachTo(Event event) {
        if (owner != null && owner != event)
            throw new IllegalArgumentException("Loại vé đã thuộc một sự kiện khác.");
        owner = java.util.Objects.requireNonNull(event);
    }
    public static String normalizeName(String name) {
        if (name == null) throw new IllegalArgumentException("Hãy chọn VIP, STANDARD hoặc ECONOMY.");
        String value = name.trim().toUpperCase(java.util.Locale.ROOT);
        if (!java.util.Set.of("VIP", "STANDARD", "ECONOMY").contains(value))
            throw new IllegalArgumentException("Chỉ hỗ trợ VIP, STANDARD hoặc ECONOMY.");
        return value;
    }
    /** Read-only check. Booking owns reservation and must check/reserve under the store lock. */
    public boolean hasAvailableQuantity(int count) {
        if (count <= 0) throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0.");
        return count <= availableQuantity;
    }
    public void checkAvailability(int count) {
        if (!salesEnabled) throw new IllegalArgumentException("Loại vé đã ngừng bán.");
        if (!hasAvailableQuantity(count))
            throw new com.nhom5.eventticket.exception.OutOfStockException("Số lượng vé đặt vượt quá số vé còn lại.");
    }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        validateStock(price, quantity);
        this.price = price;
    }
    public int getQuantity() { return quantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public boolean isAvailable() { return salesEnabled && availableQuantity > 0; }
    /** @deprecated Use EventService.updateTicketType with explicit price and total quantity. */
    @Deprecated
    public void updateQuantity() { throw new UnsupportedOperationException("Dùng EventService.updateTicketType"); }

    public void changeStock(BigDecimal newPrice, int newQuantity) {
        int sold = quantity - availableQuantity;
        validateStock(newPrice, newQuantity);
        if (newQuantity < sold) throw new IllegalArgumentException("Số lượng không được nhỏ hơn số vé đã đặt (" + sold + ").");
        this.price = newPrice;
        this.quantity = newQuantity;
        this.availableQuantity = newQuantity - sold;
    }

    /** Shared stock operations: BookingService must call these inside synchronized(store). */
    public void reserve(int count) {
        checkAvailability(count);
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        if (count > availableQuantity) throw new com.nhom5.eventticket.exception.OutOfStockException("Số lượng vé đặt vượt quá số vé còn lại.");
        availableQuantity -= count;
    }
    private static void validateStock(BigDecimal price, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng vé phải là số nguyên lớn hơn 0.");
        if (price == null || price.signum() <= 0) throw new IllegalArgumentException("Giá vé phải lớn hơn 0; chưa hỗ trợ vé miễn phí.");
    }
    public void restore(int count) {
        if (count <= 0 || count > quantity - availableQuantity)
            throw new IllegalArgumentException("Invalid ticket restore quantity");
        availableQuantity += count;
    }
}
