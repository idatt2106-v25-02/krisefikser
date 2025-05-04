package stud.ntnu.krisefikser.reflection.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access or modify a reflection
 * they are not authorized to access or modify.
 * 
 * <p>
 * This exception is associated with the HTTP 403 Forbidden status code.
 * </p>
 * 
 * @since 1.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedReflectionAccessException extends RuntimeException {

    /**
     * Constructs a new exception with a default message.
     */
    public UnauthorizedReflectionAccessException() {
        super("You are not authorized to access or modify this reflection");
    }

    /**
     * Constructs a new exception with a custom message.
     *
     * @param message the detail message
     */
    public UnauthorizedReflectionAccessException(String message) {
        super(message);
    }
}