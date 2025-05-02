package stud.ntnu.krisefikser.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseCleanupService {

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  public void clearDatabase() {
    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

    // Get list of all tables
    List<String> tableNames = entityManager.createNativeQuery(
        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'krisefikser'"
    ).getResultList();

    // Truncate each table
    for (String tableName : tableNames) {
      if (!tableName.equals("hibernate_sequence") && !tableName.equals("flyway_schema_history")) {
        entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
      }
    }

    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
  }
}