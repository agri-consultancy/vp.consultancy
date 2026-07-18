# Consultant Advertisement API Documentation

## Overview
The Consultant Advertisement feature allows consultants to create and manage promotional advertisements that can be displayed to their farmers. Farmers can view all consultant advertisements sorted by priority on their main page.

## Database Schema

```sql
CREATE TABLE IF NOT EXISTS consultant_advertisement (
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

## Architecture

### Entity
- **ConsultantAdvertisement.java**: JPA entity representing consultant advertisements
  - Linked to consultant via UserProfile
  - Contains url, title, descriptions, priority
  - Audit fields: createdAt, updatedAt

### Repository
- **ConsultantAdvertisementRepository.java**: Spring Data JPA repository
  - `findByConsultantIdOrderByPriority(Long consultantId)`: Get advertisements by consultant
  - `findAllOrderByPriority()`: Get all advertisements ordered by priority

### DTOs
- **ConsultantAdvertisementRequest.java**: Request DTO for creating/updating advertisements
  - url (required, max 512 chars)
  - title (required, max 255 chars)
  - descriptions (required, max 512 chars)
  - priority (required, non-negative)

- **ConsultantAdvertisementResponse.java**: Response DTO containing:
  - id, url, title, descriptions, priority
  - consultantId, consultantName
  - createdAt, updatedAt

### Service
- **ConsultantAdvertisementService.java**: Service interface
  - `addAdvertisement()`: Create new advertisement
  - `getConsultantAdvertisements()`: Get consultant's own advertisements
  - `getAllAdvertisements()`: Get all advertisements (for farmers)
  - `updateAdvertisement()`: Update advertisement with authorization check
  - `deleteAdvertisement()`: Delete advertisement with authorization check

- **ConsultantAdvertisementServiceImpl.java**: Service implementation with business logic

### Controllers
- **ConsultantController.java**: Consultant endpoints (restricted to CONSULTANT role)
- **FarmerController.java**: Farmer endpoints (restricted to FARMER role)

## API Endpoints

### Consultant Endpoints

All endpoints require `Authorization: Bearer <JWT_TOKEN>` header with CONSULTANT role.

#### 1. Create Advertisement
```
POST /api/consultant/advertisements
Content-Type: application/json

Request Body:
{
    "url": "https://example.com/advertisement-image.jpg",
    "title": "New Fertilizer Product Launch",
    "descriptions": "High-yield fertilizer specially formulated for wheat crops",
    "priority": 10
}

Response (201 Created):
{
    "success": true,
    "message": "Advertisement created successfully",
    "data": {
        "id": 1,
        "url": "https://example.com/advertisement-image.jpg",
        "title": "New Fertilizer Product Launch",
        "descriptions": "High-yield fertilizer specially formulated for wheat crops",
        "priority": 10,
        "consultantId": 5,
        "consultantName": "John Doe",
        "createdAt": "2026-07-12T16:00:00",
        "updatedAt": "2026-07-12T16:00:00"
    },
    "statusCode": 201
}

Error Responses:
- 400 Bad Request: Invalid request body
- 401 Unauthorized: Missing or invalid JWT token
- 403 Forbidden: User is not a CONSULTANT
- 429 Too Many Requests: Rate limit exceeded (20 requests per 60 seconds)
- 500 Internal Server Error: Server error
```

**Rate Limit**: 20 requests per 60 seconds

#### 2. Get Consultant's Advertisements
```
GET /api/consultant/advertisements
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
    "success": true,
    "message": "Advertisements retrieved successfully",
    "data": [
        {
            "id": 1,
            "url": "https://example.com/ad1.jpg",
            "title": "Advertisement 1",
            "descriptions": "Description 1",
            "priority": 10,
            "consultantId": 5,
            "consultantName": "John Doe",
            "createdAt": "2026-07-12T16:00:00",
            "updatedAt": "2026-07-12T16:00:00"
        },
        {
            "id": 2,
            "url": "https://example.com/ad2.jpg",
            "title": "Advertisement 2",
            "descriptions": "Description 2",
            "priority": 5,
            "consultantId": 5,
            "consultantName": "John Doe",
            "createdAt": "2026-07-11T10:00:00",
            "updatedAt": "2026-07-11T10:00:00"
        }
    ],
    "statusCode": 200
}

Notes:
- Advertisements are sorted by priority (descending)
- Returns only advertisements created by the authenticated consultant
- Returns empty list if no advertisements exist
```

#### 3. Update Advertisement
```
PUT /api/consultant/advertisements/{advertisementId}
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

Request Body:
{
    "url": "https://example.com/updated-image.jpg",
    "title": "Updated Title",
    "descriptions": "Updated description",
    "priority": 15
}

Response (200 OK):
{
    "success": true,
    "message": "Advertisement updated successfully",
    "data": {
        "id": 1,
        "url": "https://example.com/updated-image.jpg",
        "title": "Updated Title",
        "descriptions": "Updated description",
        "priority": 15,
        "consultantId": 5,
        "consultantName": "John Doe",
        "createdAt": "2026-07-12T16:00:00",
        "updatedAt": "2026-07-12T16:05:00"
    },
    "statusCode": 200
}

Error Responses:
- 403 Forbidden: User is not authorized to update this advertisement
- 404 Not Found: Advertisement with given ID does not exist
```

#### 4. Delete Advertisement
```
DELETE /api/consultant/advertisements/{advertisementId}
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
    "success": true,
    "message": "Advertisement deleted successfully",
    "data": null,
    "statusCode": 200
}

