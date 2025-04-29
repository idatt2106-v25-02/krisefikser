package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinHouseholdRequest {
    private UUID householdId;
}
