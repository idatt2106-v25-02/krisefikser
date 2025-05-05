package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.decorators.ValidEmail;
import stud.ntnu.krisefikser.decorators.ValidPassword;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @ValidEmail
  private String email;
  @ValidPassword
  private String password;
}