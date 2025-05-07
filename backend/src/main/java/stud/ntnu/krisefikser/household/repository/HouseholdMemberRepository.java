package stud.ntnu.krisefikser.household.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Repository interface for managing household members. Provides methods to find, check existence,
 * and retrieve members of a household.
 *
 * @since 1.0
 */
public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {

  /**
   * Finds all members of a household by its ID.
   *
   * @param householdId the ID of the household
   * @return a list of HouseholdMember entities
   */
  List<HouseholdMember> findByHouseholdId(UUID householdId);

  /**
   * Checks if a user is a member of a household.
   *
   * @param currentUser the user to check
   * @param household   the household to check against
   * @return true if the user is a member of the household, false otherwise
   */
  boolean existsByUserAndHousehold(User currentUser, Household household);

  /**
   * Finds a household member by household and user.
   *
   * @param household   the household
   * @param currentUser the user
   * @return an Optional containing the HouseholdMember if found, or empty if not
   */
  Optional<HouseholdMember> findByHouseholdAndUser(Household household, User currentUser);

  /**
   * Finds all members of a household by user.
   *
   * @param user the user
   * @return a list of HouseholdMember entities
   */
  List<HouseholdMember> findByUser(User user);
}