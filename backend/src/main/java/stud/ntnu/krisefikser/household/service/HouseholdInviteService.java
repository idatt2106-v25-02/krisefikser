package stud.ntnu.krisefikser.household.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdInviteResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdInviteRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.notification.entity.NotificationType;
import stud.ntnu.krisefikser.notification.service.NotificationService;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service class that handles business logic related to HouseholdInvite entities.
 * Provides operations for creating, accepting, declining, and cancelling household invitations
 * and integrates with notification services to inform users of invitation status changes.
 *
 * <p>This service acts as an intermediary between the controllers and the data access layer,
 * adding business validation and integrating with the notification service for user updates.
 * It ensures that only authorized users can perform actions on invitations and maintains
 * the integrity of the household membership system.</p>
 *
 * @author NTNU Krisefikser Team
 * @see HouseholdInvite
 * @see HouseholdInviteRepository
 * @see NotificationService
 */
@Service
@RequiredArgsConstructor
public class HouseholdInviteService {

  /**
   * Repository for HouseholdInvite entity operations. Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final HouseholdInviteRepository inviteRepository;

  /**
   * Repository for Household entity operations. Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final HouseholdRepository householdRepository;

  /**
   * Repository for HouseholdMember entity operations. Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final HouseholdMemberRepository memberRepository;

  /**
   * Repository for User entity operations. Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final UserRepository userRepository;

  /**
   * Service for accessing the current authenticated user.
   * Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final UserService userService;

  /**
   * Service for household related operations. Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final HouseholdService householdService;

  /**
   * Service for creating and sending notifications to users.
   * Automatically injected through constructor
   * by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final NotificationService notificationService;

  /**
   * Converts a HouseholdInvite entity to its response DTO representation.
   *
   * @param invite The household invite entity to convert
   * @return The DTO representation of the invite
   */
  private HouseholdInviteResponse toInviteResponse(HouseholdInvite invite) {
    return new HouseholdInviteResponse(invite.getId(),
        householdService.toHouseholdResponse(invite.getHousehold()),
        invite.getInvitedUser() != null ? invite.getInvitedUser().toDto() : null,
        invite.getInvitedEmail(), invite.getCreatedBy().toDto(), invite.getCreatedAt(),
        invite.getStatus(), invite.getRespondedAt());
  }

  /**
   * Checks if the current user has administrative privileges.
   *
   * @return true if the user has ADMIN or SUPER_ADMIN role, false otherwise
   */
  private boolean isAdmin() {
    User currentUser = userService.getCurrentUser();
    return currentUser.getRoles().stream().anyMatch(
        role -> role.getName() == stud.ntnu.krisefikser.auth.entity.Role.RoleType.ADMIN
            || role.getName() == stud.ntnu.krisefikser.auth.entity.Role.RoleType.SUPER_ADMIN);
  }

  /**
   * Retrieves all household invites from the database.
   *
   * @return A list containing all household invites in the database
   */
  public List<HouseholdInvite> getAllInvites() {
    return inviteRepository.findAll();
  }

