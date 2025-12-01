# JWT Authentication Guide

## Overview
JWT (JSON Web Token) authentication has been implemented in the Transport System to secure API endpoints. This guide explains how to use JWT tokens to authenticate requests.

---

## 🔐 How JWT Authentication Works

1. **User logs in** → Receives JWT token
2. **Include token in requests** → API Gateway validates token
3. **Access protected resources** → Token required for most endpoints

---

## 📝 Implementation Details

### API Gateway - JWT Validation
- **Location**: `services/apiGateway/src/main/java/com/transport/apiGateway/security/`
- **Components**:
  - `JwtUtil.java` - Token validation and claim extraction
  - `JwtAuthenticationFilter.java` - Global filter for all requests
  - `SecurityConfig.java` - Spring Security configuration

### User Service - JWT Generation
- **Location**: `services/user-service/src/main/java/com/transport/user/infrastructure/security/`
- **Components**:
  - `JwtTokenProvider.java` - Token generation on login

### Token Structure
```json
{
  "sub": "user@example.com",      // Email (subject)
  "userId": "uuid-here",           // User ID
  "role": "PASSENGER",             // User role (ADMIN, PASSENGER, DRIVER)
  "iat": 1234567890,              // Issued at timestamp
  "exp": 1234654290               // Expiration timestamp (24 hours)
}
```

---

## 🔑 Public vs Protected Endpoints

### Public Endpoints (No JWT Required)
```
POST   /api/users/register       - Register new user
POST   /api/users/login          - Login (returns JWT)
GET    /api/*/health             - Health checks for all services
GET    /actuator/*               - Actuator endpoints
GET    /fallback                 - Circuit breaker fallback
```

### Protected Endpoints (JWT Required)
```
All other endpoints require a valid JWT token in the Authorization header
```

---

## 🚀 Testing JWT Authentication

### Step 1: Register a New User

```bash
curl -X POST http://localhost:8222/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PASSENGER",
  "status": "ACTIVE",
  "createdAt": "2024-12-01T10:30:00"
}
```

---

### Step 2: Login and Get JWT Token

```bash
curl -X POST http://localhost:8222/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PASSENGER",
  "status": "ACTIVE",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJyb2xlIjoiUEFTU0VOR0VSIiwic3ViIjoiam9obi5kb2VAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDE0MzE0MDAsImV4cCI6MTcwMTUxNzgwMH0.signature",
  "tokenType": "Bearer",
  "message": "Login successful"
}
```

**⚠️ Save the `token` value - you'll need it for all subsequent requests!**

---

### Step 3: Access Protected Endpoints with JWT

#### Purchase Tickets (Protected)

```bash
# Save token to variable (Linux/Mac)
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOi..."

# Or for Windows PowerShell
$TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOi..."

# Make authenticated request
curl -X POST http://localhost:8222/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "ticketType": "DAILY",
    "quantity": 2
  }'
```

**Success Response (200 OK):**
```json
{
  "orderId": "order-uuid",
  "userId": "user-uuid",
  "totalAmount": 10.00,
  "currency": "USD",
  "paymentStatus": "PENDING",
  "tickets": [...]
}
```

---

#### Get User Profile (Protected)

```bash
curl -X GET http://localhost:8222/api/users/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer $TOKEN"
```

---

#### Create Subscription (Protected)

```bash
curl -X POST http://localhost:8222/api/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "type": "MONTHLY"
  }'
```

---

### Step 4: Test Authentication Failures

#### Request Without Token (401 Unauthorized)

```bash
curl -X POST http://localhost:8222/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "ticketType": "DAILY",
    "quantity": 2
  }'
```

**Error Response:**
```json
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

---

#### Request With Invalid Token (401 Unauthorized)

```bash
curl -X POST http://localhost:8222/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid.token.here" \
  -d '{
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "ticketType": "DAILY",
    "quantity": 2
  }'
