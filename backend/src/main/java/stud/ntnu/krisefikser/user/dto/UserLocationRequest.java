package stud.ntnu.krisefikser.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving user location update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationRequest {
    private Double latitude;
    private Double longitude;
}