  /**
   * Creates a new household invitation either by user ID or email address.
   *
   * <p>This method validates that the current user is a member of the household, checks if the
   * invited user is already a member, and handles cases where an invite might already exist.
   * It also creates appropriate notifications for the invited users.</p>
   *
   * @param request The request containing information about the invitation to create
   * @return The created invitation as a response DTO
   * @throws IllegalArgumentException If neither user ID nor email is provided
   * @throws EntityNotFoundException  If the household or invited user is not found
   * @throws IllegalStateException    If the current user is not a member of the household or the
   *                                  invited user is already a member
   */
  @Transactional
  public HouseholdInviteResponse createInvite(CreateHouseholdInviteRequest request) {
    if (request.invitedUserId() == null && request.invitedEmail() == null) {
      throw new IllegalArgumentException(
          "Either invitedUserId or invitedEmail must be provided, not both or neither");
    }

    User currentUser = userService.getCurrentUser();
    Household household = householdRepository.findById(request.householdId())
        .orElseThrow(() -> new EntityNotFoundException("Household not found"));

    // Verify current user is member of household
    if (!memberRepository.existsByUserAndHousehold(currentUser, household)) {
      throw new IllegalStateException("You must be a member of the household to invite others");
    }

    HouseholdInvite invite;
    // Handle invite by user ID
    if (request.invitedUserId() != null) {
      User invitedUser = userRepository.findById(request.invitedUserId())
          .orElseThrow(() -> new EntityNotFoundException("Invited user not found"));

      if (memberRepository.existsByUserAndHousehold(invitedUser, household)) {
        throw new IllegalStateException("User is already a member of this household");
      }

      // Check if any invite exists for this user and household (regardless of status)
      Optional<HouseholdInvite> existingInviteOpt =
          inviteRepository.findByHouseholdAndInvitedUser(household, invitedUser);

      if (existingInviteOpt.isPresent()) {
        HouseholdInvite existingInvite = existingInviteOpt.get();
        // If there's an existing invite that's not pending, update it
        if (existingInvite.getStatus() != InviteStatus.PENDING) {
          existingInvite.setStatus(InviteStatus.PENDING);
          existingInvite.setCreatedAt(LocalDateTime.now());
          existingInvite.setCreatedBy(currentUser);
          existingInvite.setRespondedAt(null);
          invite = inviteRepository.save(existingInvite);
        } else {
          // If it's already pending, just return it
          return toInviteResponse(existingInvite);
        }
      } else {
        // Create new invite if none exists
        invite = HouseholdInvite.builder().household(household).invitedUser(invitedUser)
            .createdBy(currentUser).createdAt(LocalDateTime.now()).status(InviteStatus.PENDING)
            .build();
        invite = inviteRepository.save(invite);
      }

      // Create notification for the invited user
      Notification notification =
          Notification.builder().user(invitedUser).type(NotificationType.INVITE)
              .title("Husholdningsinvitasjon").message(
                  "Du har blitt invitert til å bli med i " + household.getName() + " av "
                      + currentUser.getFirstName() + " " + currentUser.getLastName()).invite(invite)
              .household(household).build();
      notificationService.createNotification(notification);

      return toInviteResponse(invite);
    }

    // Handle invite by email
    // Check if any invite exists for this email and household (regardless of status)
    Optional<HouseholdInvite> existingInviteOpt =
        inviteRepository.findByHouseholdAndInvitedEmail(household, request.invitedEmail());

    if (existingInviteOpt.isPresent()) {
      HouseholdInvite existingInvite = existingInviteOpt.get();
      // If there's an existing invite that's not pending, update it
      if (existingInvite.getStatus() != InviteStatus.PENDING) {
        existingInvite.setStatus(InviteStatus.PENDING);
        existingInvite.setCreatedAt(LocalDateTime.now());
        existingInvite.setCreatedBy(currentUser);
        existingInvite.setRespondedAt(null);
        invite = inviteRepository.save(existingInvite);
      } else {
        // If it's already pending, just return it
        return toInviteResponse(existingInvite);
      }
    } else {
      // Create new invite if none exists
      invite = HouseholdInvite.builder().household(household).invitedEmail(request.invitedEmail())
          .createdBy(currentUser).createdAt(LocalDateTime.now()).status(InviteStatus.PENDING)
          .build();
      invite = inviteRepository.save(invite);
    }

    // Find if there's a user with this email - if so, create notification
    Optional<User> userWithEmailOpt = userRepository.findByEmail(request.invitedEmail());
    if (userWithEmailOpt.isPresent()) {
      User userWithEmail = userWithEmailOpt.get();
      Notification notification =
          Notification.builder().user(userWithEmail).type(NotificationType.INVITE)
              .title("Husholdningsinvitasjon").message(
                  "Du har blitt invitert til å bli med i " + household.getName() + " av "
                      + currentUser.getFirstName() + " " + currentUser.getLastName()).invite(invite)
              .household(household).build();
      notificationService.createNotification(notification);
    }

    return toInviteResponse(invite);
  }

