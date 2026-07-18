# Consultant Advertisement API - Quick Reference Guide

## Quick Start

### For Consultants: Create an Advertisement
```bash
curl -X POST http://localhost:8080/api/consultant/advertisements \
  -H "Authorization: Bearer YOUR_CONSULTANT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/image.jpg",
    "title": "My Advertisement Title",
    "descriptions": "This is my advertisement description",
    "priority": 10
  }'
```

### For Farmers: View All Advertisements
```bash
curl -X GET http://localhost:8080/api/farmer/consultant-advertisements \
  -H "Authorization: Bearer YOUR_FARMER_TOKEN"
```

## API Reference

### POST /api/consultant/advertisements
Create a new advertisement (Consultant only)

**Request:**
```json
{
  "url": "string",
  "title": "string", 
  "descriptions": "string",
  "priority": number
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Advertisement created successfully",
  "data": {
    "id": 1,
    "url": "...",
    "title": "...",
    "descriptions": "...",
    "priority": 10,
    "consultantId": 5,
    "consultantName": "John Doe",
    "createdAt": "2026-07-12T16:00:00",
    "updatedAt": "2026-07-12T16:00:00"
  }
}
```

---

### GET /api/consultant/advertisements
Get all own advertisements (Consultant only)

**Response (200):**
```json
{
  "success": true,
  "message": "Advertisements retrieved successfully",
  "data": [
    {
      "id": 1,
      "url": "...",
      "title": "...",
      "descriptions": "...",
      "priority": 10,
      "consultantId": 5,
      "consultantName": "John Doe",
      "createdAt": "2026-07-12T16:00:00",
      "updatedAt": "2026-07-12T16:00:00"
    }
  ]
}
```

---

### PUT /api/consultant/advertisements/{id}
Update an advertisement (Consultant only, own ads only)

**Request:**
```json
{
  "url": "string",
  "title": "string",
  "descriptions": "string", 
  "priority": number
}
```

**Response (200):** Updated advertisement

---

### DELETE /api/consultant/advertisements/{id}
Delete an advertisement (Consultant only, own ads only)

**Response (200):**
```json
{
  "success": true,
  "message": "Advertisement deleted successfully",
  "data": null
}
```

---

### GET /api/farmer/consultant-advertisements
Get all advertisements for main page (Farmer only)

**Response (200):** List of all advertisements sorted by priority (highest first)

## Validation Rules

| Field | Required | Max Length | Constraints |
|-------|----------|------------|-------------|
| url | Yes | 512 | Cannot be blank |
| title | Yes | 255 | Cannot be blank |
| descriptions | Yes | 512 | Cannot be blank |
| priority | Yes | N/A | Must be >= 0 |

## HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | Success (GET/PUT/DELETE) | Request completed successfully |
| 201 | Created (POST) | Advertisement created |
| 400 | Bad Request | Invalid JSON or validation error |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | Not authorized / wrong role |
| 404 | Not Found | Advertisement ID doesn't exist |
| 429 | Too Many Requests | Rate limit exceeded (create only) |
| 500 | Server Error | Unexpected error |

## Common Errors & Solutions

### Error: 403 Forbidden - "You are not authorized to update this advertisement"
**Cause**: Trying to update/delete an advertisement created by another consultant
**Solution**: Only modify advertisements you created

### Error: 404 Not Found - "Advertisement not found with ID: X"
**Cause**: Advertisement with given ID doesn't exist
**Solution**: Check the advertisement ID is correct

### Error: 401 Unauthorized
**Cause**: Missing or invalid JWT token in Authorization header
**Solution**: Ensure you're using a valid token in format: `Authorization: Bearer <token>`

### Error: 429 Too Many Requests
**Cause**: Exceeded rate limit on POST /api/consultant/advertisements
**Solution**: Wait 60 seconds before creating more advertisements (max 20 per 60s)

### Error: 400 Bad Request - Validation errors
**Cause**: Invalid request body
**Solutions**:
- Ensure all required fields are present
- Check field lengths don't exceed limits
- Ensure priority is a non-negative number

