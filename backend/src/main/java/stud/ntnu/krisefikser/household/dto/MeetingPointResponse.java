package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPointResponse {
    private UUID id;
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private UUID householdId;
}