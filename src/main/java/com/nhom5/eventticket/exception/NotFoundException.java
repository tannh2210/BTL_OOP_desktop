package com.nhom5.eventticket.exception;
/** Shared business exception; modules may add more specialized exceptions. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
