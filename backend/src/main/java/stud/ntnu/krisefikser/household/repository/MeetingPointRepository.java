package stud.ntnu.krisefikser.household.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.MeetingPoint;

import java.util.List;
import java.util.UUID;

public interface MeetingPointRepository extends JpaRepository<MeetingPoint, UUID> {
    List<MeetingPoint> findByHouseholdId(UUID householdId);
}