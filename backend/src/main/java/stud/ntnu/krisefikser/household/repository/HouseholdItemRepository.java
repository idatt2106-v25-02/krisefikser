package stud.ntnu.krisefikser.household.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdItem;
import stud.ntnu.krisefikser.household.entity.ProductType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HouseholdItemRepository extends JpaRepository<HouseholdItem, UUID> {
    List<HouseholdItem> findByHousehold(Household household);

    Page<HouseholdItem> findByHousehold(Household household, Pageable pageable);

    List<HouseholdItem> findByHouseholdAndProductType(Household household, ProductType productType);

    Page<HouseholdItem> findByHouseholdAndProductType(Household household, ProductType productType, Pageable pageable);

    List<HouseholdItem> findByHouseholdAndExpiryDateBefore(Household household, LocalDateTime expiryDate);
}