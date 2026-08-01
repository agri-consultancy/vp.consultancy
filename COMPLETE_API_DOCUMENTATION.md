# VP Consultancy - Complete API Documentation

## 1. Service Overview

- **Application context path:** `/agri-consultancy-service`
- **Configured port:** `8085`
- **Base URL (local):** `http://localhost:8085/agri-consultancy-service`
- **Auth type:** `Authorization: Bearer <access_token>` for protected APIs

## 2. Common Response Formats

### 2.1 `ApiResponse<T>` (most APIs)

```json
{
  "success": true,
  "message": "string",
  "data": {},
  "statusCode": 200
}
```

### 2.2 `ApiResponseDTO<T>` (master schedule APIs)

```json
{
  "success": true,
  "data": {},
  "message": "string",
  "error": null,
  "totalCount": 0,
  "timestamp": "2026-07-20T12:00:00"
}
```

### 2.3 Validation and domain errors

- Global exception handler maps:
  - `InvalidCredentialsException` -> `401`
  - `ResourceNotFoundException` -> `404`
  - `DuplicateResourceException` -> `409`
  - `RateLimitExceededException` -> `429`
  - bean validation failures -> `400`

---

## 3. Complete Endpoint Inventory

| Module | Method | URL | Access | Request | Response |
|---|---|---|---|---|---|
| Health | GET | `/api/hello` | Public | None | plain string `"ok"` |
| Auth | POST | `/api/auth/login` | Public | `LoginRequest` | `ApiResponse<LoginResponse>` |
| Auth | POST | `/api/auth/refresh` | Public | `RefreshTokenRequest` | `ApiResponse<LoginResponse>` |
| Auth | POST | `/api/auth/logout/{userId}` | Authenticated | Path: `userId` | `ApiResponse<Void>` |
| Auth | POST | `/api/auth/change-password` | Authenticated | `UpdatePasswordRequest` | `ApiResponse<Void>` |
| Auth | POST | `/api/auth/admins` | Public | `ConsultantRegistrationRequest` | `ApiResponse<UserResponse>` |
| Auth | GET | `/api/auth/user-profile` | Authenticated | None | `ApiResponse<UserDetailsResponse>` |
| Admin | POST | `/api/admin/consultants` | ADMIN | `ConsultantRegistrationRequest` | `ApiResponse<UserResponse>` |
| Admin | POST | `/api/admin/crops` | ADMIN | `CropRegistrationRequest` | `ApiResponse<CropResponse>` |
| Consultant | POST | `/api/consultant/farmers` | CONSULTANT | `FarmerRegistrationRequest` | `ApiResponse<UserResponse>` |
| Consultant | GET | `/api/consultant/farmers` | CONSULTANT | None | `ApiResponse<List<UserResponse>>` |
| Consultant | GET | `/api/consultant/crops` | CONSULTANT | None | `ApiResponse<List<CropResponse>>` |
| Consultant | GET | `/api/consultant/crops-with-varieties` | CONSULTANT | None | `ApiResponse<List<ConsultantCropVarietiesResponse>>` |
| Consultant | GET | `/api/consultant/summary/active-counts` | CONSULTANT | None | `ApiResponse<ConsultantActiveSummaryResponse>` |
| Consultant | POST | `/api/consultant/crop-varieties` | CONSULTANT | `CropVarietyRegistrationRequest` | `ApiResponse<CropVarietyResponse>` |
| Consultant | GET | `/api/consultant/crop-varieties/{cropVarietyId}/master-schedules` | CONSULTANT | Path: `cropVarietyId` | `ApiResponse<List<MasterScheduleTemplateDTO>>` |
| Consultant | GET | `/api/consultant/farmers-portfolio` | CONSULTANT | None | `ApiResponse<List<FarmerPortfolioResponse>>` |
| Consultant | GET | `/api/consultant/farmers/{farmerId}` | CONSULTANT | Path: `farmerId` | `ApiResponse<FarmerProfileDetailResponse>` |
| Consultant | POST | `/api/consultant/farmers/{farmerId}/crops` | CONSULTANT | Path: `farmerId`, body `AssignCropVarietyRequest` | `ApiResponse<FarmerCropVarietyResponse>` |
| Consultant | PUT | `/api/consultant/farmers/{farmerId}/crops/{cropVarietyId}` | CONSULTANT | Path vars + `AssignCropVarietyRequest` | `ApiResponse<FarmerCropVarietyResponse>` |
| Consultant | GET | `/api/consultant/farmer/{farmerId}/schedules/{farmerCropVarietyId}` | CONSULTANT | Path vars | `ApiResponse<FarmerScheduleResponse>` |
| Consultant | GET | `/api/consultant/schedules/preview` | CONSULTANT | Query/model `GetNextSchedulePreviewRequest` | `ApiResponse<GetNextSchedulePreviewResponse>` |
| Consultant | POST | `/api/consultant/schedules/send` | CONSULTANT | `SendScheduleRequest` | `ApiResponse<SendScheduleResponse>` |
| Consultant Adv | POST | `/api/consultant/advertisements` | CONSULTANT | `ConsultantAdvertisementRequest` | `ApiResponse<ConsultantAdvertisementResponse>` |
| Consultant Adv | GET | `/api/consultant/advertisements` | CONSULTANT | None | `ApiResponse<List<ConsultantAdvertisementResponse>>` |
| Consultant Adv | PUT | `/api/consultant/advertisements/{advertisementId}` | CONSULTANT | Path + `ConsultantAdvertisementRequest` | `ApiResponse<ConsultantAdvertisementResponse>` |
| Consultant Adv | DELETE | `/api/consultant/advertisements/{advertisementId}` | CONSULTANT | Path | `ApiResponse<Void>` |
| Farmer | GET | `/api/farmer/crop-varieties` | FARMER | None | `ApiResponse<List<FarmerCropVarietyResponse>>` |
| Farmer | GET | `/api/farmer/schedules/{farmerCropVarietyId}` | FARMER | Path | `ApiResponse<FarmerScheduleResponse>` |
| Farmer | GET | `/api/farmer/consultant-advertisements` | FARMER | None | `ApiResponse<List<ConsultantAdvertisementResponse>>` |
| Farmer | GET | `/api/farmer/profile` | FARMER | None | `ApiResponse<FarmerProfileResponse>` |
| Master Schedule | GET | `/api/consultant/schedule/templates` | CONSULTANT | None | `ApiResponseDTO<List<MasterScheduleTemplateDTO>>` |
| Master Schedule | POST | `/api/consultant/schedule/import-excel` | CONSULTANT | Multipart params | `TemplateImportResponseDTO` |
| Master Schedule | GET | `/api/consultant/schedule/templates/{template_id}` | CONSULTANT | Path | `ApiResponseDTO<MasterScheduleTemplateWithDaysDTO>` |
| Master Schedule | PUT | `/api/consultant/schedule/templates/{template_id}` | CONSULTANT | Path + `UpdateTemplateRequestDTO` | `ApiResponseDTO<MasterScheduleTemplateDTO>` |
| Master Schedule | DELETE | `/api/consultant/schedule/templates/{template_id}` | CONSULTANT | Path | `204` |
| Master Schedule | POST | `/api/consultant/schedule/templates/{template_id}/days` | CONSULTANT | Path + `CreateDayRequestDTO` | `ApiResponseDTO<MasterScheduleDayDTO>` |
| Master Schedule | GET | `/api/consultant/schedule/templates/{template_id}/days/{day_id}` | CONSULTANT | Path | `ApiResponseDTO<MasterScheduleDayDTO>` |
| Master Schedule | PUT | `/api/consultant/schedule/templates/{template_id}/days/{day_id}` | CONSULTANT | Path + `CreateDayRequestDTO` | `ApiResponseDTO<MasterScheduleDayDTO>` |
| Master Schedule | DELETE | `/api/consultant/schedule/templates/{template_id}/days/{day_id}` | CONSULTANT | Path | `204` |
| Master Schedule | POST | `/api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks` | CONSULTANT | Path + `CreateTaskRequestDTO` | `ApiResponseDTO<MasterScheduleTaskDTO>` |
| Master Schedule | PUT | `/api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks/{task_id}` | CONSULTANT | Path + `CreateTaskRequestDTO` | `ApiResponseDTO<MasterScheduleTaskDTO>` |
| Master Schedule | DELETE | `/api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks/{task_id}` | CONSULTANT | Path | `204` |
| Master Schedule | GET | `/api/consultant/schedule/templates/{template_id}/days/{day_id}/tasks/{task_id}` | CONSULTANT | Path | `ApiResponseDTO<MasterScheduleTaskDTO>` |

