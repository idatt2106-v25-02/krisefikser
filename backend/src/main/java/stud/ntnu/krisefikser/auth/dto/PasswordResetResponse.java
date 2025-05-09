package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for password reset response.
 *
 * <p>This class contains the status and message of a password reset operation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetResponse {

  String message;
  boolean success;
}
