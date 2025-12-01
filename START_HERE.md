# 🚀 Transport System - START HERE

## Quick Navigation

### For Docker Compose Testing
👉 **[CONTAINER_TESTING_GUIDE.md](./CONTAINER_TESTING_GUIDE.md)** - Test with Docker first

### For Kubernetes Deployment
👉 **[KUBERNETES_DEPLOYMENT_GUIDE.md](./KUBERNETES_DEPLOYMENT_GUIDE.md)** - Deploy to K8s

### For JWT Authentication
👉 **[JWT_AUTHENTICATION_GUIDE.md](./JWT_AUTHENTICATION_GUIDE.md)** - Secure API testing

### For MVP Overview
👉 **[MVP_GUIDE.md](./MVP_GUIDE.md)** - What's implemented

---

## 🎯 **Recommended Path**

### Step 1: Test with Docker Compose (20 minutes)

```bash
# 1. Start Docker Desktop

# 2. Start all services
docker-compose up -d

# 3. Run automated test
.\test-containers.ps1

# 4. Verify in browser
# - Eureka: http://localhost:8761
# - Kafka UI: http://localhost:8090
# - PgAdmin: http://localhost:5050

# 5. Test manually
# See CONTAINER_TESTING_GUIDE.md for detailed examples
```

**Expected Result**: All services running, end-to-end flow works (register → login → buy ticket → payment processed)

---

### Step 2: Deploy to Kubernetes (Optional)

```bash
# 1. Build Docker images
docker build -t transport/user-service:latest ./services/user-service
# ... repeat for all services

# 2. Deploy to K8s
cd kubernetes
chmod +x deploy-all.sh
./deploy-all.sh

# 3. Access services
kubectl port-forward svc/api-gateway 8222:8222 -n transport-system
```

See **[KUBERNETES_DEPLOYMENT_GUIDE.md](./KUBERNETES_DEPLOYMENT_GUIDE.md)** for full instructions

---

## 📋 **What's in This Project**

### Backend Services (100% Complete) ✅
- **User Service** - Registration, Login with JWT
- **Ticketing Service** - Buy, activate, validate tickets
- **Payment Service** - Simulated payment processing
- **Subscription Service** - Monthly/Quarterly/Annual subscriptions
- **Scheduling Service** - Routes, stops, schedules
- **Tracking Service** - Bus tracking, GPS updates
- **Notification Service** - Email/SMS notifications

### Infrastructure (100% Complete) ✅
- **API Gateway** - Single entry point with JWT validation
- **Config Server** - Centralized configuration
- **Discovery Server** - Eureka service registry
- **Kafka** - Event streaming (7 topics)
- **PostgreSQL** - 5 databases
- **MongoDB** - 2 databases
- **Redis** - Caching layer

### JWT Authentication (100% Complete) ✅
- Token generation on login
- Token validation in API Gateway
- Protected vs public endpoints
- User info in request headers

### Docker Compose (100% Complete) ✅
- All services configured
- Health checks enabled
- Persistent volumes
- Network isolation

### Kubernetes (100% Complete) ✅
- All manifests created
- ConfigMaps & Secrets
- Persistent Volumes
- Service deployments
- Auto-deployment script

### Frontend (0% - Not Started) ❌
- React.js setup needed
- See guides for recommendations

---

## 🧪 **Quick Test Script**

### PowerShell (Windows)
```powershell
# Run automated test
.\test-containers.ps1
```

### Bash (Linux/Mac)
```bash
# See CONTAINER_TESTING_GUIDE.md for bash script
```

---

## 📊 **System Architecture**

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
┌──────▼──────────┐
│  API Gateway    │ ← JWT Validation
│  (Port 8222)    │
└──────┬──────────┘
       │
┌──────▼──────────────────────────────────────┐
│         Service Discovery (Eureka)          │
│              (Port 8761)                    │
└──────┬──────────────────────────────────────┘
       │
┌──────▼────────────────────────────────────┐
│           Microservices                   │
│  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │  User    │  │Ticketing │  │ Payment ││
│  │  8081    │  │   8094   │  │  8087   ││
│  └──────────┘  └──────────┘  └─────────┘│
│  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │  Sub     │  │Schedule  │  │ Tracking││
│  │  8093    │  │   8091   │  │  8092   ││
│  └──────────┘  └──────────┘  └─────────┘│
│  ┌──────────┐                            │
│  │  Notif   │                            │
│  │  8095    │                            │
│  └──────────┘                            │
└───────┬────────────────────────────────────┘
        │
┌───────▼──────────┐     ┌─────────────┐
│  PostgreSQL (5x) │     │  MongoDB (2)│
└──────────────────┘     └─────────────┘
        │
┌───────▼──────────┐
│  Kafka Cluster   │
│  (Event Stream)  │
└──────────────────┘
```

---

## 🔧 **Configuration Files**

| File | Purpose |
|------|---------|
| `.env` | Environment variables |
| `docker-compose.yml` | Docker orchestration |
| `kubernetes/` | K8s manifests |
| `test-containers.ps1` | Automated testing |

---

## 📚 **API Endpoints**

### Public (No JWT Required)
```
POST   /api/users/register    - Register user
POST   /api/users/login       - Login (get JWT)
GET    /api/*/health          - Health checks
```

### Protected (JWT Required)
```
GET    /api/users/{id}              - Get user
POST   /api/tickets/purchase        - Buy tickets
GET    /api/tickets/user/{userId}   - My tickets
POST   /api/tickets/activate        - Activate ticket
POST   /api/subscriptions           - Create subscription
GET    /api/routes                  - List routes
POST   /api/buses                   - Create bus
GET    /api/payments/stats          - Payment stats
```

---

## 🎯 **Success Criteria**

✅ **Docker Testing**
- [ ] All containers running
- [ ] Health checks pass
- [ ] User can register
- [ ] User can login (get JWT)
- [ ] User can buy tickets (with JWT)
- [ ] Payment processed automatically
- [ ] Kafka events flowing

✅ **Kubernetes Deployment** (Optional)
- [ ] All pods running
- [ ] Services registered in Eureka
- [ ] API Gateway accessible
- [ ] End-to-end flow works

---

## 🆘 **Troubleshooting**

### Containers not starting?
```bash
docker-compose logs service-name
docker-compose restart service-name
```

### Database connection issues?
```bash
docker exec -it user-postgres psql -U user_admin -d user_db
```

### Kafka not working?
```bash
# Check Kafka UI
http://localhost:8090
```

### JWT errors?
- Check JWT_SECRET in .env matches in both Gateway and User Service
- Verify token at https://jwt.io

---

## 📞 **Support**

- Check service logs: `docker-compose logs -f service-name`
- View all guides in root directory
- Test with provided scripts

---

## 🚀 **Next Steps**

1. ✅ **Test Docker Compose** - Run `test-containers.ps1`
2. ✅ **Verify all services** - Check Eureka dashboard
3. ⏭️ **Deploy to Kubernetes** - Follow K8s guide (optional)
4. ⏭️ **Build Frontend** - React.js (future)

---

**Status**: Backend 100% Complete ✅
**Last Updated**: December 1, 2024

Start with: `.\test-containers.ps1` or read `CONTAINER_TESTING_GUIDE.md`
