package stud.ntnu.krisefikser.auth.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;

/**
 * Repository interface for managing {@link Role} entities. This interface extends CrudRepository to
 * provide CRUD operations and custom query methods.
 *
 * @since 1.0
 */
public interface RoleRepository extends CrudRepository<Role, Long> {

  /**
   * Finds a Role by its name.
   *
   * @param name the name of the Role
   * @return an Optional containing the Role if found, or empty if not found
   */
  Optional<Role> findByName(RoleType name);
}