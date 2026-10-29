package com.nhom5.eventticket.ui;
import com.nhom5.eventticket.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.time.format.DateTimeFormatter;
public class NotificationController implements ScreenController {
    @FXML private ListView<String> notificationsList;
    private AppContext context;
    @Override public void init(AppContext context,Navigator navigator){this.context=context;refresh();}
    @FXML private void refresh(){
        if(context.currentUser()==null){notificationsList.getItems().setAll("Hãy đăng nhập để xem thông báo.");return;}
        var fmt=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        notificationsList.getItems().setAll(context.notifications().forUser(context.currentUser().getUserId()).stream()
             .map(n->n.getCreatedAt().format(fmt)+"  •  "+n.getMessage()).toList());
        if(notificationsList.getItems().isEmpty())notificationsList.getItems().add("Bạn chưa có thông báo nào.");
    }
}
