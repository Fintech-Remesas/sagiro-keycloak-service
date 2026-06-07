package com.sagiro.iamservice.application.port.output;

import com.sagiro.iamservice.application.event.PasswordRecoveryEvent;

/**
 * Output port for publishing password recovery events.
 * Implemented in the infrastructure layer using Kafka.
 * Decouples the application layer from Kafka specifics.
 */
public interface PasswordRecoveryPublisherPort {

    /**
     * Publishes a password recovery event to the message broker.
     * The communication-service will consume this to send an email or SMS to the user.
     *
     * @param event the event containing the reset token (must be delivered securely)
     */
    void publish(PasswordRecoveryEvent event);
}
