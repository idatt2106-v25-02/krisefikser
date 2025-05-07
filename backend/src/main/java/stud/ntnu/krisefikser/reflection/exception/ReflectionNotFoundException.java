package stud.ntnu.krisefikser.reflection.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception thrown when a requested reflection cannot be found.
 * 
 * <p>
 * This exception is associated with the HTTP 404 Not Found status code.
 * </p>
 * 
 * @since 1.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReflectionNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with a message indicating that a reflection with
     * the specified ID was not found.
     *
     * @param id the ID of the reflection that was not found
     */
    public ReflectionNotFoundException(UUID id) {
        super("Reflection not found with ID: " + id);
    }

    /**
     * Constructs a new exception with a custom message.
     *
     * @param message the detail message
     */
    public ReflectionNotFoundException(String message) {
        super(message);
    }
}