package stud.ntnu.krisefikser.auth.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;

public interface RoleRepository extends CrudRepository<Role, Long> {

  Optional<Role> findByName(RoleType name);
}