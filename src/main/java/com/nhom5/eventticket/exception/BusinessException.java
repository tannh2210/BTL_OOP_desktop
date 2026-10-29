package com.nhom5.eventticket.exception;
/** Shared business exception; modules may add more specialized exceptions. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