```

**Error Response:**
```json
{
  "error": "Invalid or expired JWT token",
  "status": 401
}
```

---

## 🔧 Configuration

### Environment Variables

Both API Gateway and User Service need the same JWT secret:

**.env file:**
```bash
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000  # 24 hours in milliseconds
```

**⚠️ IMPORTANT:** In production, use a strong, random secret key!

### Docker Compose

JWT secret is passed via environment variables:

```yaml
api-gateway:
  environment:
    JWT_SECRET: ${JWT_SECRET}
    JWT_EXPIRATION: ${JWT_EXPIRATION}

user-service:
  environment:
    JWT_SECRET: ${JWT_SECRET}
    JWT_EXPIRATION: ${JWT_EXPIRATION}
```

---

## 🧪 Testing Tools

### Using Postman

1. **Create Environment Variable**:
   - Variable: `jwt_token`
   - Initial Value: (leave empty)

2. **Login Request**:
   - Method: POST
   - URL: `http://localhost:8222/api/users/login`
   - Body: Raw JSON
   ```json
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```
   - Tests tab:
   ```javascript
   var jsonData = pm.response.json();
   pm.environment.set("jwt_token", jsonData.token);
   ```

3. **Protected Requests**:
   - Headers:
     - Key: `Authorization`
     - Value: `Bearer {{jwt_token}}`

### Using cURL with Variables

**Linux/Mac:**
```bash
# Login and extract token
TOKEN=$(curl -s -X POST http://localhost:8222/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  | jq -r '.token')

# Use token
curl -X GET http://localhost:8222/api/users/{userId} \
  -H "Authorization: Bearer $TOKEN"
```

**Windows PowerShell:**
```powershell
# Login and extract token
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8222/api/users/login" `
  -ContentType "application/json" `
  -Body '{"email":"user@example.com","password":"password123"}'

$TOKEN = $response.token

# Use token
Invoke-RestMethod -Method Get -Uri "http://localhost:8222/api/users/{userId}" `
  -Headers @{Authorization="Bearer $TOKEN"}
```

---

## 🛡️ Security Features

### What JWT Provides

✅ **Stateless Authentication** - No session storage needed
✅ **Secure** - Signed with HMAC-SHA256
✅ **Self-contained** - Includes user ID, email, and role
✅ **Expiring** - Tokens expire after 24 hours
✅ **Tamper-proof** - Invalid signatures are rejected

### What to Implement Next (Production)

⚠️ **Token Refresh** - Add refresh token mechanism
⚠️ **Token Blacklist** - Revoke tokens on logout
⚠️ **HTTPS Only** - Never send tokens over HTTP
⚠️ **Rate Limiting** - Prevent brute force attacks
⚠️ **Strong Secret** - Use 256-bit random secret key
⚠️ **Shorter Expiration** - Consider 1-hour tokens with refresh

---

## 📊 Request Flow with JWT

```
┌─────────┐           ┌─────────────┐           ┌──────────────┐
│ Client  │           │ API Gateway │           │ User Service │
└────┬────┘           └──────┬──────┘           └──────┬───────┘
     │                       │                         │
     │ POST /users/login     │                         │
     ├──────────────────────>│ Forward to user-service │
     │                       ├────────────────────────>│
     │                       │                         │
     │                       │   Generate JWT Token    │
     │                       │<────────────────────────┤
     │  Return JWT Token     │                         │
     │<──────────────────────┤                         │
     │                       │                         │
     │ POST /tickets/purchase│                         │
     │ Authorization: Bearer │                         │
     ├──────────────────────>│                         │
     │                       │                         │
     │                  Validate JWT                   │
     │                  Extract user info              │
     │                  Add headers:                   │
     │                  - X-User-Id                    │
     │                  - X-User-Email                 │
     │                  - X-User-Role                  │
     │                       │                         │
     │                       │  Forward with headers   │
     │                       ├────────────────────────>│
     │                       │                         │
     │                       │   (Ticketing Service)   │
     │    Return response    │                         │
     │<──────────────────────┤                         │
     │                       │                         │
