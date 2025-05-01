package stud.ntnu.krisefikser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "frontend.url=http://localhost:3000",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-algorithm",
    "jwt.access-token-expiration=60000",
    "jwt.refresh-token-expiration=86400000",
    "turnstile.secret=test-secret-key",
    "spring.security.oauth2.resourceserver.jwt.enabled=false"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Test will pass if the application context loads successfully
    }
}
