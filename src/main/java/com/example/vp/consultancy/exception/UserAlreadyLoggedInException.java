package com.example.vp.consultancy.exception;

/**
 * Thrown when a user attempts to log in while an active refresh token already exists.
 */
public class UserAlreadyLoggedInException extends VPException {

    public UserAlreadyLoggedInException(String message) {
        super(message, 409, "USER_ALREADY_LOGGED_IN");
    }
}
