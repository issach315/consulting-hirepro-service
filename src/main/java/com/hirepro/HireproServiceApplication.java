package com.hirepro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HireproServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HireproServiceApplication.class, args);
	}
}