package stud.ntnu.krisefikser.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Must be a valid email")
  private String email;
  @NotBlank(message = "Password is required")
  private String password;
  @NotBlank(message = "First name is required")
  private String firstName;
  @NotBlank(message = "Last name is required")
  private String lastName;
}