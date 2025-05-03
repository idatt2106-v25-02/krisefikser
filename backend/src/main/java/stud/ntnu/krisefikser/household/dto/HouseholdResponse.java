package stud.ntnu.krisefikser.household.dto;

import stud.ntnu.krisefikser.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HouseholdResponse(
        UUID id,
        String name,
        double latitude,
        double longitude,
        String address,
        UserResponse owner,
        List<HouseholdMemberResponse> members,
        LocalDateTime createdAt,
        boolean isActive) {
}
