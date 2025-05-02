package stud.ntnu.krisefikser.household.exception;

import jakarta.persistence.EntityNotFoundException;

public class HouseholdNotFoundException extends EntityNotFoundException {

  public HouseholdNotFoundException() {
    super("Household not found");
  }
}
