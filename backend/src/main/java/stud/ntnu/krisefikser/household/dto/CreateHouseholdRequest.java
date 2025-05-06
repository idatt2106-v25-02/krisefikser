package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.entity.Household;

/**
 * Data Transfer Object (DTO) for creating a household. This class is used to transfer data between
 * the server and client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHouseholdRequest {

  @NotBlank(message = "Household name is required")
  private String name;
  @NotBlank(message = "Latitude is required")
  private double latitude;
  @NotBlank(message = "Longitude is required")
  private double longitude;
  @NotBlank(message = "Address is required")
  private String address;
  @NotBlank(message = "City is required")
  private String city;
  @NotBlank(message = "State is required")
  private String postalCode;

  public Household toEntity() {
    return Household.builder()
        .name(name)
        .latitude(latitude)
        .longitude(longitude)
        .address(address)
        .city(city)
        .postalCode(postalCode)
        .build();
  }
}
