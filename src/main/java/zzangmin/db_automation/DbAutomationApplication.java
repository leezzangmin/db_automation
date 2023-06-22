package zzangmin.db_automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DbAutomationApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbAutomationApplication.class, args);
	}

}