package stud.ntnu.krisefikser.item.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.entity.FoodItem;

/**
 * Repository interface for {@link FoodItem} entity operations.
 *
 * <p>This repository provides basic CRUD operations for food items through Spring Data JPA,
 * as well as custom queries for retrieving food items by household.</p>
 */
public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {

  /**
   * Finds all food items belonging to a specific household.
   *
   * @param household the household whose food items should be retrieved
   * @return a list of food items associated with the given household
   */
  List<FoodItem> findByHousehold(Household household);

  /**
   * Finds all food items that have an expiration date falling within the specified range.
   *
   * <p>This method is typically used to find items that are expiring soon or have already expired,
   * based on the provided start and end dates (inclusive of start, exclusive of end if typical for
   * 'between' queries, though Instant comparison is direct).</p>
   *
   * @param startDate the inclusive start of the expiration date range (e.g., Instant.now()).
   * @param endDate   the exclusive end of the expiration date range (e.g., Instant.now().plus(7,
   *                  ChronoUnit.DAYS)).
   * @return a list of {@link FoodItem} entities whose expiration dates fall within the specified
   * range. Returns an empty list if no items match the criteria.
   */
  List<FoodItem> findAllByExpirationDateBetween(Instant startDate, Instant endDate);
}
