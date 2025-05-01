package stud.ntnu.krisefikser.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private UUID id;
  private String email;
  private List<String> roles;
  private String firstName;
  private String lastName;
  private boolean notifications;
  private boolean emailUpdates;
  private boolean locationSharing;
}
