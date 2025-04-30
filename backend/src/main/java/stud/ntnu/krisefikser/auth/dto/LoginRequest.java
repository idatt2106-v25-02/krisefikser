package stud.ntnu.krisefikser.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @Email(message = "Email is required")
  @NotBlank(message = "Must be a valid email")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;
}