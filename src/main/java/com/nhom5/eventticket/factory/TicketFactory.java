package com.nhom5.eventticket.factory;

import com.nhom5.eventticket.model.*;
import java.util.Locale;
import java.util.UUID;
/** UML M3: factory creates ticket subclasses. OWNER: Booking module. */
public class TicketFactory {
    public Ticket createTicket(String typeName) {
        if (typeName == null) throw new IllegalArgumentException("typeName cannot be null");
        String id = UUID.randomUUID().toString();
        String code = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        switch (typeName.trim().toUpperCase(Locale.ROOT)) {
            case "VIP": return new VIPTicket(id, code);
            case "STANDARD": return new StandardTicket(id, code);
            case "ECONOMY": return new EconomyTicket(id, code);
            default: throw new IllegalArgumentException("Loại vé không hợp lệ. Chỉ dùng VIP, STANDARD hoặc ECONOMY.");
        }
    }
    // Milestone 5 sẽ tách Factory theo OCP. Bên gọi phải gán TicketType và gắn Ticket vào Booking.
}
