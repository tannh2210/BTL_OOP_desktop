package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Người 4 – danh sách thông báo của user đang đăng nhập. */
public class NotificationController implements ScreenController {
    @FXML private ListView<String> notificationsList;

    private AppContext context;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void init(AppContext context, Navigator navigator) {
        this.context = context;
        refresh();
    }

    @FXML
    private void refresh() {
        if (context.currentUser() == null) {
            notificationsList.getItems().setAll("Hãy đăng nhập để xem thông báo.");
            return;
        }
        List<Notification> items = context.notifications().forUser(context.currentUser().getUserId());
        if (items.isEmpty()) {
            notificationsList.getItems().setAll("Bạn chưa có thông báo nào.");
            return;
        }
        notificationsList.getItems().setAll(items.stream()
                .map(n -> n.getCreatedAt().format(FMT) + "  ·  " + n.getMessage())
                .toList());
    }
}
