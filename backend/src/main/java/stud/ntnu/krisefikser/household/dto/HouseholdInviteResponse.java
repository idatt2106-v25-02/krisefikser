package stud.ntnu.krisefikser.household.dto;

import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record HouseholdInviteResponse(
        UUID id,
        HouseholdResponse household,
        UserResponse invitedUser,
        String invitedEmail,
        UserResponse createdBy,
        LocalDateTime createdAt,
        InviteStatus status,
        LocalDateTime respondedAt) {
}