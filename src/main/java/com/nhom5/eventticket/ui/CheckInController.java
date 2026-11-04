package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.CheckIn;
import com.nhom5.eventticket.model.Ticket;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Người 4 – màn hình check-in (chỉ Admin). */
public class CheckInController implements ScreenController {
    @FXML private TextField ticketCodeField;
    @FXML private Label resultLabel;

    private AppContext context;

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.context = context;
        resultLabel.setText("");
        if (!(context.currentUser() instanceof Admin)) {
            resultLabel.setText("Vui lòng đăng nhập tài khoản quản trị viên để check-in.");
        }
    }

    @FXML
    private void checkIn() {
        if (!(context.currentUser() instanceof Admin admin)) {
            resultLabel.setText("Vui lòng đăng nhập tài khoản quản trị viên.");
            return;
        }
        try {
            CheckIn record = context.checkIns().checkIn(ticketCodeField.getText(), admin.getUserId());
            Ticket ticket = record.getTicket();
            String typeName = ticket.getTicketType() != null ? ticket.getTicketType().getName() : "—";
            // Chỉ hiện kết quả trên form — không bật thêm hộp thoại thành công (tránh báo trùng).
            resultLabel.setText("Check-in thành công.\n"
                    + "Mã vé: " + ticket.getTicketCode() + "\n"
                    + "Loại: " + typeName + "\n"
                    + "Quyền lợi: " + ticket.getBenefitSummary() + "\n"
                    + "Khách: " + ticket.getBooking().getCustomer().getFullName() + "\n"
                    + "Sự kiện: " + ticket.getBooking().getEvent().getName());
            ticketCodeField.clear();
        } catch (RuntimeException ex) {
            resultLabel.setText(ex.getMessage());
            UiAlerts.error("Check-in thất bại", ex);
        }
    }
}
