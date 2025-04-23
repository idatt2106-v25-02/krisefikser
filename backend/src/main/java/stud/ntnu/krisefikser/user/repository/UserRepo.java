package stud.ntnu.krisefikser.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stud.ntnu.krisefikser.user.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
}
