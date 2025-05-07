package stud.ntnu.krisefikser.reflection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stud.ntnu.krisefikser.reflection.entity.Reflection;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link Reflection} entities.
 * 
 * <p>
 * Provides methods to find reflections based on visibility and access
 * permissions.
 * </p>
 * 
 * @since 1.0
 */
@Repository
public interface ReflectionRepository extends JpaRepository<Reflection, UUID> {

    /**
     * Finds all reflections authored by a specific user.
     *
     * @param authorId the ID of the author
     * @return list of reflections by the specified author
     */
    List<Reflection> findByAuthorId(UUID authorId);

    /**
     * Finds all reflections with PUBLIC visibility.
     *
     * @return list of public reflections
     */
    List<Reflection> findByVisibility(VisibilityType visibility);

    /**
     * Finds all reflections associated with a specific household.
     *
     * @param householdId the ID of the household
     * @return list of reflections associated with the specified household
     */
    List<Reflection> findByHouseholdId(UUID householdId);

    /**
     * Finds all reflections that a user has access to, based on visibility
     * settings:
     * - PUBLIC reflections
     * - User's own PRIVATE reflections
     * - HOUSEHOLD reflections for households the user belongs to
     *
     * @param userId the ID of the user
     * @return list of reflections the user has access to
     */
    @Query("SELECT r FROM Reflection r WHERE r.visibility = 'PUBLIC' OR " +
            "(r.visibility = 'PRIVATE' AND r.author.id = :userId) OR " +
            "(r.visibility = 'HOUSEHOLD' AND r.household.id IN " +
            "(SELECT hm.household.id FROM HouseholdMember hm WHERE hm.user.id = :userId))")
    List<Reflection> findReflectionsAccessibleToUser(@Param("userId") UUID userId);
}