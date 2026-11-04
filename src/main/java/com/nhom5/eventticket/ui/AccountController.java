package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AccountController implements ScreenController {
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Label profileStatusLabel;
    @FXML private Button saveButton;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label passwordStatusLabel;
    @FXML private Button changePasswordButton;

    private Navigator navigator;
    private AppContext context;

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.context = context;
        this.navigator = navigator;
        User user = context.currentUser();
        if (user == null) {
            usernameLabel.setText("Bạn chưa đăng nhập.");
            roleLabel.setText("");
            fullNameField.clear();
            emailField.clear();
            saveButton.setDisable(true);
            changePasswordButton.setDisable(true);
            return;
        }
        usernameLabel.setText(user.getUsername());
        roleLabel.setText(user instanceof Admin ? "Vai trò: Quản trị viên" : "Vai trò: Khách hàng");
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        saveButton.setDisable(false);
        changePasswordButton.setDisable(false);
    }

    @FXML
    private void initialize() {
        fullNameField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        emailField.textProperty().addListener((obs, oldValue, newValue) -> clearStatus());
        currentPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearPasswordStatus());
        newPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearPasswordStatus());
        confirmNewPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearPasswordStatus());
    }

    @FXML
    private void changePassword() {
        clearPasswordStatus();
        User user = context.currentUser();
        if (user == null) {
            showPasswordError("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
            return;
        }
        String newPassword = newPasswordField.getText();
        if (!newPassword.equals(confirmNewPasswordField.getText())) {
            showPasswordError("Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return;
        }
        try {
            context.auth().changePassword(user, currentPasswordField.getText(), newPassword);
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();
            passwordStatusLabel.setText("Đã đổi mật khẩu thành công.");
            passwordStatusLabel.getStyleClass().remove("form-error");
            if (!passwordStatusLabel.getStyleClass().contains("form-success"))
                passwordStatusLabel.getStyleClass().add("form-success");
        } catch (RuntimeException ex) {
            showPasswordError(ex.getMessage() == null ? "Không thể đổi mật khẩu." : ex.getMessage());
        }
    }

    @FXML
    private void saveProfile() {
        clearStatus();
        User user = context.currentUser();
        if (user == null) {
            showError("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
            return;
        }
        try {
            context.auth().updateProfile(user, fullNameField.getText(), emailField.getText());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            navigator.refreshSessionInfo();
            profileStatusLabel.setText("Đã cập nhật thông tin cá nhân.");
            profileStatusLabel.getStyleClass().remove("form-error");
            if (!profileStatusLabel.getStyleClass().contains("form-success"))
                profileStatusLabel.getStyleClass().add("form-success");
        } catch (RuntimeException ex) {
            showError(ex.getMessage() == null ? "Không thể cập nhật thông tin." : ex.getMessage());
        }
    }

    @FXML
    private void logout() {
        navigator.requestLogout();
    }

    private void clearStatus() {
        profileStatusLabel.setText("");
        profileStatusLabel.getStyleClass().removeAll("form-error", "form-success");
    }

    private void showError(String message) {
        profileStatusLabel.setText(message);
        profileStatusLabel.getStyleClass().remove("form-success");
        if (!profileStatusLabel.getStyleClass().contains("form-error"))
            profileStatusLabel.getStyleClass().add("form-error");
    }

    private void clearPasswordStatus() {
        passwordStatusLabel.setText("");
        passwordStatusLabel.getStyleClass().removeAll("form-error", "form-success");
    }

    private void showPasswordError(String message) {
        passwordStatusLabel.setText(message);
        passwordStatusLabel.getStyleClass().remove("form-success");
        if (!passwordStatusLabel.getStyleClass().contains("form-error"))
            passwordStatusLabel.getStyleClass().add("form-error");
    }
}
