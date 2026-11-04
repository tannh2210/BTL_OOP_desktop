package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Statistics;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.util.Locale;

/** Người 4 – màn hình thống kê chỉ đọc (chỉ Admin). */
public class StatisticsController implements ScreenController {
    @FXML private Label events;
    @FXML private Label bookings;
    @FXML private Label tickets;
    @FXML private Label revenue;
    @FXML private Label bookingDetail;
    @FXML private Label ticketDetail;

    @Override
    public void init(AppContext context, Navigator navigator) {
        if (!(context.currentUser() instanceof Admin)) {
            events.setText("–");
            bookings.setText("–");
            tickets.setText("–");
            revenue.setText("Đăng nhập tài khoản quản trị để xem thống kê.");
            if (bookingDetail != null) bookingDetail.setText("");
            if (ticketDetail != null) ticketDetail.setText("");
            return;
        }
        Statistics stats = context.statistics().getStatistics();
        events.setText(String.valueOf(stats.getTotalEvents()));
        // Ô chính: số còn hiệu lực (không trộn đã hủy)
        bookings.setText(String.valueOf(stats.getConfirmedBookings()));
        tickets.setText(String.valueOf(stats.getActiveTickets() + stats.getUsedTickets()));
        if (bookingDetail != null) {
            bookingDetail.setText("Còn hiệu lực: " + stats.getConfirmedBookings()
                    + "  ·  Đã hủy: " + stats.getCancelledBookings());
        }
        if (ticketDetail != null) {
            ticketDetail.setText("Còn dùng được: " + stats.getActiveTickets()
                    + "  ·  Đã check-in: " + stats.getUsedTickets()
                    + "  ·  Đã hủy: " + stats.getCancelledTickets());
        }
        NumberFormat money = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        revenue.setText(money.format(stats.getTotalRevenue()) + " VND");
    }
}
