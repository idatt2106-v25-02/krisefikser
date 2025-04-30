package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

  private String accessToken;
  private String refreshToken;
}