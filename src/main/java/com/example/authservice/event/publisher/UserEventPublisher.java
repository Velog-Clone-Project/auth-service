package com.example.authservice.event.publisher;

import com.example.authservice.config.RabbitConfig;
import com.example.common.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserCreatedEvent(UserCreatedEvent userCreatedEvent) {

        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                RabbitConfig.USER_ROUTING_KEY,
                userCreatedEvent
        );

        log.info("Sent UserCreatedEvent to RabbitMQ: " + userCreatedEvent.getUserId());
    }
}
