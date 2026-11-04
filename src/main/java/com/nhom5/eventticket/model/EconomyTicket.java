package com.nhom5.eventticket.model;

/** UML M3: EconomyTicket extends Ticket.*/
public class EconomyTicket extends Ticket {
    public EconomyTicket(String ticketId, String ticketCode) { super(ticketId, ticketCode); }

    @Override
    public String getBenefitSummary() {
        return "Economy: giá tiết kiệm, khu vực ghế phổ thông.";
    }
}
