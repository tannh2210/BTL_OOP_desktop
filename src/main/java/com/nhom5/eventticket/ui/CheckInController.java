package com.nhom5.eventticket.ui;
import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class CheckInController implements ScreenController {
    @FXML private TextField ticketCodeField;
    @FXML private Label resultLabel;
    private AppContext context;
    @Override public void init(AppContext context,Navigator navigator){this.context=context;}
    @FXML private void checkIn(){
        if (!(context.currentUser() instanceof Admin admin)){
            resultLabel.setText("Vui lòng đăng nhập tài khoản quản trị viên.");return;
        }
        try {
            var r=context.checkIns().checkIn(ticketCodeField.getText().trim(),admin.getUserId());
            resultLabel.setText("Vé hợp lệ. Check-in thành công. Mã: " + r.getTicket().getTicketCode());
        }catch(RuntimeException ex){resultLabel.setText(ex.getMessage());}
    }
}
