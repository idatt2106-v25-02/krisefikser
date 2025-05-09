package stud.ntnu.krisefikser.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.krisefikser.article.entity.Article;
import stud.ntnu.krisefikser.article.repository.ArticleRepository;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.household.repository.MeetingPointRepository;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.enums.ChecklistCategory;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.EventRepository;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * DataSeeder is a component that seeds the database with initial data for development and testing
 * purposes. It implements CommandLineRunner to execute the seeding process when the application
 * starts.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepo;
  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository householdMemberRepository;
  private final MeetingPointRepository meetingPointRepository;
  private final ArticleRepository articleRepository;
  private final MapPointTypeRepository mapPointTypeRepository;
  private final MapPointRepository mapPointRepository;
  private final EventRepository eventRepository;
  private final RoleRepository roleRepository;
  private final FoodItemRepository foodItemRepository;
  private final ChecklistItemRepository checklistItemRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    boolean reseedDatabase = Arrays.asList(args).contains("--reseed");

    if (reseedDatabase) {
      // Clean the database and re-seed
      cleanDatabase();
      seedDatabase();

      System.out.println("Database cleaned and re-seeded successfully!");
      return;
    }

    if (userRepo.count() == 0 && householdRepository.count() == 0
        && articleRepository.count() == 0 && mapPointTypeRepository.count() == 0
        && eventRepository.count() == 0 && roleRepository.count() == 0) {
      seedDatabase();
      System.out.println("Data seeding completed successfully!");
    }
  }

  /**
   * Cleans the entire database by deleting all records and then re-seeds it.
   */
  private void cleanDatabase() {
    // Delete all records in the correct order to avoid foreign key constraints
    System.out.println("Cleaning database...");

    // First, update all users to remove their active_household_id references
    List<User> users = userRepo.findAll();
    for (User user : users) {
      user.setActiveHousehold(null);
    }
    userRepo.saveAll(users);

    // Delete household members first
    householdMemberRepository.deleteAll();

    // Delete households
    householdRepository.deleteAll();

    // Delete meeting points before households
    meetingPointRepository.deleteAll();

    // Delete food items and checklist items
    foodItemRepository.deleteAll();
    checklistItemRepository.deleteAll();

    // Delete refresh tokens
    refreshTokenRepository.deleteAll();

    // Now we can safely delete in the correct order
    mapPointRepository.deleteAll();
    mapPointTypeRepository.deleteAll();
    eventRepository.deleteAll();
    articleRepository.deleteAll();
    householdRepository.deleteAll();
    userRepo.deleteAll();
    roleRepository.deleteAll();
  }

  private void seedDatabase() {
    seedHouseholds();
    seedArticles();
    seedMapPointTypes();
    seedMapPoints();
    seedEvents();
    seedRoles();
    seedUsers();
    seedShelters();
    seedFoodItems();
    seedChecklistItems();
  }

  private void seedHouseholds() {
    List<Household> households = new ArrayList<>();
    List<User> allUsers = userRepo.findAll();

    if (allUsers.isEmpty()) {
      System.out.println("Cannot seed households: no users found");
      return;
    }

    // Trondheim coordinates
    double minLat = 63.3900;
    double maxLat = 63.4400;
    double minLong = 10.3600;
    double maxLong = 10.4500;

    // Create 30 households with random coordinates in Trondheim
    for (int i = 0; i < 30; i++) {
      double latitude = minLat + (maxLat - minLat) * random.nextDouble();
      double longitude = minLong + (maxLong - minLong) * random.nextDouble();

      // Get a random user as owner
      User randomOwner = allUsers.get(random.nextInt(allUsers.size()));

      Household household = Household.builder()
          .name(faker.address().streetName() + " " + faker.address().buildingNumber())
          .latitude(latitude)
          .longitude(longitude)
          .address(faker.address().streetAddress())
          .postalCode(faker.address().zipCode())
          .city(faker.address().city())
          .owner(randomOwner)
          .build();

      households.add(household);
    }

    householdRepository.saveAll(households);
    System.out.println("Seeded " + households.size() + " households");
  }

  private void seedArticles() {
    List<Article> articles = new ArrayList<>();

    // Create 20 articles
    for (int i = 0; i < 20; i++) {
      // Get a random past date within last 2 years
      LocalDateTime createdAt = LocalDateTime.ofInstant(
          faker.date().past(730, TimeUnit.DAYS).toInstant(), ZoneOffset.UTC);

      Article article = Article.builder()
          .title(faker.book().title())
          .text(faker.lorem().paragraphs(3).toString().replace("[", "").replace("]", ""))
          .createdAt(createdAt)
          .imageUrl("https://picsum.photos/seed/" + i + "/800/600")
          .build();

      articles.add(article);
    }

    articleRepository.saveAll(articles);
    System.out.println("Seeded " + articles.size() + " articles");
  }

  private void seedMapPointTypes() {
    List<MapPointType> mapPointTypes = new ArrayList<>();

    // Create 10 map point types
    String[] icons = {
        "hospital.png", "shelter.png", "food.png", "water.png",
        "police.png", "fire.png", "pharmacy.png", "school.png",
        "gas.png", "grocery.png"
    };

    String[] titles = {
        "Hospital", "Emergency Shelter", "Food Distribution", "Water Supply",
        "Police Station", "Fire Station", "Pharmacy", "School/Evacuation Center",
        "Gas Station", "Grocery Store"
    };

    String[] openingTimes = {
        "24/7", "08:00-20:00", "09:00-18:00", "24/7",
        "24/7", "24/7", "08:00-22:00", "07:00-16:00",
        "06:00-00:00", "07:00-23:00"
    };

    for (int i = 0; i < 10; i++) {
      MapPointType mapPointType = MapPointType.builder()
          .title(titles[i])
          .iconUrl("/images/icons/" + icons[i])
          .description(faker.lorem().paragraph())
          .openingTime(openingTimes[i])
          .build();

      mapPointTypes.add(mapPointType);
    }

    mapPointTypeRepository.saveAll(mapPointTypes);
    System.out.println("Seeded " + mapPointTypes.size() + " map point types");
  }

  private void seedMapPoints() {
    // First get all map point types
    List<MapPointType> mapPointTypes = mapPointTypeRepository.findAll();
    if (mapPointTypes.isEmpty()) {
      System.out.println("Cannot seed map points: no map point types found");
      return;
    }

    List<MapPoint> mapPoints = new ArrayList<>();

    // Trondheim coordinates
    double minLat = 63.3900;
    double maxLat = 63.4400;
    double minLong = 10.3600;
    double maxLong = 10.4500;

    // Create 30 map points
    for (int i = 0; i < 30; i++) {
      double latitude = minLat + (maxLat - minLat) * random.nextDouble();
      double longitude = minLong + (maxLong - minLong) * random.nextDouble();

      // Select a random map point type
      MapPointType type = mapPointTypes.get(random.nextInt(mapPointTypes.size()));

      MapPoint mapPoint = MapPoint.builder()
          .latitude(latitude)
          .longitude(longitude)
          .type(type)
          .build();

      mapPoints.add(mapPoint);
    }

    mapPointRepository.saveAll(mapPoints);
    System.out.println("Seeded " + mapPoints.size() + " map points");
  }

  private void seedEvents() {
    List<Event> events = new ArrayList<>();

    // Trondheim coordinates
    double minLat = 63.3900;
    double maxLat = 63.4400;
    double minLong = 10.3600;
    double maxLong = 10.4500;

    // Event titles
    String[] eventTitles = {
        "Flood Warning", "Winter Storm Alert", "Power Outage", "Traffic Accident",
        "Gas Leak", "Water Main Break", "Public Gathering", "Construction Zone",
        "Fire Hazard", "Medical Emergency Station", "Evacuation Site", "Community Meeting",
        "Disaster Relief Center", "Road Closure", "Temporary Shelter"
    };

    // Event descriptions
    String[] eventDescriptions = {
        "Area affected by rising water levels. Avoid low-lying roads.",
        "Heavy snowfall expected. Stay indoors if possible.",
        "Temporary power disruption in this area. Crews working to restore service.",
        "Major traffic disruption. Please use alternative routes.",
        "Hazardous gas leak detected. Area cordoned off for safety.",
        "Water service disruption. Repair crews on site.",
        "Large public gathering scheduled. Expect increased foot traffic.",
        "Ongoing construction work. Be cautious of equipment and workers.",
        "Elevated fire risk due to dry conditions. No open flames.",
        "Emergency medical services stationed here for rapid response.",
        "Designated evacuation point in case of emergency.",
        "Town hall meeting to discuss emergency preparedness.",
        "Distribution point for emergency supplies and assistance.",
        "Road closed due to maintenance or hazardous conditions.",
        "Temporary accommodation available for displaced residents."
    };

    // Event levels and their distribution probability
    EventLevel[] levels = EventLevel.values();
    int[] levelWeights = {60, 30, 10}; // 60% GREEN, 30% YELLOW, 10% RED

    // Event statuses and their distribution probability
    EventStatus[] statuses = EventStatus.values();
    int[] statusWeights = {30, 50, 20}; // 30% UPCOMING, 50% ONGOING, 20% FINISHED

    // Create 15 events
    ZonedDateTime now = ZonedDateTime.now();

    for (int i = 0; i < 15; i++) {
      // Random coordinates in Trondheim
      double latitude = minLat + (maxLat - minLat) * random.nextDouble();
      double longitude = minLong + (maxLong - minLong) * random.nextDouble();

      // Random radius between 50m and 1000m
      double radius = 50 + (950 * random.nextDouble());

      // Choose a level based on weighted probability
      EventLevel level = getRandomWeightedChoice(levels, levelWeights);

      // Choose a status based on weighted probability
      EventStatus status = getRandomWeightedChoice(statuses, statusWeights);

      // Set startTime and endTime based on status
      ZonedDateTime startTime;
      ZonedDateTime endTime = null;

      switch (status) {
        case UPCOMING:
          // Start time in the future (1-14 days from now)
          startTime = now.plusDays(1 + random.nextInt(14));

          // 50% chance to have an end time (1-7 days after start)
          if (random.nextBoolean()) {
            endTime = startTime.plusDays(1 + random.nextInt(7));
          }
          break;

        case ONGOING:
          // Start time in the past (1-7 days ago)
          startTime = now.minusDays(1 + random.nextInt(7));

          // 50% chance to have an end time in the future (1-7 days from now)
          if (random.nextBoolean()) {
            endTime = now.plusDays(1 + random.nextInt(7));
          }
          break;

        case FINISHED:
          // Start time in the past (8-30 days ago)
          startTime = now.minusDays(8 + random.nextInt(23));

          // End time in the past but after start time
          int daysAfterStart = random.nextInt(7) + 1; // 1-7 days after start
          endTime = startTime.plusDays(daysAfterStart);
          break;

        default:
          startTime = now;
      }

      // Create the event
      Event event = Event.builder()
          .title(eventTitles[i])
          .description(eventDescriptions[i])
          .radius(radius)
          .latitude(latitude)
          .longitude(longitude)
          .level(level)
          .startTime(startTime)
          .endTime(endTime)
          .status(status)
          .build();

      events.add(event);
    }

    eventRepository.saveAll(events);
    System.out.println("Seeded " + events.size() + " events");
  }

  private void seedRoles() {
    List<Role> roles = new ArrayList<>();

    for (RoleType roleType : RoleType.values()) {
      Role role = new Role();
      role.setName(roleType);
      roles.add(role);
    }

    roleRepository.saveAll(roles);
    System.out.println("Seeded " + roles.size() + " roles");
  }

  private void seedUsers() {
    List<User> users = new ArrayList<>();

    Role userRole = roleRepository.findByName(RoleType.USER)
        .orElseThrow(() -> new RuntimeException("User role not found"));
    Role adminRole = roleRepository.findByName(RoleType.ADMIN)
        .orElseThrow(() -> new RuntimeException("Admin role not found"));
    Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
        .orElseThrow(() -> new RuntimeException("Super Admin role not found"));

    // Create 25 random users
    for (int i = 0; i < 25; i++) {
      String firstName = faker.name().firstName();
      String lastName = faker.name().lastName();

      User user = User.builder()
          .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com")
          .password(passwordEncoder.encode("password"))
          .firstName(firstName)
          .lastName(lastName)
          .emailVerified(true)
          .roles(new HashSet<>(List.of(userRole)))
          .build();

      users.add(user);
    }

    // Add a test admin user
    User adminUser = User.builder()
        .email("admin@example.com")
        .password(passwordEncoder.encode("admin123"))
        .firstName("Admin")
        .lastName("User")
        .emailVerified(true)
        .roles(new HashSet<>(List.of(userRole, adminRole)))
        .build();
    users.add(adminUser);

    // Add a test super admin user
    User superAdminUser = User.builder()
        .email("admin@krisefikser.app")
        .password(passwordEncoder.encode("admin123"))
        .firstName("Super")
        .lastName("Admin")
        .emailVerified(true)
        .roles(new HashSet<>(List.of(userRole, adminRole, superAdminRole)))
        .build();
    users.add(superAdminUser);

    // Add brotherman testern
    User brotherman = User.builder()
        .email("brotherman@testern.no")
        .password(passwordEncoder.encode("password"))
        .firstName("Brotherman")
        .lastName("Testern")
        .emailVerified(true)
        .build();
    users.add(brotherman);

    userRepo.saveAll(users);
    System.out.println("Seeded " + users.size() + " users");
  }

  private void seedShelters() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      InputStream inputStream = getClass().getResourceAsStream("/shelters.json");

      if (inputStream == null) {
        System.out.println("Could not find shelters.json file");
        return;
      }

      // Read the root object that contains the features array
      JsonNode rootNode = objectMapper.readTree(inputStream);
      JsonNode featuresNode = rootNode.get("features");

      if (featuresNode == null || !featuresNode.isArray()) {
        System.out.println("Invalid shelters.json format - missing features array");
        return;
      }

      // First, ensure we have a shelter map point type
      MapPointType shelterType = mapPointTypeRepository.findByTitle("Emergency Shelter")
          .orElseGet(() -> {
            MapPointType type = MapPointType.builder()
                .title("Emergency Shelter")
                .iconUrl("/images/icons/shelter.png")
                .description("Emergency shelter location")
                .openingTime("24/7")
                .build();
            return mapPointTypeRepository.save(type);
          });

      List<MapPoint> mapPoints = new ArrayList<>();

      for (JsonNode feature : featuresNode) {
        JsonNode geometry = feature.get("geometry");
        JsonNode coordinates = geometry.get("coordinates");

        if (coordinates != null && coordinates.isArray() && coordinates.size() >= 2) {
          MapPoint mapPoint = MapPoint.builder()
              .latitude(coordinates.get(1).asDouble())// Latitude is second in the coordinates array
              .longitude(coordinates.get(0).asDouble())// Longitude in the coordinates array
              .type(shelterType)
              .build();

          mapPoints.add(mapPoint);
        }
      }

      mapPointRepository.saveAll(mapPoints);
      System.out.println("Seeded " + mapPoints.size() + " shelters");

    } catch (IOException e) {
      System.out.println("Error reading shelters.json: " + e.getMessage());
    }
  }

  private void seedFoodItems() {
    List<FoodItem> foodItems = new ArrayList<>();
    List<Household> households = householdRepository.findAll();

    if (households.isEmpty()) {
      System.out.println("Cannot seed food items: no households found");
      return;
    }

    // Create 5 food items per household
    for (Household household : households) {
      for (int i = 0; i < 5; i++) {
        FoodItem foodItem = FoodItem.builder()
            .household(household)
            .name(faker.food().ingredient())
            .icon("food-icon-" + i)
            .kcal(random.nextInt(1000) + 100)
            .expirationDate(Instant.now().plus(random.nextInt(30), ChronoUnit.DAYS))
            .build();

        foodItems.add(foodItem);
      }
    }

    foodItemRepository.saveAll(foodItems);
    System.out.println("Seeded " + foodItems.size() + " food items");
  }

  private void seedChecklistItems() {
    List<ChecklistItem> checklistItems = new ArrayList<>();
    List<Household> households = householdRepository.findAll();

    if (households.isEmpty()) {
      System.out.println("Cannot seed checklist items: no households found");
      return;
    }

    // Create 10 checklist items per household
    for (Household household : households) {
      for (int i = 0; i < 10; i++) {
        ChecklistItem checklistItem = ChecklistItem.builder()
            .household(household)
            .name(faker.commerce().productName())
            .icon("checklist-icon-" + i)
            .checked(random.nextBoolean())
            .type(ChecklistCategory.values()[random.nextInt(ChecklistCategory.values().length)])
            .build();

        checklistItems.add(checklistItem);
      }
    }

    checklistItemRepository.saveAll(checklistItems);
    System.out.println("Seeded " + checklistItems.size() + " checklist items");
  }

  /**
   * Helper method to get a random item from an array based on weighted probabilities.
   *
   * @param items   Array of items to choose from
   * @param weights Array of weights corresponding to the items
   * @return Randomly chosen item based on weights
   */
  private <T> T getRandomWeightedChoice(T[] items, int[] weights) {
    int totalWeight = 0;
    for (int weight : weights) {
      totalWeight += weight;
    }

    int randomValue = random.nextInt(totalWeight);
    int runningTotal = 0;

    for (int i = 0; i < items.length; i++) {
      runningTotal += weights[i];
      if (randomValue < runningTotal) {
        return items[i];
      }
    }

    // Fallback (should never happen if weights are positive)
    return items[0];
  }
}