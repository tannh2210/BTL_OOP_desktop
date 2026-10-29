package com.nhom5.eventticket.exception;
/** Shared business exception; modules may add more specialized exceptions. */
public class DuplicateCheckInException extends RuntimeException {
    public DuplicateCheckInException(String message) { super(message); }
}
