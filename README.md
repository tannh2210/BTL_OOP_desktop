# Event & Ticketing System

Ứng dụng desktop JavaFX cho đề tài **Event & Ticketing System – Nhóm 5**.

Đây là bản khởi đầu để 4 thành viên tiếp tục Milestone 4 (class, Collections, exception) và Milestone 5 (SOLID, refactoring, Factory, Observer). Chưa phải sản phẩm cuối kỳ.

## Chạy trên Windows

1. Giải nén ZIP. Không chạy file khi nó vẫn nằm trong ZIP.
2. Mở thư mục có `pom.xml`.
3. Nhấp đúp `run.bat`.

Lần đầu cần Internet để Maven tải JavaFX. Script tự tìm JDK 17 trở lên và tải Maven portable nếu máy chưa có Maven.

Trong IntelliJ hoặc VS Code: mở đúng thư mục chứa `pom.xml`, chạy Maven goal `javafx:run`.

Kiểm tra logic không cần mở cửa sổ: nhấp đúp `test.bat`.

## Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
| --- | --- | --- |
| Khách hàng | `customer` | `123456` |
| Quản trị viên | `admin` | `123456` |

Mật khẩu chỉ dùng để học. Ứng dụng chưa băm mật khẩu, nên không nhập mật khẩu thật.

Đăng ký trên màn hình đầu tạo tài khoản khách hàng mới. Sau khi đăng nhập, menu mới hiện ra. Đăng xuất đưa về màn đăng nhập.

## Màn hình hiện có

- Đăng nhập và đăng ký.
- Trang chủ: tên ứng dụng, vài sự kiện mẫu, nút xem sự kiện.
- Danh sách sự kiện của khách hàng: tìm theo tên, xem chi tiết, loại vé, giá và số lượng còn lại.
- Đặt vé: chọn sự kiện, loại vé, số lượng, xem tổng tiền, tạo booking và mã vé, xem lịch sử, hủy booking chưa check-in.
- Quản lý sự kiện của quản trị: thêm, sửa, xóa, thêm và cập nhật loại vé.
- Check-in: nhập mã vé. Kết quả là vé hợp lệ, mã không tồn tại, hoặc vé đã sử dụng.
- Thống kê: số sự kiện, số booking, số vé đã đặt và doanh thu tạm tính.
- Thông báo nội bộ sau khi đặt vé, hủy vé hoặc check-in.

Khách hàng không thấy Check-in, Thống kê và Quản lý sự kiện. Quản trị viên không thấy màn đặt vé.

## Dữ liệu và giới hạn

Mọi màn hình dùng chung một `InMemoryStore`. Đóng ứng dụng thì dữ liệu mới tạo sẽ mất.

Chưa có database, thanh toán, email, QR, sơ đồ ghế hoặc biểu đồ. Những phần này ghi ở README và ghi chú trong source, không hiện trên giao diện.

Factory đã tạo `VIPTicket`, `StandardTicket` và `EconomyTicket`. Observer cho `Notification` và việc tách SOLID sẽ làm ở Milestone 5.

Xóa sự kiện bị từ chối nếu sự kiện đã có booking. Không thể hủy booking sau khi vé đã check-in. Mỗi lần đặt từ 1 đến 10 vé.

## Cấu trúc

```
src/main/java/com/nhom5/eventticket/
  MainApp.java          điểm khởi động JavaFX
  AppContext.java       dịch vụ và người đang đăng nhập
  model/                class theo UML Milestone 3
  service/              nghiệp vụ
  ui/                   controller JavaFX
  repository/           Collections dùng chung và dữ liệu mẫu
  factory/              TicketFactory
  exception/            lỗi nghiệp vụ
src/main/resources/fxml/
src/main/resources/css/app.css
```

Class nghiệp vụ: `User`, `Customer`, `Admin`, `Event`, `TicketType`, `Booking`, `Ticket`, `CheckIn`, `Notification`, `Statistics`, `TicketFactory`, `VIPTicket`, `StandardTicket`, `EconomyTicket`.

Giao diện nằm trong `ui` và FXML. Nghiệp vụ nằm trong `service` và `model`.
