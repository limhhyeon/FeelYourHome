package com.github.individualproject.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.individualproject.repository.sensorData.SensorData;
import com.github.individualproject.repository.userProduct.UserProduct;
import com.github.individualproject.web.dto.sensor.SensorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, SensorResponse> sensorResponseRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SensorResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Jackson Serializer 설정
        Jackson2JsonRedisSerializer<SensorResponse> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, SensorResponse.class);
        // Serializer 적용
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Chatting.class));
        return template;
    }
    @Bean
    public RedisTemplate<String, UserProduct> userProductRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UserProduct> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Jackson Serializer 설정
        Jackson2JsonRedisSerializer<UserProduct> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, UserProduct.class);
        // Serializer 적용
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }
}
