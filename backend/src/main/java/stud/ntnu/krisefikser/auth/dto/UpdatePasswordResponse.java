package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for password update response.
 *
 * <p>This class contains the status and message of a password update operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordResponse {

  String message;
  boolean success;
}