```

---

## 🔍 Debugging JWT Issues

### Check Token Validity

Use [jwt.io](https://jwt.io) to decode and inspect tokens:

1. Copy your JWT token
2. Paste at jwt.io
3. Verify:
   - Header algorithm is HS256
   - Payload contains userId, role, sub (email)
   - Expiration (exp) is in the future
   - Signature is valid (paste secret key)

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| Missing Authorization header | No `Authorization` header in request | Add `Authorization: Bearer {token}` |
| Invalid token | Token is malformed or signature mismatch | Check JWT_SECRET matches in Gateway & User Service |
| Token expired | Token's exp claim is in the past | Login again to get new token |
| 401 on public endpoint | Public URL not in whitelist | Update `PUBLIC_URLS` in JwtAuthenticationFilter |

### Enable Debug Logging

**API Gateway:**
```yaml
logging:
  level:
    com.transport.apiGateway.security: DEBUG
```

**User Service:**
```yaml
logging:
  level:
    com.transport.user.infrastructure.security: DEBUG
```

Restart services and check logs for JWT validation details.

---

## 📚 API Endpoints Summary

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/users/register` | POST | ❌ No | Register new user |
| `/api/users/login` | POST | ❌ No | Login and get JWT |
| `/api/users/{id}` | GET | ✅ Yes | Get user profile |
| `/api/users/email/{email}` | GET | ✅ Yes | Get user by email |
| `/api/tickets/purchase` | POST | ✅ Yes | Purchase tickets |
| `/api/tickets/activate` | POST | ✅ Yes | Activate ticket |
| `/api/tickets/validate` | POST | ✅ Yes | Validate ticket |
| `/api/tickets/user/{userId}` | GET | ✅ Yes | Get user tickets |
| `/api/subscriptions` | POST | ✅ Yes | Create subscription |
| `/api/subscriptions/{id}` | GET | ✅ Yes | Get subscription |
| `/api/routes` | POST | ✅ Yes | Create route |
| `/api/routes` | GET | ✅ Yes | List routes |
| `/api/buses` | POST | ✅ Yes | Create bus |
| `/api/buses` | GET | ✅ Yes | List buses |
| `/api/payments/stats` | GET | ✅ Yes | Payment statistics |

---

## 🎯 Quick Testing Script

**test-jwt.sh** (Linux/Mac):
```bash
#!/bin/bash

BASE_URL="http://localhost:8222"

# Register user
echo "1. Registering user..."
curl -s -X POST $BASE_URL/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test12345",
    "firstName": "Test",
    "lastName": "User"
  }' | jq

# Login and get token
echo -e "\n2. Logging in..."
TOKEN=$(curl -s -X POST $BASE_URL/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test12345"
  }' | jq -r '.token')

echo "Token: $TOKEN"

# Test protected endpoint WITH token
echo -e "\n3. Testing protected endpoint WITH token..."
curl -s -X GET $BASE_URL/api/users/email/test@example.com \
  -H "Authorization: Bearer $TOKEN" | jq

# Test protected endpoint WITHOUT token
echo -e "\n4. Testing protected endpoint WITHOUT token (should fail)..."
curl -s -X GET $BASE_URL/api/users/email/test@example.com | jq
```

Run: `chmod +x test-jwt.sh && ./test-jwt.sh`

---

## ✅ JWT Implementation Checklist

- [x] JWT dependencies added to API Gateway
- [x] JWT dependencies added to User Service
- [x] JwtUtil created for token validation (Gateway)
- [x] JwtTokenProvider created for token generation (User Service)
- [x] JwtAuthenticationFilter created (Gateway)
- [x] SecurityConfig configured (Gateway)
- [x] Public endpoints whitelist configured
- [x] Login endpoint returns JWT token
- [x] JWT secret configured in both services
- [x] Token expiration configured (24 hours)
- [x] User info added to request headers for downstream services

---

## 🚀 Next Steps

1. **Test the implementation**:
   ```bash
   docker-compose down
   docker-compose up --build
   ```

2. **Try the authentication flow** using examples above

3. **Optional enhancements**:
   - Add refresh token endpoint
   - Implement token blacklist for logout
   - Add role-based access control (RBAC)
   - Implement password reset with JWT
   - Add email verification with JWT

---

**Updated:** December 1, 2024
**Status:** ✅ JWT Authentication Fully Implemented
