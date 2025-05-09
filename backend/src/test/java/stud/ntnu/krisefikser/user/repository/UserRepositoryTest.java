package stud.ntnu.krisefikser.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @BeforeEach
  void setUp() {
    // Clear previous data
    userRepository.deleteAll();
    roleRepository.deleteAll();

    // Create roles
    Role userRole = roleRepository.save(Role.builder()
        .name(RoleType.USER)
        .build());

    Role adminRole = roleRepository.save(Role.builder()
        .name(RoleType.ADMIN)
        .build());

    // Create test users
    User user1 = User.builder()
        .email("user1@example.com")
        .password("password1")
        .roles(new HashSet<>())
        .build();
    user1.getRoles().add(userRole);
    userRepository.save(user1);

    User user2 = User.builder()
        .email("user2@example.com")
        .password("password2")
        .roles(new HashSet<>())
        .build();
    user2.getRoles().add(adminRole);
    userRepository.save(user2);

    User user3 = User.builder()
        .email("user3@example.com")
        .password("password3")
        .roles(new HashSet<>())
        .build();
    user3.getRoles().add(userRole);
    user3.getRoles().add(adminRole);
    userRepository.save(user3);
  }

  @Test
  void findByEmail_ShouldReturnUser_WhenEmailExists() {
    // Act
    Optional<User> result = userRepository.findByEmail("user1@example.com");

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("user1@example.com");
    assertThat(result.get().getPassword()).isEqualTo("password1");
  }

  @Test
  void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
    // Act
    Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
    // Act
    boolean result = userRepository.existsByEmail("user2@example.com");

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
    // Act
    boolean result = userRepository.existsByEmail("nonexistent@example.com");

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void findByRolesName_ShouldReturnUsers_WithUserRole() {
    // Act
    List<User> result = userRepository.findByRolesName(Role.RoleType.USER);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("email")
        .containsExactlyInAnyOrder("user1@example.com", "user3@example.com");
  }

  @Test
  void findByRolesName_ShouldReturnUsers_WithAdminRole() {
    // Act
    List<User> result = userRepository.findByRolesName(Role.RoleType.ADMIN);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("email")
        .containsExactlyInAnyOrder("user2@example.com", "user3@example.com");
  }

  @Test
  void findByRolesName_ShouldReturnEmptyList_WhenNoUsersHaveRole() {
    // Act
    List<User> result = userRepository.findByRolesName(RoleType.SUPER_ADMIN);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void standardCrudOperations_ShouldWorkCorrectly() {
    // Test save new user
    User newUser = User.builder()
        .email("newuser@example.com")
        .password("newpassword")
        .build();

    User saved = userRepository.save(newUser);
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEmail()).isEqualTo("newuser@example.com");

    // Test findById with existing ID
    Optional<User> found = userRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("newuser@example.com");

    // Test findById with non-existent ID
    Optional<User> notFound = userRepository.findById(UUID.randomUUID());
    assertThat(notFound).isEmpty();

    // Test update user
    saved.setEmail("updated@example.com");
    User updated = userRepository.save(saved);
    assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    assertThat(userRepository.findById(saved.getId()).get().getEmail())
        .isEqualTo("updated@example.com");

    // Test delete
    userRepository.deleteById(saved.getId());
    assertThat(userRepository.findById(saved.getId())).isEmpty();

    // Test findAll
    List<User> allUsers = userRepository.findAll();
    assertThat(allUsers).hasSize(3); // Original 3 users
    assertThat(allUsers).extracting("email")
        .containsExactlyInAnyOrder(
            "user1@example.com",
            "user2@example.com",
            "user3@example.com"
        );
  }
}