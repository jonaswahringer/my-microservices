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
				Inventory inventory1 = new Inventory();
				inventory1.setSkuCode("IPHONE 13");
				inventory1.setQuantity(10);
				inventory1.setDeliveryInfo("No further Info provided.");

				Inventory inventory2 = new Inventory();
				inventory2.setSkuCode("IPHONE 14");
				inventory2.setQuantity(10);
				inventory2.setDeliveryInfo("Deliveries from this supplier typically take up to 2 months.");

				inventoryRepository.save(inventory1);
				inventoryRepository.save(inventory2);
		};
	}

}
