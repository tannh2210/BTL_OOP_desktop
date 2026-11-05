package com.nhom5.eventticket.model;

/** UML M3: VIPTicket extends Ticket.*/
public class VIPTicket extends Ticket {
    public VIPTicket(String ticketId, String ticketCode) { super(ticketId, ticketCode); }

    @Override
    public String getBenefitSummary() {
        return "VIP: ưu tiên lối vào, khu ghế gần sân khấu, quà tặng sự kiện.";
    }
}
