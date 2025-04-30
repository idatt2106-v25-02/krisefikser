package stud.ntnu.krisefikser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import lombok.RequiredArgsConstructor;
import stud.ntnu.krisefikser.config.FrontendConfig;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication {

	private final FrontendConfig frontendConfig;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
		BackendApplication app = context.getBean(BackendApplication.class);
		System.out.println(
				"Application started with data seeding enabled. Database will be populated with sample data if empty.");
		System.out.println("Frontend URL: " + app.frontendConfig.getUrl());
	}

}
