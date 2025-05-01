package stud.ntnu.krisefikser.household.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdItemResponse {

  private UUID id;
  private String name;
  private double amount;
  private LocalDateTime expiryDate;
  private UUID householdId;
  private ProductTypeResponse productType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}