package stud.ntnu.krisefikser.household.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * Exception thrown when a household is not found in the database. This exception is used to
 * indicate that the requested household does not exist.
 */
public class HouseholdNotFoundException extends EntityNotFoundException {

  /**
   * Constructs a new HouseholdNotFoundException with the specified detail message.
   */
  public HouseholdNotFoundException() {
    super("Household not found");
  }
}
