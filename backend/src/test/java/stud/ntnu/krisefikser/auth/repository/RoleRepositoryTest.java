package stud.ntnu.krisefikser.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class RoleRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RoleRepository roleRepository;

  @Test
  void findByName_ExistingRole_ShouldReturnRole() {
    // Arrange
    Role adminRole = createAndPersistRole(RoleType.ADMIN);

    // Act
    Optional<Role> found = roleRepository.findByName(RoleType.ADMIN);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo(RoleType.ADMIN);
    assertThat(found.get().getId()).isEqualTo(adminRole.getId());
  }

  private Role createAndPersistRole(RoleType roleType) {
    Role role = new Role();
    role.setName(roleType);
    return entityManager.persist(role);
  }

  @Test
  void findByName_NonExistingRole_ShouldReturnEmpty() {
    // Act
    Optional<Role> found = roleRepository.findByName(RoleType.SUPER_ADMIN);

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByName_NullRoleType_ShouldReturnEmpty() {
    // Act
    Optional<Role> found = roleRepository.findByName(null);

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByName_MultipleRolesExist_ShouldReturnMatchingRole() {
    // Arrange
    Role adminRole = createAndPersistRole(RoleType.ADMIN);
    Role userRole = createAndPersistRole(RoleType.USER);
    Role superAdminRole = createAndPersistRole(RoleType.SUPER_ADMIN);

    // Act
    Optional<Role> found = roleRepository.findByName(RoleType.USER);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo(RoleType.USER);
    assertThat(found.get().getId()).isEqualTo(userRole.getId());
  }
}