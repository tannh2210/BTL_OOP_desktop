package com.eventticketing.booking.integration;
import java.math.BigDecimal;
public interface TicketTypeInfo {
    String getTicketTypeId();
    String getName();
    BigDecimal getPrice();
    int getAvailableQuantity();
}
