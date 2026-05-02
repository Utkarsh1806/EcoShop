package com.ecoshop.cart.service.config;

import com.ecoshop.cart.service.domain.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Cart> cartRedisTemplate(RedisConnectionFactory connectionFactory,
                                                         ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Cart> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        Jackson2JsonRedisSerializer<Cart> ser = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Cart.class);
        template.setValueSerializer(ser);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(ser);
        template.afterPropertiesSet();
        return template;
    }
}
