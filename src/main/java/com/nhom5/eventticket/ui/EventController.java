package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Customer;
import com.nhom5.eventticket.model.Event;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Customer event list: search, details, and ticket types. */
public class EventController implements ScreenController {
    @FXML private TextField searchField;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> eventIdCol;
    @FXML private TableColumn<Event, String> eventNameCol;
    @FXML private TableColumn<Event, String> locationCol;
    @FXML private TableColumn<Event, String> timeCol;
    @FXML private TableColumn<Event, String> statusCol;
    @FXML private Label eventDetail;
    @FXML private ListView<String> ticketTypesList;
    @FXML private Button bookButton;

    private Navigator navigator;
    private FilteredList<Event> list;
    private ObservableList<Event> eventData;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.navigator = navigator;
        boolean customer = context.currentUser() instanceof Customer;
        bookButton.setVisible(customer);
        bookButton.setManaged(customer);
        eventIdCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEventId()));
        eventNameCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));
        locationCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getLocation()));
        timeCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(FMT.format(d.getValue().getStartTime())));
        statusCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getStatus()));
        eventData = FXCollections.observableArrayList(context.events().listEvents());
        list = new FilteredList<>(eventData);
        eventTable.setItems(list);
        searchField.textProperty().addListener((obs, oldV, value) -> {
            String key = value.trim().toLowerCase(Locale.ROOT);
            list.setPredicate(e -> e.getName().toLowerCase(Locale.ROOT).contains(key)
                    || e.getLocation().toLowerCase(Locale.ROOT).contains(key));
        });
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, value) -> showDetails(value));
        if (!list.isEmpty()) eventTable.getSelectionModel().selectFirst();
    }

    private void showDetails(Event event) {
        if (event == null) {
            eventDetail.setText("Chọn một sự kiện.");
            ticketTypesList.getItems().clear();
            return;
        }
        eventDetail.setText(event.getName() + "\n" + event.getDescription() + "\n"
                + event.getLocation() + " • " + event.getStartTime().format(FMT));
        ticketTypesList.getItems().setAll(event.getTicketTypes().stream()
                .map(t -> t.getName() + "  •  " + t.getPrice().toPlainString() + " VND  •  Còn " + t.getAvailableQuantity())
                .toList());
    }

    @FXML
    private void goBooking() { navigator.bookings(); }
}
