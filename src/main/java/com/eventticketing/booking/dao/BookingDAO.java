package com.eventticketing.booking.dao;

import com.eventticketing.booking.model.*;
import java.sql.*;
import java.util.*;

public class BookingDAO {
    public void insert(Connection c, Booking b) throws SQLException {
        String sql = "INSERT INTO bookings (booking_id,customer_id,event_id,event_name,"
          + "ticket_type_id,ticket_type_name,unit_price,quantity,booking_time,total_amount,status) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,b.getBookingId()); ps.setString(2,b.getCustomerId());
            ps.setString(3,b.getEventId()); ps.setString(4,b.getEventName());
            ps.setString(5,b.getTicketTypeId()); ps.setString(6,b.getTicketTypeName());
            ps.setBigDecimal(7,b.getUnitPrice()); ps.setInt(8,b.getQuantity());
            ps.setTimestamp(9,Timestamp.valueOf(b.getBookingTime()));
            ps.setBigDecimal(10,b.getTotalAmount()); ps.setString(11,b.getStatus().name());
            ps.executeUpdate();
        }
    }

    public void insertTickets(Connection c, Booking b, String ticketClass) throws SQLException {
        String sql = "INSERT INTO tickets (ticket_id,booking_id,ticket_code,ticket_type_id,"
          + "ticket_type_name,ticket_class,status) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Ticket t : b.getTickets()) {
                ps.setString(1,t.getTicketId()); ps.setString(2,b.getBookingId());
                ps.setString(3,t.getTicketCode()); ps.setString(4,t.getTicketTypeId());
                ps.setString(5,t.getTicketTypeName()); ps.setString(6,ticketClass);
                ps.setString(7,t.getStatus().name()); ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public record BookingHeader(String customerId, String ticketTypeId,
                                int quantity, BookingStatus status) {}

    public BookingHeader findHeaderForUpdate(Connection c, String id) throws SQLException {
        String sql = "SELECT customer_id,ticket_type_id,quantity,status "
                   + "FROM bookings WHERE booking_id=? FOR UPDATE";
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,id);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) return null;
                return new BookingHeader(rs.getString(1),rs.getString(2),
                        rs.getInt(3),BookingStatus.valueOf(rs.getString(4)));
            }
        }
    }

    public void updateStatus(Connection c,String id,BookingStatus status) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement(
                "UPDATE bookings SET status=? WHERE booking_id=?")) {
            ps.setString(1,status.name()); ps.setString(2,id);
            if (ps.executeUpdate()!=1) throw new SQLException("Booking not found.");
        }
    }

    public void cancelTickets(Connection c,String id) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement(
                "UPDATE tickets SET status='CANCELLED' WHERE booking_id=? AND status='VALID'")) {
            ps.setString(1,id); ps.executeUpdate();
        }
    }

    public List<Booking> findByCustomer(Connection c,String customerId) throws SQLException {
        return findMany(c,"SELECT * FROM bookings WHERE customer_id=? ORDER BY booking_time DESC",customerId);
    }
    public List<Booking> findAll(Connection c) throws SQLException {
        return findMany(c,"SELECT * FROM bookings ORDER BY booking_time DESC",null);
    }
    public Booking findById(Connection c,String id) throws SQLException {
        List<Booking> list=findMany(c,"SELECT * FROM bookings WHERE booking_id=?",id);
        return list.isEmpty()?null:list.get(0);
    }

    private List<Booking> findMany(Connection c,String sql,String parameter) throws SQLException {
        List<Booking> list=new ArrayList<>();
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            if(parameter!=null) ps.setString(1,parameter);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    String id=rs.getString("booking_id");
                    list.add(Booking.restore(id,rs.getString("customer_id"),
                        rs.getString("event_id"),rs.getString("event_name"),
                        rs.getString("ticket_type_id"),rs.getString("ticket_type_name"),
                        rs.getBigDecimal("unit_price"),rs.getInt("quantity"),
                        rs.getTimestamp("booking_time").toLocalDateTime(),
                        rs.getBigDecimal("total_amount"),
                        BookingStatus.valueOf(rs.getString("status")),
                        findTickets(c,id)));
                }
            }
        }
        return list;
    }

    private List<Ticket> findTickets(Connection c,String bookingId) throws SQLException {
        List<Ticket> list=new ArrayList<>();
        String sql="SELECT * FROM tickets WHERE booking_id=?";
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,bookingId);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    String cls=rs.getString("ticket_class");
                    Ticket t=switch(cls) {
                        case "VIP" -> new VIPTicket(rs.getString("ticket_id"),rs.getString("ticket_code"),
                                rs.getString("ticket_type_id"),rs.getString("ticket_type_name"));
                        case "STANDARD" -> new StandardTicket(rs.getString("ticket_id"),rs.getString("ticket_code"),
                                rs.getString("ticket_type_id"),rs.getString("ticket_type_name"));
                        default -> new EconomyTicket(rs.getString("ticket_id"),rs.getString("ticket_code"),
                                rs.getString("ticket_type_id"),rs.getString("ticket_type_name"));
                    };
                    if ("CANCELLED".equals(rs.getString("status"))) t.cancel();
                    list.add(t);
                }
            }
        }
        return list;
    }
}
