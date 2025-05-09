package stud.ntnu.krisefikser.household.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.dto.CreateGuestRequest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.dto.GuestResponse;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.entity.Guest;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.household.repository.GuestRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdInviteRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.item.service.ChecklistItemService;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service class for managing households in the system. Provides methods for household-related
 * operations such as creating, joining, leaving, and deleting households.
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HouseholdService {

  private final HouseholdRepository householdRepo;
  private final HouseholdMemberService householdMemberService;
  private final UserService userService;
  private final ChecklistItemService checklistItemService;
  private final GuestRepository guestRepository;
  private final HouseholdInviteRepository inviteRepository;

  /**
   * Retrieves all households that the current user is a member of.
   *
   * @return A list of household responses
   */
  @Transactional(readOnly = true)
  public List<HouseholdResponse> getUserHouseholds() {
    User currentUser = userService.getCurrentUser();
    List<HouseholdMember> userMemberships = householdMemberService.getHouseholdsByUser(currentUser);

    return userMemberships.stream()
        .map(member -> toHouseholdResponse(member.getHousehold()))
        .toList();
  }

  /**
   * Converts a Household entity to a HouseholdResponse DTO.
   *
   * @param household The household entity to convert
   * @return The converted HouseholdResponse DTO
   */
  public HouseholdResponse toHouseholdResponse(Household household) {
    User currentUser = userService.getCurrentUser();
    List<HouseholdMember> members = householdMemberService.getMembers(household.getId());
    List<Guest> guests = guestRepository.findByHousehold(household);

    boolean isActive = false;
    if (currentUser.getActiveHousehold() != null) {
      isActive = household.getId().equals(currentUser.getActiveHousehold().getId());
    }

    // Use different DTO conversion based on whether this is an active household
    if (isActive) {
      // For active households, include location data
      return new HouseholdResponse(
          household.getId(),
          household.getName(),
          household.getLatitude(),
          household.getLongitude(),
          household.getAddress(),
          household.getPostalCode(),
          household.getCity(),
          household.getOwner().toDtoWithLocation(), // Include owner's location
          members.stream().map(HouseholdMember::toDtoWithLocation).toList(),
          // Include members' location
          guests.stream()
              .map(
                  guest -> new GuestResponse(guest.getId(), guest.getName(), guest.getIcon(),
                      guest.getConsumptionMultiplier()))
              .toList(),
          household.getCreatedAt(),
          true);
    } else {
      // For non-active households, don't include location data
      return new HouseholdResponse(
          household.getId(),
          household.getName(),
          household.getLatitude(),
          household.getLongitude(),
          household.getAddress(),
          household.getPostalCode(),
          household.getCity(),
          household.getOwner().toDto(), // Don't include owner's location
          members.stream().map(HouseholdMember::toDto).toList(), // Don't include members' location
          guests.stream()
              .map(
                  guest -> new GuestResponse(guest.getId(), guest.getName(), guest.getIcon(),
                      guest.getConsumptionMultiplier()))
              .toList(),
          household.getCreatedAt(),
          false);
    }
  }

  /**
   * Joins a household by ID and sets it as active for the current user.
   *
   * @param householdId The ID of the household to join
   * @return The joined household response
   */
  @Transactional
  public HouseholdResponse joinHousehold(UUID householdId) {
    User currentUser = userService.getCurrentUser();
    Household household = householdRepo.findById(householdId)
        .orElseThrow(HouseholdNotFoundException::new);

    if (householdMemberService.isMemberOfHousehold(currentUser, household)) {
      throw new IllegalArgumentException("Already a member of this household");
    }

    HouseholdMember member = householdMemberService.addMember(household, currentUser);

    if (member == null) {
      throw new IllegalArgumentException("Failed to join household");
    }

    userService.updateActiveHousehold(household);

    return toHouseholdResponse(household);
  }

  /**
   * Sets the active household for the current user.
   *
   * @param householdId The ID of the household to set as active
   * @return The updated household response
   */
  @Transactional
  public HouseholdResponse setActiveHousehold(UUID householdId) {
    User currentUser = userService.getCurrentUser();
    Household household = householdRepo.findById(householdId)
        .orElseThrow(HouseholdNotFoundException::new);

    if (!householdMemberService.isMemberOfHousehold(currentUser, household)) {
      throw new IllegalArgumentException("Not a member of this household");
    }

    currentUser.setActiveHousehold(household);
    userService.updateActiveHousehold(household);

    return toHouseholdResponse(household);
  }

  /**
   * Leaves the specified household. The user must be a member of the household to leave it.
   *
   * @param householdId The ID of the household to leave
   */
  @Transactional
  public void leaveHousehold(UUID householdId) {
    User currentUser = userService.getCurrentUser();
    Household household = householdRepo.findById(householdId)
        .orElseThrow(HouseholdNotFoundException::new);

    if (!householdMemberService.isMemberOfHousehold(currentUser, household)) {
      throw new IllegalArgumentException("Not a member of this household");
    }

    if (isOwner(currentUser, household)) {
      throw new IllegalArgumentException("Owner cannot leave the household");
    }

    setNewActiveHousehold(currentUser, household);

    householdMemberService.removeMember(household, currentUser);
  }

  private boolean isOwner(User user, Household household) {
    return household.getOwner().getId().equals(user.getId());
  }

  /**
   * Sets the active household for the current user to null if the user is leaving the household. If
   * the user is member of another household, a random one is set as active.
   *
   * @param household The household being left
   */
  private void setNewActiveHousehold(User user, Household household) {
    if (user.getActiveHousehold() != null && user.getActiveHousehold().getId()
        .equals(household.getId())) {
      HouseholdMember hm = householdMemberService.getHouseholdsByUser(user).stream()
          .filter(h -> !h.getHousehold().getId().equals(household.getId())).findFirst()
          .orElse(null);
      Household newHousehold = hm == null ? null : hm.getHousehold();

      userService.updateActiveHousehold(newHousehold);
    }
  }

  public List<User> getHouseholdOwners() {
    return householdRepo.findAll().stream().map(Household::getOwner).toList();
  }

  /**
   * Deletes a household. Only the owner can delete the household.
   *
   * @param id The ID of the household to delete
   */
  @Transactional
  public void deleteHousehold(UUID id) {
    User currentUser = userService.getCurrentUser();
    Household household = householdRepo.findById(id)
        .orElseThrow(HouseholdNotFoundException::new);

    // Check if current user is the owner
    if (!isOwner(currentUser, household)) {
      throw new IllegalArgumentException("Only the owner can delete a household");
    }

    // For each user who has this household as their active household
    if (household.getActiveUsers() != null && !household.getActiveUsers().isEmpty()) {
      for (User user : household.getActiveUsers()) {
        // Find another household the user belongs to (if any)
        List<HouseholdMember> userMemberships = householdMemberService.getHouseholdsByUser(user);
        HouseholdMember alternativeHouseholdMember = userMemberships.stream()
            .filter(member -> !member.getHousehold().getId().equals(id))
            .findFirst()
            .orElse(null);

        // If user has another household, set it as active
        // Otherwise, set active household to null
        Household newActiveHousehold = (alternativeHouseholdMember != null)
            ? alternativeHouseholdMember.getHousehold()
            : null;

        user.setActiveHousehold(newActiveHousehold);
      }
    }
    householdRepo.delete(household);
  }

  /**
   * Creates a new household and sets it as active for the current user.
   *
   * @param createHouseholdRequest The request containing household details
   * @return The created household response
   */
  @Transactional
  public HouseholdResponse createHousehold(CreateHouseholdRequest createHouseholdRequest) {
    User currentUser = userService.getCurrentUser();
    Household newHousehold = createHouseholdRequest.toEntity(currentUser);

    householdRepo.save(newHousehold);
    householdMemberService.addMember(newHousehold, currentUser);

    currentUser.setActiveHousehold(newHousehold);
    userService.updateActiveHousehold(newHousehold);

    // Create default checklist items for the new household
    checklistItemService.createDefaultChecklistItems(newHousehold);

    return toHouseholdResponse(newHousehold);
  }

  /**
   * Sets the water amount for the active household of the current user. Throws an exception if the
   * user does not have an active household.
   *
   * @param liters the new water amount
   */
  @Transactional
  public void setWaterAmount(double liters) {
    User currentUser = userService.getCurrentUser();
    Household household = currentUser.getActiveHousehold();

    if (household == null) {
      throw new HouseholdNotFoundException();
    }

    household.setWaterLiters(liters);
    householdRepo.save(household);
  }

  /**
   * Retrieves a household by its ID.
   *
   * @param id The ID of the household
   * @return The household entity
   */
  @Transactional(readOnly = true)
  public Household getHouseholdById(UUID id) {
    return householdRepo.findById(id)
        .orElseThrow(HouseholdNotFoundException::new);
  }

  /**
   * Adds a member to the household. Only the owner can add members.
   *
   * @param householdId The ID of the household
   * @param userId      The ID of the user to be added
   * @return The updated household response
   */
  @Transactional
  public HouseholdResponse addMemberToHousehold(UUID householdId, UUID userId) {
    Household household = householdRepo.findById(householdId)
        .orElseThrow(HouseholdNotFoundException::new);

    User user = userService.getUserById(userId);

    if (householdMemberService.isMemberOfHousehold(user, household)) {
      throw new IllegalArgumentException("User is already a member of this household");
    }

    householdMemberService.addMember(household, user);
    return toHouseholdResponse(household);
  }

  /**
   * Removes a member from the household. Only the owner can remove members.
   *
   * @param householdId The ID of the household
   * @param userId      The ID of the user to be removed
   * @return The updated household response
   */
  @Transactional
  public HouseholdResponse removeMemberFromHousehold(UUID householdId, UUID userId) {
    Household household = householdRepo.findById(householdId)
        .orElseThrow(HouseholdNotFoundException::new);

    User user = userService.getUserById(userId);

    if (!householdMemberService.isMemberOfHousehold(user, household)) {
      throw new IllegalArgumentException("User is not a member of this household");
    }

    if (!household.getOwner().getId().equals(user.getId())) {
      throw new IllegalArgumentException("Only owner can remove members");
    }

    // If the user's active household is this one, clear it
    if (user.getActiveHousehold() != null && user.getActiveHousehold().getId()
        .equals(householdId)) {
      user.setActiveHousehold(null);
      userService.updateActiveHousehold(null);
    }

    householdMemberService.removeMember(household, user);
    return toHouseholdResponse(household);
  }

  /**
   * Retrieves all households in the system.
   *
   * @return A list of household responses
   */
  @Transactional(readOnly = true)
  public List<HouseholdResponse> getAllHouseholds() {
    List<Household> households = householdRepo.findAll();
    return households.stream()
        .map(this::toHouseholdResponse)
        .toList();
  }

  /**
   * Deletes a household and clears the active household for all members.
   *
   * @param id The ID of the household to delete
   */
  @Transactional
  public void deleteHouseholdAdmin(UUID id) {
    // Clear active household for all members
    List<HouseholdMember> members = householdMemberService.getMembers(id);
    for (HouseholdMember member : members) {
      if (member.getUser().getActiveHousehold() != null
          && member.getUser().getActiveHousehold().getId().equals(id)) {
        userService.updateActiveHousehold(null);
      }
    }

    // Delete all invites associated with this household
    Household household = householdRepo.findById(id)
        .orElseThrow(HouseholdNotFoundException::new);
    inviteRepository.deleteAll(inviteRepository.findByHousehold(household));

    // Delete all checklist items associated with this household
    checklistItemService.deleteAllByHousehold(household);

    // Delete the household (cascade will handle household members)
    householdRepo.deleteById(id);
  }

  /**
   * Adds a guest to the active household. A guest is a user who does not have a user account.
   *
   * @param guest The guest to be added
   * @return The updated household response
   */
  public HouseholdResponse addGuestToHousehold(CreateGuestRequest guest) {
    Household household = getActiveHousehold();

    guestRepository.save(Guest.builder()
        .name(guest.getName())
        .icon(guest.getIcon())
        .consumptionMultiplier(guest.getConsumptionMultiplier())
        .household(household)
        .build());

    return toHouseholdResponse(household);
  }

  /**
   * Retrieves the currently active household for the logged-in user.
   *
   * @return The active household entity
   * @throws HouseholdNotFoundException if no active household is set
   */
  public Household getActiveHousehold() {
    User currentUser = userService.getCurrentUser();
    Household household = currentUser.getActiveHousehold();

    if (household == null) {
      throw new HouseholdNotFoundException();
    }

    return household;
  }

  /**
   * Removes a guest from the active household.
   *
   * @param guestId The ID of the guest to be removed
   * @return The updated household response
   */
  public HouseholdResponse removeGuestFromHousehold(UUID guestId) {
    Guest guest = guestRepository.findById(guestId)
        .orElseThrow(() -> new IllegalArgumentException("Guest not found"));
    if (!guest.getHousehold().getOwner().equals(userService.getCurrentUser())) {
      throw new IllegalArgumentException("Only the owner can remove guests");
    }

    guestRepository.delete(guest);
    return toHouseholdResponse(guest.getHousehold());
  }

  /**
   * Updates the active household for the current user.
   *
   * @param createRequest The request containing updated household details
   * @return The updated household response
   */
  @Transactional
  public HouseholdResponse updateActiveHousehold(CreateHouseholdRequest createRequest) {
    User currentUser = userService.getCurrentUser();
    Household household = currentUser.getActiveHousehold();

    if (household == null) {
      throw new HouseholdNotFoundException();
    }

    return updateHousehold(household.getId(), createRequest);
  }

  /**
   * Updates the details of a household.
   *
   * @param id      The ID of the household to update
   * @param request The request containing updated household details
   * @return The updated household response
   */
  @Transactional
  public HouseholdResponse updateHousehold(UUID id, CreateHouseholdRequest request) {
    Household household = householdRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Household not found"));

    household.setName(request.getName());
    household.setLatitude(request.getLatitude());
    household.setLongitude(request.getLongitude());
    household.setAddress(request.getAddress());
    household.setPostalCode(request.getPostalCode());
    household.setCity(request.getCity());

    householdRepo.save(household);
    return toHouseholdResponse(household);
  }
}