package stud.ntnu.krisefikser.scenario.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.scenario.entity.Scenario;

/**
 * Repository interface for {@link Scenario} entity operations.
 *
 * <p>This repository provides basic CRUD operations for scenarios through Spring Data JPA. It
 * extends
 * JpaRepository to enable standard database operations and uses UUID as the primary key type for
 * scenarios.
 * </p>
 */
public interface ScenarioRepository extends JpaRepository<Scenario, UUID> {

}