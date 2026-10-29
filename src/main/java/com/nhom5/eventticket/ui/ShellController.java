package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Main navigation. This shell is shown only after the user has signed in. */
public class ShellController {
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label sessionLabel;
    @FXML private Button eventsButton;
    @FXML private Button bookingsButton;
    @FXML private VBox adminSection;

    private Navigator navigator;
    private AppContext context;

    public void setContext(AppContext context, Runnable onLogout) {
        this.context = context;
        if (context.currentUser() == null) {
            throw new IllegalStateException("Cần đăng nhập trước khi mở ứng dụng.");
        }
        navigator = new Navigator(context, contentArea, this, onLogout);
        applyRole();
        navigator.dashboard();
    }

    void setPageTitle(String title, String subtitle) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        User user = context.currentUser();
        String role = user instanceof Admin ? "Quản trị" : "Khách hàng";
        sessionLabel.setText(user.getFullName() + " · " + role);
    }

    private void applyRole() {
        boolean admin = context.currentUser() instanceof Admin;
        eventsButton.setText(admin ? "▦   Quản lý sự kiện" : "▦   Danh sách sự kiện");
        bookingsButton.setVisible(!admin);
        bookingsButton.setManaged(!admin);
        adminSection.setVisible(admin);
        adminSection.setManaged(admin);
    }

    @FXML private void dashboard() { navigator.dashboard(); }
    @FXML private void accounts() { navigator.accounts(); }
    @FXML private void events() { navigator.events(); }
    @FXML private void bookings() { navigator.bookings(); }
    @FXML private void checkIns() { navigator.checkIns(); }
    @FXML private void statistics() { navigator.statistics(); }
    @FXML private void notifications() { navigator.notifications(); }
    @FXML private void logout() { navigator.requestLogout(); }
}
