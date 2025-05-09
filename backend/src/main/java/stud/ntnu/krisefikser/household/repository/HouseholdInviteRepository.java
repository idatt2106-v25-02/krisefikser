package stud.ntnu.krisefikser.household.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Repository interface for managing {@link HouseholdInvite} entities.
 *
 * <p>This repository provides methods to perform CRUD operations and custom queries related to
 * household invites. It extends JpaRepository to leverage Spring Data JPA's capabilities.</p>
 */
public interface HouseholdInviteRepository extends JpaRepository<HouseholdInvite, UUID> {


  /**
   * Finds a household invite by the household and status.
   *
   * @param household the household associated with the invite
   * @param status    the status of the invite
   * @return a list of HouseholdInvite matching the criteria
   */
  List<HouseholdInvite> findByHouseholdAndStatus(Household household, InviteStatus status);

  /**
   * Finds a household invite by the invited user or email and status.
   *
   * @param user   the invited user
   * @param email  the invited email
   * @param status the status of the invite
   * @return a list of HouseholdInvite matching the criteria
   */
  List<HouseholdInvite> findByInvitedUserOrInvitedEmailAndStatus(User user, String email,
      InviteStatus status);

  /**
   * Finds a household invite by the household and invited email.
   *
   * <p>This method is used to check if an invite already exists for a given household and email
   * address.</p>
   *
   * @param household the household associated with the invite
   * @param email     the invited email
   * @return an Optional containing the found HouseholdInvite, or empty if not found
   */
  Optional<HouseholdInvite> findByHouseholdAndInvitedEmail(Household household, String email);

  /**
   * Finds a household invite by the household and invited user.
   *
   * <p>This method is used to check if an invite already exists for a given household and
   * user.</p>
   *
   * @param household the household associated with the invite
   * @param user      the invited user
   * @return an Optional containing the found HouseholdInvite, or empty if not found
   */
  Optional<HouseholdInvite> findByHouseholdAndInvitedUser(Household household, User user);

  /**
   * Finds all household invites associated with a specific household.
   *
   * @param household the household to find invites for
   * @return a list of HouseholdInvite associated with the specified household
   */
  List<HouseholdInvite> findByHousehold(Household household);
}