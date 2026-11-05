package com.nhom5.eventticket;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Runs the real Application and login handlers in a separate, module-path JVM. */
public final class LoginRuntimeProbe extends MainApp {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void afterPulse(Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(400));
        pause.setOnFinished(event -> action.run());
        pause.play();
    }

    private static Button button(Parent root, String text) {
        return root.lookupAll(".button").stream().filter(Button.class::isInstance)
                .map(Button.class::cast).filter(b -> b.getText().contains(text))
                .findFirst().orElseThrow();
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            System.err.println("Uncaught on " + thread.getName());
            error.printStackTrace();
            System.exit(1);
        });
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
            super.start(stage);
            String role = getParameters().getRaw().get(0);
            afterPulse(() -> {
                Parent login = stage.getScene().getRoot();
                ((TextField) login.lookup("#loginUsernameField")).setText(role);
                ((TextField) login.lookup("#loginPasswordField")).setText("wrong-password");
                button(login, "Đăng nhập").fire();
                require(stage.isShowing(), "Stage closed after invalid login");
                require(!((Label) login.lookup("#statusLabel")).getText().isBlank(), "Missing login error");
                ((TextField) login.lookup("#loginPasswordField")).setText("123456");
                button(login, "Đăng nhập").fire();
                require(stage.getScene().getRoot() != login,
                        "Login did not open shell: " + ((Label) login.lookup("#statusLabel")).getText());
                afterPulse(() -> {
                    require(stage.isShowing(), "Stage closed after login");
                    Parent shell = stage.getScene().getRoot();
                    button(shell, role.equals("admin") ? "Quản lý sự kiện" : "Danh sách sự kiện").fire();
                    afterPulse(() -> {
                        TableView<?> table = (TableView<?>) shell.lookup("#eventTable");
                        require(table != null, "Event FXML not shown");
                        require(table.getColumns().size() == (role.equals("admin") ? 4 : 5), "Wrong columns");
                        require(!table.getItems().isEmpty(), "Missing event data");
                        stage.setWidth(980);
                        afterPulse(() -> {
                            require(stage.isShowing(), "Stage closed after resize");
                            stage.setWidth(1400);
                            afterPulse(() -> {
                                button(shell, "Đăng xuất").fire();
                                require(stage.getScene().getRoot().lookup("#loginUsernameField") != null, "Logout failed");
                                System.out.println("PASS module-path login/dashboard/events/resize/logout: " + role);
                                stage.close();
                                Platform.exit();
                            });
                        });
                    });
                });
            });
    }
}