  /**
   * Accepts a household invitation, creating a household membership for the current user.
   *
   * <p>This method validates that the invitation exists, is in pending state, and is intended for
   * the current user. It then creates a household membership and sends a notification to the
   * invitation creator.</p>
   *
   * @param inviteId The ID of the invitation to accept
   * @return The updated invitation as a response DTO
   * @throws EntityNotFoundException If the invitation is not found
   * @throws IllegalStateException   If the invitation is not pending or not
   *                                 intended for the current user
   */
  @Transactional
  public HouseholdInviteResponse acceptInvite(UUID inviteId) {
    User currentUser = userService.getCurrentUser();
    HouseholdInvite invite = getAndValidateInvite(inviteId, currentUser);

    invite.setStatus(InviteStatus.ACCEPTED);
    invite.setRespondedAt(LocalDateTime.now());

    // Create household membership
    memberRepository.save(
        HouseholdMember.builder().user(currentUser).household(invite.getHousehold()).build());

    // Create notification for the invite creator
    Notification notification =
        Notification.builder().user(invite.getCreatedBy()).type(NotificationType.INVITE)
            .title("Invitasjon akseptert").message(
                currentUser.getFirstName() + " " + currentUser.getLastName()
                    + " har akseptert invitasjonen din til å bli med i "
                    + invite.getHousehold().getName())
            .invite(invite).household(invite.getHousehold())
            .build();
    notificationService.createNotification(notification);

    return toInviteResponse(inviteRepository.save(invite));
  }

  /**
   * Declines a household invitation.
   *
   * <p>This method validates that the invitation exists, is in pending state, and is intended for
   * the current user. It then updates the invitation status and sends a notification to the
   * invitation creator.</p>
   *
   * @param inviteId The ID of the invitation to decline
   * @return The updated invitation as a response DTO
   * @throws EntityNotFoundException If the invitation is not found
   * @throws IllegalStateException   If the invitation is not pending or not
   *                                 intended for the current user
   */
  @Transactional
  public HouseholdInviteResponse declineInvite(UUID inviteId) {
    User currentUser = userService.getCurrentUser();
    HouseholdInvite invite = getAndValidateInvite(inviteId, currentUser);

    invite.setStatus(InviteStatus.DECLINED);
    invite.setRespondedAt(LocalDateTime.now());

    // Create notification for the invite creator
    Notification notification =
        Notification.builder().user(invite.getCreatedBy()).type(NotificationType.INVITE)
            .title("Invitasjon avslått").message(
                currentUser.getFirstName() + " " + currentUser.getLastName()
                    + " har avslått invitasjonen din til å bli med i "
                    + invite.getHousehold().getName())
            .invite(invite).household(invite.getHousehold()).build();
    notificationService.createNotification(notification);

    return toInviteResponse(inviteRepository.save(invite));
  }

  /**
   * Cancels a pending household invitation.
   *
   * <p>This method verifies that the current user is either a member of the household or an admin,
   * and that the invitation is in pending state. It then updates the invitation status and sends
   * notifications to relevant users.</p>
   *
   * @param inviteId The ID of the invitation to cancel
   * @return The updated invitation as a response DTO
   * @throws EntityNotFoundException If the invitation is not found
   * @throws IllegalStateException   If the user doesn't have permission
   *                                 or the invitation is not pending
   */
  @Transactional
  public HouseholdInviteResponse cancelInvite(UUID inviteId) {
    User currentUser = userService.getCurrentUser();
    HouseholdInvite invite = inviteRepository.findById(inviteId)
        .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

    // Only household members or admin can cancel invites
    if (!memberRepository.existsByUserAndHousehold(currentUser, invite.getHousehold())
        && !isAdmin()) {
      throw new IllegalStateException("You don't have permission to cancel this invite");
    }

    if (invite.getStatus() != InviteStatus.PENDING) {
      throw new IllegalStateException("Can only cancel pending invites");
    }

    invite.setStatus(InviteStatus.CANCELLED);
    invite.setRespondedAt(LocalDateTime.now());

    // Notify the invited user if it's a user invite
    if (invite.getInvitedUser() != null) {
      Notification notification =
          Notification.builder().user(invite.getInvitedUser()).type(NotificationType.INVITE)
              .title("Invitasjon kansellert").message(
                  "Din invitasjon til å bli med i " + invite.getHousehold().getName()
                      + " har blitt kansellert")
              .invite(invite).household(invite.getHousehold()).build();
      notificationService.createNotification(notification);
    } else if (invite.getInvitedEmail() != null) {
      // If it's an email invite, check if there's a user with this email
      Optional<User> userWithEmailOpt = userRepository.findByEmail(invite.getInvitedEmail());
      if (userWithEmailOpt.isPresent()) {
        Notification notification =
            Notification.builder().user(userWithEmailOpt.get()).type(NotificationType.INVITE)
                .title("Invitasjon kansellert").message(
                    "Din invitasjon til å bli med i " + invite.getHousehold().getName()
                        + " har blitt kansellert").invite(invite).household(invite.getHousehold())
                .build();
        notificationService.createNotification(notification);
      }
    }

    return toInviteResponse(inviteRepository.save(invite));
  }

