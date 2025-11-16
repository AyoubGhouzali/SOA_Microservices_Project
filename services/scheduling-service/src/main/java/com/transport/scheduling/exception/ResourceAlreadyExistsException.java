package com.transport.scheduling.exception;

/**
 * Exception levée quand on tente de créer une ressource qui existe déjà
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}