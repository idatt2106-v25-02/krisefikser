package stud.ntnu.krisefikser.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for cleaning up the database between integration tests.
 * This helps ensure test isolation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final FoodItemRepository foodItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Clears the database by deleting all test data.
     * This method should be called at the beginning of integration tests.
     * The deletion order respects foreign key constraints.
     */
    @Transactional
    public void clearDatabase() {
        try {
            log.info("Clearing database for tests...");

            // Disable foreign key checks temporarily for a clean delete
            disableForeignKeyChecks();

            // Delete dependent entities first (respecting foreign key constraints)
            log.debug("Deleting checklist items...");
            checklistItemRepository.deleteAllInBatch();

            log.debug("Deleting food items...");
            foodItemRepository.deleteAllInBatch();

            // Flush to ensure all deletes are executed
            entityManager.flush();

            // Delete households
            log.debug("Deleting households...");
            householdRepository.deleteAllInBatch();

            // Delete users last
            log.debug("Deleting users...");
            userRepository.deleteAllInBatch();

            // Re-enable foreign key checks
            enableForeignKeyChecks();

            log.info("Database cleared successfully");
        } catch (Exception e) {
            // Make sure we re-enable foreign key checks even if there's an error
            enableForeignKeyChecks();

            log.error("Error clearing database", e);
            throw e;
        }
    }

    /**
     * Disable foreign key checks for H2 database to allow clean deletion
     */
    private void disableForeignKeyChecks() {
        try {
            Query query = entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE");
            query.executeUpdate();
        } catch (Exception e) {
            log.warn("Failed to disable foreign key constraints", e);
        }
    }

    /**
     * Re-enable foreign key checks for H2 database
     */
    private void enableForeignKeyChecks() {
        try {
            Query query = entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE");
            query.executeUpdate();
        } catch (Exception e) {
            log.warn("Failed to re-enable foreign key constraints", e);
        }
    }
}