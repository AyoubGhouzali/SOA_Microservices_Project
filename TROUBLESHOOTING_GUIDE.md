# Troubleshooting Guide - Transport System

## 🚨 Common Issues and Solutions

---

## Issue #1: Docker Desktop Not Running

### Symptoms
```
error during connect: open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified
```

### Solution
1. **Start Docker Desktop**
   - Press `Windows Key` + Search "Docker Desktop"
   - Click to launch Docker Desktop
   - Wait for whale icon to show "Docker Desktop is running" (1-2 min)

2. **Verify Docker is running**
   ```bash
   docker --version
   docker ps
   ```

3. **If Docker won't start:**
   - Restart Windows
   - Check WSL 2 is installed: `wsl --list --verbose`
   - Reinstall Docker Desktop from https://www.docker.com/products/docker-desktop

---

## Issue #2: Containers Won't Start

### Symptoms
```
Error starting userland proxy: listen tcp4 0.0.0.0:5432: bind: address already in use
```

### Solution
**Port already in use**

1. **Find what's using the port:**
   ```powershell
   # Check port 5432 (PostgreSQL)
   netstat -ano | findstr :5432

   # Check port 8081 (User Service)
   netstat -ano | findstr :8081
   ```

2. **Kill the process:**
   ```powershell
   # Find PID from netstat output, then:
   taskkill /PID <process_id> /F
   ```

3. **Or change ports in docker-compose.yml:**
   ```yaml
   # Example: Change PostgreSQL from 5432 to 5433
   ports:
     - "5433:5432"  # host:container
   ```

---

## Issue #3: Services Not Healthy

### Symptoms
```bash
docker-compose ps
# Shows services in "Unhealthy" or "Exited" state
```

### Solution

**Check logs for the failing service:**
```bash
# View logs
docker-compose logs user-service

# Follow logs in real-time
docker-compose logs -f user-service

# View last 100 lines
docker-compose logs --tail=100 user-service
```

**Common causes:**

1. **Database not ready**
   ```bash
   # Check database is healthy
   docker-compose ps | grep postgres

   # If not ready, restart
   docker-compose restart user-postgres
   ```

2. **Configuration error**
   ```bash
   # Check environment variables
   docker-compose exec user-service env | grep SPRING

   # Verify .env file exists
   cat .env
   ```

3. **Out of memory**
   ```bash
   # Check container resources
   docker stats

   # Restart Docker Desktop with more memory
   # Settings > Resources > Advanced > Memory: 8GB
   ```

---

## Issue #4: Kafka Not Working

### Symptoms
- Services can't connect to Kafka
- Topics not created
- Messages not flowing

### Solution

1. **Check Kafka and Zookeeper are running:**
   ```bash
   docker-compose ps | grep kafka
   docker-compose ps | grep zookeeper
   ```

2. **Check Kafka logs:**
   ```bash
   docker-compose logs kafka
   docker-compose logs zookeeper
   ```

3. **Restart Kafka cluster:**
   ```bash
   docker-compose restart zookeeper
   sleep 10
   docker-compose restart kafka
   ```

4. **Check topics in Kafka UI:**
   - Open: http://localhost:8090
   - Should see topics: `ticket.purchased`, `payment.processed`, `bus-delays`, etc.

5. **Manual topic creation (if needed):**
   ```bash
   docker exec -it kafka kafka-topics --create \
     --bootstrap-server localhost:9093 \
     --topic ticket.purchased \
     --partitions 3 \
     --replication-factor 1
   ```

---

## Issue #5: Service Won't Register with Eureka

### Symptoms
- Service running but not in Eureka dashboard
- Other services can't discover it

### Solution

1. **Check Eureka is running:**
   ```bash
   curl http://localhost:8761
   # Should return Eureka dashboard HTML
   ```

2. **Check service logs:**
   ```bash
   docker-compose logs user-service | grep -i eureka
   ```

3. **Verify EUREKA_DEFAULT_ZONE in service:**
   ```bash
   docker-compose exec user-service env | grep EUREKA
   ```

4. **Restart service:**
   ```bash
   docker-compose restart user-service
   ```

5. **Check network connectivity:**
   ```bash
   # From inside container
   docker-compose exec user-service curl http://discovery-server:8761
   ```

---

## Issue #6: JWT Token Invalid

### Symptoms
```json
{
  "error": "Invalid or expired JWT token",
  "status": 401
}
```

### Solution

1. **Check JWT_SECRET matches in both services:**
   ```bash
   # API Gateway
   docker-compose exec api-gateway env | grep JWT_SECRET

   # User Service
   docker-compose exec user-service env | grep JWT_SECRET
   ```

2. **Verify token at jwt.io:**
   - Copy your JWT token
   - Paste at https://jwt.io
   - Paste JWT_SECRET in "Verify Signature" section
   - Check signature is valid

3. **Get a new token:**
   ```bash
   curl -X POST http://localhost:8222/api/users/login \
     -H "Content-Type: application/json" \
     -d '{"email":"your@email.com","password":"password"}'
   ```

4. **Check token hasn't expired:**
   - Tokens expire after 24 hours by default
   - Login again to get fresh token

---

## Issue #7: Database Connection Failed

### Symptoms
```
Connection to localhost:5432 refused
```

### Solution

1. **Check database is running:**
   ```bash
   docker-compose ps | grep postgres
   ```

