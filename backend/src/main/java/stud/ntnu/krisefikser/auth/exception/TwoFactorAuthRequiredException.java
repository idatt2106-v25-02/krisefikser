package stud.ntnu.krisefikser.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when two-factor authentication is required for admin login.
 * This exception is associated with HTTP 428 Precondition Required status code,
 * indicating that the server requires the request to be conditional (2FA verification).
 */
@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class TwoFactorAuthRequiredException extends RuntimeException {

    /**
     * Constructs a new TwoFactorAuthRequiredException with a default message.
     */
    public TwoFactorAuthRequiredException() {
        super("Two-factor authentication is required for admin login");
    }

    /**
     * Constructs a new TwoFactorAuthRequiredException with a custom message.
     *
     * @param message the detail message
     */
    public TwoFactorAuthRequiredException(String message) {
        super(message);
    }
} 