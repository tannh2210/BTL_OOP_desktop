package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingController implements ScreenController {
    @FXML private ComboBox<Event> eventBox;
    @FXML private ComboBox<Event> filterEventBox;
    @FXML private ComboBox<TicketType> typeBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label availabilityLabel;
    @FXML private Label totalLabel;
    @FXML private Label formHeading;
    @FXML private Label historyHeading;
    @FXML private Label policyLabel;
    @FXML private Button cancelButton;
    @FXML private HBox bookingActions;
    @FXML private ListView<String> bookingList;
    @FXML private Button placeBookingButton;

    private AppContext context;
    private final List<Booking> rows = new ArrayList<>();
    private boolean admin;
    private int quantityMaximum = 10;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.context = context;
        admin = context.currentUser() instanceof Admin;
        eventBox.setConverter(eventConverter());
        filterEventBox.setConverter(eventConverter());
        typeBox.setConverter(new StringConverter<>() {
            @Override public String toString(TicketType type) {
                return type == null ? "" : type.getName() + " • " + type.getPrice().toPlainString() + " VND";
            }
            @Override public TicketType fromString(String value) { return null; }
        });
        eventBox.getItems().setAll(context.events().listEvents().stream().filter(BookingController::isBookable).toList());
        filterEventBox.getItems().setAll(context.events().listEvents());
        filterEventBox.setPromptText("Tất cả sự kiện");
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        bookingList.setCellFactory(view -> new ListCell<>() {
            private final Label text = new Label();
            {
                text.setWrapText(true);
                text.setMaxWidth(Double.MAX_VALUE);
                text.prefWidthProperty().bind(widthProperty().subtract(28));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        eventBox.valueProperty().addListener((obs, oldValue, value) -> loadTicketTypes(value));
        typeBox.valueProperty().addListener((obs, oldValue, value) -> updateQuote());
        quantitySpinner.valueProperty().addListener((obs, oldValue, value) -> updateQuote());
        filterEventBox.valueProperty().addListener((obs, oldValue, value) -> refreshHistory());
        bookingList.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, value) -> updateCancelButton());

        Event passedEvent = navigator.consumePendingEvent();
        if (passedEvent != null && eventBox.getItems().stream().anyMatch(e -> e.getEventId().equals(passedEvent.getEventId()))) {
            eventBox.getSelectionModel().select(eventBox.getItems().stream()
                    .filter(e -> e.getEventId().equals(passedEvent.getEventId())).findFirst().orElse(null));
        } else if (!eventBox.getItems().isEmpty()) {
            eventBox.getSelectionModel().selectFirst();
        }
        if (admin) {
            formHeading.setText("Quản lý booking toàn hệ thống");
            historyHeading.setText("Danh sách booking");
            policyLabel.setText("Lọc theo sự kiện. Chỉ booking CONFIRMED có thể hủy trước giờ bắt đầu; booking có vé đã check-in không thể hủy.");
            bookingActions.setVisible(false);
            bookingActions.setManaged(false);
            eventBox.setVisible(false);
            eventBox.setManaged(false);
            typeBox.setVisible(false);
            typeBox.setManaged(false);
            quantitySpinner.setVisible(false);
            quantitySpinner.setManaged(false);
            availabilityLabel.setVisible(false);
            availabilityLabel.setManaged(false);
            totalLabel.setVisible(false);
            totalLabel.setManaged(false);
            cancelButton.setText("Hủy booking đã chọn");
        } else {
            formHeading.setText("Thông tin đặt vé");
            historyHeading.setText("Lịch sử đặt vé");
            policyLabel.setText("Có thể hủy trước giờ bắt đầu sự kiện. Nếu một vé đã check-in, toàn bộ booking không thể hủy.");
            filterEventBox.setVisible(false);
            filterEventBox.setManaged(false);
        }
        refreshHistory();
        updateCancelButton();
    }

    private StringConverter<Event> eventConverter() {
        return new StringConverter<>() {
            @Override public String toString(Event event) {
                return event == null ? "" : event.getName() + " (" + event.getEventId() + ")";
            }
            @Override public Event fromString(String value) { return null; }
        };
    }

    private static boolean isBookable(Event event) {
        return event.canBook()
                && event.getTicketTypes().stream().anyMatch(t -> t.isAvailable() && t.isSalesEnabled());
    }

    private void loadTicketTypes(Event event) {
        typeBox.getItems().clear();
        if (event != null) {
            typeBox.getItems().setAll(event.getTicketTypes().stream()
                    .filter(t -> t.isAvailable() && t.isSalesEnabled()).toList());
        }
        if (!typeBox.getItems().isEmpty()) typeBox.getSelectionModel().selectFirst();
        else setQuantityMaximum(1);
        updateQuote();
    }

    private void setQuantityMaximum(int available) {
        int max = Math.max(1, Math.min(10, available));
        int value = quantitySpinner.getValue() == null ? 1 : Math.min(quantitySpinner.getValue(), max);
        if (max != quantityMaximum) {
            quantityMaximum = max;
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, value));
        } else if (quantitySpinner.getValue() > max) {
            quantitySpinner.getValueFactory().setValue(max);
        }
    }

    private void updateQuote() {
        TicketType type = typeBox.getValue();
        boolean closed = eventBox.getValue() != null && !eventBox.getValue().canBook();
        if (placeBookingButton != null) {
            placeBookingButton.setDisable(!(context.currentUser() instanceof Customer)
                    || eventBox.getValue() == null || closed
                    || type == null || !type.isSalesEnabled()
                    || type.getAvailableQuantity() < quantitySpinner.getValue());
        }
        if (closed) {
            availabilityLabel.setText("Đã đóng bán vé");
            totalLabel.setText("0 VND");
            return;
        }
        if (type == null) {
            availabilityLabel.setText("Không còn vé phù hợp.");
            totalLabel.setText("0 VND");
            setQuantityMaximum(1);
            return;
        }
        setQuantityMaximum(type.getAvailableQuantity());
        availabilityLabel.setText("Còn " + type.getAvailableQuantity() + " vé");
        totalLabel.setText(type.getPrice().multiply(BigDecimal.valueOf(quantitySpinner.getValue())).toPlainString() + " VND");
    }

    @FXML
    private void placeBooking() {
        if (!(context.currentUser() instanceof Customer customer)) return;
        if (eventBox.getValue() == null || typeBox.getValue() == null) {
            UiAlerts.error("Thiếu lựa chọn", new IllegalArgumentException("Chọn sự kiện và loại vé."));
            return;
        }
        try {
            Booking result = context.bookings().bookTicket(customer.getUserId(), eventBox.getValue().getEventId(),
                    typeBox.getValue().getTicketTypeId(), quantitySpinner.getValue());
            UiAlerts.success("Đặt vé thành công!\nMã booking: " + result.getBookingId() + "\nMã vé: "
                    + String.join(", ", result.getTickets().stream().map(Ticket::getTicketCode).toList()));
            loadTicketTypes(eventBox.getValue());
            refreshHistory();
            updateQuote();
        } catch (RuntimeException ex) {
            updateQuote();
            UiAlerts.error("Đặt vé thất bại", ex);
        }
    }

    @FXML
    private void viewHistory() { refreshHistory(); }

    private void refreshHistory() {
        rows.clear();
        bookingList.getItems().clear();
        User current = context.currentUser();
        if (admin) rows.addAll(context.bookings().getAllBookings(current.getUserId()));
        else if (current instanceof Customer customer) rows.addAll(context.bookings().getBookingHistory(customer.getUserId()));
        Event filter = filterEventBox.getValue();
        for (Booking booking : rows) {
            if (filter != null && !filter.getEventId().equals(booking.getEvent().getEventId())) continue;
            String codes = String.join(", ", booking.getTickets().stream().map(Ticket::getTicketCode).toList());
            bookingList.getItems().add(booking.getBookingId() + "  •  " + booking.getCustomer().getFullName()
                    + "  •  " + booking.getEvent().getName() + "  •  " + booking.getStatus() + "\n"
                    + "Thời gian: " + booking.getBookingTime().format(TIME) + "  •  Vé: " + codes);
        }
        if (bookingList.getItems().isEmpty()) {
            bookingList.getItems().add(admin ? "Không có booking phù hợp." : "Bạn chưa đặt vé nào.");
        }
        updateCancelButton();
    }

    private void updateCancelButton() {
        int index = bookingList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= bookingList.getItems().size()) {
            cancelButton.setDisable(true);
            return;
        }
        Booking booking = displayedRows().get(index);
        cancelButton.setDisable(!"CONFIRMED".equals(booking.getStatus())
                || !booking.getEvent().getStartTime().isAfter(LocalDateTime.now())
                || booking.getTickets().stream().anyMatch(Ticket::isUsed));
    }

    private List<Booking> displayedRows() {
        Event filter = admin ? filterEventBox.getValue() : null;
        return rows.stream().filter(b -> filter == null || filter.getEventId().equals(b.getEvent().getEventId())).toList();
    }

    @FXML
    private void cancelSelected() {
        int index = bookingList.getSelectionModel().getSelectedIndex();
        List<Booking> visibleRows = displayedRows();
        if (index < 0 || index >= visibleRows.size()) return;
        Booking booking = visibleRows.get(index);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn hủy booking " + booking.getBookingId() + "? Tất cả vé trong booking sẽ bị hủy.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setTitle("Xác nhận hủy booking");
        confirm.setHeaderText("Hủy booking");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            User current = context.currentUser();
            if (admin) context.bookings().cancelBookingAsAdmin(current.getUserId(), booking.getBookingId());
            else context.bookings().cancelBooking(current.getUserId(), booking.getBookingId());
            UiAlerts.success("Đã hủy booking " + booking.getBookingId());
            refreshHistory();
            updateQuote();
        } catch (RuntimeException ex) {
            UiAlerts.error("Không thể hủy booking", ex);
        }
    }
}