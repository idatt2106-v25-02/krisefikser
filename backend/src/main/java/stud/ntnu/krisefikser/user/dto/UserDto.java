package stud.ntnu.krisefikser.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private String email;
  private List<String> roles;
  private String firstName;
  private String lastName;
}