package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdItemDto {
    private UUID id;
    private String name;
    private double amount;
    private LocalDateTime expiryDate;
    private UUID householdId;
    private ProductTypeDto productType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}