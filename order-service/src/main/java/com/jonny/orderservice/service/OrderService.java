package com.jonny.orderservice.service;

import com.jonny.orderservice.dto.InventoryResponse;
import com.jonny.orderservice.dto.OrderLineItemsDto;
import com.jonny.orderservice.dto.OrderRequest;
import com.jonny.orderservice.dto.OrderResponse;
import com.jonny.orderservice.model.Order;
import com.jonny.orderservice.model.OrderLineItems;
import com.jonny.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final StreamBridge streamBridge;

    public OrderResponse placeOrder(@RequestBody OrderRequest orderDto) throws Error {
        List<OrderLineItemsDto> orderLineItems = orderDto.getOrderLineItemsDtoList();
        List<String> skus = orderLineItems.stream()
                .map(OrderLineItemsDto::getSku)
                .collect(Collectors.toList());

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("inventory");

        Supplier<Mono<List<InventoryResponse>>> inventorySupplier = () -> checkStock(skus);
        List<InventoryResponse> inventory = circuitBreaker.run(inventorySupplier).block();

        boolean allInStock = true;
        Map<String, Integer> quantityMap = new HashMap<>();
        Map<String, String> infoMap = new HashMap<>();
        if(inventory.size() != orderLineItems.size()) {
            return OrderResponse.builder()
                    .message("Order Failed - Inventory and Order Items do not have the same size.")
                    .build();
        }
        for (int i=0; i<inventory.size(); i++) {
            if (inventory.get(i).getQuantity() < orderLineItems.get(i).getQuantity()) {
                allInStock = false;
                quantityMap.put(orderLineItems.get(i).getSku(), inventory.get(i).getQuantity());
            }
            infoMap.put(orderLineItems.get(i).getSku(), inventory.get(i).getDeliveryInfo());
        }

        if(!allInStock) {
            return OrderResponse.builder()
                    .message("Order Failed - one of the products is out of stock! Check the quantity and delivery info.")
                    .quantityMap(quantityMap)
                    .infoMap(infoMap)
                    .build();
        }
        Order order = new Order();
        order.setOrderLineItemsList(orderDto.getOrderLineItemsDtoList().stream().map(this::mapDtoToEntity).toList());
        order.setOrderNumber(UUID.randomUUID().toString());

        orderRepository.save(order);
        log.info("Sending Order Details with Order Id {} to Notification Service", order.getId());

        streamBridge.send("notificationEventSupplier-out-0", MessageBuilder.withPayload(order.getId()).build());

        return OrderResponse.builder()
                .message("Order Successful!")
                .quantityMap(quantityMap)
                .infoMap(infoMap)
                .build();
    }

    public Mono<List<InventoryResponse>> checkStock(List<String> skus) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("lb://inventory-service/api/inventory")
                        .queryParam("skuCodes", skus.toArray())
                        .build())
                .retrieve()
                .bodyToFlux(InventoryResponse.class)
                .collectList();
    }

    private OrderLineItems mapDtoToEntity(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSku(orderLineItemsDto.getSku());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}
