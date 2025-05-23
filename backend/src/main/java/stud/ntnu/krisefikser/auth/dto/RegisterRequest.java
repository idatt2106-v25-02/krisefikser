package stud.ntnu.krisefikser.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.decorators.ValidEmail;
import stud.ntnu.krisefikser.decorators.ValidPassword;

/**
 * Data Transfer Object (DTO) for registering a user. This class is used to transfer data between
 * the server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @ValidEmail
  private String email;
  @ValidPassword
  private String password;
  @NotBlank(message = "First name is required")
  private String firstName;
  @NotBlank(message = "Last name is required")
  private String lastName;
  private String turnstileToken;
}