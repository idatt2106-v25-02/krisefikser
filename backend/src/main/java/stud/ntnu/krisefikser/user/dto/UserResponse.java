package stud.ntnu.krisefikser.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user response. This class is used to transfer user data between
 * the server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private UUID id;
  private String email;
  private List<String> roles;
  private String firstName;
  private String lastName;
  private String avatarUrl;
  private boolean notifications;
  private boolean emailUpdates;
  private boolean locationSharing;
  private Double latitude;
  private Double longitude;

  public UserResponse(UUID id, String email, List<String> roles, String firstName, String lastName,
      boolean notifications, boolean emailUpdates, boolean locationSharing, Double latitude,
      Double longitude) {
    this.id = id;
    this.email = email;
    this.roles = roles;
    this.firstName = firstName;
    this.lastName = lastName;
    this.notifications = notifications;
    this.emailUpdates = emailUpdates;
    this.locationSharing = locationSharing;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}