package stud.ntnu.krisefikser.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.entity.FoodItem;

import java.util.List;
import java.util.UUID;

public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {
    List<FoodItem> findByHousehold(Household activeHousehold);
}
