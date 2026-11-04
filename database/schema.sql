CREATE DATABASE IF NOT EXISTS event_ticketing
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE event_ticketing;

-- Chỉ dùng bảng ticket_types nếu nhóm chưa có bảng tương đương.
-- Nếu đã có, chỉnh TicketTypeDAO theo schema chung, không tạo bảng trùng.
CREATE TABLE IF NOT EXISTS ticket_types (
  ticket_type_id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  price DECIMAL(15,2) NOT NULL,
  total_quantity INT NOT NULL,
  available_quantity INT NOT NULL,
  CHECK (price >= 0),
  CHECK (total_quantity >= 0),
  CHECK (available_quantity BETWEEN 0 AND total_quantity)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
  booking_id VARCHAR(36) PRIMARY KEY,
  customer_id VARCHAR(50) NOT NULL,
  event_id VARCHAR(50) NOT NULL,
  event_name VARCHAR(255) NOT NULL,
  ticket_type_id VARCHAR(50) NOT NULL,
  ticket_type_name VARCHAR(100) NOT NULL,
  unit_price DECIMAL(15,2) NOT NULL,
  quantity INT NOT NULL,
  booking_time DATETIME NOT NULL,
  total_amount DECIMAL(15,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  INDEX idx_booking_customer(customer_id),
  INDEX idx_booking_event(event_id),
  CHECK (quantity > 0),
  CHECK (total_amount >= 0),
  CHECK (status IN ('CONFIRMED','CANCELLED'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tickets (
  ticket_id VARCHAR(36) PRIMARY KEY,
  booking_id VARCHAR(36) NOT NULL,
  ticket_code VARCHAR(30) NOT NULL UNIQUE,
  ticket_type_id VARCHAR(50) NOT NULL,
  ticket_type_name VARCHAR(100) NOT NULL,
  ticket_class VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  INDEX idx_ticket_booking(booking_id),
  CONSTRAINT fk_ticket_booking FOREIGN KEY(booking_id)
    REFERENCES bookings(booking_id),
  CHECK (ticket_class IN ('VIP','STANDARD','ECONOMY')),
  CHECK (status IN ('VALID','CANCELLED'))
) ENGINE=InnoDB;
