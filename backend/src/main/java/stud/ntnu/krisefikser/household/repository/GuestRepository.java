package stud.ntnu.krisefikser.household.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Guest;

/**
 * Repository interface for managing {@link Guest} entities.
 *
 * <p>This interface extends {@link JpaRepository} to provide CRUD operations and custom queries
 * for {@link Guest} entities.
 */
public interface GuestRepository extends JpaRepository<Guest, UUID> {

}