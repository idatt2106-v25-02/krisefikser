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
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.krisefikser.article.entity.Article;
import stud.ntnu.krisefikser.article.repository.ArticleRepository;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
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
import stud.ntnu.krisefikser.scenario.entity.Scenario;
import stud.ntnu.krisefikser.scenario.repository.ScenarioRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * DataSeeder seeds the database on startup when tables are empty (roles, users, households, articles,
 * map data, etc.). Active for dev, test, and prod so a fresh Railway/prod database gets baseline data;
 * per-table {@code count() == 0} guards avoid duplicating rows. Use {@code --reseed} only in dev/test.
 * Runs as a Spring Boot {@link CommandLineRunner} after the application context starts.
 */
@Component
@RequiredArgsConstructor
@Profile({"dev", "test", "prod"})
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
  private final ScenarioRepository scenarioRepository;
  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final PasswordEncoder passwordEncoder;

  /** Rotated in {@link #seedArticles()} so seeded news use Cloudinary delivery URLs (f_auto/q_auto in UI). */
  private static final String[] SEED_ARTICLE_IMAGE_URLS = {
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713518/krisefikser/articles/seed-article-hero-a.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713518/krisefikser/articles/seed-article-hero-b.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713519/krisefikser/articles/seed-article-hero-c.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713520/krisefikser/articles/seed-article-hero-d.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713520/krisefikser/articles/seed-article-hero-e.jpg",
  };

  /** Rotated in {@link #seedEvents()} for map event detail hero images. */
  private static final String[] SEED_EVENT_IMAGE_URLS = {
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713536/krisefikser/events/seed-event-hero-1.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713536/krisefikser/events/seed-event-hero-2.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713537/krisefikser/events/seed-event-hero-3.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713539/krisefikser/events/seed-event-hero-4.jpg",
    "https://res.cloudinary.com/dmoe4eqt4/image/upload/v1776713539/krisefikser/events/seed-event-hero-5.jpg",
  };

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

    if (roleRepository.count() == 0) {
      seedRoles();
    }
    if (userRepo.count() == 0) {
      seedUsers();
    }
    if (householdRepository.count() == 0) {
      seedHouseholds();
    }
    if (articleRepository.count() == 0) {
      seedArticles();
    }
    if (scenarioRepository.count() == 0) {
      seedScenarios();
    }
    if (mapPointTypeRepository.count() == 0) {
      seedMapPointTypes();
    }
    if (mapPointRepository.count() == 0) {
      seedMapPoints();
      seedShelters();
    }
    if (eventRepository.count() == 0) {
      seedEvents();
    }
    if (foodItemRepository.count() == 0) {
      seedFoodItems();
    }
    if (checklistItemRepository.count() == 0) {
      seedChecklistItems();
    }
    System.out.println("Data seeding completed successfully!");
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
    scenarioRepository.deleteAll();
    householdRepository.deleteAll();
    userRepo.deleteAll();
    roleRepository.deleteAll();
  }

  private void seedDatabase() {
    seedRoles();
    seedUsers();
    seedHouseholds();
    seedArticles();
    seedScenarios();
    seedMapPointTypes();
    seedMapPoints();
    seedEvents();
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

    for (Household household : households) {
      User owner = userRepo.findById(household.getOwner().getId())
          .orElseThrow(() -> new IllegalStateException("Household owner not found"));
      if (!householdMemberRepository.existsByUserAndHousehold(owner, household)) {
        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setUser(owner);
        householdMemberRepository.save(member);
      }
      if (owner.getActiveHousehold() == null) {
        owner.setActiveHousehold(household);
        userRepo.save(owner);
      }
    }

    userRepo.findByEmail("brotherman@testern.no").ifPresent(e2eUserRef -> {
      User e2eUser = userRepo.findById(e2eUserRef.getId()).orElseThrow();
      if (e2eUser.getActiveHousehold() != null) {
        return;
      }
      householdRepository.findAll().stream().findFirst().ifPresent(household -> {
        if (!householdMemberRepository.existsByUserAndHousehold(e2eUser, household)) {
          HouseholdMember member = new HouseholdMember();
          member.setHousehold(household);
          member.setUser(e2eUser);
          householdMemberRepository.save(member);
        }
        e2eUser.setActiveHousehold(household);
        userRepo.save(e2eUser);
      });
    });

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
          .imageUrl(SEED_ARTICLE_IMAGE_URLS[i % SEED_ARTICLE_IMAGE_URLS.length])
          .build();

      articles.add(article);
    }

    articleRepository.saveAll(articles);
    System.out.println("Seeded " + articles.size() + " articles");
  }

  private void seedMapPointTypes() {
    List<MapPointType> mapPointTypes = new ArrayList<>();

    // Create 10 map point types (iconUrl paths match frontend public/icons)
    String[] iconUrls = {
        "/icons/hospital.svg",
        "/icons/map/shelter.svg",
        "/icons/utensils.svg",
        "/icons/droplets.svg",
        "/icons/shield.svg",
        "/icons/flame.svg",
        "/icons/pill.svg",
        "/icons/school.svg",
        "/icons/fuel.svg",
        "/icons/shopping-cart.svg"
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
          .iconUrl(iconUrls[i])
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

    // Wider Trondheim / Trøndelag core (fewer overlaps than a tight downtown box)
    double minLat = 63.32;
    double maxLat = 63.50;
    double minLong = 10.20;
    double maxLong = 10.65;
    double minSeparationMeters = 1600;
    int maxAttemptsPerEvent = 250;
    int targetEventCount = 7;

    String[] eventTitles = {
        "Flood Warning", "Winter Storm Alert", "Power Outage", "Traffic Accident",
        "Gas Leak", "Water Main Break", "Public Gathering", "Construction Zone",
        "Fire Hazard", "Medical Emergency Station", "Evacuation Site", "Community Meeting",
        "Disaster Relief Center", "Road Closure", "Temporary Shelter"
    };

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

    EventLevel[] levels = EventLevel.values();
    int[] levelWeights = {60, 30, 10};

    EventStatus[] statuses = EventStatus.values();
    int[] statusWeights = {30, 50, 20};

    ZonedDateTime now = ZonedDateTime.now();

    for (int placed = 0; placed < targetEventCount; placed++) {
      Double latitude = null;
      Double longitude = null;
      for (int attempt = 0; attempt < maxAttemptsPerEvent; attempt++) {
        double lat = minLat + (maxLat - minLat) * random.nextDouble();
        double lon = minLong + (maxLong - minLong) * random.nextDouble();
        boolean farEnough = true;
        for (Event existing : events) {
          if (haversineMeters(lat, lon, existing.getLatitude(), existing.getLongitude())
              < minSeparationMeters) {
            farEnough = false;
            break;
          }
        }
        if (farEnough) {
          latitude = lat;
          longitude = lon;
          break;
        }
      }
      if (latitude == null || longitude == null) {
        break;
      }

      double radius = 80 + (520 * random.nextDouble());

      EventLevel level = getRandomWeightedChoice(levels, levelWeights);
      EventStatus status = getRandomWeightedChoice(statuses, statusWeights);

      ZonedDateTime startTime;
      ZonedDateTime endTime = null;

      switch (status) {
        case UPCOMING:
          startTime = now.plusDays(1 + random.nextInt(14));
          if (random.nextBoolean()) {
            endTime = startTime.plusDays(1 + random.nextInt(7));
          }
          break;

        case ONGOING:
          startTime = now.minusDays(1 + random.nextInt(7));
          if (random.nextBoolean()) {
            endTime = now.plusDays(1 + random.nextInt(7));
          }
          break;

        case FINISHED:
          startTime = now.minusDays(8 + random.nextInt(23));
          int daysAfterStart = random.nextInt(7) + 1;
          endTime = startTime.plusDays(daysAfterStart);
          break;

        default:
          startTime = now;
      }

      int titleIndex = Math.min(placed, eventTitles.length - 1);
      Event event = Event.builder()
          .title(eventTitles[titleIndex])
          .description(eventDescriptions[titleIndex])
          .radius(radius)
          .latitude(latitude)
          .longitude(longitude)
          .level(level)
          .startTime(startTime)
          .endTime(endTime)
          .status(status)
          .imageUrl(SEED_EVENT_IMAGE_URLS[placed % SEED_EVENT_IMAGE_URLS.length])
          .build();

      events.add(event);
    }

    eventRepository.saveAll(events);
    System.out.println("Seeded " + events.size() + " events");
  }

  private void seedScenarios() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      InputStream inputStream = getClass().getResourceAsStream("/scenarios.json");
      if (inputStream == null) {
        System.out.println("Could not find scenarios.json file");
        return;
      }
      JsonNode rootNode = objectMapper.readTree(inputStream);
      if (!rootNode.isArray()) {
        System.out.println("Invalid scenarios.json format - expected a JSON array");
        return;
      }
      List<Scenario> scenarios = new ArrayList<>();
      for (JsonNode node : rootNode) {
        if (!node.hasNonNull("title") || !node.hasNonNull("content")) {
          continue;
        }
        String cover = null;
        if (node.has("coverImageUrl") && !node.get("coverImageUrl").isNull()) {
          cover = node.get("coverImageUrl").asText();
        }
        scenarios.add(Scenario.builder()
            .title(node.get("title").asText())
            .content(node.get("content").asText())
            .coverImageUrl(cover)
            .build());
      }
      scenarioRepository.saveAll(scenarios);
      System.out.println("Seeded " + scenarios.size() + " scenarios");
    } catch (IOException e) {
      System.out.println("Error reading scenarios.json: " + e.getMessage());
    }
  }

  private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
    final double earthRadiusM = 6_371_000;
    double phi1 = Math.toRadians(lat1);
    double phi2 = Math.toRadians(lat2);
    double dPhi = Math.toRadians(lat2 - lat1);
    double dLambda = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
        + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadiusM * c;
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
        .roles(new HashSet<>(List.of(userRole)))
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
                .iconUrl("/icons/map/shelter.svg")
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