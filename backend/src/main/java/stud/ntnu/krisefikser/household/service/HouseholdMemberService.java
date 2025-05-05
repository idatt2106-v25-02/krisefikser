package stud.ntnu.krisefikser.household.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Service class for managing household members. Provides methods to add,
 * remove, and retrieve
 * members of a household.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class HouseholdMemberService {

  private final HouseholdMemberRepository householdMemberRepo;

  /**
   * Retrieves all members of a household by its ID.
   *
   * @param householdId the ID of the household
   * @return a list of HouseholdMember entities
   */
  public List<HouseholdMember> getMembers(UUID householdId) {
    return householdMemberRepo.findByHouseholdId(householdId);
  }

  /**
   * Checks if the user is a member of the specified household.
   *
   * @param currentUser the user to check
   * @param household   the household to check against
   * @return true if the user is a member of the household, false otherwise
   */
  public boolean isMemberOfHousehold(User currentUser, Household household) {
    return householdMemberRepo.existsByUserAndHousehold(currentUser, household);
  }

  /**
   * Adds a member to the household.
   *
   * @param household   the household to which the member is to be added
   * @param currentUser the user to be added as a member
   * @return the newly created HouseholdMember entity
   */
  public HouseholdMember addMember(Household household, User currentUser) {
    HouseholdMember member = new HouseholdMember();
    member.setHousehold(household);
    member.setUser(currentUser);
    return householdMemberRepo.save(member);
  }

  /**
   * Removes a member from the household.
   *
   * @param household   the household from which the member is to be removed
   * @param currentUser the user to be removed from the household
   */
  public void removeMember(Household household, User currentUser) {
    HouseholdMember member = householdMemberRepo.findByHouseholdAndUser(household, currentUser)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    householdMemberRepo.delete(member);
  }

  /**
   * Retrieves all households that the user is a member of.
   *
   * @param user the user whose households are to be retrieved
   * @return a list of households that the user is a member of
   */
  public List<HouseholdMember> getHouseholdsByUser(User user) {
    return householdMemberRepo.findByUser(user);
  }
}
