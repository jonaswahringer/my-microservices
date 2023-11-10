package com.jonny.apigateway.config;

import com.jonny.apigateway.dto.UserDto;
import lombok.Data;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class RoleAuthGatewayFilterFactory extends
        AbstractGatewayFilterFactory<RoleAuthGatewayFilterFactory.Config> {

    private final WebClient.Builder webClientBuilder;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    public RoleAuthGatewayFilterFactory(WebClient.Builder webClientBuilder, Resilience4JCircuitBreakerFactory circuitBreakerFactory) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public GatewayFilter apply(Config config) {
        Resilience4JCircuitBreaker circuitBreaker = (Resilience4JCircuitBreaker) circuitBreakerFactory.create("auth");
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            Optional<String> token = extractTokenFromRequest(request);
            if(token.isPresent()) {
                Supplier<Mono<UserDto>> authSupplier = () -> getUserDto(token.get());
                var authResponse = circuitBreaker.run(authSupplier).block();

                if(authResponse.getRole() != config.getRole()){
                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
            } else {
                var response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            return chain.filter(exchange);
        };
    }

    private Mono<UserDto> getUserDto(String token) {
        return webClientBuilder.build().post()
                .uri(uriBuilder -> uriBuilder
                        .path("http://auth-service/api/token/validateToken")
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(UserDto.class);
    }

    @Data
    public static class Config {
        private String role;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        // we need this to use shortcuts in the application.yml
        return Arrays.asList("role");
    }

    private Optional<String> extractTokenFromRequest(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> authorizationHeaders = headers.get(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
            String authorizationHeader = authorizationHeaders.get(0);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
                return Optional.of(token);
            }
        }

        return Optional.empty();
    }
}
