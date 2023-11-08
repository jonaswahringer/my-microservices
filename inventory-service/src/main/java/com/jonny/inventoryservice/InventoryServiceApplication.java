package com.jonny.inventoryservice;

import com.jonny.inventoryservice.model.Inventory;
import com.jonny.inventoryservice.repository.InventoryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
		return args -> {
				Inventory inventory = new Inventory();
				inventory.setSkuCode("IPHONE 13");
				inventory.setQuantity(10);

				Inventory inventory1 = new Inventory();
				inventory1.setSkuCode("IPHONE 13");
				inventory1.setQuantity(10);

				inventoryRepository.save(inventory);
				inventoryRepository.save(inventory1);
		};
	}

}
