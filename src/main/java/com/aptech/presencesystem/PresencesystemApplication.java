package com.aptech.presencesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aptech")
public class PresencesystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PresencesystemApplication.class, args);
	}

}
