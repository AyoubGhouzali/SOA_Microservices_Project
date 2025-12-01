# Container Testing Guide - Transport System

## 🚀 Starting All Services

### Step 1: Start Docker Compose

```bash
cd C:\Users\ayoub\Desktop\transport-system

# Start all services
docker-compose up -d

# Watch logs
docker-compose logs -f
```

Wait 3-5 minutes for all services to initialize.

---

## ✅ Verify All Containers are Running

```bash
# Check status of all containers
docker-compose ps
```

**Expected Output: All services should be "Up" or "healthy"**

```
NAME                    STATUS
config-server           Up (healthy)
discovery-server        Up (healthy)
api-gateway             Up
user-service            Up
ticketing-service       Up
payment-service         Up
subscription-service    Up
scheduling-service      Up
tracking-service        Up
notification-service    Up
kafka                   Up
zookeeper               Up
redis                   Up
user-postgres           Up (healthy)
ticketing-postgres      Up (healthy)
payment-postgres        Up (healthy)
subscription-postgres   Up (healthy)
scheduling-postgres     Up (healthy)
tracking-mongodb        Up
notification-mongodb    Up
```

---

## 🔍 Health Check Tests

### 1. Check Eureka Dashboard

```bash
# Open browser
http://localhost:8761
```

**Expected**: All services registered in Eureka:
- API-GATEWAY
- USER-SERVICE
- TICKETING-SERVICE
- PAYMENT-SERVICE
- SUBSCRIPTION-SERVICE
- SCHEDULING-SERVICE
- TRACKING-SERVICE
- NOTIFICATION-SERVICE

### 2. Test Individual Service Health

```bash
# Config Server
curl http://localhost:8888/actuator/health

# Discovery Server
curl http://localhost:8761/actuator/health

# Via API Gateway (all services)
curl http://localhost:8222/api/users/health
curl http://localhost:8222/api/tickets/health
curl http://localhost:8222/api/payments/health
curl http://localhost:8222/api/subscriptions/health
curl http://localhost:8222/api/routes/health
curl http://localhost:8222/api/buses/health
curl http://localhost:8222/api/notifications/health
```

**Expected**: All return `200 OK` with health status

---

## 🧪 End-to-End Testing

### Test 1: User Registration & Login

#### 1.1 Register a New User

```bash
curl -X POST http://localhost:8222/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "TestPassword123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Expected Response (201 Created)**:
```json
{
  "userId": "uuid-here",
  "email": "test.user@example.com",
  "firstName": "Test",
  "lastName": "User",
  "role": "PASSENGER",
  "status": "ACTIVE",
  "createdAt": "2024-12-01T..."
}
```

**✅ Check**: User created in database
```bash
docker exec -it user-postgres psql -U user_admin -d user_db -c "SELECT * FROM users;"
```

#### 1.2 Login

```bash
curl -X POST http://localhost:8222/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "TestPassword123"
  }'
```

**Expected Response (200 OK)**:
```json
{
  "userId": "uuid-here",
  "email": "test.user@example.com",
  "firstName": "Test",
  "lastName": "User",
  "role": "PASSENGER",
  "status": "ACTIVE",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "message": "Login successful"
}
```

**✅ Save the token and userId for next tests!**

---

### Test 2: Purchase Tickets (Full Flow with Payment)

```bash
# Save your token and userId
TOKEN="your-jwt-token-here"
USER_ID="your-user-id-here"

# Purchase 2 daily tickets
curl -X POST http://localhost:8222/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "'$USER_ID'",
    "ticketType": "DAILY",
    "quantity": 2
  }'
```

**Expected Response (200 OK)**:
```json
{
  "orderId": "uuid-here",
  "userId": "uuid-here",
  "totalAmount": 10.00,
  "currency": "USD",
  "paymentStatus": "PENDING",
  "tickets": [
    {
      "ticketId": "uuid-1",
      "type": "DAILY",
      "status": "PURCHASED",
      "price": 5.00,
      "qrCode": "TICKET-uuid-1"
    },
    {
      "ticketId": "uuid-2",
      "type": "DAILY",
      "status": "PURCHASED",
      "price": 5.00,
      "qrCode": "TICKET-uuid-2"
    }
  ]
}
```

**✅ What Happens Behind the Scenes**:
1. Ticketing service creates tickets
2. Publishes `TicketPurchasedEvent` to Kafka
3. Payment service consumes event
4. Simulates payment (95% success, 500ms delay)
5. Publishes `PaymentProcessedEvent` to Kafka

**✅ Verify in Kafka UI**:
```
http://localhost:8090
Topics -> ticket.purchased (should show messages)
Topics -> payment.processed (should show messages)
```

**✅ Check Payment in Database**:
```bash
docker exec -it payment-postgres psql -U payment_admin -d payment_db -c "SELECT * FROM payments;"
```

---

### Test 3: View User Tickets

```bash
curl -X GET http://localhost:8222/api/tickets/user/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: List of purchased tickets

