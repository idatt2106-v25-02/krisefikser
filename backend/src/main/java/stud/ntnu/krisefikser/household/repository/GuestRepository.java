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
   * Finds a list of {@link Guest} entities associated with a given {@link Household}.
   *
   * @param household the household to find guests for
   * @return a list of guests associated with the specified household
   */
  List<Guest> findByHousehold(Household household);
}