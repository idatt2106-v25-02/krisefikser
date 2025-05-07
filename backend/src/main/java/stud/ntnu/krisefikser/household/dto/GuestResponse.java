package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for creating a guest. This class is used to transfer data between the
 * server and client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {

  @NotNull
  private UUID id;
  @NotNull
  private String name;
  @NotNull
  private String icon;
  @NotNull
  private double consumptionMultiplier;
}