---

### Test 4: Activate Ticket

```bash
TICKET_ID="your-ticket-id-from-purchase"

curl -X POST http://localhost:8222/api/tickets/activate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "ticketId": "'$TICKET_ID'"
  }'
```

**Expected**: Ticket status changes to `ACTIVE`, validity period starts

---

### Test 5: Create Subscription

```bash
curl -X POST http://localhost:8222/api/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "'$USER_ID'",
    "type": "MONTHLY"
  }'
```

**Expected Response (201 Created)**:
```json
{
  "subscriptionId": "uuid-here",
  "userId": "uuid-here",
  "type": "MONTHLY",
  "status": "PENDING",
  "price": 50.00,
  "currency": "USD",
  "startDate": null,
  "endDate": null,
  "autoRenew": false
}
```

---

### Test 6: Create Route & Bus

#### 6.1 Create Route

```bash
curl -X POST http://localhost:8222/api/routes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "routeNumber": "101",
    "name": "Downtown Express",
    "description": "Fast route to downtown",
    "type": "URBAN",
    "totalDistance": 15.5,
    "estimatedDuration": 45,
    "color": "#FF5733"
  }'
```

#### 6.2 Create Bus

```bash
curl -X POST http://localhost:8222/api/buses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "busNumber": "BUS-001",
    "licensePlate": "ABC-1234",
    "type": "STANDARD",
    "status": "INACTIVE",
    "capacity": 50,
    "currentPassengers": 0
  }'
```

---

### Test 7: Bus Event & Notification

```bash
# Report a bus delay
curl -X POST http://localhost:8222/api/bus-events/delay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "busNumber": "BUS-001",
    "routeNumber": "101",
    "delayMinutes": 15,
    "message": "Heavy traffic on Main Street",
    "latitude": 40.7128,
    "longitude": -74.0060
  }'
```

**✅ Check Kafka UI**:
```
http://localhost:8090
Topic: bus-delays (should have message)
```

**✅ Check Notification Service Logs**:
```bash
docker-compose logs notification-service
```

You should see email notification being sent!

**✅ Check Notification History**:
```bash
curl -X GET http://localhost:8222/api/notifications/recent \
  -H "Authorization: Bearer $TOKEN"
```

---

### Test 8: Payment Statistics

```bash
curl -X GET http://localhost:8222/api/payments/stats \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**:
```json
{
  "totalPayments": 1,
  "completedPayments": 1,
  "failedPayments": 0,
  "pendingPayments": 0,
  "totalRevenue": 10.00
}
```

---

## 🔍 Database Inspection

### PostgreSQL Databases

```bash
# User Database
docker exec -it user-postgres psql -U user_admin -d user_db -c "\dt"
docker exec -it user-postgres psql -U user_admin -d user_db -c "SELECT email, role, status FROM users;"

# Ticketing Database
docker exec -it ticketing-postgres psql -U ticket_admin -d ticketing_db -c "SELECT * FROM tickets;"

# Payment Database
docker exec -it payment-postgres psql -U payment_admin -d payment_db -c "SELECT * FROM payments;"

# Subscription Database
docker exec -it subscription-postgres psql -U sub_admin -d subscription_db -c "SELECT * FROM subscriptions;"

# Scheduling Database
docker exec -it scheduling-postgres psql -U scheduling_admin -d scheduling_db -c "SELECT * FROM routes;"
```

### MongoDB Databases

```bash
# Access Mongo Express UI
http://localhost:8084

# Or via CLI
docker exec -it tracking-mongodb mongosh tracking_db
> db.buses.find().pretty()
> db.bus_locations.find().pretty()

