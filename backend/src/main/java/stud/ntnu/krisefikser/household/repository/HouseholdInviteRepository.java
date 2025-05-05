package stud.ntnu.krisefikser.household.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdInviteRepository extends JpaRepository<HouseholdInvite, UUID> {
        List<HouseholdInvite> findByHouseholdAndStatus(Household household, InviteStatus status);

        List<HouseholdInvite> findByInvitedUserOrInvitedEmailAndStatus(User user, String email,
                        InviteStatus status);

        List<HouseholdInvite> findByInvitedEmailAndStatus(String email, InviteStatus status);

        Optional<HouseholdInvite> findByHouseholdAndInvitedEmailAndStatus(Household household, String email,
                        InviteStatus status);

        Optional<HouseholdInvite> findByHouseholdAndInvitedUserAndStatus(Household household, User user,
                        InviteStatus status);

        boolean existsByHouseholdAndInvitedEmailAndStatusIn(Household household, String email,
                        List<InviteStatus> statuses);

        boolean existsByHouseholdAndInvitedUserAndStatusIn(Household household, User user, List<InviteStatus> statuses);

        // New methods to find invites regardless of status
        Optional<HouseholdInvite> findByHouseholdAndInvitedEmail(Household household, String email);

        Optional<HouseholdInvite> findByHouseholdAndInvitedUser(Household household, User user);
}