CROP MANAGEMENT API - 10 EXAMPLE REQUESTS FOR MAHARASHTRA FARMING CROPS
==========================================================================

Base URL: http://localhost:8080/api/crops
Authentication: Bearer {access_token} (admin token required for POST)

ENDPOINT DETAILS:
================

1. ADD NEW CROP (POST)
   URL: POST /api/crops
   Authorization: ADMIN role required
   Request Header: Content-Type: application/json
                   Authorization: Bearer {admin_access_token}

2. GET ALL CROPS
   URL: GET /api/crops
   Authorization: Authenticated users only
   Request Header: Authorization: Bearer {access_token}

3. GET CROP BY ID
   URL: GET /api/crops/{cropId}
   Authorization: Authenticated users only
   Request Header: Authorization: Bearer {access_token}

================================================================================
10 EXAMPLE API REQUESTS - MAHARASHTRA FARMING CROPS
================================================================================

REQUEST #1: ADD GRAPES
-----------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Grapes",
  "description": "Sweet and seedless grapes varieties - Famous in Nashik and Sangli regions of Maharashtra. High demand in domestic and export markets. Best for wine production and fresh table grapes."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 1,
    "name": "Grapes",
    "description": "Sweet and seedless grapes varieties - Famous in Nashik and Sangli regions of Maharashtra. High demand in domestic and export markets. Best for wine production and fresh table grapes.",
    "createdAt": "2026-07-05T23:35:00",
    "updatedAt": "2026-07-05T23:35:00"
  }
}

================================================================================

REQUEST #2: ADD SUGARCANE
--------------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Sugarcane",
  "description": "Major cash crop in Maharashtra. High sugar content varieties suitable for Kolhapur, Sangli, and Belgaum regions. Crushing season Oct-June. Requires irrigation."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 2,
    "name": "Sugarcane",
    "description": "Major cash crop in Maharashtra. High sugar content varieties suitable for Kolhapur, Sangli, and Belgaum regions. Crushing season Oct-June. Requires irrigation.",
    "createdAt": "2026-07-05T23:36:00",
    "updatedAt": "2026-07-05T23:36:00"
  }
}

================================================================================

REQUEST #3: ADD ONION
---------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Onion",
  "description": "Nashik onions - world famous red onions variety. Premium quality for export. Nashik produces 70% of India onion crop. Rabi season cultivation."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 3,
    "name": "Onion",
    "description": "Nashik onions - world famous red onions variety. Premium quality for export. Nashik produces 70% of India onion crop. Rabi season cultivation.",
    "createdAt": "2026-07-05T23:37:00",
    "updatedAt": "2026-07-05T23:37:00"
  }
}

================================================================================

REQUEST #4: ADD COTTON
----------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Cotton",
  "description": "White gold of India. High yielding BT cotton varieties. Popular in Vidarbha region. Harvest season Sep-December. Major cash crop for farmers."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 4,
    "name": "Cotton",
    "description": "White gold of India. High yielding BT cotton varieties. Popular in Vidarbha region. Harvest season Sep-December. Major cash crop for farmers.",
    "createdAt": "2026-07-05T23:38:00",
    "updatedAt": "2026-07-05T23:38:00"
  }
}

================================================================================

REQUEST #5: ADD JOWAR (SORGHUM)
-------------------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Jowar",
  "description": "Sorghum - Drought resistant crop. Hybrid varieties give high yield. Kharif crop suitable for dry regions of Maharashtra. Used as animal feed and cereal."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 5,
    "name": "Jowar",
    "description": "Sorghum - Drought resistant crop. Hybrid varieties give high yield. Kharif crop suitable for dry regions of Maharashtra. Used as animal feed and cereal.",
    "createdAt": "2026-07-05T23:39:00",
    "updatedAt": "2026-07-05T23:39:00"
  }
}

================================================================================

REQUEST #6: ADD WHEAT
--------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Wheat",
  "description": "Winter crop - Rabi season cultivation. High yielding improved varieties. Popular in Northern Maharashtra. Harvest March-April. 90-120 days crop duration."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 6,
    "name": "Wheat",
    "description": "Winter crop - Rabi season cultivation. High yielding improved varieties. Popular in Northern Maharashtra. Harvest March-April. 90-120 days crop duration.",
    "createdAt": "2026-07-05T23:40:00",
    "updatedAt": "2026-07-05T23:40:00"
  }
}

================================================================================

REQUEST #7: ADD BAJRA (PEARL MILLET)
------------------------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Bajra",
  "description": "Pearl millet - Highly drought tolerant. Summer and monsoon varieties available. Nutritious grain crop. Popular in dry regions. 60-90 days maturity."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 7,
    "name": "Bajra",
    "description": "Pearl millet - Highly drought tolerant. Summer and monsoon varieties available. Nutritious grain crop. Popular in dry regions. 60-90 days maturity.",
    "createdAt": "2026-07-05T23:41:00",
    "updatedAt": "2026-07-05T23:41:00"
  }
}

================================================================================

