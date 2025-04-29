package stud.ntnu.krisefikser.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private boolean notifications;
  private boolean emailUpdates;
  private boolean locationSharing;
}
