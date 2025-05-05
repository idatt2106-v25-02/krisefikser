package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPointRequest {
    private String name;
    private String description;
    private double latitude;
    private double longitude;
}