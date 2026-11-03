package com.nhom5.eventticket;

import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.repository.DemoData;
import com.nhom5.eventticket.ui.ScreenController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EventViewsTest {
    @BeforeAll static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(15, TimeUnit.SECONDS));
    }

    @Test void fxmlLoadsAndAdminSearchAndNewFormWork() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            assertInstanceOf(Admin.class, context.currentUser());
            Parent root = load("ManageEventsView.fxml", context);
            TableView<?> table = (TableView<?>) root.lookup("#eventTable");
            assertEquals(3, table.getItems().size());
            TextField search = (TextField) root.lookup("#searchField");
            search.setText("Technology");
            assertEquals(1, table.getItems().size());
            table.getSelectionModel().selectFirst();
            assertEquals("Technology Meetup", ((TextField) root.lookup("#nameField")).getText());
            assertEquals("09:00", ((TextField) root.lookup("#startTimeField")).getText());
            root.lookupAll(".button").stream().map(n -> (Button) n)
                    .filter(b -> "Nhập sự kiện mới".equals(b.getText())).findFirst().orElseThrow().fire();
            assertEquals("", ((TextField) root.lookup("#nameField")).getText());
            assertNull(((DatePicker) root.lookup("#datePicker")).getValue());
            return null;
        });
    }

    @Test void customerDetailsAndSearchClearSelection() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("customer", "123456"));
            Parent root = load("EventsView.fxml", context);
            Label detail = (Label) root.lookup("#eventDetail");
            assertTrue(detail.getText().contains("22:00"));
            assertEquals(3, ((ListView<?>) root.lookup("#ticketTypesList")).getItems().size());
            ((TextField) root.lookup("#searchField")).setText("no such event");
            assertEquals(0, ((TableView<?>) root.lookup("#eventTable")).getItems().size());
            assertTrue(((Button) root.lookup("#bookButton")).isDisabled());
            assertTrue(((ListView<?>) root.lookup("#ticketTypesList")).getItems().isEmpty());
            return null;
        });
    }

    private Parent load(String file, AppContext context) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + file));
        Parent root = loader.load();
        ((ScreenController) loader.getController()).init(context, null);
        // ScrollPane content is available through its content property before skin creation.
        return root instanceof ScrollPane scroll ? (Parent) scroll.getContent() : root;
    }

    private static <T> T onFx(java.util.concurrent.Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
}
