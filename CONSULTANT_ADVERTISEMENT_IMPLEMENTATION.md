# Consultant Advertisement Feature - Implementation Summary

## Overview
Successfully implemented a complete consultant advertisement feature that allows consultants to create and manage promotional advertisements for their farmers, with farmers able to view these advertisements sorted by priority on their main page.

## Implementation Date
July 12, 2026

## Components Created

### 1. Entity Layer
**File**: `ConsultantAdvertisement.java`
- JPA Entity with proper annotations
- Fields: id, consultant_id, url, title, descriptions, priority, created_at, updated_at
- Relationships: ManyToOne relationship with UserProfile (consultant)
- Indexes: idx_consultant_id, idx_priority for performance optimization
- Audit fields: created_at and updated_at with @PrePersist/@PreUpdate lifecycle hooks

### 2. Repository Layer
**File**: `ConsultantAdvertisementRepository.java`
- Spring Data JPA repository interface
- Methods:
  - `findByConsultantIdOrderByPriority(Long consultantId)`: Retrieve consultant's advertisements
  - `findAllOrderByPriority()`: Retrieve all advertisements ordered by priority

### 3. Data Transfer Objects (DTOs)

#### Request DTO
**File**: `ConsultantAdvertisementRequest.java`
- Fields: url, title, descriptions, priority
- Validation: All fields required with appropriate constraints
  - url: @NotBlank, max 512 characters
  - title: @NotBlank, max 255 characters
  - descriptions: @NotBlank, max 512 characters
  - priority: @NotNull, @Min(0) - non-negative numbers only

#### Response DTO
**File**: `ConsultantAdvertisementResponse.java`
- Fields: id, url, title, descriptions, priority, consultantId, consultantName, createdAt, updatedAt
- Used in all API responses

### 4. Service Layer

#### Service Interface
**File**: `ConsultantAdvertisementService.java`
- Methods:
  - `addAdvertisement(ConsultantAdvertisementRequest)`: Create new advertisement
  - `getConsultantAdvertisements()`: Get current consultant's advertisements
  - `getAllAdvertisements()`: Get all advertisements from all consultants
  - `updateAdvertisement(Long, ConsultantAdvertisementRequest)`: Update with authorization
  - `deleteAdvertisement(Long)`: Delete with authorization

#### Service Implementation
**File**: `ConsultantAdvertisementServiceImpl.java`
- Full business logic implementation
- Security checks: Authorization validation for update/delete operations
- Current user extraction from SecurityContext
- Exception handling: ResourceNotFoundException, AccessDeniedException
- Response conversion: LocalDateTime to String format conversion
- Transactional support via @Transactional annotation

### 5. Controller Layer

#### Consultant Controller Updates
**File**: `ConsultantController.java`
- Added ConsultantAdvertisementService dependency
- New endpoints:
  - `POST /api/consultant/advertisements` (Rate limited: 20 requests/60s)
    - Create new advertisement
    - Response: 201 Created
  
  - `GET /api/consultant/advertisements`
    - Retrieve all consultant's own advertisements
    - Response: 200 OK with list ordered by priority
  
  - `PUT /api/consultant/advertisements/{advertisementId}`
    - Update existing advertisement with authorization check
    - Response: 200 OK
  
  - `DELETE /api/consultant/advertisements/{advertisementId}`
    - Delete advertisement with authorization check
    - Response: 200 OK

#### Farmer Controller Updates
**File**: `FarmerController.java`
- Added ConsultantAdvertisementService dependency
- New endpoint:
  - `GET /api/farmer/consultant-advertisements`
    - Retrieve all advertisements from all consultants
    - Sorted by priority (descending)
    - Response: 200 OK with list of advertisements

## Security Features

1. **Role-Based Access Control**:
   - Consultant endpoints: @PreAuthorize("hasRole('CONSULTANT')")
   - Farmer endpoints: @PreAuthorize("hasRole('FARMER')")

2. **Authorization Checks**:
   - Consultants can only update/delete their own advertisements
   - Farmers have read-only access to all advertisements
   - SecurityContext used to identify current user

3. **Rate Limiting**:
   - POST /api/consultant/advertisements: Limited to 20 requests per 60 seconds
   - Prevents abuse of advertisement creation

## API Endpoints Summary

### Consultant APIs (All require CONSULTANT role)
| Method | Endpoint | Purpose | Rate Limit |
|--------|----------|---------|------------|
| POST | /api/consultant/advertisements | Create advertisement | 20/60s |
| GET | /api/consultant/advertisements | Get own advertisements | None |
| PUT | /api/consultant/advertisements/{id} | Update advertisement | None |
| DELETE | /api/consultant/advertisements/{id} | Delete advertisement | None |

### Farmer APIs (All require FARMER role)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | /api/farmer/consultant-advertisements | Get all advertisements for main page |

## Database Schema

