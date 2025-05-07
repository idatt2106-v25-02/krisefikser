package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for requesting a password reset.
 *
 * <p>This class encapsulates the data required to request a password reset, which is
 * the user's email address where the reset token will be sent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPasswordResetRequest {

  String email;
}
