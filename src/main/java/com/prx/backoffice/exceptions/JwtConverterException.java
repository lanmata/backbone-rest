package com.prx.backoffice.exceptions;

/**
 * Exception class for JWT conversion errors.
 * This class represents exceptions that occur during the conversion of JWTs.
 */
public class JwtConverterException extends Exception {

    /**
     * Default constructor.
     * Creates a new instance of JwtConverterException with no detail message.
     */
    public JwtConverterException() {
        super();
    }

    /**
     * Constructor with a message.
     * Creates a new instance of JwtConverterException with the specified detail message.
     *
     * @param message the detail message
     */
    public JwtConverterException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and cause.
     * Creates a new instance of JwtConverterException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public JwtConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with a cause.
     * Creates a new instance of JwtConverterException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public JwtConverterException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with a message, cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     * Creates a new instance of JwtConverterException with the specified detail message, cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public JwtConverterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