  /**
   * Retrieves all pending invitations for the current user.
   *
   * <p>This method finds invitations that are either addressed to the user's ID or email address
   * and are in the PENDING state.</p>
   *
   * @return List of pending invitations as response DTOs
   */
  public List<HouseholdInviteResponse> getPendingInvitesForUser() {
    User currentUser = userService.getCurrentUser();
    return inviteRepository.findByInvitedUserOrInvitedEmailAndStatus(currentUser,
        currentUser.getEmail(), InviteStatus.PENDING).stream().map(this::toInviteResponse).toList();
  }

  /**
   * Retrieves all pending invitations for a specific household.
   *
   * <p>This method verifies that the current user is either a member of the household or an admin
   * before returning the pending invitations for that household.</p>
   *
   * @param householdId The ID of the household to get invitations for
   * @return List of pending invitations as response DTOs
   * @throws EntityNotFoundException If the household is not found
   * @throws IllegalStateException   If the user doesn't have permission to view invitations
   */
  public List<HouseholdInviteResponse> getPendingInvitesForHousehold(UUID householdId) {
    User currentUser = userService.getCurrentUser();
    Household household = householdRepository.findById(householdId)
        .orElseThrow(() -> new EntityNotFoundException("Household not found"));

    // Only household members or admin can view invites
    if (!memberRepository.existsByUserAndHousehold(currentUser, household) && !isAdmin()) {
      throw new IllegalStateException(
          "You don't have permission to view invites for this household");
    }

    return inviteRepository.findByHouseholdAndStatus(household, InviteStatus.PENDING).stream()
        .map(this::toInviteResponse).toList();
  }

  /**
   * Validates that an invitation exists, is in pending state, and is intended for the current user.
   *
   * <p>This helper method performs common validation logic
   * used by both acceptInvite and declineInvite.</p>
   *
   * @param inviteId    The ID of the invitation to validate
   * @param currentUser The current authenticated user
   * @return The validated household invitation entity
   * @throws EntityNotFoundException If the invitation is not found
   * @throws IllegalStateException   If the invitation is not pending
   *                                 or not intended for the current user
   */
  private HouseholdInvite getAndValidateInvite(UUID inviteId, User currentUser) {
    HouseholdInvite invite = inviteRepository.findById(inviteId)
        .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

    System.out.println(
        "Invite status: " + invite.getStatus() + " for user: " + currentUser.getEmail());
    if (invite.getStatus() != InviteStatus.PENDING) {
      throw new IllegalStateException("Invite is no longer pending");
    }

    // Validate the invite is for the current user
    if (invite.getInvitedUser() != null && (!invite.getInvitedUser().equals(currentUser)
        && !invite.getInvitedEmail().equals(currentUser.getEmail()))) {
      throw new IllegalStateException("This invite is not for you");
    }
    if (invite.getInvitedEmail() != null
        && !invite.getInvitedEmail().equals(currentUser.getEmail())) {
      throw new IllegalStateException("This invite is not for your email address");
    }

    return invite;
  }
}