package stud.ntnu.krisefikser.common;

import java.time.Instant;
import java.util.UUID;

import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.enums.ChecklistType;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Factory class for creating test data objects.
 * This centralizes test object creation to reduce duplication.
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
    public static ChecklistItem createTestChecklistItem(String name, ChecklistType type, boolean checked, Household household) {
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
    public static FoodItem createTestFoodItem(String name, int kcal, Instant expirationDate, Household household) {
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
    public static CreateFoodItemRequest createTestFoodItemRequest(String name, int kcal, Instant expirationDate) {
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
    public static FoodItemResponse createTestFoodItemResponse(UUID id, String name, int kcal, Instant expirationDate) {
        return new FoodItemResponse(
            id,
            name,
            "food_icon",
            kcal,
            expirationDate
        );
    }

    /**
     * Creates a test checklist item response
     */
    public static ChecklistItemResponse createTestChecklistItemResponse(UUID id, String name, String icon, boolean checked) {
        return new ChecklistItemResponse(
            id,
            name,
            icon,
            checked
        );
    }
} 