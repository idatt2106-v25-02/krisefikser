package stud.ntnu.krisefikser.user.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.exception.RoleNotFoundException;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.dto.CreateUserDto;
import stud.ntnu.krisefikser.user.dto.UserDto;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * Service responsible for user management operations.
 * Handles creating, retrieving, updating, and deleting users.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Retrieves the {@link User} entity for the currently authenticated user.
   */
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return getUserByEmail(email);
  }

  /**
   * Finds a user by their email address.
   */
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UserDoesNotExistException("User with email " + email + " does not exist"));
  }

  /**
   * Creates a new user and returns a DTO.
   */
  @Transactional
  public UserDto createUser(CreateUserDto data) {
    if (userRepository.existsByEmail(data.getEmail())) {
      throw new EmailAlreadyExistsException(
          "User with email " + data.getEmail() + " already exists");
    }
    Role userRole = roleRepository.findByName(RoleType.USER)
        .orElseThrow(RoleNotFoundException::new);
    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
    User user = User.builder()
        .email(data.getEmail())
        .password(passwordEncoder.encode(data.getPassword()))
        .firstName(data.getFirstName())
        .lastName(data.getLastName())
        .notifications(data.isNotifications())
        .emailUpdates(data.isEmailUpdates())
        .locationSharing(data.isLocationSharing())
        .roles(roles)
        .build();
    User saved = userRepository.save(user);
    log.info("Created user with email {}", saved.getEmail());
    return saved.toDto();
  }

  /**
   * Updates an existing user and returns a DTO.
   */
  @Transactional
  public UserDto updateUser(UUID userId, CreateUserDto data) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserDoesNotExistException("User with id " + userId + " does not exist"));
    if (!user.getEmail().equals(data.getEmail()) && userRepository.existsByEmail(data.getEmail())) {
      throw new EmailAlreadyExistsException(
          "User with email " + data.getEmail() + " already exists");
    }
    user.setEmail(data.getEmail());
    user.setFirstName(data.getFirstName());
    user.setLastName(data.getLastName());
    user.setNotifications(data.isNotifications());
    user.setEmailUpdates(data.isEmailUpdates());
    user.setLocationSharing(data.isLocationSharing());
    if (data.getPassword() != null && !data.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(data.getPassword()));
    }
    User updated = userRepository.save(user);
    log.info("Updated user {}", userId);
    return updated.toDto();
  }

  /**
   * Deletes a user by ID.
   */
  @Transactional
  public void deleteUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserDoesNotExistException("User with id " + userId + " does not exist"));
    userRepository.delete(user);
    log.info("Deleted user {}", userId);
  }

  /**
   * Retrieves all users as DTOs.
   */
  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(User::toDto)
        .toList();
  }

  /**
   * Checks if the current user is admin or the same as the given ID.
   */
  public boolean isAdminOrSelf(UUID userId) {
    User current = getCurrentUser();
    boolean ok = current.getId().equals(userId) ||
        current.getRoles().stream()
            .anyMatch(r -> r.getName() == RoleType.ADMIN || r.getName() == RoleType.SUPER_ADMIN);
    log.debug("isAdminOrSelf({}, {}) = {}", current.getId(), userId, ok);
    return ok;
  }

  /**
   * Updates the active household for the current user.
   */
  @Transactional
  public void updateActiveHousehold(Household household) {
    User current = getCurrentUser();
    current.setActiveHousehold(household);
    userRepository.save(current);
    log.info("Updated active household for user {}", current.getId());
  }
}
