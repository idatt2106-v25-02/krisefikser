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
import stud.ntnu.krisefikser.user.dto.CreateUserDto;
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

  public User createUser(CreateUserDto data) {
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
  public User updateUser(UUID userId, CreateUserDto data) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserDoesNotExistException("User with id " + userId + " does not exist"));

    // Check if email is being changed and if it already exists
    if (!user.getEmail().equals(data.getEmail()) && userRepository.existsByEmail(data.getEmail())) {
      throw new EmailAlreadyExistsException(
          "User with email " + data.getEmail() + " already exists");
    }

    user.setEmail(data.getEmail());
    user.setFirstName(data.getFirstName());
    user.setLastName(data.getLastName());

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
            .anyMatch(role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);
  }
}
