package com.example.authservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final AmqpTemplate amqpTemplate;

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        amqpTemplate.convertAndSend("user.exchange", "user.created", event);
    }
}
