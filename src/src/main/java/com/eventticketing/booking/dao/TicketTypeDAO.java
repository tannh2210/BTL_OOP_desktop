package com.eventticketing.booking.dao;
import java.sql.*;

public class TicketTypeDAO {
    public boolean decreaseAvailableQuantity(Connection c, String typeId, int qty)
            throws SQLException {
        String sql = "UPDATE ticket_types SET available_quantity = available_quantity - ? "
                   + "WHERE ticket_type_id = ? AND available_quantity >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty); ps.setString(2, typeId); ps.setInt(3, qty);
            return ps.executeUpdate() == 1;
        }
    }

    public void increaseAvailableQuantity(Connection c, String typeId, int qty)
            throws SQLException {
        String sql = "UPDATE ticket_types SET available_quantity = available_quantity + ? "
                   + "WHERE ticket_type_id = ? AND available_quantity + ? <= total_quantity";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty); ps.setString(2, typeId); ps.setInt(3, qty);
            if (ps.executeUpdate() != 1) throw new SQLException("Could not restore inventory.");
        }
    }
}
