package com.nhom5.eventticket;

import com.nhom5.eventticket.repository.DemoData;
import com.nhom5.eventticket.ui.LoginController;
import com.nhom5.eventticket.ui.ShellController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** JavaFX entry point. Login is shown before the main window. */
public class MainApp extends Application {
    private Stage stage;
    private AppContext context;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        this.context = new AppContext(DemoData.create());
        stage.setTitle("Event & Ticketing System");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        showLogin();
        stage.show();
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            LoginController login = loader.getController();
            login.init(context, this::showShell);
            show(root);
        } catch (Exception ex) {
            throw new IllegalStateException("Không mở được màn hình đăng nhập.", ex);
        }
    }

    public void showShell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Shell.fxml"));
            Parent root = loader.load();
            ShellController shell = loader.getController();
            shell.setContext(context, this::showLogin);
            show(root);
        } catch (Exception ex) {
            throw new IllegalStateException("Không mở được màn hình chính.", ex);
        }
    }

    private void show(Parent root) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            return;
        }
        stage.getScene().setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
