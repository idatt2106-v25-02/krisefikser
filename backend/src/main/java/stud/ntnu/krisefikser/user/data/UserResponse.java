package stud.ntnu.krisefikser.user.data;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
  private String firstName;
  private String lastName;
  private LocalDateTime createdAt;
}
