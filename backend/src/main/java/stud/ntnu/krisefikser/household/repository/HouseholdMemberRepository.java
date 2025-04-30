package stud.ntnu.krisefikser.household.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.user.entity.User;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {
    List<HouseholdMember> findByHouseholdId(UUID householdId);

    boolean existsByUserAndHousehold(User currentUser, Household household);

    Optional<HouseholdMember> findByHouseholdAndUser(Household household, User currentUser);

    List<HouseholdMember> findByUser(User user);
}