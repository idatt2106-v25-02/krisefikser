package stud.ntnu.krisefikser.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.krisefikser.article.entity.Article;
import stud.ntnu.krisefikser.article.repository.ArticleRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.repository.HouseholdRepo;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.EventRepository;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepo userRepo;
    private final HouseholdRepo householdRepo;
    private final ArticleRepository articleRepository;
    private final MapPointTypeRepository mapPointTypeRepository;
    private final MapPointRepository mapPointRepository;
    private final EventRepository eventRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        boolean reseedDatabase = Arrays.asList(args).contains("--reseed");

        if (reseedDatabase) {
            // Clean the database and re-seed
            cleanDatabase();
            System.out.println("Database cleaned and re-seeded successfully!");
        }
        // Seed data only if repositories are empty and not reseed mode
        else if (userRepo.count() == 0 && householdRepo.count() == 0
                && articleRepository.count() == 0 && mapPointTypeRepository.count() == 0
                && eventRepository.count() == 0) {
            seedDatabase();
            System.out.println("Data seeding completed successfully!");
        }
    }

    /**
     * Cleans the entire database by deleting all records and then re-seeds it
     */
    private void cleanDatabase() {
        // Delete all records in the correct order to avoid foreign key constraints
        System.out.println("Cleaning database...");
        mapPointRepository.deleteAll();
        mapPointTypeRepository.deleteAll();
        eventRepository.deleteAll();
        articleRepository.deleteAll();
        householdRepo.deleteAll();
        userRepo.deleteAll();

        // Re-seed the database
        seedDatabase();
    }

    /**
     * Seeds the database with initial data
     */
    private void seedDatabase() {
        System.out.println("Seeding database...");
        if (passwordEncoder != null) {
            seedUsers();
        } else {
            System.out.println("PasswordEncoder not available, skipping user seeding");
        }

        seedHouseholds();
        seedArticles();
        seedMapPointTypes();
        seedMapPoints();
        seedEvents();
    }

    private void seedUsers() {
        List<User> users = new ArrayList<>();

        // Create 25 random users
        for (int i = 0; i < 25; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            User user = User.builder()
                    .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com")
                    .password(passwordEncoder.encode("password"))
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();

            users.add(user);
        }

        // Add a test admin user
        User adminUser = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .build();

        users.add(adminUser);

        userRepo.saveAll(users);
        System.out.println("Seeded " + users.size() + " users");
    }

    private void seedHouseholds() {
        List<Household> households = new ArrayList<>();

        // Trondheim coordinates
        double minLat = 63.3900;
        double maxLat = 63.4400;
        double minLong = 10.3600;
        double maxLong = 10.4500;

        // Create 30 households with random coordinates in Trondheim
        for (int i = 0; i < 30; i++) {
            double latitude = minLat + (maxLat - minLat) * random.nextDouble();
            double longitude = minLong + (maxLong - minLong) * random.nextDouble();

            Household household = Household.builder()
                    .name(faker.address().streetName() + " " + faker.address().buildingNumber())
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            households.add(household);
        }

        householdRepo.saveAll(households);
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
        int[] levelWeights = { 60, 30, 10 }; // 60% GREEN, 30% YELLOW, 10% RED

        // Event statuses and their distribution probability
        EventStatus[] statuses = EventStatus.values();
        int[] statusWeights = { 30, 50, 20 }; // 30% UPCOMING, 50% ONGOING, 20% FINISHED

        // Create 15 events
        LocalDateTime now = LocalDateTime.now();

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
            LocalDateTime startTime;
            LocalDateTime endTime = null;

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

    /**
     * Helper method to get a random item from an array based on weighted
     * probabilities
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