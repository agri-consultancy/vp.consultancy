package com.example.vp.consultancy.interceptor;

import com.example.vp.consultancy.annotation.RateLimit;
import com.example.vp.consultancy.exception.RateLimitExceededException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Spring MVC interceptor that implements rate limiting for endpoints
 * annotated with @RateLimit annotation.
 * 
 * This interceptor uses Google Guava's RateLimiter to implement
 * a token bucket algorithm for rate limiting. Each unique client
 * (identified by IP address) gets its own rate limiter.
 * 
 * Business Logic:
 * - Extracts client IP from the request
 * - Checks if the handler method has @RateLimit annotation
 * - If annotation exists, verifies that the client hasn't exceeded the limit
 * - Throws RateLimitExceededException if limit is exceeded
 * - Uses a cache to store rate limiters per client
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    /**
     * LoadingCache that maintains a RateLimiter for each unique client (IP).
     * Rate limiters are automatically created and cached based on client IP.
     * Each rate limiter manages the token bucket for that specific client.
     */
    private final LoadingCache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    return RateLimiter.create(1.0);
                }
            });

    /**
     * Pre-handles HTTP requests to enforce rate limiting.
     * 
     * This method is called before the actual handler method is invoked.
     * It checks if the handler has a @RateLimit annotation and if so,
     * validates that the client hasn't exceeded the specified rate limit.
     * 
     * @param request  the HTTP request object
     * @param response the HTTP response object
     * @param handler  the handler object (typically a HandlerMethod)
     * @return true if the request should proceed, false otherwise
     * @throws RateLimitExceededException if the client has exceeded the rate limit
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws ExecutionException, RateLimitExceededException {
        
        // Only process if handler is a HandlerMethod (controller method)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Check if the method has @RateLimit annotation
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        // Extract client IP address from the request
        String clientIp = getClientIp(request);
        
        // Get or create rate limiter for this client
        // Create a custom rate limiter with the specified limit and window size
        String key = clientIp + ":" + handlerMethod.getMethod().getName();
        double permitsPerSecond = (double) rateLimit.limit() / rateLimit.windowSize();
        
        RateLimiter limiter = rateLimiters.get(key);
        if (limiter.getRate() != permitsPerSecond) {
            limiter.setRate(permitsPerSecond);
        }

        // Try to acquire a permit from the rate limiter
        if (!limiter.tryAcquire()) {
            throw new RateLimitExceededException(
                "Rate limit exceeded for endpoint. Limit: " + rateLimit.limit() + 
                " requests per " + rateLimit.windowSize() + " seconds"
            );
        }

        return true;
    }

    /**
     * Extracts the client's IP address from the HTTP request.
     * 
     * This method attempts to get the real IP address of the client,
     * accounting for proxies and load balancers that may forward the
     * original IP in the X-Forwarded-For header.
     * 
     * Priority order:
     * 1. X-Forwarded-For header (for proxied requests)
     * 2. CF-Connecting-IP header (for Cloudflare)
     * 3. X-Real-IP header (for nginx)
     * 4. request.getRemoteAddr() (fallback)
     * 
     * @param request the HTTP request object
     * @return the client's IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
