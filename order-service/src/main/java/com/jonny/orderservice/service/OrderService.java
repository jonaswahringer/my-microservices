package com.jonny.orderservice.service;

import com.jonny.orderservice.client.InventoryClient;
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
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;
    private final StreamBridge streamBridge;
    private final ExecutorService traceableExecutorService;

    public OrderResponse placeOrder(@RequestBody OrderRequest orderDto) throws Error {
        circuitBreakerFactory.configureExecutorService(traceableExecutorService);
        Resilience4JCircuitBreaker circuitBreaker = (Resilience4JCircuitBreaker) circuitBreakerFactory.create("inventory");
        java.util.function.Supplier<List<InventoryResponse>> inventorySupplier = () -> orderDto.getOrderLineItemsDtoList().stream()
                .map(lineItem -> {
                    log.info("Making Call to Inventory Service for SkuCode {}", lineItem.getSku());
                    return inventoryClient.checkStock(lineItem.getSku());
                })
                .collect(Collectors.toList());

        List<InventoryResponse> inventory = circuitBreaker.run(inventorySupplier, throwable -> handleErrorCase());

        List<OrderLineItemsDto> orderLineItems = orderDto.getOrderLineItemsDtoList();

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

    private OrderLineItems mapDtoToEntity(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSku(orderLineItemsDto.getSku());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }

    private List<InventoryResponse> handleErrorCase() {
        return null;
    }
}
