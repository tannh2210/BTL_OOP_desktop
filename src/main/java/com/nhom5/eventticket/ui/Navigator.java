package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;
import com.nhom5.eventticket.model.Admin;
import com.nhom5.eventticket.model.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public final class Navigator {
    private final AppContext context;
    private final StackPane outlet;
    private final ShellController shell;
    private final Runnable onLogout;

    Navigator(AppContext context, StackPane outlet, ShellController shell, Runnable onLogout) {
        this.context = context;
        this.outlet = outlet;
        this.shell = shell;
        this.onLogout = onLogout;
    }

    public void show(String filename, String title, String subtitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + filename));
            Parent view = loader.load();
            Object controller = loader.getController();
            if (!(controller instanceof ScreenController screen)) {
                throw new IllegalStateException("Invalid screen controller: " + filename);
            }
            screen.init(context, this);
            outlet.getChildren().setAll(view);
            shell.setPageTitle(title, subtitle);
        } catch (Exception ex) {
            throw new IllegalStateException("Không mở được màn hình: " + filename, ex);
        }
    }

    public void requestLogout() {
        context.logout();
        onLogout.run();
    }

    public void refreshSessionInfo() {
        shell.refreshSessionInfo();
    }

    public void dashboard() { show("DashboardView.fxml", "Trang chủ", "Khám phá những sự kiện nổi bật"); }
    public void accounts() { show("AccountView.fxml", "Tài khoản", "Thông tin tài khoản của bạn"); }
    public void events() {
        if (context.currentUser() instanceof Admin) {
            show("ManageEventsView.fxml", "Quản lý sự kiện", "Thêm, sửa, xóa sự kiện và loại vé");
            return;
        }
        show("EventsView.fxml", "Sự kiện", "Danh sách sự kiện và loại vé");
    }
    private Event pendingEvent;
    public void bookings() {
        boolean admin = context.currentUser() instanceof Admin;
        show("BookingView.fxml", admin ? "Quản lý booking" : "Đặt vé",
                admin ? "Xem, lọc và hủy booking hệ thống" : "Chọn vé và xem lịch sử đặt chỗ");
    }
    public void bookings(Event event) { pendingEvent = event; bookings(); }
    Event consumePendingEvent() { Event event = pendingEvent; pendingEvent = null; return event; }
    public void checkIns() { show("CheckInView.fxml", "Check-in", "Xác nhận vé tham dự"); }
    public void statistics() { show("StatisticsView.fxml", "Thống kê", "Tổng quan hoạt động"); }
    public void notifications() { show("NotificationsView.fxml", "Thông báo", "Cập nhật từ các booking của bạn"); }
}
