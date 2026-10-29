package com.nhom5.eventticket;
import com.nhom5.eventticket.repository.DemoData;
import com.nhom5.eventticket.model.*;
import java.math.BigDecimal;
public class BasicLogicTest {
    private static void ok(boolean v,String message) {if(!v)throw new AssertionError(message);}
    public static void main(String[] args) {
        AppContext app=new AppContext(DemoData.create());
        var customer=app.auth().login("customer","123456");
        var admin=app.auth().login("admin","123456");
        ok(customer instanceof Customer && admin instanceof Admin,"Login");
        ok(app.events().searchEvents("nhạc").size()==1,"Search");
        var type=app.events().getEventById("EV001").findTicketType("T001");
        int before=type.getAvailableQuantity();
        Booking b=app.bookings().bookTicket(customer.getUserId(),"EV001","T001",2);
        ok(b.getTickets().size()==2,"Quantity");
        ok(type.getAvailableQuantity()==before-2,"Stock decreased");
        ok(b.getTotalAmount().compareTo(new BigDecimal("1000000"))==0,"Price");
        ok(app.bookings().getBookingHistory(customer.getUserId()).size()==1,"History");
        ok(app.statistics().getStatistics().getTotalRevenue().compareTo(new BigDecimal("1000000"))==0,"Revenue");
        String code=b.getTickets().get(0).getTicketCode();
        var record=app.checkIns().checkIn(code,admin.getUserId());
        ok(record.getTicket().isUsed(),"Checkin");
        boolean duplicate=false;
        try{app.checkIns().checkIn(code,admin.getUserId());}catch(RuntimeException ex){duplicate=true;}
        ok(duplicate,"Duplicate checkin rejected");
        boolean cancelledUsed=false;
        try{app.bookings().cancelBooking(customer.getUserId(),b.getBookingId());}catch(RuntimeException ex){cancelledUsed=true;}
        ok(cancelledUsed,"Cannot cancel used ticket");
        Booking b2=app.bookings().bookTicket(customer.getUserId(),"EV001","T002",1);
        int available=app.events().getEventById("EV001").findTicketType("T002").getAvailableQuantity();
        app.bookings().cancelBooking(customer.getUserId(),b2.getBookingId());
        ok(app.events().getEventById("EV001").findTicketType("T002").getAvailableQuantity()==available+1,"Cancellation restores stock");
        ok(!app.notifications().forUser(customer.getUserId()).isEmpty(),"Notifications");
        Event created = app.events().createEventWithTicket((Admin) admin, "Su kien thu", "Mo ta", "Hue",
                java.time.LocalDateTime.now().plusDays(12), "ECONOMY", new BigDecimal("50000"), 20);
        var createdType = created.getTicketTypes().get(0);
        app.events().updateTicketType((Admin) admin, created.getEventId(), createdType.getTicketTypeId(), new BigDecimal("60000"), 15);
        ok(createdType.getAvailableQuantity() == 15, "Stock updated");
        app.events().deleteEvent((Admin) admin, created.getEventId());
        boolean missing = false;
        try { app.events().getEventById(created.getEventId()); } catch (RuntimeException ex) { missing = true; }
        ok(missing, "Deleted");
        boolean blocked = false;
        try { app.events().deleteEvent((Admin) admin, "EV001"); } catch (RuntimeException ex) { blocked = true; }
        ok(blocked, "Cannot delete booked event");
        boolean absent = false;
        try { app.checkIns().checkIn("KHONG-CO", admin.getUserId()); } catch (RuntimeException ex) { absent = ex.getMessage().contains("không tồn tại"); }
        ok(absent, "Missing ticket");
        System.out.println("PASS: Basic desktop Java business logic tests");
    }
}
