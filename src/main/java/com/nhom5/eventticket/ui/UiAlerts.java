package com.nhom5.eventticket.ui;

import javafx.scene.control.Alert;

public final class UiAlerts {
    private UiAlerts() { }
    public static void error(String title, Throwable ex) {
        show(Alert.AlertType.ERROR, title, ex.getMessage() == null ? ex.toString() : ex.getMessage());
    }
    public static void success(String message) { show(Alert.AlertType.INFORMATION, "Thành công", message); }
    private static void show(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
