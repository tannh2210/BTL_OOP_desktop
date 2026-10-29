package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AccountController implements ScreenController {
    @FXML private Label currentUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;

    private Navigator navigator;

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.navigator = navigator;
        var user = context.currentUser();
        if (user == null) {
            currentUserLabel.setText("Bạn chưa đăng nhập.");
            roleLabel.setText("");
            emailLabel.setText("");
            return;
        }
        currentUserLabel.setText(user.getFullName() + " (" + user.getUsername() + ")");
        roleLabel.setText(user instanceof Admin ? "Vai trò: Quản trị viên" : "Vai trò: Khách hàng");
        emailLabel.setText(user.getEmail());
    }

    @FXML
    private void logout() {
        navigator.requestLogout();
    }
}
