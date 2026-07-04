package com.example.vp.consultancy.config;

import com.example.vp.consultancy.interceptor.RateLimitingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for registering custom interceptors.
 * 
 * This configuration class is responsible for:
 * - Registering the RateLimitingInterceptor
 * - Configuring which URL patterns should be rate-limited
 * 
 * The RateLimitingInterceptor will be applied to all requests
 * and will check for @RateLimit annotations on the handler methods.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final RateLimitingInterceptor rateLimitingInterceptor;

    /**
     * Constructor with dependency injection.
     * 
     * @param rateLimitingInterceptor the rate limiting interceptor to register
     */
    public WebMvcConfig(RateLimitingInterceptor rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    /**
     * Registers custom interceptors with the Spring MVC dispatcher servlet.
     * 
     * This method adds the RateLimitingInterceptor to the interceptor registry.
     * The interceptor will be applied to all requests matching the specified
     * path patterns.
     * 
     * Business Logic:
     * - Registers RateLimitingInterceptor for all API endpoints
     * - The interceptor checks if the handler method has @RateLimit annotation
     * - If annotation is present, it enforces rate limits
     * - If annotation is not present, request proceeds normally
     * 
     * @param registry the interceptor registry provided by Spring MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/swagger-ui.html");
    }
}
