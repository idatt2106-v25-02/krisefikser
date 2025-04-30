package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import stud.ntnu.krisefikser.decorators.ValidEmail;
import stud.ntnu.krisefikser.decorators.ValidPassword;

@Data
@AllArgsConstructor
public class LoginRequest {

  @ValidEmail
  private String email;
  @ValidPassword
  private String password;
}