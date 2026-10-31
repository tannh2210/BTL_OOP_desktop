# Booking Management Module

Phạm vi: đặt vé, tạo Ticket, cập nhật tồn kho, xem lịch sử, hủy Booking,
Admin xem/quản lý Booking. Không quản lý Event, không thay đổi giá vé,
không xử lý check-in.

## Tích hợp với code nhóm
Module dùng hai interface chỉ đọc:
- EventInfo: getEventId(), getEventName()
- TicketTypeInfo: getTicketTypeId(), getName(), getPrice(), getAvailableQuantity()

Thành viên quản lý Event/TicketType viết adapter từ class của họ sang hai interface
này. Không tạo kho Event/TicketType mới. Nếu nhóm đã có bảng ticket_types,
đối chiếu và chỉnh TicketTypeDAO/schema.sql, không tạo bảng trùng.

## Chạy database
1. Mở MySQL Workbench và chạy database/schema.sql.
2. Sửa USER/PASSWORD trong dao/DatabaseConfig.java.
3. Thêm MySQL Connector/J vào dự án nhóm.

## Dùng nghiệp vụ
BookingService service = new BookingService();
Booking booking = service.bookTicket(customerId, eventInfo, ticketTypeInfo, quantity);
List<Booking> history = service.getCustomerBookingHistory(customerId);
service.cancelBooking(customerId, bookingId);
List<Booking> all = service.getAllBookings(); // Admin
Booking detail = service.getBookingById(bookingId); // Admin
service.adminCancelBooking(bookingId); // Admin

JavaFX Controller chỉ gọi BookingService; không viết SQL/nghiệp vụ trong onAction.

## Quy tắc
- Đặt vé, tạo Booking, tạo Ticket và trừ tồn kho trong cùng transaction.
- Nếu lỗi, rollback.
- Hủy Booking CONFIRMED một lần; cập nhật Ticket CANCELLED và hoàn tồn kho
  trong cùng transaction.
- Không có chức năng check-in.
- Lưu ý: bảng bookings lưu snapshot tên Event/loại vé/đơn giá tại thời điểm đặt
  để lịch sử không bị đổi khi thông tin bên ngoài thay đổi.