---

## 4. Endpoint Purpose, Business Logic, and Special Behavior

## 4.1 Authentication APIs

### POST `/api/auth/login`
- **Use:** authenticate by mobile/password and issue JWT + refresh token.
- **Validation:** mobile 10 digits, password 6-100 chars, rate limit `5/60s`.
- **Behavior:** authenticates using Spring `AuthenticationManager`; adds claims (`userId`, `mobile`, `role`); persists refresh token.

### POST `/api/auth/refresh`
- **Use:** generate new access token from refresh token.
- **Validation:** refresh token required, rate limit `10/60s`.
- **Behavior:** checks token exists in DB and not expired; keeps same refresh token, returns new access token.

### POST `/api/auth/logout/{userId}`
- **Use:** invalidate all refresh tokens for user.
- **Validation:** `userId` must be positive.
- **Behavior:** deletes all tokens by user ID (all active sessions revoked).

### POST `/api/auth/change-password`
- **Use:** authenticated user updates password.
- **Validation:** old password required; new password 8-100.
- **Behavior:** verifies old password against hash, then stores new encoded password.

### POST `/api/auth/admins`
- **Use:** bootstrap/create admin user.
- **Validation:** same as consultant registration payload.
- **Behavior:** public endpoint; creates `ADMIN` user and profile.

