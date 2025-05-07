package stud.ntnu.krisefikser.decorators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for validating email addresses.
 *
 * <p>This annotation can be used to validate that a given string is a valid email address. It
 * combines the {@link NotBlank} and {@link Email} annotations to ensure that the email is not blank
 * and follows the standard email format.
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank
@Email
public @interface ValidEmail {

  /**
   * The message that will be returned if the email is invalid.
   *
   * @return the error message
   */
  String message() default "Invalid email address";

  /**
   * Allows the specification of validation groups, to which this constraint belongs.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Allows the specification of validation constraints to be carried out on the annotated element.
   *
   * @return the payload associated with the annotation
   */
  Class<? extends Payload>[] payload() default {};
}