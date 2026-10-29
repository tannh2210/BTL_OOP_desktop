package com.nhom5.eventticket.ui;
import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
public class BookingController implements ScreenController {
    @FXML private ComboBox<Event> eventBox;
    @FXML private ComboBox<TicketType> typeBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label availabilityLabel;
    @FXML private Label totalLabel;
    @FXML private ListView<String> bookingList;
    private AppContext context;
    private final List<Booking> rows=new ArrayList<>();
    @Override public void init(AppContext context,Navigator navigator) {
        this.context=context;
        eventBox.setConverter(new StringConverter<>() {
            public String toString(Event e) {return e==null?"":e.getName()+" ("+e.getEventId()+")";}
            public Event fromString(String s) {return null;}
        });
        typeBox.setConverter(new StringConverter<>() {
            public String toString(TicketType t) {return t==null?"":t.getName()+" • "+t.getPrice().toPlainString()+" VND";}
            public TicketType fromString(String s) {return null;}
        });
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,1));
        eventBox.getItems().setAll(context.events().listEvents());
        eventBox.valueProperty().addListener((obs,oldV,v)->{
            typeBox.getItems().clear();if(v!=null)typeBox.getItems().setAll(v.getTicketTypes());
            typeBox.getSelectionModel().selectFirst();updateQuote();
        });
        typeBox.valueProperty().addListener((obs,oldV,v)->updateQuote());
        quantitySpinner.valueProperty().addListener((obs,oldV,v)->updateQuote());
        eventBox.getSelectionModel().selectFirst();refreshHistory();
    }
    private void updateQuote() {
        TicketType type=typeBox.getValue();
        if(type==null){availabilityLabel.setText("Hãy chọn hạng vé.");totalLabel.setText("0 VND");return;}
        availabilityLabel.setText("Còn " + type.getAvailableQuantity() + " vé");
        totalLabel.setText(type.getPrice().multiply(BigDecimal.valueOf(quantitySpinner.getValue())).toPlainString()+" VND");
    }
    @FXML private void placeBooking() {
        if (!(context.currentUser() instanceof Customer customer)) {
            UiAlerts.error("Chưa đăng nhập",new IllegalArgumentException("Hãy đăng nhập tài khoản khách hàng trước khi đặt vé."));return;
        }
        if(eventBox.getValue()==null ||typeBox.getValue()==null){UiAlerts.error("Thiếu lựa chọn",new IllegalArgumentException("Chọn sự kiện và loại vé."));return;}
        try {
            Booking result=context.bookings().bookTicket(customer.getUserId(),eventBox.getValue().getEventId(),
                      typeBox.getValue().getTicketTypeId(),quantitySpinner.getValue());
            String codes=String.join(", ",result.getTickets().stream().map(Ticket::getTicketCode).toList());
            UiAlerts.success("Đặt vé thành công!\nMã booking: " + result.getBookingId() + "\nMã vé: " + codes);
            updateQuote();refreshHistory();
        } catch(RuntimeException ex){UiAlerts.error("Đặt vé thất bại",ex);}
    }
    @FXML private void viewHistory() {refreshHistory();}
    private void refreshHistory() {
        rows.clear();bookingList.getItems().clear();
        if (!(context.currentUser() instanceof Customer user)) {
            bookingList.getItems().add("Đăng nhập tài khoản khách hàng để xem vé của bạn.");return;
        }
        rows.addAll(context.bookings().getBookingHistory(user.getUserId()));
        for(Booking b:rows){
            String codes=String.join(", ",b.getTickets().stream().map(Ticket::getTicketCode).toList());
            bookingList.getItems().add(b.getBookingId()+"  •  "+b.getEvent().getName()+"  •  "+b.getStatus()+"\nMã vé: "+codes);
        }
        if(rows.isEmpty())bookingList.getItems().add("Bạn chưa đặt vé nào.");
    }
    @FXML private void cancelSelected() {
        if (!(context.currentUser() instanceof Customer user)) {UiAlerts.error("Chưa đăng nhập",new IllegalArgumentException("Hãy đăng nhập trước."));return;}
        int idx=bookingList.getSelectionModel().getSelectedIndex();
        if(idx<0||idx>=rows.size()){UiAlerts.error("Chưa chọn booking",new IllegalArgumentException("Chọn một booking trong lịch sử."));return;}
        Booking b=rows.get(idx);
        try {context.bookings().cancelBooking(user.getUserId(),b.getBookingId());
            UiAlerts.success("Đã hủy booking " + b.getBookingId());refreshHistory();updateQuote();
        } catch(RuntimeException ex){UiAlerts.error("Không thể hủy booking",ex);}
    }
}
