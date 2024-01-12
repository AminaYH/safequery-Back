package com.safequery.back.projectdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:4200")

@SpringBootApplication
public class ProjectBackDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectBackDemoApplication.class, args);
	}

}
