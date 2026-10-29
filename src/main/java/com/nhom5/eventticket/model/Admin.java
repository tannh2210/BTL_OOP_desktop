package com.nhom5.eventticket.model;

/** UML M3: Admin extends User.*/
public class Admin extends User {
    private final String adminId;
    public Admin(String adminId, String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email);
        this.adminId = java.util.Objects.requireNonNull(adminId);
    }
    public String getAdminId() { return adminId; }
    public void manageEvents() { throw new UnsupportedOperationException("Chưa hỗ trợ: EventService"); }
    public void manageTickets() { throw new UnsupportedOperationException("Chưa hỗ trợ: Event/Ticket service"); }
    public void manageBookings() { throw new UnsupportedOperationException("Chưa hỗ trợ: BookingService"); }
    public boolean checkInTicket() { throw new UnsupportedOperationException("Chưa hỗ trợ: CheckInService"); }
    public Statistics viewStatistics() { throw new UnsupportedOperationException("Chưa hỗ trợ: StatisticsService"); }
}