```sql
CREATE TABLE consultant_advertisement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consultant_id BIGINT NOT NULL,
    url VARCHAR(512) NOT NULL,
    title VARCHAR(255) NOT NULL,
    descriptions VARCHAR(512) NOT NULL,
    priority BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES user_profiles(id) ON DELETE SET NULL,
    INDEX idx_consultant_id (consultant_id),
    INDEX idx_priority (priority)
);
```

## Key Design Decisions

1. **Priority-Based Ordering**
   - Advertisements sorted by priority (descending) for optimal visibility
   - Consultants control display order via priority field

2. **Farmer Advertisement Access**
   - Farmers see all advertisements from all consultants
   - Not filtered by consultant relationship
   - Promotes visibility of all consultant offerings

3. **Security Model**
   - Consultants: Full CRUD on own advertisements only
   - Farmers: Read-only access to all advertisements
   - Authorization checks prevent unauthorized modifications

4. **Validation**
   - All fields validated at request level via Jakarta validation annotations
   - Non-negative priority values enforced
   - Field length constraints match database schema

5. **Error Handling**
   - Comprehensive exception handling with meaningful messages
   - Consistent API response format with ApiResponse wrapper
   - Proper HTTP status codes (201 for creation, 200 for success, 403 for authorization, 404 for not found)

## Testing Recommendations

1. **Unit Tests**:
   - Test service layer business logic
   - Test authorization checks
   - Test exception scenarios

2. **Integration Tests**:
   - Test API endpoints with valid/invalid tokens
   - Test role-based access control
   - Test CRUD operations
   - Test rate limiting

3. **E2E Tests**:
   - Test complete workflow: consultant creates ad → farmer retrieves and displays
   - Test priority ordering
   - Test authorization scenarios

## Sample Test Scenarios

### Scenario 1: Consultant Creates Advertisement
```bash
POST /api/consultant/advertisements
Authorization: Bearer <consultant-token>
{
  "url": "https://example.com/ad.jpg",
  "title": "New Crop Fertilizer",
  "descriptions": "High-yield fertilizer for wheat",
  "priority": 10
}
Expected: 201 Created with advertisement details
```

### Scenario 2: Farmer Views Advertisements
```bash
GET /api/farmer/consultant-advertisements
Authorization: Bearer <farmer-token>
Expected: 200 OK with list of all advertisements sorted by priority
```

### Scenario 3: Consultant Updates Own Advertisement
```bash
PUT /api/consultant/advertisements/1
Authorization: Bearer <consultant-token>
{
  "url": "https://example.com/updated-ad.jpg",
  "title": "Updated Title",
  "descriptions": "Updated description",
  "priority": 15
}
Expected: 200 OK with updated advertisement
```

### Scenario 4: Consultant Cannot Update Another's Advertisement
```bash
PUT /api/consultant/advertisements/2 (created by different consultant)
Authorization: Bearer <consultant-token>
Expected: 403 Forbidden with error message
```

## Files Modified

1. **ConsultantController.java**:
   - Added import for ConsultantAdvertisementService and DTOs
   - Added service dependency to constructor
   - Added 4 new endpoint methods

2. **FarmerController.java**:
   - Added import for ConsultantAdvertisementService and DTOs
   - Added service dependency via @RequiredArgsConstructor
   - Added 1 new endpoint method

## Files Created

1. Entity: `ConsultantAdvertisement.java`
2. Repository: `ConsultantAdvertisementRepository.java`
3. Request DTO: `ConsultantAdvertisementRequest.java`
4. Response DTO: `ConsultantAdvertisementResponse.java`
5. Service Interface: `ConsultantAdvertisementService.java`
6. Service Implementation: `ConsultantAdvertisementServiceImpl.java`

## Documentation Created

1. **CONSULTANT_ADVERTISEMENT_API.md**: Comprehensive API documentation with examples, error handling, and design decisions

## Build Status

✓ Project builds successfully with no compilation errors
✓ All dependencies resolved
✓ Code follows project conventions and patterns

## Compliance

✓ Follows existing architecture patterns (Entity → Repository → DTO → Service → Controller)
✓ Uses existing security framework (@PreAuthorize, SecurityContext)
✓ Consistent with code style and documentation standards
✓ Proper exception handling with custom exceptions
✓ Transactional support for data consistency
✓ Lombok annotations for boilerplate reduction
✓ JPA best practices for entity mapping

## Future Enhancement Opportunities

1. Pagination for advertisement lists
2. Search/filtering by consultant, date range, priority
3. Soft delete with logical deletion
4. Advertisement analytics (view counts, impressions)
5. Direct image upload support
6. Scheduled publishing with start/end dates
7. Advertisement expiration functionality
8. A/B testing support for advertisements
9. Geographic targeting
10. Performance metrics and reporting

## Conclusion

The Consultant Advertisement feature has been successfully implemented with a complete architecture covering:
- Database layer with proper schema and indexes
- Repository layer with query methods
- Service layer with business logic and security
- Controller layer with REST endpoints
- DTOs with validation
- Comprehensive API documentation

The implementation follows enterprise standards and integrates seamlessly with the existing codebase.
