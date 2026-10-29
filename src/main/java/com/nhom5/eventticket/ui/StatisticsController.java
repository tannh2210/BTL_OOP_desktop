package com.nhom5.eventticket.ui;
import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class StatisticsController implements ScreenController {
    @FXML private Label events,bookings,tickets,revenue;
    @Override public void init(AppContext context,Navigator navigator){
        if(!(context.currentUser() instanceof Admin)) {
            events.setText("–");bookings.setText("–");tickets.setText("–");
            revenue.setText("Đăng nhập tài khoản quản trị để xem thống kê.");return;
        }
        var s=context.statistics().getStatistics();
        events.setText(String.valueOf(s.getTotalEvents()));
        bookings.setText(String.valueOf(s.getTotalBookings()));
        tickets.setText(String.valueOf(s.getTotalTickets()));
        revenue.setText(s.getTotalRevenue().toPlainString()+" VND");
    }
}
