package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.entity.User;

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
  private double latitude;
  private double longitude;
  @NotBlank(message = "Address is required")
  private String address;
  @NotBlank(message = "City is required")
  private String city;
  @NotBlank(message = "Postal code is required")
  private String postalCode;

  /**
   * Converts this DTO to a Household entity.
   *
   * @return a Household entity with the same data as this DTO
   */
  public Household toEntity(User owner) {
    return Household.builder()
        .name(name)
        .owner(owner)
        .latitude(latitude)
        .longitude(longitude)
        .address(address)
        .city(city)
        .postalCode(postalCode)
        .build();
  }
}
