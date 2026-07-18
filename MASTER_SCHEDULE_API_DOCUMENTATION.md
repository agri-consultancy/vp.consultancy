# Master Schedule APIs - Complete Documentation

## Overview
This document provides comprehensive API documentation for the Master Schedule management system. All endpoints are secured with JWT authentication and require `CONSULTANT` role.

## Base URL
```
/api/consultant/schedule
```

## Authentication
All requests require a valid JWT token in the `Authorization` header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## API Endpoints Summary

| # | Method | Endpoint | Description |
|----|--------|----------|-------------|
| 1 | GET | `/templates` | List all templates |
| 2 | POST | `/import-excel` | Import template from Excel |
| 3 | GET | `/templates/{template_id}` | Get template with all days/tasks |
| 4 | PUT | `/templates/{template_id}` | Update template metadata |
| 5 | DELETE | `/templates/{template_id}` | Delete template |
| 6 | POST | `/templates/{template_id}/days` | Add new day |
| 7 | GET | `/templates/{template_id}/days/{day_id}` | Get day details |
| 8 | PUT | `/templates/{template_id}/days/{day_id}` | Update day |
| 9 | DELETE | `/templates/{template_id}/days/{day_id}` | Delete day |
| 10 | POST | `/templates/{template_id}/days/{day_id}/tasks` | Add task |
| 11 | PUT | `/templates/{template_id}/days/{day_id}/tasks/{task_id}` | Update task |
| 12 | DELETE | `/templates/{template_id}/days/{day_id}/tasks/{task_id}` | Delete task |
| 13 | GET | `/templates/{template_id}/days/{day_id}/tasks/{task_id}` | Get task details |

---

## Detailed Endpoint Documentation

### 1. List All Templates

**Endpoint:**
```
GET /api/consultant/schedule/templates
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "cropVarietyId": 1,
      "version": 1,
      "description": "Standardized growth cycle for temperate climates...",
      "status": "ACTIVE",
      "totalPhases": 24,
      "lastUpdated": "2023-10-12T10:30:00",
      "createdAt": "2023-10-12T10:30:00"
    }
  ],
  "totalCount": 4,
  "timestamp": "2023-10-12T11:00:00"
}
```

---

### 2. Import Template from Excel

