package com.example.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    // RedisTemplate<K, V>는 스프링 데이터 Redis에서 제공하는 클래스이며,
    // K = String, V = String 으로 설정하며, 키와 값 모두 문자열로 설정된 Redis 템플릿을 생성한다.

    // RedisConnectionFactory는 Redis 서버와의 연결을 관리하는 객체로,
    // 스프링부트가 RedisConnectionFactory 빈을 자동으로 생성해주므로, 이 파라미터는 자동으로 주입된다.
    // 내부적으로 Lettuce 또는 Jedis 클라이언트를 사용하여 Redis와 실제 커넥션을 생성한다.
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        // Redis 연동을 위한 템플릿 객체 생성
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Redis 연결을 위한 팩토리 설정
        template.setConnectionFactory(redisConnectionFactory);

        // 키와 값을 문자열로 직렬화하도록 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}
