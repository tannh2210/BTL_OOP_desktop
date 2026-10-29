package com.nhom5.eventticket.model;

import java.math.BigDecimal;
/** UML M3: TicketType belongs to one Event.*/
public class TicketType {
    private final String ticketTypeId;
    private String name;
    private BigDecimal price;
    private int quantity;
    private int availableQuantity;
    public TicketType(String ticketTypeId, String name, BigDecimal price, int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("quantity must be >= 0");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("price must be >= 0");
        this.ticketTypeId = java.util.Objects.requireNonNull(ticketTypeId);
        this.name = java.util.Objects.requireNonNull(name);
        this.price = price;
        this.quantity = quantity;
        this.availableQuantity = quantity;
    }
    public String getTicketTypeId() { return ticketTypeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = java.util.Objects.requireNonNull(name); }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("price must be >= 0");
        this.price = price;
    }
    public int getQuantity() { return quantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public boolean isAvailable() { return availableQuantity > 0; }
    /** UML method kept without arguments. Stock changes go through EventService. */
    public void updateQuantity() { throw new UnsupportedOperationException("Dùng EventService.updateTicketType"); }

    public void changeStock(BigDecimal newPrice, int newQuantity) {
        int sold = quantity - availableQuantity;
        if (newPrice == null || newPrice.signum() < 0) throw new IllegalArgumentException("Giá vé không hợp lệ.");
        if (newQuantity < sold) throw new IllegalArgumentException("Số lượng không được nhỏ hơn số vé đã đặt (" + sold + ").");
        this.price = newPrice;
        this.quantity = newQuantity;
        this.availableQuantity = newQuantity - sold;
    }

    /** Shared stock operations: BookingService must call these inside synchronized(store). */
    public void reserve(int count) {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        if (count > availableQuantity) throw new com.nhom5.eventticket.exception.OutOfStockException("Số lượng vé đặt vượt quá số vé còn lại.");
        availableQuantity -= count;
    }
    public void restore(int count) {
        if (count <= 0 || availableQuantity + count > quantity)
            throw new IllegalArgumentException("Invalid ticket restore quantity");
        availableQuantity += count;
    }
}
