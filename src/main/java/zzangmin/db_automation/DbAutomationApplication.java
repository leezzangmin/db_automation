package zzangmin.db_automation;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@EnableScheduling
@SpringBootApplication
public class DbAutomationApplication {

	public static void main(String[] args) {
		// Load .env file based on active profile
		String profile = System.getProperty("spring.profiles.active", "local"); // Default to local if not set
		String dotenvFilename = ".env." + profile;

		Dotenv dotenv = Dotenv.configure()
				.filename(dotenvFilename)
				.ignoreIfMissing()
				.load();

		// Prepare properties from dotenv
		Map<String, Object> dotenvProperties = new HashMap<>();
		dotenv.entries().forEach(entry -> dotenvProperties.put(entry.getKey(), entry.getValue()));

		// Create a SpringApplication instance and set the properties
		SpringApplication app = new SpringApplication(DbAutomationApplication.class);
		app.setDefaultProperties(dotenvProperties); // Set properties before context is created

		// Run the application
		app.run(args);
	}

}