docker exec -it notification-mongodb mongosh notification_db
> db.notification_history.find().pretty()
```

---

## 📊 Admin UIs

| Service | URL | Credentials |
|---------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | None |
| Kafka UI | http://localhost:8090 | None |
| PgAdmin | http://localhost:5050 | admin@transport.com / admin123 |
| Mongo Express | http://localhost:8084 | None |
| Redis Commander | http://localhost:8083 | None |

---

## 🐛 Troubleshooting

### Services Not Starting

```bash
# Check logs for specific service
docker-compose logs service-name

# Example:
docker-compose logs payment-service
docker-compose logs kafka

# Restart specific service
docker-compose restart service-name
```

### Database Connection Issues

```bash
# Check if database is running
docker-compose ps | grep postgres

# Check database logs
docker-compose logs user-postgres

# Verify connection
docker exec -it user-postgres pg_isready -U user_admin
```

### Kafka Issues

```bash
# Check Kafka is running
docker-compose logs kafka
docker-compose logs zookeeper

# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9093

# Check consumer groups
docker exec -it kafka kafka-consumer-groups --list --bootstrap-server localhost:9093
```

### JWT Token Issues

```bash
# Check if JWT_SECRET is set
echo $JWT_SECRET

# Verify token at jwt.io
# Copy token and paste at https://jwt.io
# Secret should be: mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
```

---

## 📝 Complete Test Script

Save as `test-all-services.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8222"

echo "==================================="
echo "Testing Transport System Services"
echo "==================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Test 1: Health Checks
echo -e "\n${GREEN}Test 1: Health Checks${NC}"
curl -s http://localhost:8222/api/users/health && echo " ✓ User Service"
curl -s http://localhost:8222/api/tickets/health && echo " ✓ Ticketing Service"
curl -s http://localhost:8222/api/payments/health && echo " ✓ Payment Service"

# Test 2: Register User
echo -e "\n${GREEN}Test 2: Register User${NC}"
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "automated.test@example.com",
    "password": "Test12345",
    "firstName": "Auto",
    "lastName": "Test"
  }')

echo $REGISTER_RESPONSE | jq '.'
USER_ID=$(echo $REGISTER_RESPONSE | jq -r '.userId')
echo "User ID: $USER_ID"

# Test 3: Login
echo -e "\n${GREEN}Test 3: Login${NC}"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "automated.test@example.com",
    "password": "Test12345"
  }')

echo $LOGIN_RESPONSE | jq '.'
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "Token: ${TOKEN:0:50}..."

# Test 4: Purchase Tickets
echo -e "\n${GREEN}Test 4: Purchase Tickets${NC}"
PURCHASE_RESPONSE=$(curl -s -X POST $BASE_URL/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "'$USER_ID'",
    "ticketType": "DAILY",
    "quantity": 2
  }')

echo $PURCHASE_RESPONSE | jq '.'
ORDER_ID=$(echo $PURCHASE_RESPONSE | jq -r '.orderId')
echo "Order ID: $ORDER_ID"

# Wait for payment processing
echo -e "\n${GREEN}Waiting 2 seconds for payment processing...${NC}"
sleep 2

# Test 5: Check Payment
echo -e "\n${GREEN}Test 5: Check Payment Status${NC}"
curl -s -X GET $BASE_URL/api/payments/order/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.'

# Test 6: Payment Stats
echo -e "\n${GREEN}Test 6: Payment Statistics${NC}"
curl -s -X GET $BASE_URL/api/payments/stats \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n${GREEN}==================================="
echo "All Tests Completed!"
echo "===================================${NC}"
```

Run with:
```bash
chmod +x test-all-services.sh
./test-all-services.sh
```

---

## ✅ Success Criteria

Your system is working correctly if:

- [x] All containers are running (`docker-compose ps` shows "Up")
- [x] All services registered in Eureka (http://localhost:8761)
- [x] Health checks return 200 OK
- [x] User can register successfully
- [x] User can login and receive JWT token
- [x] User can purchase tickets with JWT authentication
- [x] Kafka events are published (check Kafka UI)
- [x] Payment service processes tickets automatically
- [x] Payment records created in database
- [x] Bus events trigger notifications
- [x] All databases are accessible

---

## 🚀 Next Steps

Once all tests pass:
1. ✅ All services are working
2. ✅ Ready for Kubernetes deployment
3. ✅ Ready for frontend integration

---

**Last Updated**: December 1, 2024
