package com.nhom5.eventticket.exception;

/** Vé tồn tại nhưng không đủ điều kiện check-in (hủy, chưa xác nhận, ...). */
public class InvalidTicketException extends BusinessException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
