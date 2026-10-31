package com.eventticketing.booking.service;

import com.eventticketing.booking.dao.*;
import com.eventticketing.booking.factory.TicketFactory;
import com.eventticketing.booking.integration.*;
import com.eventticketing.booking.model.*;
import java.sql.*;
import java.util.*;

public class BookingService {
    private final BookingDAO bookingDAO=new BookingDAO();
    private final TicketTypeDAO ticketTypeDAO=new TicketTypeDAO();
    private final TicketFactory ticketFactory=new TicketFactory();

    public Booking bookTicket(String customerId, EventInfo event,
                              TicketTypeInfo type, int quantity) throws SQLException {
        if(customerId==null || customerId.isBlank() || event==null || type==null)
            throw new IllegalArgumentException("Customer, Event and TicketType are required.");
        if(quantity<=0) throw new IllegalArgumentException("Quantity must be positive.");
        if(type.getPrice()==null || type.getPrice().signum()<0)
            throw new IllegalArgumentException("Invalid ticket price.");
        if(type.getAvailableQuantity()<quantity)
            throw new IllegalArgumentException("Not enough tickets available.");

        String cls=type.getName().trim().toUpperCase(Locale.ROOT);
        List<Ticket> tickets=new ArrayList<>();
        for(int i=0;i<quantity;i++)
            tickets.add(ticketFactory.createTicket(cls,type.getTicketTypeId(),type.getName()));

        Booking booking=new Booking(UUID.randomUUID().toString(),customerId,
                event,type,quantity,tickets);

        try(Connection c=DatabaseConfig.getConnection()) {
            c.setAutoCommit(false);
            try {
                if(!ticketTypeDAO.decreaseAvailableQuantity(c,type.getTicketTypeId(),quantity))
                    throw new IllegalStateException("Inventory changed. Refresh and try again.");
                bookingDAO.insert(c,booking);
                bookingDAO.insertTickets(c,booking,cls);
                c.commit();
                return booking;
            } catch(Exception ex) {
                c.rollback();
                if(ex instanceof SQLException se) throw se;
                if(ex instanceof RuntimeException re) throw re;
                throw new SQLException(ex);
            }
        }
    }

    public List<Booking> getCustomerBookingHistory(String customerId) throws SQLException {
        try(Connection c=DatabaseConfig.getConnection()) {
            return bookingDAO.findByCustomer(c,customerId);
        }
    }

    public void cancelBooking(String customerId,String bookingId) throws SQLException {
        cancelInternal(customerId,bookingId);
    }

    public List<Booking> getAllBookings() throws SQLException {
        try(Connection c=DatabaseConfig.getConnection()) { return bookingDAO.findAll(c); }
    }

    public Booking getBookingById(String bookingId) throws SQLException {
        try(Connection c=DatabaseConfig.getConnection()) { return bookingDAO.findById(c,bookingId); }
    }

    public void adminCancelBooking(String bookingId) throws SQLException {
        cancelInternal(null,bookingId);
    }

    private void cancelInternal(String customerId,String bookingId) throws SQLException {
        try(Connection c=DatabaseConfig.getConnection()) {
            c.setAutoCommit(false);
            try {
                BookingDAO.BookingHeader h=bookingDAO.findHeaderForUpdate(c,bookingId);
                if(h==null) throw new IllegalArgumentException("Booking not found.");
                if(customerId!=null && !customerId.equals(h.customerId()))
                    throw new SecurityException("Cannot cancel another customer's booking.");
                if(h.status()!=BookingStatus.CONFIRMED)
                    throw new IllegalStateException("Only CONFIRMED booking can be cancelled.");

                ticketTypeDAO.increaseAvailableQuantity(c,h.ticketTypeId(),h.quantity());
                bookingDAO.cancelTickets(c,bookingId);
                bookingDAO.updateStatus(c,bookingId,BookingStatus.CANCELLED);
                c.commit();
            } catch(Exception ex) {
                c.rollback();
                if(ex instanceof SQLException se) throw se;
                if(ex instanceof RuntimeException re) throw re;
                throw new SQLException(ex);
            }
        }
    }
}
