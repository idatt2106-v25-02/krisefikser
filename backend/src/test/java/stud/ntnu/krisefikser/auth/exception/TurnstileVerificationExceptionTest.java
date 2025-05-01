package stud.ntnu.krisefikser.auth.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

class TurnstileVerificationExceptionTest {

    @Test
    void constructor_WithoutMessage_ShouldUseDefaultMessage() {
        // Act
        TurnstileVerificationException exception = new TurnstileVerificationException();

        // Assert
        assertThat(exception)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("CAPTCHA verification failed. Please try again.");
    }

    @Test
    void constructor_WithCustomMessage_ShouldUseProvidedMessage() {
        // Arrange
        String customMessage = "Custom verification error message";

        // Act
        TurnstileVerificationException exception = new TurnstileVerificationException(customMessage);

        // Assert
        assertThat(exception)
            .isInstanceOf(RuntimeException.class)
            .hasMessage(customMessage);
    }

    @Test
    void annotation_ShouldHaveCorrectResponseStatus() {
        // Act
        ResponseStatus annotation = TurnstileVerificationException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertThat(annotation)
            .isNotNull()
            .satisfies(status -> {
                assertThat(status.value()).isEqualTo(HttpStatus.BAD_REQUEST);
            });
    }
} 