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

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank
@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
    message = "Passorded må inneholde minst en stor bokstav, en liten bokstav, et tall, et"
        + "spesialtegn og være minst 8 tegn langt")
public @interface ValidPassword {

  String message() default "Invalid password";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
