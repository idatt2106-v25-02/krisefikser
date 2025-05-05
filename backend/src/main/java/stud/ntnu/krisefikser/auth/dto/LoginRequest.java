package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.decorators.ValidEmail;
import stud.ntnu.krisefikser.decorators.ValidPassword;

/**
 * Data Transfer Object (DTO) for representing a login request. This class is used to transfer data
 * between the server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @ValidEmail
  private String email;
  @ValidPassword
  private String password;
}