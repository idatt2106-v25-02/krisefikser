package stud.ntnu.krisefikser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class BackendApplicationTests {

  @Test
  void contextLoads() {
    // This test will simply check if the Spring application context loads successfully.
  }

}
