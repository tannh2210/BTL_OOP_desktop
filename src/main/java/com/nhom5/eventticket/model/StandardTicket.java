package com.nhom5.eventticket.model;

/** UML M3: StandardTicket extends Ticket.*/
public class StandardTicket extends Ticket {
    public StandardTicket(String ticketId, String ticketCode) { super(ticketId, ticketCode); }

    @Override
    public String getBenefitSummary() {
        return "Standard: chỗ ngồi tiêu chuẩn, vào cửa theo giờ mở cổng.";
    }
}
