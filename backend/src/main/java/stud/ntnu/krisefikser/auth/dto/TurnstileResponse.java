package stud.ntnu.krisefikser.auth.dto;

import java.util.List;
import lombok.Data;

@Data
public class TurnstileResponse {
    private boolean success;
    private List<String> errorCodes;
}
