package stud.ntnu.krisefikser.household.service;

import jakarta.persistence.EntityNotFoundException;
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
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import stud.ntnu.krisefikser.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseholdInviteService {
    private final HouseholdInviteRepository inviteRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final HouseholdService householdService;

    private HouseholdInviteResponse toInviteResponse(HouseholdInvite invite) {
        return new HouseholdInviteResponse(
                invite.getId(),
                householdService.toHouseholdResponse(invite.getHousehold()),
                invite.getInvitedUser() != null ? invite.getInvitedUser().toDto() : null,
                invite.getInvitedEmail(),
                invite.getCreatedBy().toDto(),
                invite.getCreatedAt(),
                invite.getStatus(),
                invite.getRespondedAt());
    }

    private boolean isAdmin() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == stud.ntnu.krisefikser.auth.entity.Role.RoleType.ADMIN ||
                        role.getName() == stud.ntnu.krisefikser.auth.entity.Role.RoleType.SUPER_ADMIN);
    }

    public List<HouseholdInvite> getAllInvites() {
        return inviteRepository.findAll();
    }

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

        // Handle invite by user ID
        if (request.invitedUserId() != null) {
            User invitedUser = userRepository.findById(request.invitedUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Invited user not found"));

            if (memberRepository.existsByUserAndHousehold(invitedUser, household)) {
                throw new IllegalStateException("User is already a member of this household");
            }

            // Check if any invite exists for this user and household (regardless of status)
            Optional<HouseholdInvite> existingInviteOpt = inviteRepository.findByHouseholdAndInvitedUser(household,
                    invitedUser);

            if (existingInviteOpt.isPresent()) {
                HouseholdInvite existingInvite = existingInviteOpt.get();
                // If there's an existing invite that's not pending, update it
                if (existingInvite.getStatus() != InviteStatus.PENDING) {
                    existingInvite.setStatus(InviteStatus.PENDING);
                    existingInvite.setCreatedAt(LocalDateTime.now());
                    existingInvite.setCreatedBy(currentUser);
                    existingInvite.setRespondedAt(null);
                    return toInviteResponse(inviteRepository.save(existingInvite));
                }
                // If it's already pending, just return it
                return toInviteResponse(existingInvite);
            }

            // Create new invite if none exists
            HouseholdInvite invite = HouseholdInvite.builder()
                    .household(household)
                    .invitedUser(invitedUser)
                    .createdBy(currentUser)
                    .createdAt(LocalDateTime.now())
                    .status(InviteStatus.PENDING)
                    .build();

            return toInviteResponse(inviteRepository.save(invite));
        }

        // Handle invite by email
        // Check if any invite exists for this email and household (regardless of
        // status)
        Optional<HouseholdInvite> existingInviteOpt = inviteRepository.findByHouseholdAndInvitedEmail(household,
                request.invitedEmail());

        if (existingInviteOpt.isPresent()) {
            HouseholdInvite existingInvite = existingInviteOpt.get();
            // If there's an existing invite that's not pending, update it
            if (existingInvite.getStatus() != InviteStatus.PENDING) {
                existingInvite.setStatus(InviteStatus.PENDING);
                existingInvite.setCreatedAt(LocalDateTime.now());
                existingInvite.setCreatedBy(currentUser);
                existingInvite.setRespondedAt(null);
                return toInviteResponse(inviteRepository.save(existingInvite));
            }
            // If it's already pending, just return it
            return toInviteResponse(existingInvite);
        }

        // Create new invite if none exists
        HouseholdInvite invite = HouseholdInvite.builder()
                .household(household)
                .invitedEmail(request.invitedEmail())
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .status(InviteStatus.PENDING)
                .build();

        return toInviteResponse(inviteRepository.save(invite));
    }

    @Transactional
    public HouseholdInviteResponse acceptInvite(UUID inviteId) {
        User currentUser = userService.getCurrentUser();
        HouseholdInvite invite = getAndValidateInvite(inviteId, currentUser);

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setRespondedAt(LocalDateTime.now());

        // Create household membership
        memberRepository.save(HouseholdMember.builder()
                .user(currentUser)
                .household(invite.getHousehold())
                .build());

        return toInviteResponse(inviteRepository.save(invite));
    }

    @Transactional
    public HouseholdInviteResponse declineInvite(UUID inviteId) {
        User currentUser = userService.getCurrentUser();
        HouseholdInvite invite = getAndValidateInvite(inviteId, currentUser);

        invite.setStatus(InviteStatus.DECLINED);
        invite.setRespondedAt(LocalDateTime.now());

        return toInviteResponse(inviteRepository.save(invite));
    }

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

        return toInviteResponse(inviteRepository.save(invite));
    }

    public List<HouseholdInviteResponse> getPendingInvitesForUser() {
        User currentUser = userService.getCurrentUser();
        System.out.println("Getting pending invites for user: " + currentUser);
        return inviteRepository.findByInvitedUserAndStatus(currentUser, InviteStatus.PENDING)
                .stream()
                .map(this::toInviteResponse)
                .toList();
    }

    public List<HouseholdInviteResponse> getPendingInvitesForHousehold(UUID householdId) {
        User currentUser = userService.getCurrentUser();
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new EntityNotFoundException("Household not found"));

        // Only household members or admin can view invites
        if (!memberRepository.existsByUserAndHousehold(currentUser, household)
                && !isAdmin()) {
            throw new IllegalStateException("You don't have permission to view invites for this household");
        }

        return inviteRepository.findByHouseholdAndStatus(household, InviteStatus.PENDING)
                .stream()
                .map(this::toInviteResponse)
                .toList();
    }

    private HouseholdInvite getAndValidateInvite(UUID inviteId, User currentUser) {
        HouseholdInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        System.out.println("Invite status: " + invite.getStatus() + " for user: " + currentUser.getEmail());
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalStateException("Invite is no longer pending");
        }

        // Validate the invite is for the current user
        if (invite.getInvitedUser() != null && !invite.getInvitedUser().equals(currentUser)) {
            throw new IllegalStateException("This invite is not for you");
        }
        if (invite.getInvitedEmail() != null && !invite.getInvitedEmail().equals(currentUser.getEmail())) {
            throw new IllegalStateException("This invite is not for your email address");
        }

        return invite;
    }
}