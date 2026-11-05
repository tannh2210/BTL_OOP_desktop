package com.nhom5.eventticket;

import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Event;
import com.nhom5.eventticket.model.TicketType;
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

    @Test void inlineValidationRejectsZeroNegativeDecimalAndEmptyQuantity() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            Parent root = load("ManageEventsView.fxml", context);
            TextField quantity = (TextField) root.lookup("#quantityField");
            for (String invalid : new String[]{"0", "-1", "1.5", "", "abc", "2147483648"}) {
                quantity.setText(invalid);
                assertFalse(((Label) root.lookup("#quantityError")).getText().isEmpty());
            }
            quantity.setText("2");
            assertEquals("", ((Label) root.lookup("#quantityError")).getText());
            TextField price = (TextField) root.lookup("#priceField");
            price.setText("0");
            assertFalse(((Label) root.lookup("#priceError")).getText().isEmpty());
            return null;
        });
    }

    @Test void customerSeesClosedEventAndHiddenStoppedType() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            Admin admin = (Admin) context.auth().login("admin", "123456");
            context.events().setTicketSalesOpen(admin, "EV001", false);
            context.events().setTicketTypeSalesEnabled(admin, "EV001", "T001", false);
            context.loginAs(context.auth().login("customer", "123456"));
            Parent root = load("EventsView.fxml", context);
            assertTrue(((Label) root.lookup("#eventDetail")).getText().contains("Đã đóng bán vé"));
            assertTrue(((Button) root.lookup("#bookButton")).isDisabled());
            assertEquals(2, ((ListView<?>) root.lookup("#ticketTypesList")).getItems().size());
            return null;
        });
    }

    @Test void mismatchedTicketCategoryDoesNotUpdateSelectedInventory() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            Parent root = load("ManageEventsView.fxml", context);
            var selected = context.events().getEventById("EV001").findTicketType("T001");
            ((ComboBox<?>) root.lookup("#typeBox")).getSelectionModel().select(1);
            ((TextField) root.lookup("#priceField")).setText("1");
            ((TextField) root.lookup("#quantityField")).setText("1");
            var message = new java.util.concurrent.atomic.AtomicReference<String>();
            Platform.runLater(() -> {
                for (var window : java.util.List.copyOf(javafx.stage.Window.getWindows())) {
                    if (window.getScene() != null && window.getScene().getRoot() instanceof DialogPane dialog) {
                        message.set(dialog.getContentText());
                        ((Button) dialog.lookupButton(ButtonType.OK)).fire();
                    }
                }
            });
            root.lookupAll(".button").stream().map(n -> (Button) n)
                    .filter(b -> "Cập nhật loại vé".equals(b.getText())).findFirst().orElseThrow().fire();
            assertNotNull(message.get());
            assertTrue(message.get().contains("Hạng vé phải khớp"));
            assertEquals(new java.math.BigDecimal("500000"), selected.getPrice());
            assertEquals(80, selected.getQuantity());
            return null;
        });
    }

    private static <T> T onFx(java.util.concurrent.Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }

    private static Button button(Parent root, String text) {
        return root.lookupAll(".button").stream().map(n -> (Button) n)
                .filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
    }

    /** Exercise real FXML handlers and modal dialogs on the JavaFX thread, without OS input. */
    private static java.util.List<String> dialogs(Runnable action, ButtonType response) {
        var messages = new java.util.ArrayList<String>();
        javafx.collections.ListChangeListener<javafx.stage.Window> listener = change -> {
            while (change.next()) if (change.wasAdded()) {
                for (var window : java.util.List.copyOf(change.getAddedSubList())) Platform.runLater(() -> {
                    if (window.isShowing() && window.getScene().getRoot() instanceof DialogPane pane) {
                        messages.add(pane.getContentText());
                        ButtonType choice = pane.getButtonTypes().contains(response) ? response : ButtonType.OK;
                        ((Button) pane.lookupButton(choice)).fire();
                    }
                });
            }
        };
        javafx.stage.Window.getWindows().addListener(listener);
        try { action.run(); } finally { javafx.stage.Window.getWindows().removeListener(listener); }
        return messages;
    }

    @Test void salesButtonsUpdateImmediatelyAndRetainSelectedTicket() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            Parent root = load("ManageEventsView.fxml", context);
            Button sales = (Button) root.lookup("#salesButton");
            assertEquals("Đóng bán vé", sales.getText());
            sales.fire();
            assertFalse(context.events().getEventById("EV001").isTicketSalesOpen());
            assertEquals("Mở bán vé", sales.getText());
            assertEquals("Đã đóng bán", ((Label) root.lookup("#salesLabel")).getText());
            sales.fire();
            assertEquals("Đóng bán vé", sales.getText());
            @SuppressWarnings("unchecked") ListView<TicketType> types = (ListView<TicketType>) root.lookup("#ticketList");
            types.getSelectionModel().select(1);
            TicketType selected = types.getSelectionModel().getSelectedItem();
            Button toggle = (Button) root.lookup("#ticketSalesButton");
            assertEquals(1, dialogs(toggle::fire, ButtonType.CANCEL).size());
            assertTrue(selected.isSalesEnabled());
            assertEquals(1, dialogs(toggle::fire, ButtonType.OK).size());
            assertFalse(selected.isSalesEnabled());
            assertSame(selected, types.getSelectionModel().getSelectedItem());
            assertEquals("Mở bán loại vé", toggle.getText());
            dialogs(toggle::fire, ButtonType.OK);
            assertTrue(selected.isSalesEnabled());
            assertEquals("Ngừng bán loại vé", toggle.getText());
            button(root, "Nhập sự kiện mới").fire();
            assertTrue(sales.isDisabled()); assertTrue(toggle.isDisabled());
            return null;
        });
    }

    @Test void ticketCreateAndUpdateHandlersRejectInvalidInputsWithoutMutation() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            Parent root = load("ManageEventsView.fxml", context);
            var type = context.events().getEventById("EV001").findTicketType("T001");
            for (String action : new String[]{"Thêm loại vé", "Cập nhật loại vé"}) {
                for (String invalid : new String[]{"0", "-1", "1.5", "", "abc"}) {
                    ((TextField) root.lookup("#quantityField")).setText(invalid);
                    var messages = dialogs(() -> button(root, action).fire(), ButtonType.OK);
                    assertEquals(1, messages.size());
                    assertTrue(messages.get(0).contains("số nguyên lớn hơn 0"));
                    assertEquals(80, type.getQuantity());
                }
                ((TextField) root.lookup("#quantityField")).setText("80");
                for (String invalid : new String[]{"0", "-1", "", "abc"}) {
                    ((TextField) root.lookup("#priceField")).setText(invalid);
                    var messages = dialogs(() -> button(root, action).fire(), ButtonType.OK);
                    assertTrue(messages.get(0).contains("Giá vé phải lớn hơn 0"));
                    assertEquals(new java.math.BigDecimal("500000"), type.getPrice());
                }
                ((TextField) root.lookup("#priceField")).setText("500000");
            }
            assertEquals(3, context.events().getEventById("EV001").getTicketTypes().size());
            return null;
        });
    }

    @Test void eventAndTicketCrudThroughFxmlHandlers() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            Parent root = load("ManageEventsView.fxml", context);
            button(root, "Nhập sự kiện mới").fire();
            ((TextField) root.lookup("#nameField")).setText("UI regression");
            ((TextField) root.lookup("#locationField")).setText("Huế");
            var date = java.time.LocalDate.now().plusDays(30);
            for (String id : new String[]{"#datePicker", "#endDatePicker"}) {
                DatePicker picker = (DatePicker) root.lookup(id);
                // Enter through the editor: the controller commits editor text before saving.
                picker.getEditor().setText(picker.getConverter().toString(date));
            }
            ((TextField) root.lookup("#priceField")).setText("100");
            ((TextField) root.lookup("#quantityField")).setText("5");
            assertEquals(java.util.List.of("Đã thêm sự kiện."), dialogs(() -> button(root, "Thêm sự kiện").fire(), ButtonType.OK));
            @SuppressWarnings("unchecked") TableView<Event> table = (TableView<Event>) root.lookup("#eventTable");
            Event event = table.getSelectionModel().getSelectedItem();
            assertEquals("UI regression", event.getName()); assertEquals(4, table.getItems().size());
            ((TextField) root.lookup("#nameField")).setText("UI edited");
            assertEquals(java.util.List.of("Đã lưu sự kiện."), dialogs(() -> button(root, "Lưu sửa").fire(), ButtonType.OK));
            assertEquals("UI edited", event.getName());
            ((ComboBox<?>) root.lookup("#typeBox")).getSelectionModel().select(0);
            assertEquals(java.util.List.of("Đã thêm loại vé."), dialogs(() -> button(root, "Thêm loại vé").fire(), ButtonType.OK));
            @SuppressWarnings("unchecked") ListView<TicketType> types = (ListView<TicketType>) root.lookup("#ticketList");
            types.getSelectionModel().select(1);
            TicketType vip = types.getSelectionModel().getSelectedItem();
            ((TextField) root.lookup("#priceField")).setText("200");
            ((TextField) root.lookup("#quantityField")).setText("8");
            dialogs(() -> button(root, "Cập nhật loại vé").fire(), ButtonType.OK);
            assertEquals(new java.math.BigDecimal("200"), vip.getPrice()); assertEquals(8, vip.getQuantity());
            assertSame(vip, types.getSelectionModel().getSelectedItem());
            dialogs(() -> ((Button) root.lookup("#deleteTicketButton")).fire(), ButtonType.CANCEL);
            assertEquals(2, types.getItems().size());
            dialogs(() -> ((Button) root.lookup("#deleteTicketButton")).fire(), ButtonType.OK);
            assertNull(event.findTicketType(vip.getTicketTypeId())); assertEquals(1, types.getItems().size());
            assertEquals(2, dialogs(() -> button(root, "Xóa").fire(), ButtonType.OK).size());
            assertEquals(3, table.getItems().size());
            assertTrue(context.events().searchEvents("UI edited").isEmpty());
            return null;
        });
    }

    @Test void sameSessionRoleSwitchPreservesSalesStockAndHistory() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            context.loginAs(context.auth().login("admin", "123456"));
            Parent admin = load("ManageEventsView.fxml", context);
            ((Button) admin.lookup("#salesButton")).fire();
            context.logout(); context.loginAs(context.auth().login("customer", "123456"));
            Parent customer = load("BookingView.fxml", context);
            assertEquals("Đã đóng bán vé", ((Label) customer.lookup("#availabilityLabel")).getText());
            assertTrue(((Button) customer.lookup("#placeBookingButton")).isDisabled());
            assertThrows(IllegalArgumentException.class, () -> context.bookings().bookTicket("U1", "EV001", "T001", 1));
            context.logout(); context.loginAs(context.auth().login("admin", "123456"));
            admin = load("ManageEventsView.fxml", context);
            assertEquals("Mở bán vé", ((Button) admin.lookup("#salesButton")).getText());
            ((Button) admin.lookup("#salesButton")).fire();
            context.logout(); context.loginAs(context.auth().login("customer", "123456"));
            customer = load("BookingView.fxml", context);
            Parent bookingView = customer;
            var success = dialogs(() -> ((Button) bookingView.lookup("#placeBookingButton")).fire(), ButtonType.OK);
            assertTrue(success.get(0).contains("Đặt vé thành công"));
            assertEquals("Còn 79 vé", ((Label) customer.lookup("#availabilityLabel")).getText());
            assertEquals(1, context.bookings().getBookingHistory("U1").size());
            var booking = context.bookings().getBookingHistory("U1").get(0);
            var code = booking.getTickets().get(0).getTicketCode();
            assertTrue(((ListView<?>) customer.lookup("#bookingList")).getItems().get(0).toString().contains(code));
            context.logout(); context.loginAs(context.auth().login("admin", "123456"));
            admin = load("ManageEventsView.fxml", context);
            Parent adminView = admin;
            assertTrue(((Label) admin.lookup("#stockLabel")).getText().contains("Còn 79 vé"));
            var refused = dialogs(() -> ((Button) adminView.lookup("#deleteTicketButton")).fire(), ButtonType.OK);
            assertEquals(2, refused.size()); assertTrue(refused.get(1).contains("lịch sử"));
            dialogs(() -> ((Button) adminView.lookup("#ticketSalesButton")).fire(), ButtonType.OK);
            assertEquals("Mở bán loại vé", ((Button) admin.lookup("#ticketSalesButton")).getText());
            context.logout(); context.loginAs(context.auth().login("customer", "123456"));
            customer = load("BookingView.fxml", context);
            assertEquals(2, ((ComboBox<?>) customer.lookup("#typeBox")).getItems().size());
            assertTrue(booking.getTickets().get(0).validateTicket());
            assertEquals("CONFIRMED", booking.getStatus());
            assertTrue(((ListView<?>) customer.lookup("#bookingList")).getItems().get(0).toString().contains(code));
            ((ListView<?>) customer.lookup("#bookingList")).getSelectionModel().selectFirst();
            Parent cancelView = customer;
            dialogs(() -> button(cancelView, "Hủy booking đã chọn").fire(), ButtonType.OK);
            assertEquals("CANCELLED", booking.getStatus());
            assertEquals(80, context.events().getEventById("EV001").findTicketType("T001").getAvailableQuantity());
            assertTrue(((ListView<?>) customer.lookup("#bookingList")).getItems().get(0).toString().contains("CANCELLED"));
            return null;
        });
    }

    @Test void shownShellRoutesRolesAndLogoutWithoutResettingStore() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            javafx.stage.Stage stage = new javafx.stage.Stage();
            try {
                for (String username : new String[]{"admin", "customer", "admin"}) {
                    context.loginAs(context.auth().login(username, "123456"));
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Shell.fxml"));
                    Parent shell = loader.load();
                    ((com.nhom5.eventticket.ui.ShellController) loader.getController()).setContext(context, () -> {});
                    stage.setScene(new javafx.scene.Scene(shell, 1200, 760));
                    stage.getScene().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
                    stage.show(); shell.applyCss(); shell.layout();
                    boolean admin = "admin".equals(username);
                    button(shell, admin ? "▦   Quản lý sự kiện" : "▦   Danh sách sự kiện").fire();
                    shell.applyCss(); shell.layout();
                    if (admin) {
                        Button sales = (Button) shell.lookup("#salesButton");
                        assertNotNull(sales);
                        if (context.events().getEventById("EV001").isTicketSalesOpen()) sales.fire();
                        assertEquals("Mở bán vé", sales.getText());
                    } else {
                        assertNull(shell.lookup("#salesButton"));
                        assertTrue(((Button) shell.lookup("#bookButton")).isDisabled());
                        assertTrue(((Label) shell.lookup("#eventDetail")).getText().contains("Đã đóng bán vé"));
                    }
                    button(shell, "Đăng xuất").fire();
                    assertNull(context.currentUser());
                }
            } finally { stage.close(); }
            return null;
        });
    }

    @Test void eventSwitchAndRoleSwitchShowIndependentAndEffectiveSalesStates() throws Exception {
        onFx(() -> {
            AppContext context = new AppContext(DemoData.create());
            Admin actor = (Admin) context.auth().login("admin", "123456");
            context.loginAs(actor);
            Parent root = load("ManageEventsView.fxml", context);
            @SuppressWarnings("unchecked") TableView<Event> table = (TableView<Event>) root.lookup("#eventTable");
            @SuppressWarnings("unchecked") ListView<TicketType> types = (ListView<TicketType>) root.lookup("#ticketList");
            TicketType vip = types.getItems().get(0);
            TicketType standard = types.getItems().get(1);
            types.getSelectionModel().select(standard);
            dialogs(() -> ((Button) root.lookup("#ticketSalesButton")).fire(), ButtonType.OK);
            ((Button) root.lookup("#salesButton")).fire();
            assertEquals("Đã đóng bán", ((Label) root.lookup("#salesLabel")).getText());
            assertTrue(((Label) root.lookup("#salesHint")).getText().contains("không thể đặt vé"));
            assertTrue(vip.isSalesEnabled()); assertFalse(standard.isSalesEnabled());
            assertTrue(vip.getSalesLabel().contains("sự kiện đã đóng bán"));
            assertTrue(standard.getSalesLabel().contains("Đã ngừng bán riêng"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            try {
                stage.setScene(new javafx.scene.Scene(root, 1200, 760)); stage.show();
                root.applyCss(); root.layout(); types.refresh();
                root.applyCss(); root.layout();
                assertTrue(types.lookupAll(".list-cell").stream().filter(n -> n instanceof ListCell<?>)
                        .map(n -> ((ListCell<?>) n).getText()).filter(java.util.Objects::nonNull)
                        .anyMatch(s -> s.contains("VIP") && s.contains("sự kiện đã đóng bán")));
                table.getSelectionModel().select(1);
                assertEquals("Đang mở bán", ((Label) root.lookup("#salesLabel")).getText());
                assertEquals("Đóng bán vé", ((Button) root.lookup("#salesButton")).getText());
                table.getSelectionModel().select(0);
                assertEquals("Đã đóng bán", ((Label) root.lookup("#salesLabel")).getText());
                assertEquals("Mở bán vé", ((Button) root.lookup("#salesButton")).getText());
                ((Button) root.lookup("#salesButton")).fire();
                assertTrue(vip.isSalesEnabled()); assertFalse(standard.isSalesEnabled());
                assertTrue(vip.getSalesLabel().contains("Có thể đặt vé"));
                assertEquals("Đang mở bán", ((Label) root.lookup("#salesLabel")).getText());
                context.logout(); context.loginAs(context.auth().login("customer", "123456"));
                Parent customer = load("BookingView.fxml", context);
                @SuppressWarnings("unchecked") ComboBox<TicketType> options = (ComboBox<TicketType>) customer.lookup("#typeBox");
                assertTrue(options.getItems().contains(vip)); assertFalse(options.getItems().contains(standard));
                assertFalse(((Button) customer.lookup("#placeBookingButton")).isDisabled());
                context.logout(); context.loginAs(actor);
                Parent reopened = load("ManageEventsView.fxml", context);
                assertEquals("Đang mở bán", ((Label) reopened.lookup("#salesLabel")).getText());
                assertTrue(vip.isSalesEnabled()); assertFalse(standard.isSalesEnabled());
            } finally { stage.close(); }
            return null;
        });
    }
}
