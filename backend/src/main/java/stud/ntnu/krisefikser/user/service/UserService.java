package stud.ntnu.krisefikser.user.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.exception.RoleNotFoundException;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UserNotFoundException;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * Service class for managing users. This class provides methods to create, update, delete, and
 * retrieve users.
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates a new user in the system.
   *
   * @param data the user data
   * @return the created User entity
   * @throws EmailAlreadyExistsException if the email is already in use by another user
   */
  public User createUser(CreateUser data) {
    if (userRepository.existsByEmail(data.getEmail())) {
      throw new EmailAlreadyExistsException(
          "User with email " + data.getEmail() + " already exists");
    }

    Role userRole =
        roleRepository.findByName(RoleType.USER).orElseThrow(RoleNotFoundException::new);

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    User user =
        User.builder().email(data.getEmail()).password(passwordEncoder.encode(data.getPassword()))
            .firstName(data.getFirstName()).lastName(data.getLastName())
            .notifications(data.isNotifications()).emailUpdates(data.isEmailUpdates())
            .locationSharing(data.isLocationSharing()).roles(roles).passwordRetries(0)
            .lockedUntil(null).build();

    return userRepository.save(user);
  }

  /**
   * Updates the password of an existing user.
   *
   * @param userId      the UUID of the user to update
   * @param newPassword the new password
   * @return the updated User entity
   * @throws UserNotFoundException if the user with the given ID does not exist
   */
  public User updatePassword(UUID userId, String newPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    user.setPassword(passwordEncoder.encode(newPassword));
    return userRepository.save(user);
  }

  /**
   * Updates an existing user's information.
   *
   * @param userId the UUID of the user to update
   * @param data   the updated user data
   * @return the updated User entity
   * @throws UserNotFoundException       if the user with the given ID does not exist
   * @throws EmailAlreadyExistsException if the new email is already in use by another user
   */
  public User updateUser(UUID userId, CreateUser data) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

    // Check if email is being changed and if it already exists
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

    // Only update password if it's provided
    if (data.getPassword() != null && !data.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(data.getPassword()));
    }

    return userRepository.save(user);
  }

  /**
   * Deletes a user from the system.
   *
   * @param userId the UUID of the user to delete
   * @throws UserNotFoundException if the user with the given ID does not exist
   */
  public void deleteUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    userRepository.deleteById(userId);
  }

  /**
   * Retrieves all users in the system.
   *
   * @return a list of all User entities
   */
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  /**
   * Retrieves all admins in the system.
   *
   * @return a list of all User entities
   */
  public List<User> getAllAdmins() {
    return userRepository.findByRolesName(RoleType.ADMIN);
  }

  /**
   * Checks if the current user is either an admin or the user being accessed.
   *
   * @param userId the UUID of the user being accessed
   * @return true if the current user is an admin or the user being accessed,
   * false otherwise
   */
  public boolean isAdminOrSelf(UUID userId) {
    User currentUser = getCurrentUser();
    return currentUser.getId().equals(userId)
        || currentUser.getRoles().stream()
        .anyMatch(
            role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);
  }

  /**
   * Retrieves the currently authenticated user.
   *
   * @return the User entity of the currently authenticated user
   * @throws UserNotFoundException if the user is not found in the database
   */
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
  }

  /**
   * Updates the active household of the currently authenticated user.
   *
   * @param household the new active household to set
   */
  public void updateActiveHousehold(Household household) {
    User currentUser = getCurrentUser();
    currentUser.setActiveHousehold(household);
    userRepository.save(currentUser);
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param id the UUID of the user to retrieve
   * @return the User entity with the specified ID
   * @throws UserNotFoundException if the user with the given ID does not exist
   */
  public User getUserById(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }

  /**
   * Retrieves a user by their mail.
   *
   * @param email the mail of the user to retrieve
   * @return the User entity with the specified mail
   * @throws UserNotFoundException if the user with the given ID does not exist
   */
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(
        () -> new UserNotFoundException("User with email " + email + " does not exist"));
  }
}
