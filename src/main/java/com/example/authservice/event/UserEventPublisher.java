package com.example.authservice.event;

import com.example.authservice.config.RabbitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final RabbitProperties properties;

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        amqpTemplate.convertAndSend(
                properties.getExchanges().getUser(),
                properties.getRoutingKeys().getUser().getCreated(),
                event);
    }
}
