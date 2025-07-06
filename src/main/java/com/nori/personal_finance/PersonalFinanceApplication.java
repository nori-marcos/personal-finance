package com.nori.personal_finance;

import com.nori.personal_finance.configuration.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PersonalFinanceApplication {

	public static void main(final String[] args) {
    System.setProperty("spring.datasource.url", EnvConfig.get("DB_URL"));
    System.setProperty("spring.datasource.username", EnvConfig.get("DB_USER"));
    System.setProperty("spring.datasource.password", EnvConfig.get("DB_PASSWORD"));
		SpringApplication.run(PersonalFinanceApplication.class, args);
	}

}