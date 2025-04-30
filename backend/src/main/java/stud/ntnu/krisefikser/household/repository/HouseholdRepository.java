package stud.ntnu.krisefikser.household.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {

}