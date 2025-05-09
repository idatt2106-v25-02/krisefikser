package stud.ntnu.krisefikser.household.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.user.dto.UserResponse;

/**
 * Data Transfer Object for representing a household invite response.
 *
 * <p>This class contains information about the household invite, including the ID, household
 * details, invited user, email, creator, creation time, status, and response time.</p>
 */
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