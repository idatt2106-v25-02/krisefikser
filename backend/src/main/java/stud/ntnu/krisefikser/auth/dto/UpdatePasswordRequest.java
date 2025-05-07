package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.decorators.ValidPassword;

/**
 * Data Transfer Object (DTO) for updating a user's password.
 *
 * <p>This class encapsulates the new password data for updating a user's password.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordRequest {

  @ValidPassword
  String password;
}
