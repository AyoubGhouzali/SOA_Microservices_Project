package com.transport.apiGateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * JWT Authentication Filter for Spring Cloud Gateway
 * This filter validates JWT tokens for protected routes
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_URLS = List.of(
        "/api/users/register",
        "/api/users/login",
        "/api/users/health",
        "/api/tickets/health",
        "/api/payments/health",
        "/api/subscriptions/health",
        "/api/routes/health",
        "/api/schedules/health",
        "/api/buses/health",
        "/api/tracking/health",
        "/api/notifications/health",
        "/actuator",
        "/fallback"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        logger.debug("Processing request: {}", path);

        // Check if this is a public endpoint
        if (isPublicPath(path)) {
            logger.debug("Public path, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // Extract token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            logger.warn("Invalid JWT token for path: {}", path);
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        // Extract user information and add to request headers
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            logger.debug("Authenticated user: {} (ID: {}, Role: {})", username, userId, role);

            // Add user info to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            logger.error("Error extracting user info from token: {}", e.getMessage());
            return onError(exchange, "Invalid token claims", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Check if the path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String path) {
        Predicate<String> publicPathPredicate = PUBLIC_URLS.stream()
            .map(url -> (Predicate<String>) p -> p.startsWith(url))
            .reduce(p -> false, Predicate::or);

        return publicPathPredicate.test(path);
    }

    /**
     * Handle authentication errors
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}",
                                            message, httpStatus.value());

        return response.writeWith(Mono.just(response.bufferFactory()
            .wrap(errorResponse.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }
}
