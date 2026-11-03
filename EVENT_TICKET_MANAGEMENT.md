# Module Event và TicketType

Giữ kiến trúc JavaFX → controller → service/model → `InMemoryStore` hiện có.
Dữ liệu vẫn chỉ tồn tại trong phiên chạy ứng dụng.

## Sử dụng giao diện

- Admin → Quản lý sự kiện: tìm theo tên/địa điểm, chọn dòng để xem và sửa.
- Nhấn **Nhập sự kiện mới** để xóa lựa chọn và làm trống form.
- Nhập tên, địa điểm, mô tả, ngày/giờ bắt đầu và kết thúc, cùng loại vé đầu tiên.
- Giờ nhập theo `HH:mm`. Kết thúc phải sau bắt đầu; lịch mới phải ở tương lai.
- Chọn sự kiện để thêm VIP, STANDARD, ECONOMY; mỗi hạng xuất hiện một lần.
- Chọn loại vé để cập nhật giá và **tổng số lượng**, bao gồm vé đã đặt.
  Tổng mới không được thấp hơn số vé đã đặt; số còn lại được tính lại.
- Giá và tổng số lượng cho phép bằng 0, không được âm.
- Không xóa sự kiện đã được bất kỳ booking nào tham chiếu.
- Khách hàng xem danh sách, tìm kiếm, chi tiết và giá/số vé còn lại.

## API dành cho Booking

```java
context.events().checkTicketAvailability(eventId, ticketTypeId, requestedQuantity);
```

API chỉ kiểm tra tồn vé, không trừ/hoàn vé và không tạo/sửa Booking.

- Thành công: trả về `void`, dữ liệu không đổi.
- Số lượng <= 0: `IllegalArgumentException`.
- Không có sự kiện hoặc loại vé không thuộc sự kiện: `NotFoundException`.
- Số lượng vượt tồn hiện tại: `OutOfStockException`.

Ở model có `TicketType.hasAvailableQuantity(int)` trả về boolean và
`TicketType.checkAvailability(int)` ném exception khi thiếu vé.
Kiểm tra tồn không bao gồm chính sách trạng thái/thời gian mở bán hay giới hạn
số vé mỗi booking; các chính sách đó vẫn thuộc Booking.

Kết quả kiểm tra không giữ chỗ. Khi tích hợp, thành viên phụ trách Booking phải
kiểm tra và trừ vé trong cùng `synchronized(store)`. EventService đã dùng cùng
monitor khi sửa sức chứa hoặc xóa sự kiện. Module này không sửa BookingService,
BookingController, Booking, Ticket hay TicketFactory.

Các overload cũ của EventService được giữ để tương thích. Khi sửa lịch bằng
overload chỉ nhận ngày bắt đầu, thời lượng cũ được giữ nguyên. Validation thất bại
không làm cập nhật dở dang thông tin sự kiện hoặc giá/sức chứa loại vé.

## Kiểm thử

```text
mvn test
```

JUnit Jupiter/Surefire thực thi `EventTicketManagementTest` (10 test, gồm gọi lại
luồng hồi quy `BasicLogicTest`) và `EventViewsTest` (2 test JavaFX/FXML).
Test JavaFX cần môi trường desktop hỗ trợ JavaFX toolkit.
Test giao diện kiểm tra nạp FXML, dữ liệu chi tiết, lọc danh sách, lựa chọn và
làm trống form; không thay thế kiểm tra bố cục trực quan bằng tay.

Kết quả trên môi trường Windows hiện tại: **12 test, 0 failures, 0 errors,
0 skipped; BUILD SUCCESS**. Runtime có cảnh báo native access/unnamed module
của JavaFX và cache ở `C:\.openjfx` không ghi được trong sandbox; test vẫn pass.

Máy hiện tại dùng JDK/Maven kèm Apache NetBeans và cache dependency riêng:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Apache NetBeans/jdk'
& 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' '-Dmaven.repo.local=D:/BTL_OOP_Git/.tools/m2' test -B -o
```

`.tools` đã được Git ignore. Không commit, push hoặc merge trong quá trình triển khai.
