package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** First screen. The rest of the application opens only after a successful sign-in. */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    @FXML private VBox registerBox;

    private AppContext context;
    private Runnable onAuthenticated;

    public void init(AppContext context, Runnable onAuthenticated) {
        this.context = context;
        this.onAuthenticated = onAuthenticated;
    }

    @FXML
    private void showRegister() {
        registerBox.setVisible(true);
        registerBox.setManaged(true);
    }

    @FXML
    private void login() {
        try {
            var user = context.auth().login(usernameField.getText(), passwordField.getText());
            context.loginAs(user);
            onAuthenticated.run();
        } catch (RuntimeException ex) {
            statusLabel.setText(message(ex));
        }
    }

    @FXML
    private void register() {
        try {
            var user = context.auth().registerCustomer(
                    usernameField.getText(),
                    passwordField.getText(),
                    fullNameField.getText(),
                    emailField.getText());
            context.loginAs(user);
            onAuthenticated.run();
        } catch (RuntimeException ex) {
            statusLabel.setText(message(ex));
        }
    }

    private static String message(RuntimeException ex) {
        return ex.getMessage() == null ? "Không thể đăng nhập." : ex.getMessage();
    }
}
