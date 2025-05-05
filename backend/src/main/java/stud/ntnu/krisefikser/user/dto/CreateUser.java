package stud.ntnu.krisefikser.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for creating a user. This class is used to transfer data between the
 * server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUser {

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;
  @NotBlank(message = "Password is required")
  private String password;
  @NotBlank(message = "First name is required")
  private String firstName;
  @NotBlank(message = "Last name is required")
  private String lastName;
  private boolean notifications;
  private boolean emailUpdates;
  private boolean locationSharing;
}
