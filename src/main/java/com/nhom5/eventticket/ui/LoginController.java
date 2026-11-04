package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Customer;
import com.nhom5.eventticket.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** First screen. The rest of the application opens only after a successful sign-in. */
public class LoginController {
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField registerUsernameField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    @FXML private Label formTitle;
    @FXML private Label formDescription;
    @FXML private VBox loginBox;
    @FXML private VBox registerBox;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    private AppContext context;
    private Runnable onAuthenticated;

    public void init(AppContext context, Runnable onAuthenticated) {
        this.context = context;
        this.onAuthenticated = onAuthenticated;
    }

    @FXML
    private void initialize() {
        loginUsernameField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        loginPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        registerUsernameField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        registerPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        confirmPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        fullNameField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        emailField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        showLogin();
    }

    @FXML
    private void showRegister() {
        clearStatus();
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        registerBox.setVisible(true);
        registerBox.setManaged(true);
        loginButton.setDefaultButton(false);
        registerButton.setDefaultButton(true);
        formTitle.setText("Tạo tài khoản");
        formDescription.setText("Nhập đầy đủ thông tin để đăng ký tài khoản khách hàng.");
        fullNameField.requestFocus();
    }

    @FXML
    private void showLogin() {
        clearStatus();
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        registerButton.setDefaultButton(false);
        loginButton.setDefaultButton(true);
        formTitle.setText("Đăng nhập");
        formDescription.setText("Đăng nhập để tiếp tục sử dụng ứng dụng.");
        loginUsernameField.requestFocus();
    }

    @FXML
    private void login() {
        clearStatus();
        final User user;
        try {
            user = context.auth().login(loginUsernameField.getText(), loginPasswordField.getText());
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
        if (!registerPasswordField.getText().equals(confirmPasswordField.getText())) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }
        final Customer user;
        try {
            user = context.auth().registerCustomer(
                    registerUsernameField.getText(),
                    registerPasswordField.getText(),
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
