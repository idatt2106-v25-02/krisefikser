package stud.ntnu.krisefikser.household.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.MeetingPoint;

/**
 * Repository interface for managing meeting points. Provides methods to find meeting points by
 * household ID.
 *
 * @since 1.0
 */
public interface MeetingPointRepository extends JpaRepository<MeetingPoint, UUID> {

  /**
   * Finds all meeting points associated with a specific household ID.
   *
   * @param householdId the ID of the household
   * @return a list of MeetingPoint entities
   */
  List<MeetingPoint> findByHouseholdId(UUID householdId);
}