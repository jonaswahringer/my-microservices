package com.jonny.orderservice.client;

import com.jonny.orderservice.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory/{skuCode}")
    InventoryResponse checkStock(@PathVariable String skuCode);
}
