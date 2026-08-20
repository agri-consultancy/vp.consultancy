package com.example.vp.consultancy.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (StringUtils.hasText(redisPassword)) {
            config.setPassword(RedisPassword.of(redisPassword));
        }
        return new LettuceConnectionFactory(config);
    }

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        ObjectMapper redisObjectMapper = createRedisObjectMapper();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper redisObjectMapper = createRedisObjectMapper();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("cropsAll", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("cropById", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("consultantFarmers", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("consultantCropsWithVarieties", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("consultantActiveSummary", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("farmersPortfolio", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("farmerProfileDetail", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("farmerCropsByFarmerId", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("currentFarmerCrops", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("currentFarmerProfile", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("consultantAdvertisements", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("allConsultantAdvertisements", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("schedulePreview", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("farmerScheduleByVariety", defaultConfig.entryTtl(Duration.ofMinutes(3)));
        cacheConfigs.put("templatesByConsultant", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("activeTemplatesByConsultantAndVariety", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("templateById", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("templateDayById", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("templateTaskById", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
