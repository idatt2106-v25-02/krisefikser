package stud.ntnu.krisefikser.decorators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for validating passwords.
 *
 * <p>This annotation checks that the password meets the following criteria:</p>
 * <ul>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one lowercase letter</li>
 *   <li>At least one digit</li>
 *   <li>At least one special character</li>
 *   <li>Minimum length of 8 characters</li>
 * </ul>
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank
@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
    message = "Passordet må inneholde minst en stor bokstav, en liten bokstav, et tall, et"
        + "spesialtegn og være minst 8 tegn langt")
public @interface ValidPassword {

  /**
   * The default message to be returned when the password is invalid.
   *
   * @return the default error message
   */
  String message() default "Invalid password";

  /**
   * The groups the constraint belongs to.
   *
   * @return the groups
   */
  Class<?>[] groups() default {};

  /**
   * The payload associated with the constraint.
   *
   * @return the payload
   */
  Class<? extends Payload>[] payload() default {};
}
