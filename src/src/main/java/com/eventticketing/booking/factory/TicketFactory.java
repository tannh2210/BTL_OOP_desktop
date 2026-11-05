package com.eventticketing.booking.factory;

import com.eventticketing.booking.model.*;
import java.util.Locale;
import java.util.UUID;

public class TicketFactory {
    public Ticket createTicket(String typeName, String typeId, String typeLabel) {
        if (typeName == null || typeName.isBlank())
            throw new IllegalArgumentException("Ticket type is required.");

        String id = UUID.randomUUID().toString();
        String code = "TKT-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);

        return switch (typeName.trim().toUpperCase(Locale.ROOT)) {
            case "VIP" -> new VIPTicket(id, code, typeId, typeLabel);
            case "STANDARD" -> new StandardTicket(id, code, typeId, typeLabel);
            case "ECONOMY" -> new EconomyTicket(id, code, typeId, typeLabel);
            default -> throw new IllegalArgumentException("Unsupported ticket type: " + typeName);
        };
    }
}
