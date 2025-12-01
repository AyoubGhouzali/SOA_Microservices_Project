#!/bin/bash

# Master Deployment Script for Transport System on Kubernetes
# This script deploys the entire transport system in the correct order

set -e  # Exit on error

echo "========================================="
echo "Transport System - Kubernetes Deployment"
echo "========================================="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Step 1: Create Namespace
echo -e "\n${YELLOW}[1/8] Creating namespace...${NC}"
kubectl apply -f namespace.yaml
echo -e "${GREEN}✓ Namespace created${NC}"

# Step 2: Apply ConfigMaps and Secrets
echo -e "\n${YELLOW}[2/8] Applying ConfigMaps and Secrets...${NC}"
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml
echo -e "${GREEN}✓ ConfigMaps and Secrets applied${NC}"

# Step 3: Create Persistent Volume Claims
echo -e "\n${YELLOW}[3/8] Creating Persistent Volume Claims...${NC}"
kubectl apply -f databases/persistent-volumes.yaml
echo -e "${GREEN}✓ PVCs created${NC}"

# Step 4: Deploy Databases
echo -e "\n${YELLOW}[4/8] Deploying databases...${NC}"
kubectl apply -f databases/postgres-databases.yaml
kubectl apply -f databases/mongodb-databases.yaml
echo -e "${GREEN}✓ Databases deployed${NC}"

echo "Waiting for databases to be ready..."
kubectl wait --for=condition=ready pod -l app=user-postgres -n transport-system --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=ticketing-postgres -n transport-system --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=payment-postgres -n transport-system --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=subscription-postgres -n transport-system --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=scheduling-postgres -n transport-system --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=tracking-mongodb -n transport-system --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=notification-mongodb -n transport-system --timeout=300s || true
echo -e "${GREEN}✓ Databases are ready${NC}"

# Step 5: Deploy Infrastructure
echo -e "\n${YELLOW}[5/8] Deploying infrastructure services...${NC}"
kubectl apply -f infrastructure/zookeeper.yaml
sleep 10
kubectl apply -f infrastructure/kafka.yaml
sleep 10
kubectl apply -f infrastructure/redis.yaml
kubectl apply -f infrastructure/config-server.yaml

echo "Waiting for Config Server to be ready..."
kubectl wait --for=condition=ready pod -l app=config-server -n transport-system --timeout=300s
echo -e "${GREEN}✓ Config Server is ready${NC}"

kubectl apply -f infrastructure/discovery-server.yaml
echo "Waiting for Discovery Server to be ready..."
kubectl wait --for=condition=ready pod -l app=discovery-server -n transport-system --timeout=300s
echo -e "${GREEN}✓ Discovery Server is ready${NC}"

kubectl apply -f infrastructure/api-gateway.yaml
echo -e "${GREEN}✓ Infrastructure deployed${NC}"

# Step 6: Deploy Microservices
echo -e "\n${YELLOW}[6/8] Deploying microservices...${NC}"
kubectl apply -f services/user-service.yaml
kubectl apply -f services/ticketing-service.yaml
kubectl apply -f services/payment-service.yaml
kubectl apply -f services/all-services.yaml
echo -e "${GREEN}✓ Microservices deployed${NC}"

# Step 7: Verify Deployment
echo -e "\n${YELLOW}[7/8] Verifying deployment...${NC}"
sleep 30
kubectl get pods -n transport-system
kubectl get services -n transport-system

# Step 8: Display Access Information
echo -e "\n${YELLOW}[8/8] Access Information${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"

echo -e "\n${YELLOW}To access the API Gateway:${NC}"
if command -v minikube &> /dev/null; then
    MINIKUBE_IP=$(minikube ip)
    echo "  API Gateway: http://$MINIKUBE_IP:30222"
    echo "  Eureka: http://$MINIKUBE_IP:30761"
else
    echo "  Use: kubectl port-forward svc/api-gateway 8222:8222 -n transport-system"
    echo "  Then access: http://localhost:8222"
fi

echo -e "\n${YELLOW}To view logs:${NC}"
echo "  kubectl logs -f deployment/user-service -n transport-system"

echo -e "\n${YELLOW}To check service health:${NC}"
echo "  kubectl get pods -n transport-system"
echo "  kubectl get services -n transport-system"

echo -e "\n${GREEN}Done!${NC}"
