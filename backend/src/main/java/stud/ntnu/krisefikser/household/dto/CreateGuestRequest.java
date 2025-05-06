package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CreateGuestRequest {

  @NotBlank(message = "Name of guest is required")
  private String name;
  @NotBlank(message = "Icon is required")
  private String icon;
  @NotBlank(message = "Consumption multiplier is required")
  private double consumptionMultiplier;
}
