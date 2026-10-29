package com.nhom5.eventticket.ui;
import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.time.format.DateTimeFormatter;
public class DashboardController implements ScreenController {
    @FXML private Label eventCount;
    @FXML private Label bookingCount;
    @FXML private Label ticketCount;
    @FXML private ListView<String> upcomingList;
    @FXML private Button viewEventsButton;
    @FXML private Button bookButton;
    private Navigator navigator;
    @Override public void init(AppContext context, Navigator navigator) {
        this.navigator=navigator;
        boolean customer = context.currentUser() instanceof Customer;
        viewEventsButton.setText(customer ? "Xem sự kiện" : "Quản lý sự kiện");
        bookButton.setVisible(customer);
        bookButton.setManaged(customer);
        eventCount.setText(String.valueOf(context.store().allEvents().size()));
        bookingCount.setText(String.valueOf(context.store().allBookings().size()));
        ticketCount.setText(String.valueOf(context.store().allTickets().size()));
        var fmt=DateTimeFormatter.ofPattern("dd/MM/yyyy");
        upcomingList.getItems().setAll(context.events().listEvents().stream()
           .map(e-> e.getName()+"   •   "+e.getLocation()+"   •   "+e.getStartTime().format(fmt)).toList());
    }
    @FXML private void goEvents() {navigator.events();}
    @FXML private void goBooking() {navigator.bookings();}
}
