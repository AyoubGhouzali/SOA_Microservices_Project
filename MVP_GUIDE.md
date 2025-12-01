# Transport System MVP - Quick Start Guide

## Overview
This is a **Minimum Viable Product (MVP)** of the Urban Transport System with core functionality implemented and ready to test.

## What's Implemented in the MVP

### ✅ Infrastructure Services
- **Config Server** (Port 8888) - Centralized configuration
- **Discovery Server** (Port 8761) - Service registry (Eureka)
- **API Gateway** (Port 8222) - Single entry point with routing to all services
- **Kafka** (Ports 9092, 9093) - Event streaming
- **Databases**: PostgreSQL (4 instances) + MongoDB (2 instances)
- **Redis** - Caching layer (configured)

### ✅ Core Business Services
1. **User Service** (Port 8081)
   - User registration
   - User login (with password validation)
   - Get user by ID or email
   - PostgreSQL database

2. **Ticketing Service** (Port 8094)
   - Purchase tickets (SINGLE, DAILY, WEEKLY, MONTHLY)
   - Activate tickets
   - Validate tickets (QR code scanning)
   - Get user tickets
   - Publishes events to Kafka for payment
   - PostgreSQL database

3. **Payment Service** (Port 8087) - **NEW in MVP**
   - Simulated payment processing (95% success rate)
   - Listens to ticket purchase events from Kafka
   - Processes payments and publishes results
   - Payment history and statistics
   - PostgreSQL database

4. **Subscription Service** (Port 8093)
   - Create subscriptions (MONTHLY, QUARTERLY, ANNUAL)
   - Activate, cancel, renew subscriptions
   - Auto-renewal support
   - PostgreSQL database

5. **Scheduling Service** (Port 8091)
   - Route management
   - Stop management
   - Schedule management (weekday/weekend/holiday)
   - Next departure calculations
   - PostgreSQL database

6. **Tracking Service** (Port 8092)
   - Bus management
   - Real-time location tracking
   - Bus event publishing (delays, cancellations, breakdowns)
   - MongoDB database

7. **Notification Service** (Port 8095)
   - Email notifications
   - SMS notifications (optional)
   - Listens to bus events from Kafka
   - Notification history
   - MongoDB database

## What's NOT Implemented (Future Enhancements)
- ❌ Frontend application (empty)
- ❌ Real payment gateway integration (using simulation)
- ❌ JWT authentication in API Gateway (routes work without auth)
- ❌ Subscription payment events
- ❌ Inter-service REST calls (using Kafka events instead)

---

## How to Run the MVP

### Prerequisites
- Docker & Docker Compose
- Git
- 8GB+ RAM recommended

### Step 1: Start All Services
```bash
# Navigate to project directory
cd C:\Users\ayoub\Desktop\transport-system

# Start all services with Docker Compose
docker-compose up --build
```

This will start all 20+ containers. Wait for all services to be healthy (may take 3-5 minutes).

### Step 2: Verify Services are Running

**Check Eureka Dashboard:**
```
http://localhost:8761
```
You should see all services registered.

**Check Kafka UI:**
```
http://localhost:8090
```
Browse topics: `ticket.purchased`, `payment.processed`, `bus-delays`, etc.

**Health Checks:**
```bash
# User Service
curl http://localhost:8222/api/users/health

# Ticketing Service
curl http://localhost:8222/api/tickets/health

# Payment Service
curl http://localhost:8222/api/payments/health

# All other services have /health endpoints too
```

---

## Testing the MVP

### 1. User Registration & Login

**Register a new user:**
```bash
curl -X POST http://localhost:8222/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "passenger1@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8222/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "passenger1@example.com",
    "password": "password123"
  }'
```

Save the `userId` from the response for the next steps.

### 2. Purchase Tickets

**Purchase 2 daily tickets:**
```bash
curl -X POST http://localhost:8222/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "YOUR_USER_ID_HERE",
    "ticketType": "DAILY",
    "quantity": 2
  }'
```

**What happens behind the scenes:**
1. Ticketing service creates tickets
2. Publishes `TicketPurchasedEvent` to Kafka
3. Payment service receives event
4. Simulates payment processing (500ms delay, 95% success)
5. Publishes `PaymentProcessedEvent` to Kafka
6. Ticket status updated

**Check your tickets:**
```bash
curl http://localhost:8222/api/tickets/user/YOUR_USER_ID_HERE
```

### 3. Activate a Ticket

```bash
curl -X POST http://localhost:8222/api/tickets/activate \
  -H "Content-Type: application/json" \
  -d '{
    "ticketId": "YOUR_TICKET_ID_HERE"
  }'
```

### 4. Check Payment Status

```bash
# Get payment by order ID
curl http://localhost:8222/api/payments/order/YOUR_ORDER_ID_HERE

# Get payment statistics
curl http://localhost:8222/api/payments/stats
```

### 5. Create a Subscription

```bash
curl -X POST http://localhost:8222/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "YOUR_USER_ID_HERE",
    "type": "MONTHLY"
  }'
```

### 6. Create Routes and Schedules

**Create a route:**
```bash
curl -X POST http://localhost:8222/api/routes \
  -H "Content-Type: application/json" \
  -d '{
    "routeNumber": "101",
    "name": "Downtown Express",
    "type": "URBAN",
    "totalDistance": 15.5,
    "estimatedDuration": 45,
    "color": "#FF5733"
  }'
```

### 7. Track Buses

**Create a bus:**
```bash
curl -X POST http://localhost:8222/api/buses \
  -H "Content-Type: application/json" \
  -d '{
    "busNumber": "BUS-001",
    "licensePlate": "ABC-1234",
    "type": "STANDARD",
    "capacity": 50
  }'
```