### GET `/api/auth/user-profile`
- **Use:** get current logged-in user full profile.
- **Behavior:** derives mobile from JWT subject; merges user + profile + address + consultantId (for farmers).

## 4.2 Admin APIs

### POST `/api/admin/consultants`
- **Use:** register consultant.
- **Validation:** mobile unique, email unique, password >= 8, rate limit `10/60s`.
- **Behavior:** creates `User(role=CONSULTANT)` + `UserProfile`.

### POST `/api/admin/crops`
- **Use:** add crop in master list.
- **Validation:** name 2-255, unique name, optional description <=500, rate limit `20/60s`.
- **Behavior:** trim and save crop.

## 4.3 Consultant Farmer/Crop APIs

### POST `/api/consultant/farmers`
- **Use:** consultant registers farmer under self.
- **Validation:** farmer profile/address fields required, unique mobile/email, rate limit `20/60s`.
- **Behavior:** auto-generates random 10-char password; links farmer profile to consultant.

### GET `/api/consultant/farmers`
- **Use:** consultant's farmer list.
- **Behavior:** consultant resolved from token mobile; returns mapped `UserResponse`.

### GET `/api/consultant/crops`
- **Use:** list all crops sorted by name.
- **Special:** rate limit `20/60s`.

### GET `/api/consultant/crops-with-varieties`
- **Use:** crops grouped with varieties created by current consultant.
- **Special:** rate limit `20/60s`.

### GET `/api/consultant/summary/active-counts`
- **Use:** dashboard counts.
- **Behavior:** active farmers and active crop variety assignments.

### POST `/api/consultant/crop-varieties`
- **Use:** add consultant-owned crop variety.
- **Validation:** cropId required; climate enum (`Tropical|Temperate|Arid|Subtropical`); cycle 1-365; rate limit `20/60s`.

### GET `/api/consultant/crop-varieties/{cropVarietyId}/master-schedules`
- **Use:** active templates for consultant + crop variety.
- **Special:** verifies crop variety ownership before query, rate limit `20/60s`.

### GET `/api/consultant/farmers-portfolio`
- **Use:** summary cards for consultant's farmers.

### GET `/api/consultant/farmers/{farmerId}`
- **Use:** detailed farmer profile including assigned crops.

