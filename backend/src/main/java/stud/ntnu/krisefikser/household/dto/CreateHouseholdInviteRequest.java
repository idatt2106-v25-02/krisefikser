package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateHouseholdInviteRequest(
        @NotNull UUID householdId,

        UUID invitedUserId,

        @Email String invitedEmail) {

}