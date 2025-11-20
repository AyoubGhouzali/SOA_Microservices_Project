package com.transport.notification.dto;

/**
 * Types d'événements de bus qui déclenchent des notifications
 */
public enum BusEventType {
    DELAY,              // Retard
    CANCELLATION,       // Annulation
    ROUTE_CHANGE,       // Changement d'itinéraire
    BREAKDOWN,          // Panne
    TRAFFIC_ALERT       // Alerte trafic
}
