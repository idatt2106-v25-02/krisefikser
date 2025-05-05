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
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return userRepository.findByEmail(email).orElseThrow(
        () -> new UserDoesNotExistException("User with email " + email + " does not exist"));
  }

  public User createUser(CreateUser data) {
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

    return userRepository.save(user);
  }

  /**
   * Updates an existing user's information.
   *
   * @param userId the UUID of the user to update
   * @param data   the updated user data
   * @return the updated User entity
   * @throws UserDoesNotExistException   if the user with the given ID does not
   *                                     exist
   * @throws EmailAlreadyExistsException if the new email is already in use by
   *                                     another user
   */
  public User updateUser(UUID userId, CreateUser data) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new UserDoesNotExistException("User with id " + userId + " does not exist"));

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

    // Check if location sharing is being disabled
    if (user.isLocationSharing() && !data.isLocationSharing()) {
      // Clear location data when disabling location sharing
      user.setLatitude(null);
      user.setLongitude(null);
    }

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
   * @throws UserDoesNotExistException if the user with the given ID does not
   *                                   exist
   */
  public void deleteUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserDoesNotExistException("User with id " + userId + " does not exist");
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
   * Checks if the current user is either an admin or the user being accessed.
   *
   * @param userId the UUID of the user being accessed
   * @return true if the current user is an admin or the user being accessed,
   *         false otherwise
   */
  public boolean isAdminOrSelf(UUID userId) {
    User currentUser = getCurrentUser();
    return currentUser.getId().equals(userId) ||
        currentUser.getRoles().stream()
            .anyMatch(
                role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);
  }

  public void updateActiveHousehold(Household household) {
    User currentUser = getCurrentUser();
    currentUser.setActiveHousehold(household);
    userRepository.save(currentUser);
  }

  public User getUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserDoesNotExistException("User with ID " + id + " does not exist"));
  }

  /**
   * Updates a user's location coordinates.
   *
   * @param userId    the UUID of the user to update
   * @param latitude  the latitude coordinate
   * @param longitude the longitude coordinate
   * @return the updated User entity
   * @throws UserDoesNotExistException if the user with the given ID does not
   *                                   exist
   */
  public User updateUserLocation(UUID userId, Double latitude, Double longitude) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new UserDoesNotExistException("User with id " + userId + " does not exist"));

    // Only update location if user has enabled location sharing
    if (user.isLocationSharing()) {
      user.setLatitude(latitude);
      user.setLongitude(longitude);
      return userRepository.save(user);
    }

    // Return the user without updating location if sharing is disabled
    return user;
  }
}