**Endpoint:**
```
POST /api/consultant/schedule/import-excel
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| crop_variety_id | Long | Yes | ID of crop variety |
| version | Long | Yes | Version number |
| description | String | No | Template description |
| status | String | Yes | Template status (ACTIVE/ARCHIVED) |
| file | File | Yes | Excel file (.xlsx) |
| create_new_version | Boolean | No | Create new version if exists (default: false) |

**Excel File Format:**

Column Headers (Row 1):
```
day_number | title | description | fertilizer_name | quantity | proportion | priority | task_type | task_description
```

Example Data Row:
```
1 | Planting Day | Initial Soil Prep | Nitrogen Base | 150kg | 50% | 1 | FERTILIZER | Apply nitrogen before planting
```

**Success Response (201 CREATED):**
```json
{
  "success": true,
  "templateId": 1,
  "cropVarietyId": 1,
  "version": 1,
  "totalDaysImported": 24,
  "totalTasksImported": 68,
  "message": "Template created successfully",
  "timestamp": "2023-10-12T11:05:00"
}
```

**Error Response (400 BAD REQUEST):**
```json
{
  "success": false,
  "templateId": null,
  "error": "Row 5: fertilizer_name cannot be empty",
  "failedDayNumber": "14",
  "rollbackMessage": "Transaction rolled back. No data was imported.",
  "timestamp": "2023-10-12T11:05:00"
}
```

---

### 3. Get Template Details

**Endpoint:**
```
GET /api/consultant/schedule/templates/{template_id}
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| template_id | Long | Template ID |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "cropVarietyId": 1,
    "cropVarietyName": "Winter Wheat",
    "version": 1,
    "description": "Standardized growth cycle...",
    "status": "ACTIVE",
    "totalPhases": 6,
    "createdAt": "2023-10-12T10:30:00",
    "lastUpdated": "2023-10-12T10:30:00",
    "scheduleDays": [
      {
        "id": 101,
        "templateId": 1,
        "dayNumber": 1,
        "title": "Planting Day",
        "description": "Initial Soil Prep & Seeding",
        "displayOrder": 1,
        "createdAt": "2023-10-12T10:30:00",
        "updatedAt": "2023-10-12T10:30:00",
        "tasks": [
          {
            "id": 501,
            "scheduleDayId": 101,
            "fertilizerName": "Nitrogen Base",
            "quantity": "150kg",
            "proportion": "50%",
            "priority": 1,
            "description": "Apply nitrogen base before planting",
            "taskType": "FERTILIZER",
            "createdAt": "2023-10-12T10:30:00",
            "updatedAt": "2023-10-12T10:30:00"
          }
        ]
      }
    ]
  },
  "timestamp": "2023-10-12T11:10:00"
}
```

---

### 4. Update Template Metadata

**Endpoint:**
```
PUT /api/consultant/schedule/templates/{template_id}
```

**Request Body:**
```json
{
  "description": "Updated description",
  "status": "ARCHIVED"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "cropVarietyId": 1,
    "version": 1,
    "description": "Updated description",
    "status": "ARCHIVED",
    "totalPhases": 6,
    "lastUpdated": "2023-10-12T11:15:00",
    "createdAt": "2023-10-12T10:30:00"
  },
  "message": "Template updated successfully",
  "timestamp": "2023-10-12T11:15:00"
}
```

---

### 5. Delete Template

**Endpoint:**
```
DELETE /api/consultant/schedule/templates/{template_id}
```

**Response (204 NO CONTENT):**
```
No body
```

---

### 6. Add New Day to Template

**Endpoint:**
```
POST /api/consultant/schedule/templates/{template_id}/days
```

**Request Body:**
```json
{
  "dayNumber": 75,
  "title": "Flowering Stage",
  "description": "Peak flowering period",
  "displayOrder": 7
}
```

**Response (201 CREATED):**
```json
{
  "success": true,
  "data": {
    "id": 210,
    "templateId": 1,
    "dayNumber": 75,
    "title": "Flowering Stage",
    "description": "Peak flowering period",
    "displayOrder": 7,
    "createdAt": "2023-10-12T11:20:00",
    "updatedAt": "2023-10-12T11:20:00",
    "tasks": []
  },
  "message": "Day added successfully",
  "timestamp": "2023-10-12T11:20:00"
}
```

---

### 7. Get Day Details

**Endpoint:**
```
GET /api/consultant/schedule/templates/{template_id}/days/{day_id}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 101,
    "templateId": 1,
    "dayNumber": 1,
    "title": "Planting Day",
    "description": "Initial Soil Prep & Seeding",
    "displayOrder": 1,
    "createdAt": "2023-10-12T10:30:00",
    "updatedAt": "2023-10-12T10:30:00",
    "tasks": [
      {
        "id": 501,
        "scheduleDayId": 101,
        "fertilizerName": "Nitrogen Base",
        "quantity": "150kg",
        "proportion": "50%",
        "priority": 1,
        "description": "Apply nitrogen base",
        "taskType": "FERTILIZER",
        "createdAt": "2023-10-12T10:30:00",
        "updatedAt": "2023-10-12T10:30:00"
      }
    ]
  },
  "timestamp": "2023-10-12T11:25:00"
}
```

---

### 8. Update Day Details

**Endpoint:**
```
PUT /api/consultant/schedule/templates/{template_id}/days/{day_id}
```

**Request Body:**
```json
{
  "dayNumber": 1,
  "title": "Updated Title",
  "description": "Updated description",
  "displayOrder": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 101,
    "templateId": 1,
    "dayNumber": 1,
    "title": "Updated Title",
    "description": "Updated description",
    "displayOrder": 1,
    "createdAt": "2023-10-12T10:30:00",
    "updatedAt": "2023-10-12T11:30:00"
  },
  "message": "Day updated successfully",
  "timestamp": "2023-10-12T11:30:00"
}
```

---

### 9. Delete Day

**Endpoint:**
```
DELETE /api/consultant/schedule/templates/{template_id}/days/{day_id}
```

**Response (204 NO CONTENT):**
```
No body
```

---

### 10. Add Task to Day

**Endpoint:**
```
POST /api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks
```

**Request Body:**
```json
{
  "fertilizerName": "NPK 20-20-20",
  "quantity": "100kg",
  "proportion": "50%",
  "priority": 1,
  "description": "Apply NPK fertilizer",
  "taskType": "FERTILIZER"
}
```

**Response (201 CREATED):**
```json
{
  "success": true,
  "data": {
    "id": 503,
    "scheduleDayId": 101,
    "fertilizerName": "NPK 20-20-20",
    "quantity": "100kg",
    "proportion": "50%",
    "priority": 1,
    "description": "Apply NPK fertilizer",
    "taskType": "FERTILIZER",
    "createdAt": "2023-10-12T11:35:00",
    "updatedAt": "2023-10-12T11:35:00"
  },
  "message": "Task added successfully",
  "timestamp": "2023-10-12T11:35:00"
}
```

---

### 11. Update Task

**Endpoint:**
```
PUT /api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks/{task_id}
```

**Request Body:**
```json
{
  "fertilizerName": "Updated NPK",
  "quantity": "120kg",
  "proportion": "60%",
  "priority": 2,
  "description": "Updated description",
  "taskType": "FERTILIZER"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 503,
    "scheduleDayId": 101,
    "fertilizerName": "Updated NPK",
    "quantity": "120kg",
    "proportion": "60%",
    "priority": 2,
    "description": "Updated description",
    "taskType": "FERTILIZER",
    "createdAt": "2023-10-12T11:35:00",
    "updatedAt": "2023-10-12T11:40:00"
  },
  "message": "Task updated successfully",
  "timestamp": "2023-10-12T11:40:00"
}
```

---

### 12. Delete Task

**Endpoint:**
```
DELETE /api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks/{task_id}
```

**Response (204 NO CONTENT):**
```
No body
```

---

### 13. Get Task Details

**Endpoint:**
```
GET /api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks/{task_id}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 503,
    "scheduleDayId": 101,
    "fertilizerName": "NPK 20-20-20",
    "quantity": "100kg",
    "proportion": "50%",
    "priority": 1,
    "description": "Apply NPK fertilizer",
    "taskType": "FERTILIZER",
    "createdAt": "2023-10-12T11:35:00",
    "updatedAt": "2023-10-12T11:35:00"
  },
  "timestamp": "2023-10-12T11:45:00"
}
```

---

## Error Responses

### 404 Not Found
```json
{
  "success": false,
  "error": "Template not found with ID: 999",
  "timestamp": "2023-10-12T11:50:00"
}
```

### 400 Bad Request
```json
{
  "success": false,
  "error": "Day with number 1 already exists in this template",
  "timestamp": "2023-10-12T11:55:00"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "error": "An unexpected error occurred",
  "timestamp": "2023-10-12T12:00:00"
}
```

---

## Common Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Successful deletion |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Missing or invalid JWT token |
| 403 | Forbidden - User lacks required role (CONSULTANT) |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Server error |

---

## Validation Rules

### Template Import
- `crop_variety_id` - Required, must be a valid crop variety
- `version` - Required, must be positive
- `status` - Required, must be ACTIVE or ARCHIVED
- `file` - Required, must be .xlsx format
- Maximum file size: 5MB

### Day Creation
- `day_number` - Required, must be unique within template
- `title` - Required, max 255 characters
- `description` - Optional, max 255 characters
- `displayOrder` - Optional, determines display sequence

### Task Creation
- `fertilizerName` - Required, max 255 characters
- `quantity` - Required, max 255 characters
- `proportion` - Optional, max 50 characters
- `priority` - Optional, positive number
- `taskType` - Optional, max 255 characters
- `description` - Required, max 255 characters

---

## Notes

1. All timestamps are in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss)
2. Excel import performs atomic transaction - all or nothing
3. Deleting a template cascades to all associated days and tasks
4. Deleting a day cascades to all associated tasks
5. Duplicate day numbers per template are prevented
6. All operations require valid JWT authentication with CONSULTANT role
