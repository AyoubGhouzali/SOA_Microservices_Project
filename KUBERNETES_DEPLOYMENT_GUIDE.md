# Kubernetes Deployment Guide - Transport System

## Overview
This guide explains how to deploy the Transport System to Kubernetes cluster.

---

## 📋 Prerequisites

### Required Tools
- **Kubernetes Cluster** (Minikube, kind, EKS, AKS, GKE)
- **kubectl** CLI tool
- **Docker** (for building images)
- **Helm** (optional, for package management)

### Verify Installation
```bash
kubectl version --client
docker --version
```

---

## 🏗️ Architecture on Kubernetes

### Namespace
All resources deployed in: `transport-system`

### Services
- **Infrastructure**: Config Server, Discovery Server, API Gateway
- **Databases**: 5x PostgreSQL, 2x MongoDB, Redis
- **Message Broker**: Kafka + Zookeeper
- **Microservices**: User, Ticketing, Payment, Subscription, Scheduling, Tracking, Notification

### Storage
- **Persistent Volumes** for all databases
- **ConfigMaps** for configuration
- **Secrets** for sensitive data

---

## 🚀 Deployment Steps

### Step 1: Build Docker Images

#### Option A: Build All Images Locally
```bash
cd C:\Users\ayoub\Desktop\transport-system

# Build all service images
docker build -t transport/config-server:latest ./services/ConfigServer
docker build -t transport/discovery-server:latest ./services/DiscoveryServer
docker build -t transport/api-gateway:latest ./services/apiGateway
docker build -t transport/user-service:latest ./services/user-service
docker build -t transport/ticketing-service:latest ./services/ticketing-service
docker build -t transport/payment-service:latest ./services/payment-service
docker build -t transport/subscription-service:latest ./services/subscription-service
docker build -t transport/scheduling-service:latest ./services/scheduling-service
docker build -t transport/tracking-service:latest ./services/tracking-service
docker build -t transport/notification-service:latest ./services/notification-service
```

#### Option B: Push to Docker Registry
```bash
# Tag for registry
docker tag transport/user-service:latest your-registry.com/transport/user-service:latest

# Push to registry
docker push your-registry.com/transport/user-service:latest

# Repeat for all services
```

### Step 2: Create Kubernetes Namespace

```bash
kubectl apply -f kubernetes/namespace.yaml
```

**Verify:**
```bash
kubectl get namespaces
```

### Step 3: Apply ConfigMaps and Secrets

```bash
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/secrets.yaml
```

**Verify:**
```bash
kubectl get configmaps -n transport-system
kubectl get secrets -n transport-system
```

### Step 4: Create Persistent Volume Claims

```bash
kubectl apply -f kubernetes/databases/persistent-volumes.yaml
```

**Verify:**
```bash
kubectl get pvc -n transport-system
```

### Step 5: Deploy Databases

```bash
# PostgreSQL databases
kubectl apply -f kubernetes/databases/postgres-databases.yaml

# MongoDB databases (create this file)
# kubectl apply -f kubernetes/databases/mongodb-databases.yaml
```

**Verify databases are running:**
```bash
kubectl get pods -n transport-system | grep postgres
kubectl get pods -n transport-system | grep mongodb
```

**Wait for all database pods to be ready:**
```bash
kubectl wait --for=condition=ready pod -l app=user-postgres -n transport-system --timeout=300s
```

### Step 6: Deploy Infrastructure Services

```bash
# Zookeeper and Kafka (create these files)
kubectl apply -f kubernetes/infrastructure/zookeeper.yaml
kubectl apply -f kubernetes/infrastructure/kafka.yaml

# Redis
kubectl apply -f kubernetes/infrastructure/redis.yaml

# Config Server
kubectl apply -f kubernetes/infrastructure/config-server.yaml

# Wait for Config Server
kubectl wait --for=condition=ready pod -l app=config-server -n transport-system --timeout=300s

# Discovery Server
kubectl apply -f kubernetes/infrastructure/discovery-server.yaml

# Wait for Discovery Server
kubectl wait --for=condition=ready pod -l app=discovery-server -n transport-system --timeout=300s

# API Gateway
kubectl apply -f kubernetes/infrastructure/api-gateway.yaml
```

### Step 7: Deploy Microservices

```bash
# User Service
kubectl apply -f kubernetes/services/user-service.yaml

# Ticketing Service
kubectl apply -f kubernetes/services/ticketing-service.yaml

# Payment Service
kubectl apply -f kubernetes/services/payment-service.yaml

# Subscription Service
kubectl apply -f kubernetes/services/subscription-service.yaml

# Scheduling Service
kubectl apply -f kubernetes/services/scheduling-service.yaml

# Tracking Service
kubectl apply -f kubernetes/services/tracking-service.yaml

# Notification Service
kubectl apply -f kubernetes/services/notification-service.yaml
```

**Verify all services:**
```bash
kubectl get pods -n transport-system
kubectl get services -n transport-system
```

---

## 🔍 Verification & Testing

### Check All Pods are Running

```bash
kubectl get pods -n transport-system -w
```

All pods should show `STATUS: Running` and `READY: 1/1`

### Access Services

