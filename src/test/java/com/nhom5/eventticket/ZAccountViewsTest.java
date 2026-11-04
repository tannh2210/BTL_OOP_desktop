package com.nhom5.eventticket;

import com.nhom5.eventticket.model.User;
import com.nhom5.eventticket.repository.DemoData;
import com.nhom5.eventticket.ui.AccountController;
import com.nhom5.eventticket.ui.LoginController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ZAccountViewsTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready::countDown);
        }
        assertTrue(ready.await(15, TimeUnit.SECONDS));
    }

    @Test
    void loginAndRegistrationFormsHaveTheirOwnEnterActionAndDefaultButton() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            AtomicInteger authenticated = new AtomicInteger();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.init(context, authenticated::incrementAndGet);

            VBox login = (VBox) root.lookup("#loginBox");
            VBox register = (VBox) root.lookup("#registerBox");
            Button loginButton = (Button) root.lookup("#loginButton");
            Button registerButton = (Button) root.lookup("#registerButton");
            assertTrue(login.isVisible());
            assertFalse(register.isVisible());
            assertTrue(loginButton.isDefaultButton());
            assertFalse(registerButton.isDefaultButton());

            ((Button) root.lookup("#registerLinkButton")).fire();
            assertFalse(login.isVisible());
            assertTrue(register.isVisible());
            assertFalse(loginButton.isDefaultButton());
            assertTrue(registerButton.isDefaultButton());

            // Enter in any registration field invokes registration validation, never login.
            for (String id : new String[]{"#fullNameField", "#registerUsernameField", "#emailField",
                    "#registerPasswordField", "#confirmPasswordField"}) {
                Event.fireEvent(root.lookup(id), new ActionEvent());
                assertEquals("Vui lòng nhập đủ thông tin.", ((Label) root.lookup("#statusLabel")).getText());
            }

            ((TextField) root.lookup("#fullNameField")).setText("A Test User");
            ((TextField) root.lookup("#registerUsernameField")).setText("a-test");
            ((TextField) root.lookup("#emailField")).setText("a-test@example.com");
            ((PasswordField) root.lookup("#registerPasswordField")).setText("secret1");
            ((PasswordField) root.lookup("#confirmPasswordField")).setText("secret1");
            Event.fireEvent(root.lookup("#emailField"), new ActionEvent());
            assertEquals("a-test", context.currentUser().getUsername());
            assertEquals(1, authenticated.get());

            ((Button) root.lookupAll(".button").stream().map(n -> (Button) n)
                    .filter(b -> "Đã có tài khoản? Đăng nhập".equals(b.getText())).findFirst().orElseThrow()).fire();
            ((TextField) root.lookup("#loginUsernameField")).setText("customer");
            ((PasswordField) root.lookup("#loginPasswordField")).setText("123456");
            Event.fireEvent(root.lookup("#loginPasswordField"), new ActionEvent());
            assertEquals("customer", context.currentUser().getUsername());
            assertEquals(2, authenticated.get());
            return null;
        });
    }

    @Test
    void accountViewChangesPasswordWithoutEndingSessionAndChecksConfirmation() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            User signedIn = context.auth().login("customer", "123456");
            context.loginAs(signedIn);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AccountView.fxml"));
            ScrollPane scrollPane = loader.load();
            Parent root = (Parent) scrollPane.getContent();
            ((AccountController) loader.getController()).init(context, null);

            ((PasswordField) root.lookup("#currentPasswordField")).setText("123456");
            ((PasswordField) root.lookup("#newPasswordField")).setText("newpass");
            ((PasswordField) root.lookup("#confirmNewPasswordField")).setText("mismatch");
            ((Button) root.lookup("#changePasswordButton")).fire();
            assertEquals("Mật khẩu mới và xác nhận mật khẩu không khớp.",
                    ((Label) root.lookup("#passwordStatusLabel")).getText());

            ((PasswordField) root.lookup("#confirmNewPasswordField")).setText("newpass");
            ((Button) root.lookup("#changePasswordButton")).fire();
            assertEquals("Đã đổi mật khẩu thành công.", ((Label) root.lookup("#passwordStatusLabel")).getText());
            assertSame(signedIn, context.currentUser());
            assertSame(signedIn, context.auth().login("customer", "newpass"));
            return null;
        });
    }

    private static <T> T onFx(java.util.concurrent.Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
}
