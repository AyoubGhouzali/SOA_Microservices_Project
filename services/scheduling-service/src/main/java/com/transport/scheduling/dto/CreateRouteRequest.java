package com.transport.scheduling.dto;

import com.transport.scheduling.model.RouteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour créer une nouvelle ligne de bus
 */
@Setter
@Getter
public class CreateRouteRequest {

    // Getters and Setters
    @NotBlank(message = "Route number is required")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Route number must be alphanumeric")
    private String routeNumber;

    @NotBlank(message = "Route name is required")
    private String name;

    private String description;

    @NotNull(message = "Route type is required")
    private RouteType type;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code")
    private String color;

}