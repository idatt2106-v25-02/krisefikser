package stud.ntnu.krisefikser.household.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Guest;
import stud.ntnu.krisefikser.household.entity.Household;

/**
 * Repository interface for managing {@link Guest} entities.
 *
 * <p>This interface extends {@link JpaRepository} to provide CRUD operations and custom queries
 * for {@link Guest} entities.
 */
public interface GuestRepository extends JpaRepository<Guest, UUID> {

  /**
   * Finds a {@link Guest} by its ID and the associated {@link Household}.
   *
   * @param household the household to which the guest belongs
   * @return the guest with the specified ID and household, or null if not found
   */
  List<Guest> findAllByHousehold(Household household);
}