### POST `/api/consultant/farmers/{farmerId}/crops`
- **Use:** assign crop variety to farmer.
- **Validation:** status must be one of `Preparing|Active|Harvesting|Completed`; sowing/harvest dates required; rate limit `20/60s`.

### PUT `/api/consultant/farmers/{farmerId}/crops/{cropVarietyId}`
- **Use:** update assignment.
- **Behavior:** enforces assignment belongs to farmer.

## 4.4 Consultant Schedule Dispatch APIs

### GET `/api/consultant/schedules/preview`
- **Use:** preview next N days from selected master template for farmer assignment.
- **Validation:** all IDs positive via query model.
- **Behavior:** checks farmer ownership and template crop-variety match; computes next range from `lastSentDay`.

### POST `/api/consultant/schedules/send`
- **Use:** send/freeze schedule days/tasks to farmer.
- **Validation:** scheduleDays non-empty, nested task validation.
- **Behavior:** sorts days, stores snapshot in `farmer_crop_variety_schedule`, `farmer_schedule_days`, `farmer_schedule_tasks`.

### GET `/api/consultant/farmer/{farmerId}/schedules/{farmerCropVarietyId}`
- **Use:** consultant reads assigned schedules for farmer crop.

## 4.5 Consultant Advertisement APIs

### POST `/api/consultant/advertisements`
- **Use:** create ad visible to farmers.
- **Validation:** type must be `image|video|text`; priority >=0; rate limit `20/60s`.
- **Behavior:** normalizes type to lowercase.

### GET `/api/consultant/advertisements`
- **Use:** list own ads ordered by priority.

### PUT `/api/consultant/advertisements/{advertisementId}`
- **Use:** update own ad.
- **Behavior:** ownership check; throws `403` if ad belongs to another consultant.

### DELETE `/api/consultant/advertisements/{advertisementId}`
- **Use:** delete own ad.
- **Behavior:** ownership check before delete.

## 4.6 Farmer APIs

### GET `/api/farmer/crop-varieties`
- **Use:** current farmer's assigned crop varieties.

### GET `/api/farmer/schedules/{farmerCropVarietyId}`
- **Use:** schedule history/details for one assignment.
- **Validation:** path ID positive.

### GET `/api/farmer/consultant-advertisements`
- **Use:** all consultant ads for farmer home page.
- **Behavior:** global list ordered by priority.

### GET `/api/farmer/profile`
- **Use:** farmer profile card with consultant info and personal/farm info.

## 4.7 Master Schedule CRUD APIs (`/api/consultant/schedule`)

### GET `/templates`
- **Use:** list consultant templates.

### POST `/import-excel`
- **Use:** create/update template from `.xlsx`.
- **Required multipart fields:** `crop_variety_id`, `version`, `status`, `file`.
- **Optional:** `description`, `create_new_version`.
- **Behavior:** validates file not empty and `.xlsx`; parses day/task rows; all-or-nothing transactional import.
- **Excel required columns per row:** `day_number`, `title`, `quantity` (others optional depending task).

### GET `/templates/{template_id}`
- **Use:** template details including days and tasks.

### PUT `/templates/{template_id}`
- **Use:** update template metadata (`description`, `status`).

### DELETE `/templates/{template_id}`
- **Use:** delete template.

### POST `/templates/{template_id}/days`
- **Use:** add day to template.
- **Behavior:** blocks duplicate `dayNumber` inside same template.

### GET/PUT/DELETE `/templates/{template_id}/days/{day_id}`
- **Use:** read/update/delete specific day.
- **Behavior:** verifies day belongs to template.

### POST/PUT/DELETE/GET `/templates/{template_id}/days/{day_id}/tasks{/task_id}`
- **Use:** task CRUD under a template day.
- **Behavior:** verifies task->day->template chain before update/delete/read.

---

## 5. Request/Response Body Contracts

## 5.1 Authentication DTOs

