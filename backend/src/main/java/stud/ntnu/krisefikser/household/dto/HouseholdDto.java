package stud.ntnu.krisefikser.household.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import stud.ntnu.krisefikser.user.dto.UserDto;

public class HouseholdDto {
    private UUID id;
    private String name;
    private double latitude;
    private double longitude;
    private UserDto owner;
    private List<HouseholdMemberDto> members;
    private LocalDateTime createdAt;
}
