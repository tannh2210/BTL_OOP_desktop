package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Event;
import com.nhom5.eventticket.model.TicketType;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/** Admin form for events and ticket types. */
public class ManageEventController implements ScreenController {
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> eventIdCol;
    @FXML private TableColumn<Event, String> eventNameCol;
    @FXML private TableColumn<Event, String> locationCol;
    @FXML private TableColumn<Event, String> timeCol;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private ListView<TicketType> ticketList;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Label stockLabel;

    private AppContext context;
    private ObservableList<Event> eventData;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.context = context;
        eventIdCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEventId()));
        eventNameCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));
        locationCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getLocation()));
        timeCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(FMT.format(d.getValue().getStartTime())));
        eventData = FXCollections.observableArrayList(context.events().listEvents());
        eventTable.setItems(eventData);
        typeBox.getItems().setAll("VIP", "STANDARD", "ECONOMY");
        typeBox.getSelectionModel().select("STANDARD");
        ticketList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TicketType type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    return;
                }
                setText(type.getName() + "  •  " + type.getPrice().toPlainString()
                        + " VND  •  Còn " + type.getAvailableQuantity() + "/" + type.getQuantity());
            }
        });
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, value) -> showEvent(value));
        ticketList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, value) -> showTicketType(value));
        if (!eventData.isEmpty()) eventTable.getSelectionModel().selectFirst();
    }

    @FXML
    private void addEvent() {
        Admin admin = requireAdmin();
        if (admin == null || !hasDate()) return;
        try {
            Event created = context.events().createEventWithTicket(admin, nameField.getText(), descriptionField.getText(),
                    locationField.getText(), datePicker.getValue().atTime(19, 0),
                    typeBox.getValue(), money(priceField.getText()), count(quantityField.getText()));
            reload(created.getEventId());
            UiAlerts.success("Đã thêm sự kiện.");
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể thêm sự kiện", ex);
        }
    }

    @FXML
    private void saveEvent() {
        Admin admin = requireAdmin();
        Event selected = selectedEvent();
        if (admin == null || selected == null || !hasDate()) return;
        try {
            context.events().updateEvent(admin, selected.getEventId(), nameField.getText(), descriptionField.getText(),
                    locationField.getText(), datePicker.getValue().atTime(selected.getStartTime().toLocalTime()));
            reload(selected.getEventId());
            UiAlerts.success("Đã lưu sự kiện.");
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể sửa sự kiện", ex);
        }
    }

    @FXML
    private void deleteEvent() {
        Admin admin = requireAdmin();
        Event selected = selectedEvent();
        if (admin == null || selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa sự kiện \"" + selected.getName() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Xóa sự kiện");
        confirm.setHeaderText("Xác nhận xóa");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            context.events().deleteEvent(admin, selected.getEventId());
            reload(null);
            UiAlerts.success("Đã xóa sự kiện.");
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể xóa sự kiện", ex);
        }
    }

    @FXML
    private void addTicketType() {
        Admin admin = requireAdmin();
        Event selected = selectedEvent();
        if (admin == null || selected == null) return;
        try {
            context.events().addTicketType(admin, selected.getEventId(), typeBox.getValue(),
                    money(priceField.getText()), count(quantityField.getText()));
            reload(selected.getEventId());
            UiAlerts.success("Đã thêm loại vé.");
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể thêm loại vé", ex);
        }
    }

    @FXML
    private void updateTicketType() {
        Admin admin = requireAdmin();
        Event selected = selectedEvent();
        TicketType type = ticketList.getSelectionModel().getSelectedItem();
        if (admin == null || selected == null) return;
        if (type == null) {
            UiAlerts.error("Chưa chọn loại vé", new IllegalArgumentException("Hãy chọn một loại vé trong danh sách."));
            return;
        }
        try {
            context.events().updateTicketType(admin, selected.getEventId(), type.getTicketTypeId(),
                    money(priceField.getText()), count(quantityField.getText()));
            reload(selected.getEventId());
            UiAlerts.success("Đã cập nhật loại vé.");
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể cập nhật loại vé", ex);
        }
    }

    private void showEvent(Event event) {
        ticketList.getItems().clear();
        if (event == null) {
            stockLabel.setText("");
            return;
        }
        nameField.setText(event.getName());
        descriptionField.setText(event.getDescription());
        locationField.setText(event.getLocation());
        datePicker.setValue(event.getStartTime().toLocalDate());
        ticketList.getItems().setAll(event.getTicketTypes());
        if (!ticketList.getItems().isEmpty()) ticketList.getSelectionModel().selectFirst();
    }

    private void showTicketType(TicketType type) {
        if (type == null) {
            stockLabel.setText("");
            return;
        }
        typeBox.setValue(type.getName());
        priceField.setText(type.getPrice().toPlainString());
        quantityField.setText(String.valueOf(type.getQuantity()));
        int sold = type.getQuantity() - type.getAvailableQuantity();
        stockLabel.setText("Đã đặt " + sold + " vé. Còn " + type.getAvailableQuantity() + " vé.");
    }

    private void reload(String eventId) {
        eventData.setAll(context.events().listEvents());
        if (eventId == null) {
            if (!eventData.isEmpty()) eventTable.getSelectionModel().selectFirst();
            else showEvent(null);
            return;
        }
        eventData.stream().filter(event -> event.getEventId().equals(eventId)).findFirst()
                .ifPresent(event -> eventTable.getSelectionModel().select(event));
    }

    private Event selectedEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) UiAlerts.error("Chưa chọn sự kiện", new IllegalArgumentException("Hãy chọn một sự kiện trong bảng."));
        return selected;
    }

    private boolean hasDate() {
        if (datePicker.getValue() != null) return true;
        UiAlerts.error("Thiếu ngày", new IllegalArgumentException("Hãy chọn ngày tổ chức."));
        return false;
    }

    private Admin requireAdmin() {
        if (context.currentUser() instanceof Admin admin) return admin;
        UiAlerts.error("Yêu cầu đăng nhập", new IllegalArgumentException("Chỉ quản trị viên được quản lý sự kiện."));
        return null;
    }

    private static BigDecimal money(String text) {
        try {
            return new BigDecimal(text.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Giá vé không hợp lệ.");
        }
    }

    private static int count(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Số lượng vé không hợp lệ.");
        }
    }
}