- **LoginRequest:** `mobile`, `password`
- **RefreshTokenRequest:** `refreshToken`
- **UpdatePasswordRequest:** `oldPassword`, `newPassword`
- **LoginResponse:** `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `role`, `mobile`, `userDetails`
- **UserDetailsResponse:** user fields + nested `address`

## 5.2 User/Crop/Farmer DTOs

- **ConsultantRegistrationRequest:** `mobile,password,firstName,lastName,email`
- **FarmerRegistrationRequest:** `mobile,firstName,lastName,email,addressLine,city,district,state,postalCode,sector`
- **UserResponse:** `id,mobile,role,status,firstName,lastName,email,mobileVerified,emailVerified,createdAt,generatedPassword`
- **CropRegistrationRequest:** `name,description`
- **CropVarietyRegistrationRequest:** `cropId,name,description,climate,yieldPotential,cycleDurationDays`
- **AssignCropVarietyRequest:** `cropVarietyId,totalLand,totalPlants,sowingDate,expectedHarvestDate,status`
- **FarmerProfileResponse / FarmerProfileDetailResponse / FarmerPortfolioResponse / FarmerCropVarietyResponse:** profile and crop-assignment projections from service mapping.

## 5.3 Schedule Dispatch DTOs

- **GetNextSchedulePreviewRequest:** `farmerId,farmerCropVarietyId,masterScheduleTemplateId,numberOfDays`
- **GetNextSchedulePreviewResponse:** same IDs + `startDay,endDay,totalDays,scheduleDays`
- **SendScheduleRequest:** `farmerId,farmerCropVarietyId,numberOfDays,scheduleDays[]`
- **SendScheduleDayRequest:** `dayNumber,dayTitle,dayDescription,status,displayOrder,tasks[]`
- **SendScheduleTaskRequest:** `taskType,taskDescription,fertilizerName,quantity,proportion,priority`
- **SendScheduleResponse:** `farmerId,varietyId,scheduleId,daysSent,startDay,endDay,message`

## 5.4 Advertisement DTOs

- **ConsultantAdvertisementRequest:** `url,title,descriptions,priority,type`
- **ConsultantAdvertisementResponse:** `id,url,title,descriptions,priority,type,consultantId,consultantName,createdAt,updatedAt`

## 5.5 Master Schedule DTOs

- **CreateTemplateRequestDTO:** `cropVarietyId,version,description,status`
- **UpdateTemplateRequestDTO:** `description,status`
- **CreateDayRequestDTO:** `dayNumber,title,description,displayOrder`
- **CreateTaskRequestDTO:** `fertilizerName,quantity,proportion,priority,description,taskType`
- **MasterScheduleTemplateDTO:** template metadata
- **MasterScheduleTemplateWithDaysDTO:** template + `scheduleDays[]`
- **MasterScheduleDayDTO:** day + optional `tasks[]`
- **MasterScheduleTaskDTO:** task fields
- **TemplateImportResponseDTO:** import success/error metadata

---

## 6. Rate-Limited Endpoints

- `POST /api/auth/login` -> `5` requests / `60s`
- `POST /api/auth/refresh` -> `10` requests / `60s`
- `POST /api/admin/consultants` -> `10` requests / `60s`
- `POST /api/admin/crops` -> `20` requests / `60s`
- `POST /api/consultant/farmers` -> `20` requests / `60s`
- `GET /api/consultant/crops` -> `20` requests / `60s`
- `GET /api/consultant/crops-with-varieties` -> `20` requests / `60s`
- `POST /api/consultant/crop-varieties` -> `20` requests / `60s`
- `GET /api/consultant/crop-varieties/{cropVarietyId}/master-schedules` -> `20` requests / `60s`
- `POST /api/consultant/farmers/{farmerId}/crops` -> `20` requests / `60s`
- `POST /api/consultant/advertisements` -> `20` requests / `60s`

---

## 7. Important Implementation Notes

- JWT carries `userId`, `mobile`, `role`; subject is mobile.
- Access token expiry is configured as `3600000 ms` (1 hour).
- Refresh token expiry is configured as `604800000 ms` (7 days).
- Master schedule APIs use a different response envelope (`ApiResponseDTO` / `TemplateImportResponseDTO`) than other modules.
- `GET` is globally permitted by HTTP security config, but most business endpoints still enforce role checks via `@PreAuthorize` at controller level.
