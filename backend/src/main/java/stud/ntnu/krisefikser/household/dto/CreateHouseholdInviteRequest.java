package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for creating a household invite.
 *
 * @param householdId   the ID of the household to invite the user to
 * @param invitedUserId the ID of the user to invite (optional)
 * @param invitedEmail  the email address of the user to invite (optional)
 */
public record CreateHouseholdInviteRequest(
    @NotNull
    UUID householdId,

    UUID invitedUserId,
    @Email
    String invitedEmail) {

}