## Key Points

1. **Authentication Required**: All endpoints require valid JWT token in Authorization header
2. **Role-Based**: Consultant endpoints for consultants only, farmer endpoints for farmers only
3. **Priority Ordering**: Advertisements displayed by priority (highest first)
4. **Authorization**: Consultants can only modify their own advertisements
5. **Farmer Access**: Farmers can view advertisements from any consultant
6. **Rate Limiting**: Creating advertisements limited to 20 per 60 seconds

## Response Format

All responses follow the same wrapper format:
```json
{
  "success": true/false,
  "message": "Description of result",
  "data": { ... } or [ ... ] or null,
  "statusCode": 200
}
```

## Example Workflows

### Workflow 1: Consultant Creates and Manages Ads
1. Create ad with POST /api/consultant/advertisements
2. View all ads with GET /api/consultant/advertisements
3. Update priority with PUT /api/consultant/advertisements/{id}
4. Delete when no longer needed with DELETE /api/consultant/advertisements/{id}

### Workflow 2: Farmer Views Main Page
1. Call GET /api/farmer/consultant-advertisements
2. Display ads sorted by priority
3. Allow farmer to interact with ad links (handled in frontend)

## Integration with Frontend

### Display Advertisements (Farmer Main Page)
```javascript
// Fetch advertisements
const response = await fetch('/api/farmer/consultant-advertisements', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const { data: advertisements } = await response.json();

// Render sorted by priority
advertisements.forEach(ad => {
  console.log(`${ad.title} (Priority: ${ad.priority})`);
  console.log(`Consultant: ${ad.consultantName}`);
  console.log(`Description: ${ad.descriptions}`);
  console.log(`URL: ${ad.url}`);
});
```

### Create Advertisement (Consultant Dashboard)
```javascript
const adData = {
  url: 'https://example.com/ad.jpg',
  title: 'Product Launch',
  descriptions: 'Check out our new product',
  priority: 10
};

const response = await fetch('/api/consultant/advertisements', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(adData)
});

const result = await response.json();
if (result.success) {
  console.log('Advertisement created:', result.data);
}
```

## Rate Limit Info

**Create Advertisement Only**:
- Limit: 20 requests per 60 seconds
- After 60 seconds, counter resets
- Returns HTTP 429 when exceeded
- Applies per consultant (not global)

## File Locations

- Entity: `src/main/java/.../entity/ConsultantAdvertisement.java`
- Repository: `src/main/java/.../repository/ConsultantAdvertisementRepository.java`
- Service: `src/main/java/.../service/ConsultantAdvertisementService.java`
- Implementation: `src/main/java/.../service/impl/ConsultantAdvertisementServiceImpl.java`
- DTOs: `src/main/java/.../dto/ConsultantAdvertisement{Request,Response}.java`
- Controllers: `src/main/java/.../controller/{Consultant,Farmer}Controller.java`

## Testing Tips

1. **Get a valid token**: Login with consultant/farmer credentials
2. **Test create**: Create an ad and verify response
3. **Test list**: List your ads, verify sorting by priority
4. **Test update**: Change priority and verify
5. **Test farmer view**: Login as farmer, get all ads
6. **Test authorization**: Try accessing another consultant's ad (should fail)

## Database Queries

### View all advertisements
```sql
SELECT * FROM consultant_advertisement 
ORDER BY priority DESC, created_at DESC;
```

### View consultant's advertisements
```sql
SELECT ca.* FROM consultant_advertisement ca
JOIN user_profiles up ON ca.consultant_id = up.id
WHERE up.id = ?
ORDER BY ca.priority DESC, ca.created_at DESC;
```

### Get high-priority advertisements
```sql
SELECT * FROM consultant_advertisement 
WHERE priority >= 50
ORDER BY priority DESC, created_at DESC;
```

## Related Documentation

- Full API Documentation: `CONSULTANT_ADVERTISEMENT_API.md`
- Implementation Details: `CONSULTANT_ADVERTISEMENT_IMPLEMENTATION.md`
- Main API Documentation: `API_DOCUMENTATION.md`
