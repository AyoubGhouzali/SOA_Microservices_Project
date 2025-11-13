# 🚌 Urban Transport System

Microservices-based transport management system with event-driven architecture.

## 🏗️ Architecture

- **7 Microservices**: User, Ticketing, Payment, Subscription, Scheduling, Tracking, Notification
- **Event-Driven**: Apache Kafka
- **Databases**: Supabase (PostgreSQL), MongoDB Atlas
- **Cache**: Redis
- **Frontend**: React.js

## 🚀 Quick Start

### Prerequisites
- JDK 21
- Node.js 22+
- Docker Desktop
- Maven

### Setup

1. **Clone repository**
```bash
   git clone <your-repo>
   cd transport-system
```

2. **Configure environment**
```bash
   cp .env.example .env
   # Edit .env with your Supabase credentials
```

3. **Start infrastructure**
```bash
   docker-compose up -d
```

4. **Run services**
```bash
   # Terminal 1 - User Service
   cd services/user-service
   ./mvnw spring-boot:run

   # Terminal 2 - Ticketing Service
   cd services/ticketing-service
   ./mvnw spring-boot:run
```

5. **Access UIs**
   - Kafka UI: http://localhost:8090
   - MongoDB UI: http://localhost:8081
   - Redis UI: http://localhost:8082

## 📖 Documentation

See `/docs` folder for detailed documentation.

## 🧪 Testing
```bash
curl http://localhost:8081/api/users/health
```

## 📝 License

MIT