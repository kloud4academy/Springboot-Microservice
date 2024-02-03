package com.kloud4.product.productmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import repository.ProductRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = ProductRepository.class)
public class ProductmicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductmicroserviceApplication.class, args);
	}
}
