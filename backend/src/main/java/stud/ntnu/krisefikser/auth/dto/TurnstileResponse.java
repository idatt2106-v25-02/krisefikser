package stud.ntnu.krisefikser.auth.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnstileResponse {

  private boolean success;
  private List<String> errorCodes;
}
