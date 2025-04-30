package stud.ntnu.krisefikser.user.service;

import java.util.HashSet;
import java.util.Set;
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
import stud.ntnu.krisefikser.user.dto.UserDto;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * Service responsible for user management operations.
 * It handles creating new users and retrieving existing users.
 * This service is primarily used by {@link stud.ntnu.krisefikser.auth.service.AuthService} during registration
 * and authentication flows, and potentially by {@link stud.ntnu.krisefikser.auth.service.CustomUserDetailsService}
 * to load user data for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Retrieves the {@link User} entity for the currently authenticated user.
   * Uses Spring Security context to identify the user.
   *
   * @return The {@link User} entity.
   */
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return getUserByEmail(email);
  }

  /**
   * Finds a user by their email address.
   * Used by various services, including {@link stud.ntnu.krisefikser.auth.service.AuthService},
   * to fetch user details.
   *
   * @param email The email address of the user.
   * @return The corresponding {@link User} entity.
   * @throws UserDoesNotExistException if no user is found with the given email.
   */
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(
        () -> new UserDoesNotExistException("User with email " + email + " does not exist"));
  }

  /**
   * Creates a new user in the system.
   * Called by {@link stud.ntnu.krisefikser.auth.service.AuthService#register(stud.ntnu.krisefikser.auth.dto.RegisterRequest)}.
   * Assigns the default 'USER' role.
   *
   * @param data DTO containing the necessary information to create a user.
   * @return A DTO representing the newly created user.
   * @throws EmailAlreadyExistsException if the email is already in use.
   * @throws RoleNotFoundException if the default 'USER' role doesn't exist.
   */
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
        .roles(roles)
        .build();

    User savedUser = userRepository.save(user);
    return savedUser.toDto();
  }
}