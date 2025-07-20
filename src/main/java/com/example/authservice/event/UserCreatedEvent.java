package com.example.authservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreatedEvent {

    private String userId;
    private String email;
    private String profileName;
    private String bio;
}
