package com.transport.scheduling.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO pour ajouter un arrêt à une ligne
 */
@Setter
@Getter
public class AddStopRequest {

    // Getters and Setters
    @NotNull(message = "Route ID is required")
    private UUID routeId;

    @NotBlank(message = "Stop name is required")
    private String name;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    private Integer sequenceOrder;

    private Double distanceToNext;
    private Integer durationToNext;

}