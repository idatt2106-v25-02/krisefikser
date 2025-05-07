package stud.ntnu.krisefikser.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.enums.ChecklistCategory;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.reflection.dto.CreateReflectionRequest;
import stud.ntnu.krisefikser.reflection.dto.ReflectionResponse;
import stud.ntnu.krisefikser.reflection.dto.UpdateReflectionRequest;
import stud.ntnu.krisefikser.reflection.entity.Reflection;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;

/**
 * Factory class for creating test data objects. This centralizes test object
 * creation to reduce
 * duplication.
 */
public class TestDataFactory {

  /**
   * Creates a test user for item tests
   */
  public static User createTestUser(String email) {
    return User.builder()
        .email(email)
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();
  }

  /**
   * Creates a test household for item tests
   */
  public static Household createTestHousehold(String name, User owner) {
    return Household.builder()
        .name(name)
        .owner(owner)
        .address("Test Address")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();
  }

  /**
   * Creates a test checklist item
   */
  public static ChecklistItem createTestChecklistItem(String name, ChecklistCategory type,
      boolean checked, Household household) {
    return ChecklistItem.builder()
        .name(name)
        .type(type)
        .checked(checked)
        .icon(type.toString().toLowerCase() + "_icon")
        .household(household)
        .build();
  }

  /**
   * Creates a test food item
   */
  public static FoodItem createTestFoodItem(String name, int kcal, Instant expirationDate,
      Household household) {
    return FoodItem.builder()
        .name(name)
        .kcal(kcal)
        .icon("food_icon")
        .expirationDate(expirationDate)
        .household(household)
        .build();
  }

  /**
   * Creates a test food item request
   */
  public static CreateFoodItemRequest createTestFoodItemRequest(String name, int kcal,
      Instant expirationDate) {
    CreateFoodItemRequest request = new CreateFoodItemRequest();
    request.setName(name);
    request.setIcon("food_icon");
    request.setKcal(kcal);
    request.setExpirationDate(expirationDate);
    return request;
  }

  /**
   * Creates a test food item response
   */
  public static FoodItemResponse createTestFoodItemResponse(UUID id, String name, int kcal,
      Instant expirationDate) {
    return new FoodItemResponse(
        id,
        name,
        "food_icon",
        kcal,
        expirationDate);
  }

  /**
   * Creates a test checklist item response
   */
  public static ChecklistItemResponse createTestChecklistItemResponse(UUID id, String name,
      String icon, boolean checked) {
    return new ChecklistItemResponse(
        id,
        name,
        icon,
        checked);
  }

  /**
   * Creates a test reflection entity
   */
  public static Reflection createTestReflection(String title, String content,
      VisibilityType visibility, User author, Household household) {
    return Reflection.builder()
        .title(title)
        .content(content)
        .visibility(visibility)
        .author(author)
        .household(visibility == VisibilityType.HOUSEHOLD ? household : null)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a test create reflection request
   */
  public static CreateReflectionRequest createTestReflectionRequest(String title, String content,
      VisibilityType visibility, UUID householdId) {
    return CreateReflectionRequest.builder()
        .title(title)
        .content(content)
        .visibility(visibility)
        .householdId(visibility == VisibilityType.HOUSEHOLD ? householdId : null)
        .build();
  }

  /**
   * Creates a test update reflection request
   */
  public static UpdateReflectionRequest createTestUpdateReflectionRequest(String title, String content,
      VisibilityType visibility, UUID householdId) {
    return UpdateReflectionRequest.builder()
        .title(title)
        .content(content)
        .visibility(visibility)
        .householdId(visibility == VisibilityType.HOUSEHOLD ? householdId : null)
        .build();
  }

  /**
   * Creates a test reflection response
   */
  public static ReflectionResponse createTestReflectionResponse(UUID id, String title, String content,
      VisibilityType visibility, UUID authorId, String authorName, UUID householdId, String householdName) {
    return ReflectionResponse.builder()
        .id(id)
        .title(title)
        .content(content)
        .visibility(visibility)
        .authorId(authorId)
        .authorName(authorName)
        .householdId(visibility == VisibilityType.HOUSEHOLD ? householdId : null)
        .householdName(visibility == VisibilityType.HOUSEHOLD ? householdName : null)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}