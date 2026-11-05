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
import javafx.scene.control.Button;
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
    @FXML private TextField searchField;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private ListView<TicketType> ticketList;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Label stockLabel;
    @FXML private Label salesLabel;
    @FXML private Label salesHint;
    @FXML private Label priceError;
    @FXML private Label quantityError;
    @FXML private Button salesButton;
    @FXML private Button ticketSalesButton;
    @FXML private Button deleteTicketButton;

    private AppContext context;
    private ObservableList<Event> eventData;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.context = context;
        priceField.textProperty().addListener((o, a, b) -> validatePrice());
        quantityField.textProperty().addListener((o, a, b) -> validateQuantity());
        eventIdCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEventId()));
        eventNameCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));
        locationCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getLocation()));
        timeCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(FMT.format(d.getValue().getStartTime())));
        eventData = FXCollections.observableArrayList(context.events().listEvents());
        eventTable.setItems(eventData);
        searchField.textProperty().addListener((obs, old, value) -> {
            eventData.setAll(context.events().searchEvents(value));
            eventTable.getSelectionModel().clearSelection();
            showEvent(null);
        });
        eventTable.setPlaceholder(new Label("Không có sự kiện phù hợp."));
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
                        + " VND  •  Còn " + type.getAvailableQuantity() + "/" + type.getQuantity()
                        + " • " + type.getSalesLabel());
            }
        });
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, value) -> showEvent(value));
        ticketList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, value) -> showTicketType(value));
        if (!eventData.isEmpty()) eventTable.getSelectionModel().selectFirst();
        else showEvent(null);
    }

    @FXML
    private void addEvent() {
        Admin admin = requireAdmin();
        if (admin == null) return;
        try {
            Event created = context.events().createEventWithTicket(admin, nameField.getText(), descriptionField.getText(),
                    locationField.getText(), schedule(datePicker, startTimeField), schedule(endDatePicker, endTimeField),
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
        if (admin == null || selected == null) return;
        try {
            context.events().updateEvent(admin, selected.getEventId(), nameField.getText(), descriptionField.getText(),
                    locationField.getText(), schedule(datePicker, startTimeField), schedule(endDatePicker, endTimeField));
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
            if (!type.getName().equals(typeBox.getValue()))
                throw new IllegalArgumentException("Hạng vé phải khớp với loại vé đang chọn. Dùng Thêm loại vé để thêm hạng khác.");
            context.events().updateTicketType(admin, selected.getEventId(), type.getTicketTypeId(),
                    money(priceField.getText()), count(quantityField.getText()));
            reload(selected.getEventId());
            UiAlerts.success("Đã cập nhật loại vé.");
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể cập nhật loại vé", ex);
        }
    }

    private void showEvent(Event event) {
        salesLabel.setText(event == null ? "Chưa chọn sự kiện"
                : event.isTicketSalesOpen() ? "Đang mở bán" : "Đã đóng bán");
        salesHint.setText(event == null ? ""
                : !event.isTicketSalesOpen() ? "Khách hàng không thể đặt vé. Trạng thái bật bán riêng của từng loại vé được giữ nguyên."
                : !event.canBook() ? "Khách hàng không thể đặt vé: sự kiện đã bắt đầu hoặc không còn hợp lệ."
                : "Khách hàng chỉ đặt được loại vé đang bật bán riêng và còn đủ tồn kho.");
        salesButton.setText(event != null && event.isTicketSalesOpen() ? "Đóng bán vé" : "Mở bán vé");
        salesButton.setDisable(event == null || (!event.isTicketSalesOpen()
                && (!"OPEN".equals(event.getStatus()) || !event.getStartTime().isAfter(java.time.LocalDateTime.now()))));
        ticketList.getItems().clear();
        if (event == null) {
            nameField.clear(); descriptionField.clear(); locationField.clear();
            datePicker.setValue(null); endDatePicker.setValue(null);
            startTimeField.setText("19:00"); endTimeField.setText("22:00");
            priceField.clear(); quantityField.clear(); typeBox.setValue("STANDARD");
            stockLabel.setText("");
            return;
        }
        nameField.setText(event.getName());
        descriptionField.setText(event.getDescription());
        locationField.setText(event.getLocation());
        datePicker.setValue(event.getStartTime().toLocalDate());
        startTimeField.setText(event.getStartTime().toLocalTime().toString());
        endDatePicker.setValue(event.getEndTime().toLocalDate());
        endTimeField.setText(event.getEndTime().toLocalTime().toString());
        ticketList.getItems().setAll(event.getTicketTypes());
        if (!ticketList.getItems().isEmpty()) ticketList.getSelectionModel().selectFirst();
        else showTicketType(null);
    }

    private void showTicketType(TicketType type) {
        ticketSalesButton.setText(type != null && type.isSalesEnabled() ? "Ngừng bán loại vé" : "Mở bán loại vé");
        ticketSalesButton.setDisable(type == null);
        deleteTicketButton.setDisable(type == null);
        if (type == null) {
            priceField.clear(); quantityField.clear(); typeBox.setValue("STANDARD");
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
        TicketType selectedType = ticketList.getSelectionModel().getSelectedItem();
        eventData.setAll(context.events().searchEvents(searchField.getText()));
        if (eventId == null) {
            if (!eventData.isEmpty()) eventTable.getSelectionModel().selectFirst();
            else showEvent(null);
            return;
        }
        eventData.stream().filter(event -> event.getEventId().equals(eventId)).findFirst()
                .ifPresent(event -> eventTable.getSelectionModel().select(event));
        eventTable.refresh();
        showEvent(eventTable.getSelectionModel().getSelectedItem());
        if (selectedType != null) ticketList.getItems().stream()
                .filter(t -> t.getTicketTypeId().equals(selectedType.getTicketTypeId())).findFirst()
                .ifPresent(t -> ticketList.getSelectionModel().select(t));
    }

    @FXML private void newEvent() {
        eventTable.getSelectionModel().clearSelection();
        showEvent(null);
    }

    private static java.time.LocalDateTime schedule(DatePicker date, TextField time) {
        try {
            date.commitValue();
            if (date.getValue() == null) throw new IllegalArgumentException();
            return date.getValue().atTime(java.time.LocalTime.parse(time.getText().trim()));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Hãy nhập ngày hợp lệ và giờ theo HH:mm (ví dụ 19:00).");
        }
    }

    private Event selectedEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) UiAlerts.error("Chưa chọn sự kiện", new IllegalArgumentException("Hãy chọn một sự kiện trong bảng."));
        return selected;
    }

    private Admin requireAdmin() {
        if (context.currentUser() instanceof Admin admin) return admin;
        UiAlerts.error("Yêu cầu đăng nhập", new IllegalArgumentException("Chỉ quản trị viên được quản lý sự kiện."));
        return null;
    }

    private static BigDecimal money(String text) {
        try {
            BigDecimal value = new BigDecimal(text.trim());
            if (value.signum() <= 0) throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Giá vé phải lớn hơn 0.");
        }
    }

    private static int count(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            if (value <= 0) throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Số lượng vé phải là số nguyên lớn hơn 0.");
        }
    }

    private void validatePrice() {
        try { money(priceField.getText()); priceError.setText(""); }
        catch (IllegalArgumentException ex) { priceError.setText(ex.getMessage()); }
    }
    private void validateQuantity() {
        try { count(quantityField.getText()); quantityError.setText(""); }
        catch (IllegalArgumentException ex) { quantityError.setText(ex.getMessage()); }
    }
    @FXML private void toggleSales() {
        Admin admin = requireAdmin();
        Event event = selectedEvent();
        if (admin == null || event == null) return;
        try {
            context.events().setTicketSalesOpen(admin, event.getEventId(), !event.isTicketSalesOpen());
            reload(event.getEventId());
        } catch (RuntimeException ex) { UiAlerts.error("Không thể đổi trạng thái bán vé", ex); }
    }
    @FXML private void deleteTicketType() { changeTicketSales(true); }
    @FXML private void toggleTicketSales() { changeTicketSales(false); }
    private void changeTicketSales(boolean delete) {
        Admin admin = requireAdmin();
        Event event = selectedEvent();
        TicketType type = ticketList.getSelectionModel().getSelectedItem();
        if (admin == null || event == null) return;
        if (type == null) {
            UiAlerts.error("Chưa chọn loại vé", new IllegalArgumentException("Hãy chọn loại vé."));
            return;
        }
        String action = delete ? "Xóa loại vé" : type.isSalesEnabled() ? "Ngừng bán loại vé" : "Mở bán loại vé";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, action + " " + type.getName() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(action);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            if (delete) context.events().deleteTicketType(admin, event.getEventId(), type.getTicketTypeId());
            else context.events().setTicketTypeSalesEnabled(admin, event.getEventId(), type.getTicketTypeId(), !type.isSalesEnabled());
            reload(event.getEventId());
        } catch (RuntimeException ex) { UiAlerts.error("Không thể " + action.toLowerCase(), ex); }
    }
}