REQUEST #8: ADD RICE (PADDY)
---------------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Rice",
  "description": "Paddy crop - Kharif and summer varieties. High yielding hybridized seeds. Popular in coastal Maharashtra. Requires 120-150 days. 5-6 ft waterlogging needed."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 8,
    "name": "Rice",
    "description": "Paddy crop - Kharif and summer varieties. High yielding hybridized seeds. Popular in coastal Maharashtra. Requires 120-150 days. 5-6 ft waterlogging needed.",
    "createdAt": "2026-07-05T23:42:00",
    "updatedAt": "2026-07-05T23:42:00"
  }
}

================================================================================

REQUEST #9: ADD TOMATO
---------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Tomato",
  "description": "Commercial vegetable crop. Summer and winter varieties. High market demand. Popular near Nashik, Pune. 60-90 days to harvest. Suitable for greenhouse cultivation."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 9,
    "name": "Tomato",
    "description": "Commercial vegetable crop. Summer and winter varieties. High market demand. Popular near Nashik, Pune. 60-90 days to harvest. Suitable for greenhouse cultivation.",
    "createdAt": "2026-07-05T23:43:00",
    "updatedAt": "2026-07-05T23:43:00"
  }
}

================================================================================

REQUEST #10: ADD CHICKPEA (GRAM)
-------------------------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {admin_access_token}' \
--data '{
  "name": "Chickpea",
  "description": "Gram/Chana - Rabi crop. High protein legume crop. Helps in soil nitrogen fixing. 110-130 days duration. Popular in Vidarbha region. Export quality available."
}'

RESPONSE (201 Created):
{
  "success": true,
  "message": "Crop added successfully",
  "data": {
    "id": 10,
    "name": "Chickpea",
    "description": "Gram/Chana - Rabi crop. High protein legume crop. Helps in soil nitrogen fixing. 110-130 days duration. Popular in Vidarbha region. Export quality available.",
    "createdAt": "2026-07-05T23:44:00",
    "updatedAt": "2026-07-05T23:44:00"
  }
}

================================================================================
RETRIEVE CROPS EXAMPLES
================================================================================

GET ALL CROPS:
--------------
curl --location 'http://localhost:8080/api/crops' \
--header 'Authorization: Bearer {access_token}'

RESPONSE (200 OK):
{
  "success": true,
  "message": "Crops retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Grapes",
      "description": "Sweet and seedless grapes varieties...",
      "createdAt": "2026-07-05T23:35:00",
      "updatedAt": "2026-07-05T23:35:00"
    },
    {
      "id": 2,
      "name": "Sugarcane",
      "description": "Major cash crop in Maharashtra...",
      "createdAt": "2026-07-05T23:36:00",
      "updatedAt": "2026-07-05T23:36:00"
    },
    ...
  ]
}

================================================================================

GET CROP BY ID (e.g., Grapes with ID 1):
-----------------------------------------
curl --location 'http://localhost:8080/api/crops/1' \
--header 'Authorization: Bearer {access_token}'

RESPONSE (200 OK):
{
  "success": true,
  "message": "Crop retrieved successfully",
  "data": {
    "id": 1,
    "name": "Grapes",
    "description": "Sweet and seedless grapes varieties - Famous in Nashik and Sangli regions of Maharashtra. High demand in domestic and export markets. Best for wine production and fresh table grapes.",
    "createdAt": "2026-07-05T23:35:00",
    "updatedAt": "2026-07-05T23:35:00"
  }
}

================================================================================
ERROR RESPONSES
================================================================================

DUPLICATE CROP NAME ERROR (409 Conflict):
POST /api/crops with existing crop name
{
  "success": false,
  "message": "Crop 'Grapes' already exists",
  "data": null
}

UNAUTHORIZED ERROR (401 Unauthorized):
POST /api/crops or GET /api/crops without valid JWT
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}

FORBIDDEN ERROR (403 Forbidden):
POST /api/crops with non-admin user token
{
  "success": false,
  "message": "Access denied",
  "data": null
}

NOT FOUND ERROR (404 Not Found):
GET /api/crops/999 (non-existent crop)
{
  "success": false,
  "message": "Crop not found with ID: 999",
  "data": null
}

================================================================================
IMPLEMENTATION NOTES
================================================================================

1. Admin Authentication Required:
   - Only ADMIN role users can POST /api/crops
   - Use admin access token obtained from /api/auth/login
   - Example: Login with mobile "7875513279" and admin token will be provided

2. Consultant & Farmer Access:
   - Both CONSULTANT and FARMER roles can GET /api/crops and /api/crops/{id}
   - Cannot POST (add) crops - this is admin-only privilege

3. Rate Limiting:
   - POST /api/crops is rate limited to 20 requests per 60 seconds
   - Prevents abuse of crop addition

4. Data Validation:
   - Crop name: Required, 2-255 characters, unique
   - Description: Optional, max 500 characters
   - Duplicate names are rejected with HTTP 409

5. Success Response Format:
   - All successful responses use ApiResponse wrapper
   - Contains: success (boolean), message (string), data (object/array)

6. Error Response Format:
   - Uses same ApiResponse wrapper
   - success: false
   - message: descriptive error message
   - data: null

