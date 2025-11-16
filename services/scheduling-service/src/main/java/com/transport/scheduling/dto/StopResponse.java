package com.transport.scheduling.dto;

import com.transport.scheduling.model.Stop;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO de réponse pour Stop
 */
@Setter
@Getter
public class StopResponse {
    // Getters and Setters
    private UUID id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer sequenceOrder;
    private Double distanceToNext;
    private Integer durationToNext;

    public static StopResponse fromEntity(Stop stop) {
        StopResponse response = new StopResponse();
        response.setId(stop.getId());
        response.setName(stop.getName());
        response.setLatitude(stop.getLatitude());
        response.setLongitude(stop.getLongitude());
        response.setSequenceOrder(stop.getSequenceOrder());
        response.setDistanceToNext(stop.getDistanceToNext());
        response.setDurationToNext(stop.getDurationToNext());
        return response;
    }

}