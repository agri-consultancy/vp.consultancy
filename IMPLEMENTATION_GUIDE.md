# VP Consultancy - Complete Implementation Guide
# Architecture: Service Layer, Service Implementation, Controllers, and Configuration

This document contains all remaining code files needed to complete the application.

## Directory Structure to Create:
- src/main/java/com/example/vp/consultancy/annotation/
- src/main/java/com/example/vp/consultancy/interceptor/
- src/main/java/com/example/vp/consultancy/service/
- src/main/java/com/example/vp/consultancy/service/impl/
- src/main/java/com/example/vp/consultancy/config/
- src/main/java/com/example/vp/consultancy/controller/
- src/test/java/com/example/vp/consultancy/

## FILES CREATED SO FAR:
✓ entity/Address.java
✓ entity/User.java
✓ entity/UserRole.java
✓ entity/UserProfile.java
✓ entity/RefreshToken.java
✓ dto/LoginRequest.java
✓ dto/LoginResponse.java
✓ dto/RefreshTokenRequest.java
✓ dto/ConsultantRegistrationRequest.java
✓ dto/FarmerRegistrationRequest.java
✓ dto/UserResponse.java
✓ dto/ApiResponse.java
✓ repository/UserRepository.java
✓ repository/UserProfileRepository.java
✓ repository/AddressRepository.java
✓ repository/RefreshTokenRepository.java
✓ exception/VPException.java
✓ exception/ResourceNotFoundException.java
✓ exception/InvalidCredentialsException.java
✓ exception/DuplicateResourceException.java
✓ exception/ForbiddenException.java
✓ exception/RateLimitExceededException.java
✓ exception/GlobalExceptionHandler.java
✓ resources/db_setup.sql
✓ resources/application.properties (to be updated)

## NEXT STEPS:
1. Create annotation/RateLimit.java
2. Create interceptor/RateLimitingInterceptor.java
3. Create service/AuthenticationService.java
4. Create service/impl/AuthenticationServiceImpl.java
5. Create service/UserService.java
6. Create service/impl/UserServiceImpl.java
7. Create config/SecurityConfig.java (updated)
8. Create config/JwtTokenUtil.java (updated)
9. Create config/JwtAuthenticationFilter.java (updated)
10. Create config/WebMvcConfig.java (register interceptor)
11. Create controller/AuthController.java (minimal code)
12. Create controller/AdminController.java (minimal code)
13. Create controller/ConsultantController.java (minimal code)
14. Add unit tests
15. Update application.properties

## KEY IMPLEMENTATION NOTES:

### Service Layer Pattern:
- Service interfaces define contracts
- ServiceImpl provides implementations
- Controllers call service methods only
- Services handle business logic and validation
- Repositories handle database operations

### Rate Limiting:
- Applied via @RateLimit annotation
- Enforced by RateLimitingInterceptor
- Different limits per endpoint
- IP-based or User-based identification

### Exception Handling:
- Custom exceptions extend VPException
- GlobalExceptionHandler catches and formats all exceptions
- Consistent JSON response format
- Proper HTTP status codes

### Validation:
- @NotBlank, @NotNull, @Email, @Pattern annotations
- Validated in DTOs
- Handled by Spring Boot validation framework
- Error messages displayed in response

---

Due to response token limits, I'm providing the remaining code in a continuation.
Please proceed with the following files in order.
