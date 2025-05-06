package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
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

  /**
   * Converts this DTO to a Household entity.
   *
   * @return a Household entity with the same data as this DTO
   */
  public Household toEntity(User owner) {
    return toEntity(owner, null);
  }

  /**
   * Converts this DTO to a Household entity with a specified ID.
   *
   * @param owner       the owner of the household
   * @param householdId the ID of the household
   * @return a Household entity with the same data as this DTO and the specified ID
   */
  public Household toEntity(User owner, UUID householdId) {
    return Household.builder()
        .id(householdId)
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
