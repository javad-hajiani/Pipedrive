package com.test.demo;

/**
 * Exception to be used when trying to add an already existing user.
 */
public class UserAlreadyExistsException extends Exception {

    /**
     * Constructor.
     */
    public UserAlreadyExistsException() {
        super();
    }

    /**
     * Constructor with a message.
     *
     * @param message message.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
