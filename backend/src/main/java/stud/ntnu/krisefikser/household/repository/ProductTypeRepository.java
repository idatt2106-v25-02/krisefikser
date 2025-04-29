package stud.ntnu.krisefikser.household.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.ProductType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductTypeRepository extends JpaRepository<ProductType, UUID> {
    Optional<ProductType> findByName(String name);

    List<ProductType> findByNameContainingIgnoreCase(String name);
}