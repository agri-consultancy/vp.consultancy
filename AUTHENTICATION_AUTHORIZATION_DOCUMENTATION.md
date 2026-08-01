# Authentication, Roles, Role-Mapping, and User Identity Documentation

## 1. Authentication Architecture

- **Mechanism:** stateless JWT authentication with Spring Security.
- **JWT filter:** `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`, validates token, loads user by mobile, and sets `SecurityContext`.
- **Token utility:** `JwtTokenUtil`
  - signs with `app.jwt.secret`
  - access token expiry: `app.jwt.expiration-ms` (`3600000` ms)
  - claims added: `userId`, `mobile`, `role`
  - JWT subject: mobile number
- **Password storage:** encoded via Spring `PasswordEncoder` (bcrypt-compatible).

## 2. Login / Refresh / Logout Flow

## 2.1 Login (`POST /api/auth/login`)

1. Validates `mobile` and `password`.
2. Authenticates using `AuthenticationManager`.
3. Generates JWT access token with role/user claims.
4. Creates refresh token in DB (`refresh_token` table).
5. Returns `LoginResponse`.

## 2.2 Refresh (`POST /api/auth/refresh`)

1. Validates refresh token input.
2. Finds token in DB.
3. Rejects if not found or expired.
4. Generates a **new access token**.
5. Returns same refresh token in response.

## 2.3 Logout (`POST /api/auth/logout/{userId}`)

1. Validates `userId`.
2. Deletes all refresh tokens for that user (`deleteByUserId`).
3. User must login again to get new refresh token.

## 3. Roles and Role Mapping

## 3.1 Defined roles

- `ADMIN`
- `CONSULTANT`
- `FARMER`

Stored in `users.role` (`UserRole` enum).

## 3.2 Spring authority mapping

- Runtime authority format: `ROLE_<ROLE_NAME>`
- Mapping done in `UserServiceImpl#getAuthorities`.
- Example: `CONSULTANT` -> `ROLE_CONSULTANT`.

## 3.3 Endpoint role mapping

| Endpoint area | Required role |
|---|---|
| `/api/admin/**` | ADMIN |
| `/api/consultant/**` | CONSULTANT |
| `/api/farmer/**` | FARMER |
| `/api/auth/change-password` | any authenticated user |
| `/api/auth/user-profile` | any authenticated user |
| `/api/auth/login`, `/api/auth/refresh`, `/api/auth/admins` | public |

## 3.4 Behavior notes

- Controller-level `@PreAuthorize` is primary RBAC enforcement for business APIs.
- `POST /api/auth/admins` is intentionally public for bootstrap.
- HTTP security currently permits all `GET` requests globally, but protected controllers still enforce role checks by method security.

## 4. User Profile Fields

## 4.1 Core auth user (`users` / `User`)

- `id`
- `mobile` (unique)
- `password` (hashed)
- `role`
- `status`
- `isMobileVerified`
- `isEmailVerified`
- `failedLoginAttempts`
- `createdAt`
- `updatedAt`

## 4.2 Extended profile (`user_profiles` / `UserProfile`)

- `id`
- `firstName`
- `lastName`
- `email` (unique)
- `user` (1:1 with `users`)
- `consultant` (farmer -> consultant link)
- `address`
- `sector`
- `createdAt`
- `updatedAt`

## 4.3 Address (`address`)

- `id`
- `addressLine`
- `city`
- `district`
- `state`
- `postalCode`
- `createdAt`
- `updatedAt`

## 4.4 Profile API output (`GET /api/auth/user-profile`)

Returns `UserDetailsResponse` with:
- user identity: `id,mobile,role,status`
-(if present) profile: `firstName,lastName,email`
-(if present) address object
-(if farmer) `consultantId`

## 5. Email/Mobile Verification Status

- **Exists in data model:** Yes (`is_mobile_verified`, `is_email_verified`).
- **Default behavior:** both set to `false` on entity creation (`@PrePersist`).
- **Verification APIs/workflow present:** No dedicated verification endpoints or OTP flow found in current code.
- **Exposure in responses:** fields exist in `UserResponse` as `mobileVerified` and `emailVerified`.

## 6. Refresh Token Handling

## 6.1 Storage model

- Table: `refresh_token`
- Fields: `id`, `token` (unique UUID string), `expiryDate`, `user_id`, `createdAt`

## 6.2 Token creation

- Done at login via `RefreshTokenServiceImpl#createRefreshToken`.
- Token value generated as `UUID.randomUUID().toString()`.
- Expiry set from `app.jwt.refresh-ms` (configured to 7 days).

## 6.3 Token validation

- Refresh API looks up token in DB.
- Expired tokens are rejected.
- Missing tokens are rejected.

## 6.4 Rotation/reuse behavior

- Refresh API issues new access token.
- Existing refresh token is reused (no rotation on refresh).
- Logout deletes all refresh tokens for user (global session revoke for that user).

## 7. Authentication API Contracts

## 7.1 LoginRequest

```json
{
  "mobile": "10-digit string",
  "password": "6-100 chars"
}
```

## 7.2 LoginResponse

```json
{
  "accessToken": "jwt",
  "refreshToken": "uuid-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "role": "ADMIN|CONSULTANT|FARMER",
  "mobile": "string",
  "userDetails": {
    "id": 0,
    "firstName": "string",
    "lastName": "string",
    "email": "string"
  }
}
```

## 7.3 RefreshTokenRequest

```json
{
  "refreshToken": "string"
}
```

## 7.4 UpdatePasswordRequest

```json
{
  "oldPassword": "string",
  "newPassword": "8-100 chars"
}
```

## 8. Security and Rate-Limit Controls

- `POST /api/auth/login` -> rate limit `5/60s`
- `POST /api/auth/refresh` -> rate limit `10/60s`
- Auth exceptions are normalized by `GlobalExceptionHandler`.
- JWT validation failures and invalid credentials return `401`.
