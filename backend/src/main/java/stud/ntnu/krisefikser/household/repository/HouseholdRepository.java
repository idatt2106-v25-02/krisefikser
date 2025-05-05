package stud.ntnu.krisefikser.household.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;

/**
 * Repository interface for managing {@link Household} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations and custom queries for
 * {@link Household} entities.
 */
public interface HouseholdRepository extends JpaRepository<Household, UUID> {

}