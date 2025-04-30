package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHouseholdRequest {

  private String name;
  private double latitude;
  private double longitude;
  private String address;
  private String city;
  private String postalCode;
}
