package stud.ntnu.krisefikser.auth.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for the response from the Turnstile service. This class is used to
 * transfer data between the server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnstileResponse {

  private boolean success;
  private List<String> errorCodes;
}
