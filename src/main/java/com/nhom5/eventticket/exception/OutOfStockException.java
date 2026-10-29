package com.nhom5.eventticket.exception;
/** Shared business exception; modules may add more specialized exceptions. */
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) { super(message); }
}
