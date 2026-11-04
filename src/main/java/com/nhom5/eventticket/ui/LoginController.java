package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/** First screen. The rest of the application opens only after a successful sign-in. */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    @FXML private VBox registerBox;
    @FXML private Button loginButton;
    @FXML private Button registerLinkButton;

    private AppContext context;
    private Runnable onAuthenticated;

    public void init(AppContext context, Runnable onAuthenticated) {
        this.context = context;
        this.onAuthenticated = onAuthenticated;
    }

    @FXML
    private void initialize() {
        usernameField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        confirmPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        fullNameField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        emailField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
    }

    @FXML
    private void showRegister() {
        clearStatus();
        registerBox.setVisible(true);
        registerBox.setManaged(true);
        loginButton.setVisible(false);
        loginButton.setManaged(false);
        registerLinkButton.setVisible(false);
        registerLinkButton.setManaged(false);
        usernameField.requestFocus();
    }

    @FXML
    private void showLogin() {
        clearStatus();
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        loginButton.setVisible(true);
        loginButton.setManaged(true);
        registerLinkButton.setVisible(true);
        registerLinkButton.setManaged(true);
    }

    @FXML
    private void login() {
        clearStatus();
        final com.nhom5.eventticket.model.User user;
        try {
            user = context.auth().login(usernameField.getText(), passwordField.getText());
        } catch (RuntimeException ex) {
            showError(message(ex));
            return;
        }
        context.loginAs(user);
        onAuthenticated.run();
    }

    @FXML
    private void register() {
        clearStatus();
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }
        final com.nhom5.eventticket.model.Customer user;
        try {
            user = context.auth().registerCustomer(
                    usernameField.getText(),
                    passwordField.getText(),
                    fullNameField.getText(),
                    emailField.getText());
        } catch (RuntimeException ex) {
            showError(message(ex));
            return;
        }
        context.loginAs(user);
        onAuthenticated.run();
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("form-error", "form-success");
        statusLabel.getStyleClass().add("form-error");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().remove("form-success");
        if (!statusLabel.getStyleClass().contains("form-error")) statusLabel.getStyleClass().add("form-error");
    }

    private static String message(RuntimeException ex) {
        return ex.getMessage() == null ? "Không thể xử lý yêu cầu." : ex.getMessage();
    }
}