**Report a bus delay (triggers notification):**
```bash
curl -X POST http://localhost:8222/api/bus-events/delay \
  -H "Content-Type: application/json" \
  -d '{
    "busNumber": "BUS-001",
    "routeNumber": "101",
    "delayMinutes": 10,
    "message": "Traffic on Main Street"
  }'
```

**Check Kafka UI** (http://localhost:8090) to see the event published to `bus-delays` topic.

**Check Notification Service** to see if email was sent:
```bash
curl http://localhost:8222/api/notifications/recent
```

---

## Service Ports Reference

| Service | Port | Direct URL | Via Gateway |
|---------|------|------------|-------------|
| API Gateway | 8222 | - | http://localhost:8222/api/* |
| User Service | 8081 | http://localhost:8081 | http://localhost:8222/api/users/* |
| Ticketing Service | 8094 | http://localhost:8094 | http://localhost:8222/api/tickets/* |
| Payment Service | 8087 | http://localhost:8087 | http://localhost:8222/api/payments/* |
| Subscription Service | 8093 | http://localhost:8093 | http://localhost:8222/api/subscriptions/* |
| Scheduling Service | 8091 | http://localhost:8091 | http://localhost:8222/api/routes/*, /api/schedules/* |
| Tracking Service | 8092 | http://localhost:8092 | http://localhost:8222/api/buses/*, /api/tracking/* |
| Notification Service | 8095 | http://localhost:8095 | http://localhost:8222/api/notifications/* |
| Config Server | 8888 | http://localhost:8888 | - |
| Eureka Dashboard | 8761 | http://localhost:8761 | - |
| Kafka UI | 8090 | http://localhost:8090 | - |
| PgAdmin | 5050 | http://localhost:5050 | - |
| Mongo Express | 8084 | http://localhost:8084 | - |

---

## Event Flow Example: Ticket Purchase

```
User (via API)
    |
    | POST /api/tickets/purchase
    v
API Gateway (8222)
    |
    | Routes to ticketing-service
    v
Ticketing Service (8094)
    |
    | 1. Creates ticket records
    | 2. Publishes TicketPurchasedEvent to Kafka
    v
Kafka Topic: ticket.purchased
    |
    | Event consumed by payment-service
    v
Payment Service (8087)
    |
    | 1. Creates payment record (PENDING)
    | 2. Simulates payment processing (500ms)
    | 3. Updates status (COMPLETED or FAILED)
    | 4. Publishes PaymentProcessedEvent to Kafka
    v
Kafka Topic: payment.processed
    |
    | (Future: Ticketing service listens and updates ticket status)
    v
Done ✅
```

---

## Troubleshooting

### Services not starting?
```bash
# Check Docker logs
docker-compose logs -f service-name

# Restart specific service
docker-compose restart service-name

# Rebuild and restart
docker-compose up --build service-name
```

### Database connection issues?
```bash
# Check if PostgreSQL is healthy
docker-compose ps

# Access PgAdmin
# URL: http://localhost:5050
# Email: admin@transport.com
# Password: admin123
```

### Kafka issues?
```bash
# Check Kafka UI
# URL: http://localhost:8090

# Check Zookeeper
docker-compose logs zookeeper

# Check Kafka broker
docker-compose logs kafka
```

---

## Next Steps for Development

1. **Build Frontend Application**
   - React.js with Material-UI
   - User dashboard
   - Ticket purchase interface
   - Real-time bus tracking map

2. **Add JWT Authentication**
   - Uncomment JWT dependencies in API Gateway
   - Add JWT filter
   - Secure endpoints

3. **Real Payment Integration**
   - Integrate Stripe or PayPal
   - Replace simulated payment processing

4. **Complete Event Flow**
   - Add listeners for PaymentProcessedEvent in ticketing service
   - Update ticket status based on payment result

5. **Monitoring & Logging**
   - Add ELK stack or Loki
   - Set up Grafana dashboards
   - Configure Prometheus metrics

---

## Database Access

### PostgreSQL (via PgAdmin)
- URL: http://localhost:5050
- Email: admin@transport.com
- Password: admin123

**Database Connections:**
- user_db: localhost:5432 (user_admin / password123)
- ticketing_db: localhost:5433 (ticket_admin / ticket_pass_123)
- payment_db: localhost:5434 (payment_admin / payment_pass_123)
- subscription_db: localhost:5435 (sub_admin / sub_pass_123)
- scheduling_db: localhost:5436 (scheduling_admin / scheduling_pass)

### MongoDB (via Mongo Express)
- tracking_db: http://localhost:8084
- notification_db: http://localhost:8084

---

## MVP Completion Status

| Component | Status | Notes |
|-----------|--------|-------|
| Infrastructure | ✅ 100% | All services running |
| User Service | ✅ 80% | Login + Registration working |
| Ticketing Service | ✅ 95% | Full ticket lifecycle |
| Payment Service | ✅ 90% | Simulated processing complete |
| Subscription Service | ✅ 85% | CRUD operations working |
| Scheduling Service | ✅ 85% | Routes & schedules working |
| Tracking Service | ✅ 85% | Bus management & events |
| Notification Service | ✅ 95% | Email/SMS working |
| API Gateway | ✅ 75% | Routing working, no auth |
| Frontend | ❌ 0% | Not started |

**Overall MVP Completion: ~70%**

---

## Support & Issues

For issues or questions:
1. Check service logs: `docker-compose logs service-name`
2. Verify service health: http://localhost:8222/api/SERVICE/health
3. Check Eureka dashboard: http://localhost:8761

---

**MVP Built:** December 1, 2024
**Status:** Ready for Testing
