package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
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

  @NotNull
  private UUID id;
  @NotNull
  private String name;
  @NotNull
  private double amount;
  @NotNull
  private LocalDateTime expiryDate;
  @NotNull
  private UUID householdId;
  @NotNull
  private ProductTypeResponse productType;
  @NotNull
  private LocalDateTime createdAt;
  @NotNull
  private LocalDateTime updatedAt;
}