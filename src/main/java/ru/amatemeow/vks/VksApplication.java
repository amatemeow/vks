package ru.amatemeow.vks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VksApplication {

	public static void main(String[] args) {
		SpringApplication.run(VksApplication.class, args);
	}
}
