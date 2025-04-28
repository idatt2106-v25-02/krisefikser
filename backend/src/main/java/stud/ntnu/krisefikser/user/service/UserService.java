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

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return getUserByEmail(email);
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(
        () -> new UserDoesNotExistException("User with email " + email + " does not exist"));
  }

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