package stud.ntnu.krisefikser.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for verifying an admin token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyAdminTokenResponse {

  @NotNull
  @Email
  private String email;
}
