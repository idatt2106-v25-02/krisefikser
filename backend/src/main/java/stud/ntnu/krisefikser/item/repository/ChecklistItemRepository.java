package stud.ntnu.krisefikser.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.item.entity.FoodItem;

import java.util.UUID;

public interface ChecklistItemRepository extends JpaRepository<FoodItem, UUID> {
}