2. **Check database logs:**
   ```bash
   docker-compose logs user-postgres
   ```

3. **Test database connection:**
   ```bash
   docker exec -it user-postgres psql -U user_admin -d user_db -c "\dt"
   ```

4. **Verify connection string:**
   ```bash
   docker-compose exec user-service env | grep DATASOURCE
   ```

5. **Reset database:**
   ```bash
   docker-compose down
   docker volume rm transport-system_user-postgres-data
   docker-compose up -d user-postgres
   ```

---

## Issue #8: Out of Disk Space

### Symptoms
```
no space left on device
```

### Solution

1. **Check Docker disk usage:**
   ```bash
   docker system df
   ```

2. **Clean up unused resources:**
   ```bash
   # Remove stopped containers
   docker container prune

   # Remove unused images
   docker image prune -a

   # Remove unused volumes
   docker volume prune

   # Remove everything unused
   docker system prune -a --volumes
   ```

3. **Increase Docker disk space:**
   - Docker Desktop > Settings > Resources > Advanced
   - Increase "Disk image size"

---

## Issue #9: Containers Keep Restarting

### Symptoms
```bash
docker-compose ps
# Shows "Restarting (1) X seconds ago"
```

### Solution

1. **Check why it's crashing:**
   ```bash
   docker-compose logs --tail=50 user-service
   ```

2. **Common causes:**
   - **Database not ready:** Add `depends_on` with health check
   - **Config Server not ready:** Increase startup delays
   - **Out of memory:** Check `docker stats`
   - **Port conflict:** Check `netstat -ano | findstr :8081`

3. **Disable auto-restart for debugging:**
   ```yaml
   # In docker-compose.yml
   user-service:
     restart: "no"  # Change from "unless-stopped"
   ```

---

## Issue #10: Service Returns 503 (Service Unavailable)

### Symptoms
```bash
curl http://localhost:8222/api/users/health
# Returns 503 Service Unavailable
```

### Solution

1. **Check if user-service is running:**
   ```bash
   docker-compose ps user-service
   ```

2. **Check if registered in Eureka:**
   - Open: http://localhost:8761
   - Look for USER-SERVICE in the list

3. **Check API Gateway logs:**
   ```bash
   docker-compose logs api-gateway | grep user-service
   ```

4. **Wait for services to register:**
   - Services can take 30-60 seconds to register with Eureka
   - Be patient after startup

---

## Diagnostic Commands

### Quick Health Check
```bash
# Check all containers
docker-compose ps

# Check all services are registered
curl http://localhost:8761/eureka/apps

# Test all health endpoints
curl http://localhost:8222/api/users/health
curl http://localhost:8222/api/tickets/health
curl http://localhost:8222/api/payments/health
```

### View All Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs user-service

# Follow logs
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100
```

### Resource Usage
```bash
# Check memory and CPU
docker stats

# Check disk usage
docker system df
```

### Network Issues
```bash
# List networks
docker network ls

# Inspect transport network
docker network inspect transport-system_transport-network

# Test connectivity between containers
docker-compose exec user-service ping discovery-server
```

### Database Access
```bash
# PostgreSQL
docker exec -it user-postgres psql -U user_admin -d user_db

# MongoDB
docker exec -it tracking-mongodb mongosh tracking_db

# Check tables
docker exec -it user-postgres psql -U user_admin -d user_db -c "\dt"
```

---

## Complete Reset (Nuclear Option)

If all else fails, completely reset everything:

```bash
# Stop all containers
docker-compose down

# Remove all volumes (DELETES ALL DATA!)
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Clean Docker system
docker system prune -a --volumes

# Restart Docker Desktop

# Start fresh
docker-compose up -d --build
```

---

## Performance Optimization

### If services are slow to start:

1. **Allocate more resources to Docker:**
   - Docker Desktop > Settings > Resources
   - CPU: 4+ cores
   - Memory: 8+ GB
   - Swap: 2+ GB

2. **Reduce number of replicas:**
   - Edit docker-compose.yml
   - Remove unused services temporarily

3. **Use cached images:**
   ```bash
   # Build once
   docker-compose build

   # Start without rebuild
   docker-compose up -d
   ```

---

## Getting Help

### Check Service Health
```bash
# Individual service
curl http://localhost:8222/api/users/health

# All services via gateway
for service in users tickets payments subscriptions routes buses notifications; do
  echo "Testing $service..."
  curl -s http://localhost:8222/api/$service/health
done
```

### Export Logs for Debugging
```bash
# Export all logs to file
docker-compose logs > docker-logs.txt

# Export specific service
docker-compose logs user-service > user-service-logs.txt
```

### Check Configuration
```bash
# View environment variables
docker-compose config

# Check .env file
cat .env
```

---

## Quick Fixes Checklist

- [ ] Docker Desktop is running
- [ ] All ports are available (5432, 8081, 8222, etc.)
- [ ] .env file exists and is correct
- [ ] No spaces in file paths
- [ ] Enough disk space (10GB+)
- [ ] Enough RAM allocated to Docker (8GB+)
- [ ] WSL 2 is installed (Windows)
- [ ] Antivirus not blocking Docker
- [ ] VPN not interfering with localhost

---

**Still having issues?**
1. Check all logs: `docker-compose logs`
2. Reset everything: `docker-compose down -v && docker-compose up -d`
3. Share logs for specific help

Last Updated: December 1, 2024
