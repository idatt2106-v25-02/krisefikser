package stud.ntnu.krisefikser.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponse {

  @NotBlank
  private String accessToken;
  @NotBlank
  private String refreshToken;
}