Error Responses:
- 403 Forbidden: User is not authorized to delete this advertisement
- 404 Not Found: Advertisement with given ID does not exist
```

### Farmer Endpoints

All endpoints require `Authorization: Bearer <JWT_TOKEN>` header with FARMER role.

#### 1. Get Consultant Advertisements (for Main Page)
```
GET /api/farmer/consultant-advertisements
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
    "success": true,
    "message": "Consultant advertisements retrieved successfully",
    "data": [
        {
            "id": 3,
            "url": "https://example.com/ad3.jpg",
            "title": "High Priority Ad",
            "descriptions": "Important advertisement",
            "priority": 50,
            "consultantId": 5,
            "consultantName": "John Doe",
            "createdAt": "2026-07-10T09:00:00",
            "updatedAt": "2026-07-10T09:00:00"
        },
        {
            "id": 1,
            "url": "https://example.com/ad1.jpg",
            "title": "Medium Priority Ad",
            "descriptions": "Secondary advertisement",
            "priority": 10,
            "consultantId": 6,
            "consultantName": "Jane Smith",
            "createdAt": "2026-07-12T16:00:00",
            "updatedAt": "2026-07-12T16:00:00"
        },
        {
            "id": 2,
            "url": "https://example.com/ad2.jpg",
            "title": "Low Priority Ad",
            "descriptions": "Additional information",
            "priority": 5,
            "consultantId": 7,
            "consultantName": "Bob Wilson",
            "createdAt": "2026-07-11T10:00:00",
            "updatedAt": "2026-07-11T10:00:00"
        }
    ],
    "statusCode": 200
}

Notes:
- Returns all advertisements from all consultants
- Sorted by priority (highest first), then by creation date (newest first)
- Includes consultant information for each advertisement
- Returns empty list if no advertisements exist
- Farmer can see advertisements from any consultant, not just their assigned consultant
```

## Request/Response Formats

### ConsultantAdvertisementRequest
```json
{
    "url": "string (required, max 512 chars) - Image URL or web link",
    "title": "string (required, max 255 chars) - Advertisement title",
    "descriptions": "string (required, max 512 chars) - Advertisement description",
    "priority": "number (required, >= 0) - Priority for display ordering"
}
```

### ConsultantAdvertisementResponse
```json
{
    "id": "number - Advertisement ID",
    "url": "string - Image URL or web link",
    "title": "string - Advertisement title",
    "descriptions": "string - Advertisement description",
    "priority": "number - Priority value",
    "consultantId": "number - ID of consultant who created the advertisement",
    "consultantName": "string - Full name of consultant",
    "createdAt": "string (ISO 8601) - Creation timestamp",
    "updatedAt": "string (ISO 8601) - Last update timestamp"
}
```

## Authentication & Authorization

### Required Headers
All API requests must include:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Role-Based Access Control

**Consultant Endpoints** (`/api/consultant/advertisements/*`):
- Require: `CONSULTANT` role
- Create (POST): Rate limited to 20 requests per 60 seconds
- Read (GET): No rate limit
- Update (PUT): Full access to own advertisements only
- Delete (DELETE): Full access to own advertisements only

**Farmer Endpoints** (`/api/farmer/consultant-advertisements`):
- Require: `FARMER` role
- Read-only access to all advertisements
- Can see advertisements from any consultant

## Error Handling

All error responses follow standard format:
```json
{
    "success": false,
    "message": "Error description",
    "statusCode": 400
}
```

### Common HTTP Status Codes
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters or body
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions or unauthorized access
- `404 Not Found`: Resource does not exist
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

## Usage Examples

### Example 1: Create Advertisement as Consultant
```bash
curl -X POST http://localhost:8080/api/consultant/advertisements \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://cdn.example.com/fertilizer-ad.jpg",
    "title": "Premium Crop Fertilizer",
    "descriptions": "Increase yield by 30% with our premium fertilizer",
    "priority": 20
  }'
```

### Example 2: Get All Own Advertisements
```bash
curl -X GET http://localhost:8080/api/consultant/advertisements \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 3: Update Advertisement
```bash
curl -X PUT http://localhost:8080/api/consultant/advertisements/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://cdn.example.com/updated-ad.jpg",
    "title": "Updated Ad Title",
    "descriptions": "Updated description text",
    "priority": 25
  }'
```

### Example 4: Get Advertisements as Farmer
```bash
curl -X GET http://localhost:8080/api/farmer/consultant-advertisements \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Design Decisions

1. **Priority-Based Display**: Advertisements are ordered by priority (descending) to ensure important ads are displayed first to farmers.

2. **Authorization**: 
   - Consultants can only modify their own advertisements
   - Farmers can view all advertisements from all consultants (not filtered by consultant relationship)

3. **Farmer Access**: Farmers get all advertisements regardless of which consultant they are associated with, allowing them to see promotional content from all consultants in the system.

4. **Rate Limiting**: Write operations (create, update, delete) are not explicitly rate-limited on update/delete, but create is rate-limited to prevent abuse.

5. **Audit Trail**: All advertisements include createdAt and updatedAt timestamps for audit purposes.

6. **Soft Delete Not Implemented**: Hard delete is used as advertisements are promotional content that can be simply removed.

## Database Indexes

The ConsultantAdvertisement table has two indexes for performance:
- `idx_consultant_id`: Speeds up queries to find advertisements by consultant
- `idx_priority`: Speeds up sorting by priority

## Future Enhancements

1. **Pagination**: Add pagination to endpoints returning multiple advertisements
2. **Search/Filter**: Add filtering by consultant, date range, or priority
3. **Soft Delete**: Implement soft delete to maintain historical data
4. **Analytics**: Track advertisement views/clicks
5. **Image Upload**: Support direct image uploads instead of URLs only
6. **Scheduled Publishing**: Allow scheduling advertisements for future display
7. **Expiration**: Add expiration dates for limited-time advertisements
