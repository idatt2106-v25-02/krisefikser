package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for completing a password reset.
 *
 * <p>This class encapsulates the data required to complete a password reset, including
 * the reset token and the new password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletePasswordResetRequest {
  String token;
  String newPassword;
}
