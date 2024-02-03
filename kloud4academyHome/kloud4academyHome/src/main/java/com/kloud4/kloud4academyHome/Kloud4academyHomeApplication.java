package com.kloud4.kloud4academyHome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class Kloud4academyHomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Kloud4academyHomeApplication.class, args);
	}
	
}
