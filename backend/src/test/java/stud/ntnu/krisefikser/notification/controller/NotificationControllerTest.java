package stud.ntnu.krisefikser.notification.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.map.controller.MapPointController;

@WebMvcTest(MapPointController.class)
@Import(TestSecurityConfig.class)
public class NotificationControllerTest {
}