#### Option 1: Port Forward (For Testing)
```bash
# API Gateway
kubectl port-forward svc/api-gateway 8222:8222 -n transport-system

# Eureka Dashboard
kubectl port-forward svc/discovery-server 8761:8761 -n transport-system

# Kafka UI (if deployed)
kubectl port-forward svc/kafka-ui 8090:8080 -n transport-system
```

#### Option 2: NodePort (Minikube/Local)
```bash
# Get Minikube IP
minikube ip

# Access API Gateway
curl http://$(minikube ip):30222/api/users/health
```

#### Option 3: LoadBalancer (Cloud)
```bash
# Get external IP
kubectl get svc api-gateway -n transport-system

# Access via external IP
curl http://<EXTERNAL-IP>:8222/api/users/health
```

### Test Health Endpoints

```bash
# Via port-forward
curl http://localhost:8222/api/users/health
curl http://localhost:8222/api/tickets/health
curl http://localhost:8222/api/payments/health
```

### View Logs

```bash
# View logs for specific service
kubectl logs -f deployment/user-service -n transport-system

# View logs for all pods
kubectl logs -l app=ticketing-service -n transport-system

# View logs with timestamp
kubectl logs deployment/payment-service -n transport-system --timestamps
```

---

## 📊 Monitoring & Management

### Dashboard Access

```bash
# Kubernetes Dashboard (if installed)
kubectl proxy
# Access: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/

# Eureka Dashboard
kubectl port-forward svc/discovery-server 8761:8761 -n transport-system
# Access: http://localhost:8761
```

### Resource Usage

```bash
# Check resource usage
kubectl top pods -n transport-system
kubectl top nodes

# Describe specific pod
kubectl describe pod <pod-name> -n transport-system
```

### Scaling

```bash
# Scale user service to 3 replicas
kubectl scale deployment user-service --replicas=3 -n transport-system

# Auto-scale based on CPU
kubectl autoscale deployment user-service --cpu-percent=70 --min=2 --max=10 -n transport-system
```

---

## 🔧 Troubleshooting

### Pod Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n transport-system

# Check events
kubectl get events -n transport-system --sort-by='.lastTimestamp'

# Check logs
kubectl logs <pod-name> -n transport-system
```

### Database Connection Issues

```bash
# Test database connectivity
kubectl run -it --rm debug --image=postgres:15-alpine --restart=Never -n transport-system -- psql -h user-postgres -U user_admin -d user_db

# Check database pod logs
kubectl logs deployment/user-postgres -n transport-system
```

### Service Discovery Issues

```bash
# Check Eureka dashboard
kubectl port-forward svc/discovery-server 8761:8761 -n transport-system

# Check if services are registered
curl http://localhost:8761/eureka/apps
```

### ConfigMap/Secret Changes

```bash
# Update ConfigMap
kubectl apply -f kubernetes/configmap.yaml

# Restart deployment to pick up changes
kubectl rollout restart deployment/user-service -n transport-system
```

---

## 🔄 Updates & Rollback

### Rolling Update

```bash
# Update image
kubectl set image deployment/user-service user-service=transport/user-service:v2 -n transport-system

# Check rollout status
kubectl rollout status deployment/user-service -n transport-system
```

### Rollback

```bash
# View rollout history
kubectl rollout history deployment/user-service -n transport-system

# Rollback to previous version
kubectl rollout undo deployment/user-service -n transport-system

# Rollback to specific revision
kubectl rollout undo deployment/user-service --to-revision=2 -n transport-system
```

---

## 🗑️ Cleanup

### Delete Specific Service

```bash
kubectl delete deployment user-service -n transport-system
kubectl delete service user-service -n transport-system
```

### Delete All Resources in Namespace

```bash
kubectl delete namespace transport-system
```

---

## 📝 Production Considerations

### 1. Resource Limits
- Set appropriate CPU/memory limits for all pods
- Monitor resource usage and adjust

### 2. High Availability
- Run multiple replicas of each service
- Use Pod Disruption Budgets
- Deploy across multiple availability zones

### 3. Security
- Use proper RBAC policies
- Rotate secrets regularly
- Enable network policies
- Use private container registry

### 4. Backup
- Regular database backups
- Backup persistent volumes
- Export Kubernetes manifests

### 5. Monitoring
- Deploy Prometheus + Grafana
- Set up alerts
- Monitor application metrics

### 6. Logging
- Deploy ELK stack or Loki
- Centralize logs from all services
- Set up log retention policies

---

## 🎯 Quick Commands Reference

```bash
# Get all resources
kubectl get all -n transport-system

# Watch pods
kubectl get pods -n transport-system -w

# Exec into pod
kubectl exec -it <pod-name> -n transport-system -- /bin/sh

# Copy files from pod
kubectl cp transport-system/<pod-name>:/path/to/file ./local-file

# View resource YAML
kubectl get deployment user-service -n transport-system -o yaml

# Delete pod (will be recreated by deployment)
kubectl delete pod <pod-name> -n transport-system
```

---

## 📚 Next Steps

1. **Test the deployment** using the Container Testing Guide
2. **Set up monitoring** (Prometheus/Grafana)
3. **Configure Ingress** for external access
4. **Set up CI/CD** pipeline for automated deployments
5. **Deploy frontend** application

---

**Status**: Kubernetes manifests created ✅
**Next**: Complete remaining manifest files and test deployment

For full Kubernetes manifest files, check the `kubernetes/` directory.
