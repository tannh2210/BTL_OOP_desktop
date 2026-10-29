package com.nhom5.eventticket.repository;
import com.nhom5.eventticket.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Small set of sample data; kept only for the current application session. */
public final class DemoData {
    private DemoData() { }
    public static InMemoryStore create() {
        InMemoryStore store = new InMemoryStore();
        store.saveUser(new Customer("CUS1", "U1", "customer", "123456", "Khách hàng mẫu", "customer@example.local"));
        store.saveUser(new Admin("ADM1", "U2", "admin", "123456", "Quản trị viên", "admin@example.local"));
        Event music = new Event("EV001", "Đêm nhạc mùa đông", "Chương trình âm nhạc và giao lưu nghệ sĩ", "Hà Nội",
                LocalDateTime.of(2027, 1, 15, 19, 0), LocalDateTime.of(2027, 1, 15, 22, 0), "OPEN");
        music.addTicketType(new TicketType("T001", "VIP", new BigDecimal("500000"), 80));
        music.addTicketType(new TicketType("T002", "STANDARD", new BigDecimal("250000"), 160));
        music.addTicketType(new TicketType("T003", "ECONOMY", new BigDecimal("150000"), 200));
        store.saveEvent(music);
        Event tech = new Event("EV002", "Technology Meetup", "Gặp gỡ cộng đồng yêu công nghệ và lập trình", "Đà Nẵng",
                LocalDateTime.of(2027, 2, 5, 9, 0), LocalDateTime.of(2027, 2, 5, 12, 0), "OPEN");
        tech.addTicketType(new TicketType("T004", "STANDARD", new BigDecimal("100000"), 120));
        store.saveEvent(tech);
        Event arts = new Event("EV003", "Hội chợ sáng tạo", "Triển lãm sản phẩm thủ công và thiết kế", "TP. Hồ Chí Minh",
                LocalDateTime.of(2027, 3, 12, 8, 0), LocalDateTime.of(2027, 3, 12, 17, 0), "OPEN");
        arts.addTicketType(new TicketType("T005", "STANDARD", new BigDecimal("80000"), 300));
        store.saveEvent(arts);
        return store;
    }
}
