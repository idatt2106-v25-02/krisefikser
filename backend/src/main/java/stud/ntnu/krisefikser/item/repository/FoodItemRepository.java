package stud.ntnu.krisefikser.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.entity.FoodItem;

import java.util.List;
import java.util.UUID;

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
}
