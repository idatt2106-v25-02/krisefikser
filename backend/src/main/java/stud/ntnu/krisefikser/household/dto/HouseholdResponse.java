package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdResponse {
    private UUID id;
    private String name;
    private double latitude;
    private double longitude;
    private UserDto owner;
    private List<HouseholdMemberDto> members;
    private LocalDateTime createdAt;
}
